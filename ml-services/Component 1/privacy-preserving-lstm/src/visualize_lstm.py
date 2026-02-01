"""
MANO Component 2: LSTM Visualization Suite
Generates Confusion Matrix, ROC, and PR Curves for Risk Prediction.
"""
import torch
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.metrics import confusion_matrix, roc_curve, auc, precision_recall_curve
from sklearn.preprocessing import label_binarize
import sys
import os
from pathlib import Path

# --- SETUP PATHS ---
sys.path.insert(0, str(Path(__file__).parent))
config_path = os.path.join(os.getcwd(), 'ml-services', 'privacy-preserving-lstm', 'config')
sys.path.append(config_path)

from lstm_config import config
from lstm_model import RiskPredictionModel
from lstm_data_loader import load_and_split_data, HybridDataset
from torch.utils.data import DataLoader

def visualize_lstm():
    print("\n" + "="*60)
    print("GENERATING LSTM VISUALIZATIONS")
    print("="*60)
    
    device = config.training.DEVICE
    save_dir = Path("ml-services/privacy-preserving-gan/gan_logs/plots/lstm")
    save_dir.mkdir(parents=True, exist_ok=True)
    
    # 1. Load Data (Test Set Only)
    _, _, test_loader, _ = load_and_split_data()
    
    # 2. Load Model
    model = RiskPredictionModel(config)
    model.load_state_dict(torch.load(config.data.BEST_MODEL_PATH, map_location=device))
    model.to(device)
    model.eval()
    
    # 3. Inference
    y_true = []
    y_pred = []
    y_score = [] # Probabilities
    
    print("Running Inference on Test Set...")
    with torch.no_grad():
        for x_d, x_s, y in test_loader:
            x_d, x_s = x_d.to(device), x_s.to(device)
            logits = model(x_d, x_s)
            probs = torch.softmax(logits, dim=1)
            _, preds = torch.max(logits, 1)
            
            y_true.extend(y.numpy())
            y_pred.extend(preds.cpu().numpy())
            y_score.extend(probs.cpu().numpy())
            
    y_true = np.array(y_true)
    y_pred = np.array(y_pred)
    y_score = np.array(y_score)
    
    # --- PLOT 1: CONFUSION MATRIX ---
    print("1. Generating Confusion Matrix...")
    cm = confusion_matrix(y_true, y_pred)
    # Normalize for better visualization
    cm_norm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]
    
    plt.figure(figsize=(8, 6))
    sns.heatmap(cm_norm, annot=True, fmt='.2%', cmap='Blues', 
                xticklabels=['Low', 'Med', 'High'], 
                yticklabels=['Low', 'Med', 'High'])
    plt.title('Normalized Confusion Matrix')
    plt.ylabel('True Risk')
    plt.xlabel('Predicted Risk')
    plt.savefig(save_dir / "confusion_matrix.png", dpi=300)
    plt.close()
    
    # --- PLOT 2: ROC CURVE (Multi-Class) ---
    print("2. Generating ROC Curve...")
    n_classes = 3
    y_true_bin = label_binarize(y_true, classes=[0, 1, 2])
    
    plt.figure(figsize=(10, 8))
    colors = ['blue', 'green', 'red']
    labels = ['Low Risk', 'Medium Risk', 'High Risk']
    
    for i in range(n_classes):
        fpr, tpr, _ = roc_curve(y_true_bin[:, i], y_score[:, i])
        roc_auc = auc(fpr, tpr)
        plt.plot(fpr, tpr, color=colors[i], lw=2,
                 label=f'{labels[i]} (AUC = {roc_auc:.2f})')
                 
    plt.plot([0, 1], [0, 1], 'k--', lw=2)
    plt.xlim([0.0, 1.0])
    plt.ylim([0.0, 1.05])
    plt.xlabel('False Positive Rate')
    plt.ylabel('True Positive Rate')
    plt.title('Multi-Class ROC Curve')
    plt.legend(loc="lower right")
    plt.savefig(save_dir / "roc_curve.png", dpi=300)
    plt.close()
    
    print(f"✅ LSTM Visualizations saved to: {save_dir}")

if __name__ == "__main__":
    visualize_lstm()