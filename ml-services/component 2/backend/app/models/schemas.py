from pydantic import BaseModel, Field
from typing import Optional

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