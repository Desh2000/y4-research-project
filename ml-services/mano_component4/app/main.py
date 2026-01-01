# app/main.py
# This is where our FastAPI application starts

"""
Main FastAPI Application
========================
This file creates and configures the FastAPI app.
"""

from fastapi import FastAPI
from app.api.routes import router

# Create the FastAPI application
app = FastAPI(
    title="Mano Component 4",
    description="""
    ## Community-Driven Resilience Clustering System

    This API provides:

    * **Scoring** - Calculate stress/resilience scores from user data
    * **Clustering** - Assign users to peer support groups
    * **Analysis** - Complete user analysis with interpretation
    * **Recommendations** - Activity suggestions (coming soon)

    ### How to use:

    1. Send user data to `/api/score` to get stress scores
    2. Send user data to `/api/analyze` for complete analysis including peer group
    3. Use `/api/groups` to see all available peer groups
    """,
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# Include the API routes
app.include_router(router, prefix="/api", tags=["API"])


# Root endpoint - just to test if API is working
@app.get("/")
def read_root():
    """
    Root endpoint.
    Returns a welcome message.
    """
    return {
        "message": "Welcome to Mano Component 4!",
        "status": "running",
        "component": "Community-Driven Resilience Clustering System",
        "documentation": "/docs",
        "api_base": "/api"
    }


# Health check endpoint
@app.get("/health")
def health_check():
    """
    Health check endpoint.
    Returns OK if the system is running.
    """
    return {"status": "OK", "service": "Mano Component 4"}