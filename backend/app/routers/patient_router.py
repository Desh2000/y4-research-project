"""
Patient CRUD Router.
Manages patient profiles — create, read, update, delete.
All simulation results are linked to patients via foreign key.
"""
from fastapi import APIRouter, HTTPException, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func
from sqlalchemy.orm import selectinload

from backend.app.core.database import get_db
from backend.app.core.logging import get_logger
from backend.app.models.models import Patient, SimulationResult
from backend.app.schemas.patient_schema import (
    PatientCreate,
    PatientUpdate,
    PatientResponse,
    PatientWithHistory,
    PatientListResponse,
    SimulationHistoryItem,
)

logger = get_logger("patient_router")
router = APIRouter()


@router.post("/", response_model=PatientResponse, status_code=201)
async def create_patient(data: PatientCreate, db: AsyncSession = Depends(get_db)):
    """
    Register a new patient in the system.
    Provide their demographic features and optionally their initial vitals.
    """
    # Convert vitals from Pydantic objects to JSON-serializable dicts
    vitals_json = None
    if data.latest_vitals:
        vitals_json = [v.model_dump() for v in data.latest_vitals]

    patient = Patient(
        name=data.name,
        static_features=data.static_features,
        latest_vitals=vitals_json,
    )

    db.add(patient)
    await db.commit()
    await db.refresh(patient)

    logger.info("patient_created", patient_id=patient.id, name=patient.name)

    return PatientResponse(
        id=patient.id,
        name=patient.name,
        static_features=patient.static_features,
        latest_vitals=data.latest_vitals,
        current_risk_level=patient.current_risk_level,
        risk_confidence=patient.risk_confidence,
        created_at=patient.created_at,
        updated_at=patient.updated_at,
    )


@router.get("/", response_model=PatientListResponse)
async def list_patients(
    skip: int = 0,
    limit: int = 20,
    db: AsyncSession = Depends(get_db),
):
    """
    List all patients with pagination.
    """
    # Count total
    count_result = await db.execute(select(func.count(Patient.id)))
    total = count_result.scalar()

    # Fetch page
    result = await db.execute(
        select(Patient).offset(skip).limit(limit).order_by(Patient.created_at.desc())
    )
    patients = result.scalars().all()

    return PatientListResponse(
        total=total,
        patients=[
            PatientResponse(
                id=p.id,
                name=p.name,
                static_features=p.static_features,
                latest_vitals=_parse_vitals(p.latest_vitals),
                current_risk_level=p.current_risk_level,
                risk_confidence=p.risk_confidence,
                created_at=p.created_at,
                updated_at=p.updated_at,
            )
            for p in patients
        ],
    )


@router.get("/{patient_id}", response_model=PatientWithHistory)
async def get_patient(patient_id: str, db: AsyncSession = Depends(get_db)):
    """
    Get a single patient with their full simulation history.
    """
    result = await db.execute(
        select(Patient)
        .where(Patient.id == patient_id)
        .options(selectinload(Patient.simulations))
    )
    patient = result.scalar_one_or_none()

    if not patient:
        raise HTTPException(status_code=404, detail=f"Patient {patient_id} not found")

    return PatientWithHistory(
        id=patient.id,
        name=patient.name,
        static_features=patient.static_features,
        latest_vitals=_parse_vitals(patient.latest_vitals),
        current_risk_level=patient.current_risk_level,
        risk_confidence=patient.risk_confidence,
        created_at=patient.created_at,
        updated_at=patient.updated_at,
        simulations=[
            SimulationHistoryItem(
                id=s.id,
                intervention_type=s.intervention_type,
                intensity=s.intensity,
                original_risk=s.original_risk,
                projected_risk=s.projected_risk,
                risk_reduction_score=s.risk_reduction_score,
                created_at=s.created_at,
            )
            for s in sorted(patient.simulations, key=lambda s: s.created_at, reverse=True)
        ],
    )


@router.put("/{patient_id}", response_model=PatientResponse)
async def update_patient(
    patient_id: str,
    data: PatientUpdate,
    db: AsyncSession = Depends(get_db),
):
    """
    Update a patient's demographics or vitals.
    Only the fields you provide will be updated.
    """
    result = await db.execute(select(Patient).where(Patient.id == patient_id))
    patient = result.scalar_one_or_none()

    if not patient:
        raise HTTPException(status_code=404, detail=f"Patient {patient_id} not found")

    if data.name is not None:
        patient.name = data.name
    if data.static_features is not None:
        patient.static_features = data.static_features
    if data.latest_vitals is not None:
        patient.latest_vitals = [v.model_dump() for v in data.latest_vitals]

    await db.commit()
    await db.refresh(patient)

    logger.info("patient_updated", patient_id=patient.id)

    return PatientResponse(
        id=patient.id,
        name=patient.name,
        static_features=patient.static_features,
        latest_vitals=_parse_vitals(patient.latest_vitals),
        current_risk_level=patient.current_risk_level,
        risk_confidence=patient.risk_confidence,
        created_at=patient.created_at,
        updated_at=patient.updated_at,
    )


@router.delete("/{patient_id}", status_code=204)
async def delete_patient(patient_id: str, db: AsyncSession = Depends(get_db)):
    """
    Delete a patient and all their simulation history.
    (cascade delete is defined on the ORM model)
    """
    result = await db.execute(select(Patient).where(Patient.id == patient_id))
    patient = result.scalar_one_or_none()

    if not patient:
        raise HTTPException(status_code=404, detail=f"Patient {patient_id} not found")

    await db.delete(patient)
    await db.commit()

    logger.info("patient_deleted", patient_id=patient_id)


# --- HELPERS ---

def _parse_vitals(vitals_json):
    """Convert stored JSON vitals back to DayVitals schema objects."""
    if not vitals_json:
        return None
    from backend.app.schemas.simulation_schema import DayVitals
    return [DayVitals(**v) for v in vitals_json]
