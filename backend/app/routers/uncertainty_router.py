"""
MC Dropout Uncertainty — Router

Monte Carlo Dropout for uncertainty quantification on the LSTM risk predictor.

WHAT IS MC DROPOUT?
Instead of running inference once with dropout disabled (model.eval()),
we run it N times with dropout ENABLED (model.train() for dropout layers).
Each forward pass produces slightly different predictions because dropout
randomly zeroes different neurons each time.

The distribution of predictions tells us:
- HOW CERTAIN the model is (low variance = confident)
- WHICH CLASS the model is uncertain about (high entropy = confused)
- WHETHER TO TRUST the prediction (stability score)

This is a well-established Bayesian approximation technique from
Gal & Ghahramani (2016): "Dropout as a Bayesian Approximation."

SAFETY: We temporarily enable dropout for inference, then restore
eval mode. The model weights are NEVER modified.
"""
from fastapi import APIRouter, HTTPException, Depends
import torch
import numpy as np

# --- Schemas ---
from backend.app.schemas.uncertainty_schema import (
    UncertaintyRequest,
    UncertaintyResponse,
    ClassDistribution,
)
from backend.app.schemas.simulation_schema import (
    RiskPredictionResponse,
    RiskLevel,
)

# --- Reuse existing helper ---
from backend.app.routers.simulation_router import parse_patient_state

# --- Services ---
from backend.app.services.risk_service import RiskPredictionService

from backend.app.core.logging import get_logger

logger = get_logger("uncertainty_router")

router = APIRouter()

RISK_MAP = {0: RiskLevel.LOW, 1: RiskLevel.MEDIUM, 2: RiskLevel.HIGH}
RISK_NAMES = ["Low", "Medium", "High"]


def get_risk_service():
    return RiskPredictionService()


def enable_dropout(model):
    """Enable dropout layers while keeping batch norm in eval mode."""
    for module in model.modules():
        if isinstance(module, torch.nn.Dropout):
            module.train()


def restore_eval(model):
    """Restore full eval mode."""
    model.eval()


@router.post("/evaluate", response_model=UncertaintyResponse)
async def evaluate_uncertainty(
    request: UncertaintyRequest,
    risk_service: RiskPredictionService = Depends(get_risk_service),
):
    """
    Runs MC Dropout on the LSTM risk predictor for uncertainty quantification.
    """
    if risk_service.model is None:
        raise HTTPException(status_code=503, detail="Risk model not loaded")

    logger.info("mc_dropout_request", n_samples=request.n_samples)

    # Step 1: Parse patient state
    dyn_np, stat_np = parse_patient_state(request.patient_state)
    dyn_tensor = torch.FloatTensor(dyn_np).to(risk_service.device)
    stat_tensor = torch.FloatTensor(stat_np).to(risk_service.device)

    # Step 2: Standard point estimate (dropout OFF)
    point_risk = risk_service.predict(dyn_np, stat_np)

    # Step 3: MC Dropout — run N forward passes with dropout ON
    model = risk_service.model
    all_probs = []

    try:
        enable_dropout(model)

        for _ in range(request.n_samples):
            with torch.no_grad():
                logits = model(dyn_tensor, stat_tensor)
                probs = torch.softmax(logits, dim=1)
                all_probs.append(probs.cpu().numpy()[0])

    finally:
        # ALWAYS restore eval mode, even if an exception occurs
        restore_eval(model)

    all_probs = np.array(all_probs)  # Shape: (N, 3)

    # Step 4: Compute statistics per class
    class_distributions = []
    for c in range(3):
        class_distributions.append(ClassDistribution(
            risk_class=RISK_NAMES[c],
            mean_probability=round(float(np.mean(all_probs[:, c])), 4),
            std_probability=round(float(np.std(all_probs[:, c])), 4),
            min_probability=round(float(np.min(all_probs[:, c])), 4),
            max_probability=round(float(np.max(all_probs[:, c])), 4),
        ))

    # Step 5: Predictive entropy (aleatoric + epistemic uncertainty)
    mean_probs = np.mean(all_probs, axis=0)
    mean_probs_clipped = np.clip(mean_probs, 1e-10, 1.0)
    predictive_entropy = float(-np.sum(mean_probs_clipped * np.log(mean_probs_clipped)))

    # Step 6: Mutual information (epistemic uncertainty only)
    # MI = H[E[p]] - E[H[p]] — measures model's own uncertainty
    per_sample_entropies = []
    for probs in all_probs:
        probs_clipped = np.clip(probs, 1e-10, 1.0)
        per_sample_entropies.append(-np.sum(probs_clipped * np.log(probs_clipped)))

    expected_entropy = float(np.mean(per_sample_entropies))
    mutual_information = max(0.0, predictive_entropy - expected_entropy)

    # Step 7: Prediction stability
    point_class = point_risk["risk_class"]
    mc_predictions = np.argmax(all_probs, axis=1)
    agreement = float(np.mean(mc_predictions == point_class))

    # Step 8: Reliability flag
    is_reliable = agreement > 0.7 and predictive_entropy < 1.0

    # Step 9: Build explanation
    if is_reliable:
        uncertainty_summary = (
            f"The model is confident in its {RISK_NAMES[point_class]} risk prediction. "
            f"{agreement:.0%} of {request.n_samples} MC samples agree with the point estimate. "
            f"Predictive entropy is {predictive_entropy:.3f} (low)."
        )
    elif agreement > 0.5:
        # Find the competing class
        runner_up = np.argmax(np.bincount(mc_predictions[mc_predictions != point_class], minlength=3))
        uncertainty_summary = (
            f"The model shows moderate uncertainty. "
            f"{agreement:.0%} of MC samples predict {RISK_NAMES[point_class]}, "
            f"but {RISK_NAMES[runner_up]} appears in some samples. "
            f"Consider additional data or clinical judgment."
        )
    else:
        uncertainty_summary = (
            f"⚠️ High uncertainty detected. Only {agreement:.0%} of MC samples agree. "
            f"The model cannot reliably distinguish between risk classes for this patient. "
            f"Clinical judgment should take precedence."
        )

    logger.info(
        "mc_dropout_complete",
        point_class=point_class,
        stability=round(agreement, 3),
        entropy=round(predictive_entropy, 3),
    )

    return UncertaintyResponse(
        point_estimate=RiskPredictionResponse(
            current_risk_class=RISK_MAP[point_class],
            confidence=point_risk["confidence"],
            probabilities=point_risk["probabilities"],
        ),
        n_samples=request.n_samples,
        class_distributions=class_distributions,
        predictive_entropy=round(predictive_entropy, 4),
        mutual_information=round(mutual_information, 4),
        prediction_stability=round(agreement, 4),
        is_reliable=is_reliable,
        uncertainty_summary=uncertainty_summary,
    )
