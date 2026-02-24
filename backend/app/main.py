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

# Also add backend/app/ so that "from models_repo.xxx" resolves correctly
# when uvicorn runs from the project root (not from backend/app/).
_app_dir = str(Path(__file__).resolve().parent)
if _app_dir not in sys.path:
    sys.path.insert(0, _app_dir)

# --- CORE UTILITIES ---
from backend.app.core.logging import setup_logging, get_logger
from backend.app.core.middleware import RequestLoggingMiddleware, ErrorHandlerMiddleware
from backend.app.core.health import router as health_router

# --- DATABASE ---
from backend.app.core.database import create_tables

# --- IMPORT ROUTERS ---
from backend.app.routers import simulation_router
from backend.app.routers import patient_router
from backend.app.routers import whatif_router
from backend.app.routers import xai_router
from backend.app.routers import nba_router

# --- IMPORT SERVICES ---
from backend.app.services.risk_service import RiskPredictionService
from backend.app.services.intervention_service import InterventionService

# Initialize structured logging BEFORE anything else
setup_logging()
logger = get_logger("main")


# --- LIFESPAN MANAGER (STARTUP/SHUTDOWN LOGIC) ---
@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    This function runs ONCE before the server starts accepting requests.
    It is the perfect place to load heavy ML models into memory (RAM/VRAM).
    """
    logger.info("startup_begin", message="MANO AI ENGINE: STARTING UP")

    # Track which models loaded successfully (used by /health endpoint)
    models_status = {"lstm": False, "simulator": False, "agent": False}

    # 1. Hardware Detection
    device = "cuda" if torch.cuda.is_available() else "cpu"
    app.state.device = device
    app.state.gpu_enabled = device == "cuda"

    if device == "cuda":
        logger.info("gpu_detected", gpu_name=torch.cuda.get_device_name(0))
    else:
        logger.warning("no_gpu", message="Running on CPU. Inference will be slower.")

    # 2. Locate the Models Repository
    base_dir = Path(__file__).resolve().parent
    repo_path = base_dir / "models_repo"

    # 3. Load the Brains into the Singleton Services
    try:
        # Load the Hybrid LSTM
        logger.info("loading_model", model="risk_lstm", path=str(repo_path / "risk_lstm.pth"))
        risk_svc = RiskPredictionService()
        risk_svc.load_model(str(repo_path / "risk_lstm.pth"), device)
        models_status["lstm"] = True
        logger.info("model_loaded", model="risk_lstm", status="success")
    except Exception as e:
        logger.error("model_load_failed", model="risk_lstm", error=str(e))

    try:
        # Load the AMISE Simulator & Agent
        logger.info("loading_model", model="amise_engines")
        int_svc = InterventionService()
        int_svc.load_models(
            sim_path=str(repo_path / "seq2seq_simulator.pth"),
            agent_path=str(repo_path / "ppo_agent.pth"),
            device=device
        )
        models_status["simulator"] = True
        models_status["agent"] = True
        logger.info("model_loaded", model="amise_engines", status="success")
    except Exception as e:
        logger.error("model_load_failed", model="amise_engines", error=str(e))

    # Store status on app.state for the /health endpoint
    app.state.models_loaded = models_status

    # 4. Create database tables
    logger.info("creating_db_tables", message="Initializing database...")
    await create_tables()
    logger.info("db_ready", message="Database tables created.")

    if all(models_status.values()):
        logger.info("startup_complete", message="All models loaded. System ready.")
    else:
        logger.warning("startup_degraded", models_status=models_status,
                       message="Some models failed to load. Check logs above.")

    yield  # --- THE SERVER RUNS HERE ---

    logger.info("shutdown", message="MANO AI ENGINE: SHUTTING DOWN")


# --- INITIALIZE APP ---
app = FastAPI(
    title="MANO AI Engine - Component 1",
    description="Privacy-Preserving Mental Health Intervention & Simulation API",
    version="1.0.0",
    lifespan=lifespan
)

# --- MIDDLEWARE STACK ---
# Order matters! Error handler wraps everything, then request logging runs inside it.
# Think of it like layers of an onion:
#   ErrorHandler → RequestLogging → CORS → Your Route Handler
app.add_middleware(ErrorHandlerMiddleware)
app.add_middleware(RequestLoggingMiddleware)

# --- CORS CONFIGURATION ---
origins = [
    "http://localhost:3000",
    "http://127.0.0.1:3000",
    "http://localhost:5173",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- BASE ENDPOINTS ---
@app.get("/")
async def root_liveness():
    """
    Liveness probe. Just confirms the process is alive.
    For model readiness, use /health instead.
    """
    return {
        "status": "online",
        "system": "MANO AI Backend",
        "gpu_enabled": torch.cuda.is_available()
    }

# --- REGISTER ROUTERS ---
app.include_router(health_router)
app.include_router(
    simulation_router.router,
    prefix="/api/v1/simulation",
    tags=["Simulation & Optimization"]
)
app.include_router(
    patient_router.router,
    prefix="/api/v1/patients",
    tags=["Patient Management"]
)
app.include_router(
    whatif_router.router,
    prefix="/api/v1/whatif",
    tags=["What-If Lifestyle Simulator"]
)
app.include_router(
    xai_router.router,
    prefix="/api/v1/xai",
    tags=["Explainable AI"]
)
app.include_router(
    nba_router.router,
    prefix="/api/v1/nba",
    tags=["Next-Best-Action"]
)

# --- DIRECT EXECUTION ---
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "backend.app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=False  # IMPORTANT: reload=True crashes CUDA DLL on Windows subprocess fork
    )