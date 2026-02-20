"""
Patient CRUD Schemas.
Pydantic models for patient creation, updates, and API responses.
"""
from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime

from backend.app.schemas.simulation_schema import DayVitals


# --- INPUT SCHEMAS ---

class PatientCreate(BaseModel):
    """Schema for creating a new patient profile."""
    name: str = Field(..., min_length=1, max_length=100, description="Patient display name")
    static_features: List[float] = Field(
        ..., 
        min_length=20, max_length=20,
        description="20-dim normalized demographic vector"
    )
    latest_vitals: Optional[List[DayVitals]] = Field(
        None, 
        min_length=7, max_length=7,
        description="Initial 7-day wearable history (optional, can be added later)"
    )


class PatientUpdate(BaseModel):
    """Schema for updating a patient. All fields are optional."""
    name: Optional[str] = Field(None, min_length=1, max_length=100)
    static_features: Optional[List[float]] = Field(None, min_length=20, max_length=20)
    latest_vitals: Optional[List[DayVitals]] = Field(None, min_length=7, max_length=7)


# --- OUTPUT SCHEMAS ---

class SimulationHistoryItem(BaseModel):
    """One simulation record in the patient's history."""
    id: str
    intervention_type: str
    intensity: float
    original_risk: str
    projected_risk: str
    risk_reduction_score: float
    created_at: datetime


class PatientResponse(BaseModel):
    """Full patient profile returned by the API."""
    id: str
    name: str
    static_features: List[float]
    latest_vitals: Optional[List[DayVitals]] = None
    current_risk_level: Optional[str] = None
    risk_confidence: Optional[float] = None
    created_at: datetime
    updated_at: datetime

    model_config = {"from_attributes": True}


class PatientWithHistory(PatientResponse):
    """Patient profile with attached simulation history."""
    simulations: List[SimulationHistoryItem] = []

    model_config = {"from_attributes": True}


class PatientListResponse(BaseModel):
    """Paginated list of patients."""
    total: int
    patients: List[PatientResponse]
