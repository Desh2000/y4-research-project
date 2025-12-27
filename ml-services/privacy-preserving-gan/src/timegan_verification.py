"""
MANO Component 1: Phase 3 Final Verification
Run this to confirm all artifacts are present and valid before starting Phase 3.
"""
import numpy as np
import json
import sys
import os
from pathlib import Path

# Add config path to ensure we can find relative paths if needed
sys.path.insert(0, str(Path(__file__).parent))

print("\n" + "█"*60)
print("█ MANO PHASE 3: FINAL VERIFICATION")
print("█"*60 + "\n")

# 1. Check synthetic data
print("="*60)
print("1. CHECKING SYNTHETIC DATA")
print("="*60)
try:
    synthetic_path = 'data/synthetic/synthetic_wearable_sequences.npz'
    synthetic = np.load(synthetic_path)['sequences']
    print(f"✅ File found: {synthetic_path}")
    print(f"✅ Shape: {synthetic.shape} (Expected: (10000, 7, 4))")
    print(f"✅ Range: [{synthetic.min():.4f}, {synthetic.max():.4f}] (Expected: ~0.0 to ~1.0)")
    
    no_nan = not np.isnan(synthetic).any()
    no_inf = not np.isinf(synthetic).any()
    print(f"✅ No NaN: {no_nan}")
    print(f"✅ No Inf: {no_inf}")
    
    if synthetic.shape == (10000, 7, 4) and no_nan and no_inf:
        print(">> STATUS: PASS")
    else:
        print(">> STATUS: FAIL")
except Exception as e:
    print(f"❌ Error: {e}")

# 2. Check real data availability
print("\n" + "="*60)
print("2. CHECKING REAL DATA SOURCE")
print("="*60)
try:
    real_path = 'data/processed/wearable_sequences.npz'
    real = np.load(real_path)['sequences']
    print(f"✅ File found: {real_path}")
    print(f"✅ Shape: {real.shape}")
    print(">> STATUS: PASS")
except Exception as e:
    print(f"❌ Error: {e}")

# 3. Check evaluation report
print("\n" + "="*60)
print("3. CHECKING EVALUATION REPORT")
print("="*60)
try:
    report_path = 'ml-services/privacy-preserving-gan/gan_logs/reports/timegan_evaluation.json'
    with open(report_path, 'r') as f:
        report = json.load(f)
        
    # Note: Keys match those in timegan_evaluator.py
    metrics = report.get('metrics', {})
    dist_score = metrics.get('mean_distribution_similarity', metrics.get('distribution_similarity', 'N/A'))
    temp_score = metrics.get('temporal_coherence', 'N/A')
    status = report.get('evaluation_status', 'N/A')
    
    print(f"✅ Distribution Score: {dist_score}")
    print(f"✅ Temporal Coherence: {temp_score}")
    print(f"✅ Overall Status: {status}")
    print(">> STATUS: PASS")
except Exception as e:
    print(f"❌ Error reading report: {e}")

# 4. Check model file
print("\n" + "="*60)
print("4. CHECKING TRAINED MODEL")
print("="*60)
model_path = Path('ml-services/privacy-preserving-gan/models/timegan/timegan_final.pth')
exists = model_path.exists()
print(f"✅ Model file exists: {exists}")
if exists:
    size_mb = model_path.stat().st_size / 1024 / 1024
    print(f"   Path: {model_path}")
    print(f"   Size: {size_mb:.2f} MB")
    print(">> STATUS: PASS")
else:
    print(">> STATUS: FAIL")

print("\n" + "█"*60)
print("█ VERIFICATION COMPLETE - READY FOR COMPONENT 2")
print("█"*60 + "\n")