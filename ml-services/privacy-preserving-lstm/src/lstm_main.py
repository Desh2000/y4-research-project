"""
MANO Component 2: LSTM Main Orchestration Script
Entry point for Training, Evaluation, and Inference.

FEATURES:
- CLI Argument Parsing for hyperparameter tuning.
- Modular execution modes (Train, Eval, Infer).
- Integrated logging and artifact management.
"""
import sys
import os
import argparse
import torch
import numpy as np
from pathlib import Path
from sklearn.model_selection import train_test_split
from torch.utils.data import DataLoader
from sklearn.metrics import classification_report, confusion_matrix

# --- SETUP PATHS ---
sys.path.insert(0, str(Path(__file__).parent))
config_path = os.path.join(os.getcwd(), 'ml-services', 'privacy-preserving-lstm', 'config')
sys.path.append(config_path)

from lstm_config import config
from lstm_model import RiskPredictionModel
from lstm_trainer import LSTMTrainer, HybridDataset

# --- DATA UTILS ---
def load_and_split_data():
    """Load NPZ and split into Train/Val/Test with Stratification"""
    path = config.data.DATASET_PATH
    if not Path(path).exists():
        print(f"❌ Error: Data not found at {path}. Run Data Fusion (Phase 4) first.")
        sys.exit(1)
        
    print(f"\nLOADING: {path}")
    data = np.load(path)
    X_dyn = data['X_dynamic']
    X_stat = data['X_static']
    y = data['y']
    
    # Auto-update static dimension
    config.model.STATIC_INPUT_DIM = X_stat.shape[1]
    
    # Split 1: Train vs Temp
    X_d_train, X_d_temp, X_s_train, X_s_temp, y_train, y_temp = train_test_split(
        X_dyn, X_stat, y, 
        test_size=(1 - config.data.TRAIN_RATIO), 
        stratify=y, 
        random_state=42
    )
    
    # Split 2: Test vs Val
    X_d_val, X_d_test, X_s_val, X_s_test, y_val, y_test = train_test_split(
        X_d_temp, X_s_temp, y_temp, 
        test_size=0.5, 
        stratify=y_temp, 
        random_state=42
    )
    
    print(f"✅ Data Splits | Train: {len(y_train)} | Val: {len(y_val)} | Test: {len(y_test)}")
    return (X_d_train, X_s_train, y_train), (X_d_val, X_s_val, y_val), (X_d_test, X_s_test, y_test)

# --- MODES ---
def train_model(args):
    """Execute Training Pipeline"""
    print("\n" + "█"*70)
    print("█ MANO COMPONENT 2: TRAINING MODE")
    print("█"*70 + "\n")
    
    # 1. Load Data
    train_data, val_data, _ = load_and_split_data()
    
    train_loader = DataLoader(HybridDataset(*train_data), batch_size=config.data.BATCH_SIZE, shuffle=True)
    val_loader = DataLoader(HybridDataset(*val_data), batch_size=config.data.BATCH_SIZE)
    
    # Calculate Class Counts for Balancing
    class_counts = np.bincount(train_data[2])
    
    # 2. Initialize Model
    model = RiskPredictionModel(config)
    print(f"\nInitialized Model (Static Dim: {config.model.STATIC_INPUT_DIM})")
    
    # 3. Train
    trainer = LSTMTrainer(model, config, class_counts)
    trainer.fit(train_loader, val_loader)
    
    print(f"\n✅ Training Complete! Model saved to: {config.data.BEST_MODEL_PATH}")

def evaluate_model(args):
    """Execute Evaluation Pipeline"""
    print("\n" + "█"*70)
    print("█ MANO COMPONENT 2: EVALUATION MODE")
    print("█"*70 + "\n")
    
    # 1. Load Data (We only need Test set here)
    _, _, test_data = load_and_split_data()
    test_loader = DataLoader(HybridDataset(*test_data), batch_size=config.data.BATCH_SIZE, shuffle=False)
    
    # 2. Load Model
    model = RiskPredictionModel(config)
    model.load_state_dict(torch.load(config.data.BEST_MODEL_PATH, map_location=config.training.DEVICE))
    model.to(config.training.DEVICE)
    model.eval()
    
    # 3. Inference
    all_preds = []
    all_labels = []
    
    with torch.no_grad():
        for x_d, x_s, y in test_loader:
            x_d, x_s = x_d.to(config.training.DEVICE), x_s.to(config.training.DEVICE)
            outputs = model(x_d, x_s)
            _, preds = torch.max(outputs, 1)
            all_preds.extend(preds.cpu().numpy())
            all_labels.extend(y.numpy())
            
    # 4. Metrics
    print("\n" + "="*60)
    print("TEST SET RESULTS")
    print("="*60)
    print(classification_report(all_labels, all_preds, target_names=['Low Risk', 'Medium Risk', 'High Risk']))
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
        model.load_state_dict(torch.load(config.data.BEST_MODEL_PATH, map_location=config.training.DEVICE))
    except FileNotFoundError:
        print("❌ Error: No trained model found. Run --mode train first.")
        return
        
    model.to(config.training.DEVICE)
    model.eval()
    
    # Create Dummy Sample (1 Patient) for Demo
    # In production, this would come from an API endpoint
    print("Generating random sample for demonstration...")
    sample_dynamic = torch.randn(1, 7, 4).to(config.training.DEVICE)  # 1 Week of data
    sample_static = torch.randn(1, config.model.STATIC_INPUT_DIM).to(config.training.DEVICE)
    
    with torch.no_grad():
        logits = model(sample_dynamic, sample_static)
        probs = torch.softmax(logits, dim=1) # Convert logits to probabilities
        risk_score = torch.argmax(probs).item()
        
    risk_map = {0: "LOW RISK", 1: "MEDIUM RISK", 2: "HIGH RISK"}
    print(f"\n🔮 Prediction:")
    print(f"   Risk Level: {risk_map[risk_score]}")
    print(f"   Confidence: {probs[0][risk_score]*100:.2f}%")
    print(f"   Raw Probabilities: Low={probs[0][0]:.2f}, Med={probs[0][1]:.2f}, High={probs[0][2]:.2f}")

# --- MAIN ENTRY POINT ---
def main():
    parser = argparse.ArgumentParser(description='MANO Component 2: LSTM Execution')
    
    parser.add_argument('--mode', choices=['train', 'eval', 'infer'], 
                        default='train', help='Execution mode')
    
    # Hyperparameter Overrides
    parser.add_argument('--epochs', type=int, help='Override training epochs')
    parser.add_argument('--batch_size', type=int, help='Override batch size')
    parser.add_argument('--lr', type=float, help='Override learning rate')
    
    args = parser.parse_args()
    
    # Apply Overrides to Config
    if args.epochs: config.training.EPOCHS = args.epochs
    if args.batch_size: config.data.BATCH_SIZE = args.batch_size
    if args.lr: config.training.LEARNING_RATE = args.lr
    
    # Execute Mode
    if args.mode == 'train':
        train_model(args)
    elif args.mode == 'eval':
        evaluate_model(args)
    elif args.mode == 'infer':
        infer_model(args)

if __name__ == "__main__":
    main()