# app/main.py
# This is where our FastAPI application starts

"""
Main FastAPI Application
========================
This file creates and configures the FastAPI app.
"""

from fastapi import FastAPI

# Create the FastAPI application
app = FastAPI(
    title="Mano Component 4",
    description="Community-Driven Resilience Clustering System",
    version="1.0.0"
)

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
        "component": "Community-Driven Resilience Clustering System"
    }

# Health check endpoint
@app.get("/health")
def health_check():
    """
    Health check endpoint.
    Returns OK if the system is running.
    """
    return {"status": "OK"}