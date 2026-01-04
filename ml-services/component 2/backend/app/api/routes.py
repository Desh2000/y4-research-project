from fastapi import APIRouter, HTTPException
from app.models.schemas import PatientData, PredictionResponse
from app.services.predictor import predictor

router = APIRouter()

@router.post("/predict", response_model=PredictionResponse)
async def predict_risk(patient: PatientData):
    try:
        return predictor.predict(patient)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))