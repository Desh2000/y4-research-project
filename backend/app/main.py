from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import torch
import sys
import os
from pathlib import Path

# --- PATH FIX ---
# When running this file directly (python main.py), Python doesn't know that
# this file lives inside a package called "backend.app". We fix that by adding
# the project root (two levels up from this file) to sys.path, so Python can
# find "backend.app.routers", "backend.app.services", etc.
_project_root = str(Path(__file__).resolve().parent.parent.parent)
if _project_root not in sys.path:
    sys.path.insert(0, _project_root)

# --- IMPORT ROUTERS ---
# We import the router we just created. Think of this as plugging a module into the main board.
from backend.app.routers import simulation_router

# --- IMPORT SERVICES ---
from backend.app.services.risk_service import RiskPredictionService
from backend.app.services.intervention_service import InterventionService

# --- LIFESPAN MANAGER (STARTUP/SHUTDOWN LOGIC) ---
@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    This function runs ONCE before the server starts accepting requests.
    It is the perfect place to load heavy ML models into memory (RAM/VRAM).
    """
    print("\n" + "█"*60)
    print("🚀 MANO AI ENGINE: STARTING UP (FORTRESS MODE)")
    print("█"*60)
    
    # 1. Hardware Detection
    device = "cuda" if torch.cuda.is_available() else "cpu"
    if device == "cuda":
        print(f"   ✅ GPU DETECTED: {torch.cuda.get_device_name(0)}")
    else:
        print("   ⚠️  WARNING: Running on CPU. Inference will be slower.")

    # 2. Locate the Models Repository
    # We resolve the absolute path to ensure no "file not found" errors regardless of where you run uvicorn
    base_dir = Path(__file__).resolve().parent
    repo_path = base_dir / "models_repo"
    
    # 3. Load the Brains into the Singleton Services
    try:
        # Load the Hybrid LSTM
        risk_svc = RiskPredictionService()
        risk_svc.load_model(str(repo_path / "risk_lstm.pth"), device)
        
        # Load the AMISE Simulator & Agent
        int_svc = InterventionService()
        int_svc.load_models(
            sim_path=str(repo_path / "seq2seq_simulator.pth"),
            agent_path=str(repo_path / "ppo_agent.pth"),
            device=device
        )
    except Exception as e:
        print(f"\n❌ CRITICAL ERROR: Failed to load models.\nDetails: {str(e)}")
        print("Did you run `python scripts/setup_backend.py` to copy the models?")
        # We don't strictly exit here so the /health endpoint can still report failure if needed in K8s
        
    print("   ✅ System Ready. Awaiting requests...")
    
    yield # --- THE SERVER RUNS HERE ---
    
    print("\n🛑 MANO AI ENGINE: SHUTTING DOWN")
    # Resources are automatically cleaned up when the script exits.

# --- INITIALIZE APP ---
app = FastAPI(
    title="MANO AI Engine - Component 1",
    description="Privacy-Preserving Mental Health Intervention & Simulation API",
    version="1.0.0",
    lifespan=lifespan
)

# --- CORS CONFIGURATION ---
# Security rule: Specifies which external websites are allowed to talk to this API.
# We whitelist typical React/Next.js local development ports.
origins = [
    "http://localhost:3000",
    "http://127.0.0.1:3000",
    "http://localhost:5173", # Standard Vite port, just in case
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"], # Allow GET, POST, PUT, DELETE
    allow_headers=["*"], # Allow all headers
)

# --- BASE ENDPOINTS ---
@app.get("/")
async def root_health_check():
    """
    Root health check. Used by Docker/Kubernetes to verify the container is alive.
    """
    return {
        "status": "online",
        "system": "MANO AI Backend",
        "gpu_enabled": torch.cuda.is_available()
    }

# --- REGISTER ROUTERS ---
# We attach our simulation endpoints under the /api/v1/simulation prefix.
app.include_router(
    simulation_router.router, 
    prefix="/api/v1/simulation", 
    tags=["Simulation & Optimization"]
)

# --- DIRECT EXECUTION ---
# This allows you to run the file directly: python main.py
# It starts uvicorn which properly loads the app as a package.
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "backend.app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=True
    )