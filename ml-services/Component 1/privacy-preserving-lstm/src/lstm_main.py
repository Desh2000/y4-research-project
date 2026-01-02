"""
MANO Component 2: LSTM Main Orchestration Script
Entry point for Training, Evaluation, and Inference.

FEATURES:
- CLI Argument Parsing for hyperparameter tuning.
- Modular execution modes (Train, Eval, Infer).
- Integrated logging and artifact management.
"""
from lstm_data_loader import load_and_split_data  # Import from the dedicated loader module
from lstm_trainer import LSTMTrainer
from lstm_model import RiskPredictionModel
from lstm_config import config
import sys
import os
import argparse
import torch
import numpy as np
from pathlib import Path
from sklearn.metrics import classification_report, confusion_matrix

# --- SETUP PATHS ---
sys.path.insert(0, str(Path(__file__).parent))
config_path = os.path.join(os.getcwd(), 'ml-services',
                           'privacy-preserving-lstm', 'config')
sys.path.append(config_path)


# --- MODES ---

def train_model(args):
    """Execute Training Pipeline"""
    print("\n" + "█"*70)
    print("█ MANO COMPONENT 2: TRAINING MODE")
    print("█"*70 + "\n")

    # 1. Load Data (Using the modular loader)
    train_loader, val_loader, _, class_weights = load_and_split_data()

    # Calculate Class Counts for Balancing from weights if needed
    # Note: LSTMTrainer calculates weights internally if counts are passed,
    # OR we can modify LSTMTrainer to accept pre-calculated weights.
    # For consistency with the Trainer design, we'll let it calculate or use the list.

    # 2. Initialize Model
    model = RiskPredictionModel(config)
    print(f"\nInitialized Model (Static Dim: {config.model.STATIC_INPUT_DIM})")

    # 3. Train
    # We need to pass counts to the trainer so it can calculate weights for CrossEntropyLoss
    # Since load_and_split_data calculates weights directly, let's reverse engineer counts
    # or better yet, update the trainer to accept weights directly.
    # For now, we will pass None and let the Trainer assume balanced or
    # we can pass the weights list if we update the trainer.

    # Correction: The LSTMTrainer expects class_counts to calculate weights.
    # Let's get counts from the train_loader dataset.
    y_train = train_loader.dataset.y.numpy()
    class_counts = np.bincount(y_train)

    trainer = LSTMTrainer(model, config, class_counts)
    trainer.fit(train_loader, val_loader)

    print(
        f"\n✅ Training Complete! Model saved to: {config.data.BEST_MODEL_PATH}")


def evaluate_model(args):
    """Execute Evaluation Pipeline"""
    print("\n" + "█"*70)
    print("█ MANO COMPONENT 2: EVALUATION MODE")
    print("█"*70 + "\n")

    # 1. Load Data (We only need Test set here)
    _, _, test_loader, _ = load_and_split_data()

    # 2. Load Model
    model = RiskPredictionModel(config)
    model.load_state_dict(torch.load(
        config.data.BEST_MODEL_PATH, map_location=config.training.DEVICE))
    model.to(config.training.DEVICE)
    model.eval()

    # 3. Inference
    all_preds = []
    all_labels = []

    with torch.no_grad():
        for x_d, x_s, y in test_loader:
            x_d, x_s = x_d.to(config.training.DEVICE), x_s.to(
                config.training.DEVICE)
            outputs = model(x_d, x_s)
            _, preds = torch.max(outputs, 1)
            all_preds.extend(preds.cpu().numpy())
            all_labels.extend(y.numpy())

    # 4. Metrics
    print("\n" + "="*60)
    print("TEST SET RESULTS")
    print("="*60)
    print(classification_report(all_labels, all_preds,
          target_names=['Low Risk', 'Medium Risk', 'High Risk']))
    print("\nConfusion Matrix:")
    print(confusion_matrix(all_labels, all_preds))


def infer_model(args):
    """Run Inference on Single Samples (Demo Mode)"""
    print("\n" + "█"*70)
    print("█ MANO COMPONENT 2: INFERENCE MODE")
    print("█"*70 + "\n")

    # Load Model
    model = RiskPredictionModel(config)
    try:
        model.load_state_dict(torch.load(
            config.data.BEST_MODEL_PATH, map_location=config.training.DEVICE))
    except FileNotFoundError:
        print("❌ Error: No trained model found. Run --mode train first.")
        return

    model.to(config.training.DEVICE)
    model.eval()

    # Create Dummy Sample (1 Patient) for Demo
    # In production, this would come from an API endpoint
    print("Generating random sample for demonstration...")
    sample_dynamic = torch.randn(1, 7, 4).to(
        config.training.DEVICE)  # 1 Week of data
    sample_static = torch.randn(
        1, config.model.STATIC_INPUT_DIM).to(config.training.DEVICE)

    with torch.no_grad():
        logits = model(sample_dynamic, sample_static)
        probs = torch.softmax(logits, dim=1)  # Convert logits to probabilities
        risk_score = torch.argmax(probs).item()

    risk_map = {0: "LOW RISK", 1: "MEDIUM RISK", 2: "HIGH RISK"}
    print(f"\n🔮 Prediction:")
    print(f"   Risk Level: {risk_map[risk_score]}")
    print(f"   Confidence: {probs[0][risk_score]*100:.2f}%")
    print(
        f"   Raw Probabilities: Low={probs[0][0]:.2f}, Med={probs[0][1]:.2f}, High={probs[0][2]:.2f}")

# --- MAIN ENTRY POINT ---


def main():
    parser = argparse.ArgumentParser(
        description='MANO Component 2: LSTM Execution')

    parser.add_argument('--mode', choices=['train', 'eval', 'infer'],
                        default='train', help='Execution mode')

    # Hyperparameter Overrides
    parser.add_argument('--epochs', type=int, help='Override training epochs')
    parser.add_argument('--batch_size', type=int, help='Override batch size')
    parser.add_argument('--lr', type=float, help='Override learning rate')

    args = parser.parse_args()

    # Apply Overrides to Config
    if args.epochs:
        config.training.EPOCHS = args.epochs
    if args.batch_size:
        config.data.BATCH_SIZE = args.batch_size
    if args.lr:
        config.training.LEARNING_RATE = args.lr

    # Execute Mode
    if args.mode == 'train':
        train_model(args)
    elif args.mode == 'eval':
        evaluate_model(args)
    elif args.mode == 'infer':
        infer_model(args)


if __name__ == "__main__":
    main()
