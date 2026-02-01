"""
MANO Component 2: LSTM Robustness & Logic Test
Verifies that the trained model behaves logically (e.g., High Stress -> Higher Risk Score).
"""
import torch
import numpy as np
import sys
import os
from pathlib import Path

# --- SETUP PATHS ---
sys.path.insert(0, str(Path(__file__).parent))
config_path = os.path.join(os.getcwd(), 'ml-services', 'privacy-preserving-lstm', 'config')
sys.path.append(config_path)

from lstm_config import config
from lstm_model import RiskPredictionModel

def test_model_logic():
    print("\n" + "="*60)
    print("TEST 1: LSTM LOGIC & ROBUSTNESS")
    print("="*60)
    
    device = config.training.DEVICE
    
    # 1. Load Model
    print("1. Loading Trained Model...")
    model = RiskPredictionModel(config)
    try:
        model.load_state_dict(torch.load(config.data.BEST_MODEL_PATH, map_location=device))
    except FileNotFoundError:
        print("❌ Error: Trained model not found. Train LSTM first.")
        return
        
    model.to(device)
    model.eval()
    
    # 2. Create Baseline Patient (Average Health)
    # Dynamic: [1, 7, 4] -> All 0.5 (Mid-range)
    base_dyn = torch.full((1, 7, 4), 0.5).to(device)
    # Static: [1, 20] -> All 0.5
    base_stat = torch.full((1, config.model.STATIC_INPUT_DIM), 0.5).to(device)
    
    # 3. Create "Stressed" Patient
    # Stress is Index 3. We set it to 1.0 (Max Stress)
    stressed_dyn = base_dyn.clone()
    stressed_dyn[:, :, 3] = 1.0 
    
    # 4. Create "Healthy" Patient
    # Sleep (Index 0) = 1.0, Stress (Index 3) = 0.0
    healthy_dyn = base_dyn.clone()
    healthy_dyn[:, :, 0] = 1.0
    healthy_dyn[:, :, 3] = 0.0
    
    print("2. Running Inference Scenarios...")
    with torch.no_grad():
        # Get Risk Probabilities (High Risk is Index 2)
        base_prob = torch.softmax(model(base_dyn, base_stat), dim=1)[0, 2].item()
        stress_prob = torch.softmax(model(stressed_dyn, base_stat), dim=1)[0, 2].item()
        healthy_prob = torch.softmax(model(healthy_dyn, base_stat), dim=1)[0, 2].item()
        
    print(f"   Baseline High-Risk Prob: {base_prob:.4f}")
    print(f"   Stressed High-Risk Prob: {stress_prob:.4f}")
    print(f"   Healthy High-Risk Prob:  {healthy_prob:.4f}")
    
    # 5. Assertions
    print("\n3. Verifying Logic...")
    
    if stress_prob > base_prob:
        print("   ✅ PASS: Increased Stress increased Risk Score.")
    else:
        print("   ❌ FAIL: Model ignored high stress.")
        
    if healthy_prob < base_prob:
        print("   ✅ PASS: Good Habits decreased Risk Score.")
    else:
        print("   ❌ FAIL: Model ignored healthy habits.")
        
    if healthy_prob < 0.1 and stress_prob > 0.5:
        print("   ✅ PASS: Model shows strong discrimination capability.")
    else:
        print("   ⚠️ WARNING: Model sensitivity might be low.")

if __name__ == "__main__":
    test_model_logic()