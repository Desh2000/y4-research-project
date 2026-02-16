"""
MANO Backend Setup Script
Automates the migration of trained models and architecture files 
from the 'Research' environment (ml-services) to the 'Production' environment (backend).

Run this whenever you re-train your models and want to update the API.
"""
import shutil
import os
from pathlib import Path

# --- CONFIGURATION ---
PROJECT_ROOT = Path(__file__).resolve().parent.parent
ML_SRC = PROJECT_ROOT / "ml-services"
BACKEND_REPO = PROJECT_ROOT / "backend/app/models_repo"

# Ensure destination exists
if BACKEND_REPO.exists():
    shutil.rmtree(BACKEND_REPO) # Clean slate
BACKEND_REPO.mkdir(parents=True, exist_ok=True)

# --- FILE MAPPING ---
# Format: (Source Path relative to ML_SRC, Destination Filename)
ARTIFACTS_TO_COPY = [
    # 1. LSTM Predictor
    ("privacy-preserving-lstm/models/risk_lstm_best.pth", "risk_lstm.pth"),
    ("privacy-preserving-lstm/src/lstm_model.py", "lstm_model_Def.py"), # Renamed to indicate it's a Definition
    
    # 2. Seq2Seq Simulator
    ("intervention-simulation/models/seq2seq_simulator.pth", "seq2seq_simulator.pth"),
    ("intervention-simulation/src/seq2seq_model.py", "seq2seq_model_Def.py"),
    
    # 3. RL Agent
    ("intervention-simulation/models/rl_agent/ppo_agent.pth", "ppo_agent.pth"),
    ("intervention-simulation/src/rl_agent.py", "rl_agent_Def.py"),
    
    # 4. Data Fusion (Logic Engine)
    # We need the MedicalRuleEngine class logic, but maybe not the whole fusion pipeline.
    # For now, let's grab the file, we might refactor later.
    ("privacy-preserving-lstm/src/data_fusion.py", "rule_engine_Def.py"),
]

def main():
    print(f"🚀 Starting Migration to {BACKEND_REPO}...")
    
    success_count = 0
    
    for src_rel, dest_name in ARTIFACTS_TO_COPY:
        src_path = ML_SRC / src_rel
        dest_path = BACKEND_REPO / dest_name
        
        if not src_path.exists():
            print(f"❌ MISSING: {src_rel}")
            print(f"   (Did you finish training all components?)")
            continue
            
        print(f"📦 Copying: {dest_name}...")
        shutil.copy2(src_path, dest_path)
        
        # Create an __init__.py so Python treats models_repo as a package
        init_file = BACKEND_REPO / "__init__.py"
        init_file.touch()
        
        success_count += 1
        
    print("-" * 40)
    print(f"✅ Migration Complete. {success_count}/{len(ARTIFACTS_TO_COPY)} files copied.")
    print("   Your backend now has the latest brains.")

if __name__ == "__main__":
    main()