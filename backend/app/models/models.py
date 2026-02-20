"""
MANO ORM Models.
Defines the database tables as Python classes.

Each class = one table.
Each attribute = one column.
SQLAlchemy handles the SQL CREATE TABLE, INSERT, SELECT, etc. behind the scenes.
"""
import uuid
from datetime import datetime, timezone
from sqlalchemy import String, Float, DateTime, ForeignKey, Text, JSON
from sqlalchemy.orm import Mapped, mapped_column, relationship
from typing import Optional, List

from backend.app.core.database import Base


def generate_uuid() -> str:
    return str(uuid.uuid4())


class Patient(Base):
    """
    Represents a patient profile in the system.
    
    Stores both demographic data (static features) and the latest
    wearable readings (dynamic vitals). The latest_vitals field is
    updated each time new wearable data arrives.
    """
    __tablename__ = "patients"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=generate_uuid)
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    
    # Demographic vector (20 normalized features) — stored as JSON array
    static_features: Mapped[dict] = mapped_column(JSON, nullable=False)
    
    # Latest 7-day wearable history — stored as JSON array of objects
    # Example: [{"sleep_hours": 7, "sleep_quality": 0.8, ...}, ...]
    latest_vitals: Mapped[Optional[dict]] = mapped_column(JSON, nullable=True)
    
    # Cached risk assessment (updated whenever predict_risk is called)
    current_risk_level: Mapped[Optional[str]] = mapped_column(String(10), nullable=True)
    risk_confidence: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    
    # Timestamps
    created_at: Mapped[datetime] = mapped_column(
        DateTime, default=lambda: datetime.now(timezone.utc)
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime, 
        default=lambda: datetime.now(timezone.utc),
        onupdate=lambda: datetime.now(timezone.utc)
    )
    
    # Relationship: one patient has many simulation results
    simulations: Mapped[List["SimulationResult"]] = relationship(
        back_populates="patient", cascade="all, delete-orphan"
    )


class SimulationResult(Base):
    """
    Stores the result of a "What-If" simulation for a patient.
    
    Each record answers the question:
    "If we applied {intervention} at {intensity}, what would happen?"
    
    This creates a history of all simulations run for a patient,
    enabling comparison and trend analysis.
    """
    __tablename__ = "simulation_results"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=generate_uuid)
    patient_id: Mapped[str] = mapped_column(String(36), ForeignKey("patients.id"), nullable=False)
    
    # What intervention was tested
    intervention_type: Mapped[str] = mapped_column(String(20), nullable=False)
    intensity: Mapped[float] = mapped_column(Float, nullable=False)
    
    # Risk before and after simulation
    original_risk: Mapped[str] = mapped_column(String(10), nullable=False)
    original_confidence: Mapped[float] = mapped_column(Float, nullable=False)
    projected_risk: Mapped[str] = mapped_column(String(10), nullable=False)
    projected_confidence: Mapped[float] = mapped_column(Float, nullable=False)
    risk_reduction_score: Mapped[float] = mapped_column(Float, nullable=False)
    
    # Full simulated 7-day trajectory (stored as JSON)
    future_vitals: Mapped[dict] = mapped_column(JSON, nullable=False)
    
    # Timestamp
    created_at: Mapped[datetime] = mapped_column(
        DateTime, default=lambda: datetime.now(timezone.utc)
    )
    
    # Relationship back to patient
    patient: Mapped["Patient"] = relationship(back_populates="simulations")
