from pydantic import BaseModel, Field
from typing import Optional, List

class PatientData(BaseModel):
    gender: str = Field(..., example="Male")
    age: int = Field(..., example=30)
    occupation: str = Field(..., example="Software Engineer")
    sleep_duration: float = Field(..., example=6.5)
    sleep_quality: int = Field(..., example=6)
    physical_activity: int = Field(..., example=45)
    bmi_category: str = Field(..., example="Overweight")
    blood_pressure: str = Field(..., example="126/83")
    heart_rate: int = Field(..., example=78)
    daily_steps: int = Field(..., example=6000)
    sleep_disorder: Optional[str] = Field("None", example="None")

class PredictionResponse(BaseModel):
    risk_class: int
    risk_label: str
    confidence: float
    probabilities: dict

class ForecastRequest(BaseModel):
    history: List[PatientData] = Field(
        ..., 
        min_items=3, 
        max_items=3, 
        description="Exact 3-day history: [Day T-2, Day T-1, Today]"
    )

class ForecastResponse(BaseModel):
    forecast_target: str = Field(..., example="Tomorrow (Day T+1)")
    predicted_risk_class: int
    predicted_risk_label: str
    trend_analysis: str = Field(..., example="Sleep is dropping rapidly (-1.5h/day)")
    confidence: float