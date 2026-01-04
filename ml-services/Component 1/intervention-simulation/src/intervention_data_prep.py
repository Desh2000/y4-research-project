"""
MANO Component 3: Virtual Clinical Trial Data Generator
Phase 5.0: Generates synthetic training data for the Seq2Seq Simulator.

ALGORITHM:
1. Load 10,000 synthetic patients (from Phase 3).
2. For each patient, simulate 5 random "Clinical Trials":
   - Select a random Intervention (e.g., CBT).
   - Select a random Intensity (e.g., 85%).
   - Apply mathematical rules (Intervention Config) to the WHOLE sequence.
3. Save 50,000 (Source, Action, Target) triplets for training.

Author: MANO Team
Status: Production Ready
"""

import numpy as np
import logging
import sys
import os
from pathlib import Path
from typing import Tuple, Dict

# --- SETUP PATHS ---
# Robustly find the config directory regardless of where script is run
current_file = Path(__file__).resolve()
project_root = current_file.parent.parent.parent.parent
config_dir = current_file.parent.parent / "config"
sys.path.append(str(config_dir))

try:
    from intervention_config import config
except ImportError:
    print(f"❌ CRITICAL ERROR: Could not import 'intervention_config'.")
    print(f"   Checked path: {config_dir}")
    sys.exit(1)

# Setup Logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class SimulationDataGenerator:
    """
    Engine for creating counterfactual medical histories.
    """
    def __init__(self):
        self.config = config
        self.intervention_types = config.interventions.INTERVENTION_TYPES
        self.base_effects = config.interventions.BASE_EFFECTS
        
        # Validation
        self._validate_signals()

    def _validate_signals(self):
        """Ensure config matches data expectations"""
        expected = self.config.model.INPUT_DIM
        actual = len(self.config.interventions.SIGNAL_MAP)
        if expected != actual:
            logger.error(f"Signal mismatch! Model expects {expected}, Config has {actual}")
            sys.exit(1)
        logger.info(f"✅ Config validated. Signals: {self.config.interventions.SIGNAL_MAP}")

    def load_population(self) -> np.ndarray:
        """Load the 10k synthetic patients (Source Population)"""
        path = Path(self.config.data.INPUT_DATA_PATH)
        if not path.exists():
            logger.error(f"Input data not found at {path}")
            sys.exit(1)
            
        data = np.load(path)
        X = data['X_dynamic'] # Shape: (10000, 7, 4)
        logger.info(f"✅ Loaded Population: {X.shape} (Patients, Days, Signals)")
        return X

    def get_intervention_vector(self, idx: int, intensity: float) -> np.ndarray:
        """
        Create the conditional input vector for the Neural Network.
        Format: [One-Hot-Encoding (5)] + [Intensity (1)] = Shape (6,)
        """
        one_hot = np.zeros(len(self.intervention_types))
        one_hot[idx] = 1.0
        return np.concatenate([one_hot, [intensity]])

    def simulate_outcome(self, sequence: np.ndarray, intervention_idx: int, intensity: float) -> np.ndarray:
        """
        The 'Physics Engine'. Applies the medical rules to a patient sequence.
        
        Args:
            sequence: (7, 4) array
            intervention_idx: Int ID
            intensity: Float 0.1 - 1.0
            
        Returns:
            modified_sequence: (7, 4) array
        """
        intervention_name = self.intervention_types[intervention_idx]
        
        # 1. Get Base Effect Vector (e.g. [+0.05, -0.10...])
        base_effect = np.array(self.base_effects[intervention_name])
        
        # 2. Scale by Intensity
        # Stronger dose = Stronger effect
        scaled_effect = base_effect * intensity
        
        # 3. Add Probabilistic Variance (The "Human Factor")
        # Not everyone responds identically. We add noise to the *Effect*, not just the data.
        noise_std = self.config.interventions.EFFECT_VARIANCE
        effect_noise = np.random.normal(0, noise_std, sequence.shape)
        
        # 4. Apply to Sequence
        # Model: Future = Current * (1 + Effect)
        # We apply this to the WHOLE sequence to simulate the "New Normal" for this patient
        total_effect = scaled_effect + effect_noise
        modified_sequence = sequence * (1.0 + total_effect)
        
        # 5. Enforce Biological Constraints (Safety Limits)
        # Don't let Sleep drop below 0.1 or HR go above 0.95
        limits = self.config.env.SAFETY_LIMITS
        
        # Signal Map: 0:Sleep, 1:Quality, 2:HR, 3:Stress
        modified_sequence[:, 0] = np.clip(modified_sequence[:, 0], limits['min_sleep'], 1.0)
        modified_sequence[:, 2] = np.clip(modified_sequence[:, 2], 0.0, limits['max_hr'])
        modified_sequence[:, 3] = np.clip(modified_sequence[:, 3], 0.0, limits['max_stress'])
        
        # Clip everything else to [0,1] just in case
        modified_sequence = np.clip(modified_sequence, 0.0, 1.0)
        
        return modified_sequence

    def run_virtual_trials(self):
        """Main Loop: Generate the Dataset"""
        logger.info("\n" + "="*60)
        logger.info("STARTING VIRTUAL CLINICAL TRIALS")
        logger.info("="*60)
        
        patients = self.load_population()
        n_patients = len(patients)
        trials_per_patient = 5 # Data Augmentation
        
        sources = []       # X_t (History)
        conditions = []    # Intervention Vector
        targets = []       # X_t+1 (Future)
        
        logger.info(f"Simulating {trials_per_patient} trials per patient...")
        
        for i in range(n_patients):
            patient_seq = patients[i]
            
            for _ in range(trials_per_patient):
                # A. Randomize Treatment
                # Random type (0-4) and Random intensity (0.1-1.0)
                action_idx = np.random.randint(0, len(self.intervention_types))
                intensity = np.random.uniform(
                    self.config.interventions.MIN_INTENSITY,
                    self.config.interventions.MAX_INTENSITY
                )
                
                # B. Create Input Condition Vector
                cond_vector = self.get_intervention_vector(action_idx, intensity)
                
                # C. Simulate Outcome (Ground Truth for Training)
                future_seq = self.simulate_outcome(patient_seq, action_idx, intensity)
                
                # Store
                sources.append(patient_seq)
                conditions.append(cond_vector)
                targets.append(future_seq)
            
            if (i+1) % 2000 == 0:
                logger.info(f"   Processed {i+1}/{n_patients} patients")
                
        # Convert to Tensor-ready Arrays
        X_src = np.array(sources, dtype=np.float32)
        X_cond = np.array(conditions, dtype=np.float32)
        y_tgt = np.array(targets, dtype=np.float32)
        
        # Validation Stats
        self._print_stats(X_src, X_cond, y_tgt)
        
        # Save
        self._save_data(X_src, X_cond, y_tgt)

    def _print_stats(self, src, cond, tgt):
        logger.info("\n📊 DATASET STATISTICS:")
        logger.info(f"   Total Samples:      {len(src):,}")
        logger.info(f"   Source Shape:       {src.shape}")
        logger.info(f"   Condition Shape:    {cond.shape}")
        logger.info(f"   Target Shape:       {tgt.shape}")
        
        # Calculate average impact
        # Compare Mean(Target) vs Mean(Source) for Stress (Index 3)
        avg_diff = np.mean(tgt[:,:,3]) - np.mean(src[:,:,3])
        logger.info(f"   Avg Stress Change:  {avg_diff:.4f} (Validation)")

    def _save_data(self, src, cond, tgt):
        output_path = self.config.data.SIMULATION_DATA_PATH
        Path(output_path).parent.mkdir(parents=True, exist_ok=True)
        
        np.savez_compressed(
            output_path,
            sources=src,
            conditions=cond,
            targets=tgt
        )
        
        size_mb = Path(output_path).stat().st_size / (1024 * 1024)
        logger.info(f"\n💾 SAVED: {output_path}")
        logger.info(f"   Size: {size_mb:.2f} MB")
        logger.info("✅ Phase 5.0 Complete. Ready for Seq2Seq Training.")

if __name__ == "__main__":
    gen = SimulationDataGenerator()
    gen.run_virtual_trials()