from fastapi import APIRouter, HTTPException
from app.models.schemas import PatientData, PredictionResponse
from app.services.predictor import predictor
from app.models.schemas import PatientData, PredictionResponse, ForecastRequest, ForecastResponse
from app.services.predictor import predictor

router = APIRouter()

@router.post("/predict", response_model=PredictionResponse)
async def predict_risk(patient: PatientData):
    try:
        return predictor.predict(patient)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    
@router.post("/forecast", response_model=ForecastResponse, tags=["Future Forecasting"])
async def forecast_risk(history: ForecastRequest):
    """
    **Predict Next-Day Risk**
    
    Accepts 3 days of history. 
    Detects the trend (e.g., Sleep declining) and predicts tomorrow's risk state.
    """
    try:
        return predictor.predict_trajectory(history)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))