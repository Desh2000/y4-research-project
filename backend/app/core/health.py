"""
MANO Health Check Endpoint.
Reports the actual readiness of the AI engine, not just "is the process alive".

WHY A SEPARATE HEALTH CHECK?
In Kubernetes/Docker, there are two types of probes:
1. Liveness Probe ("Is the container alive?")  → the root `/` endpoint
2. Readiness Probe ("Can it serve requests?")  → this `/health` endpoint

If models fail to load, the server process is still alive (liveness = OK),
but it CAN'T serve predictions (readiness = FAIL). This distinction matters
in production — K8s won't route traffic to a pod that isn't ready.
"""
from fastapi import APIRouter, Request

router = APIRouter(tags=["System"])


@router.get("/health")
async def readiness_check(request: Request):
    """
    Deep health check. Verifies that all ML models are actually loaded
    and ready to serve inference requests.
    
    Returns 200 if all models are loaded, 503 if any model failed.
    """
    # Read model status from app.state (set during lifespan startup)
    models_status = getattr(request.app.state, "models_loaded", {})

    all_healthy = all(models_status.values()) if models_status else False

    response = {
        "status": "healthy" if all_healthy else "degraded",
        "models": models_status,
        "gpu_enabled": getattr(request.app.state, "gpu_enabled", False),
        "device": getattr(request.app.state, "device", "unknown"),
    }

    if not all_healthy:
        # Return 503 Service Unavailable so load balancers know to skip this instance
        from fastapi.responses import JSONResponse
        return JSONResponse(status_code=503, content=response)

    return response
