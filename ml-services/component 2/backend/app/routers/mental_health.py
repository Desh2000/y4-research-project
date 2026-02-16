# routers/mental_health.py
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
import pandas as pd
import numpy as np
import tensorflow as tf
import joblib
import os

# Create the Router (Think of this as a "Mini App" for Mental Health)
router = APIRouter(
    prefix="/mental-health",
    tags=["Mental Health AI"]
)

# --- 1. CONFIGURATION & LOADING ---
# Define paths to your saved model files
MODEL_PATH = "model_artifacts/model.keras"
SCALER_PATH = "model_artifacts/3-class-scaler.pkl"
ENCODERS_PATH = "model_artifacts/3-class-encoders.pkl"

print(">>> Loading Mental Health AI Artifacts...")
try:
    # Check if files exist before loading to avoid crashing the whole server
    if os.path.exists(MODEL_PATH) and os.path.exists(SCALER_PATH):
        model = tf.keras.models.load_model(MODEL_PATH)
        scaler = joblib.load(SCALER_PATH)
        encoders = joblib.load(ENCODERS_PATH)
        print("✅ Mental Health Model Loaded Successfully!")
    else:
        print("⚠️ Warning: Model files not found in /models directory.")
        model, scaler, encoders = None, None, None
except Exception as e:
    print(f"❌ Error loading model: {e}")
    model, scaler, encoders = None, None, None

# --- 2. DEFINE INPUT DATA MODEL (All 16 Features) ---
class UserData(BaseModel):
    # Demographics
    Age: int
    Gender: str              # "Male", "Female", etc.
    Education_Level: str     # "Bachelor", "Master", "PhD", etc.
    Employment_Status: str   # "Employed", "Student", "Unemployed"
    
    # Lifestyle
    Sleep_Hours: float
    Physical_Activity_Hrs: float
    Social_Support_Score: int # Scale 1-10
    
    # Medical History (0 = No, 1 = Yes)
    Family_History_Mental_Illness: int 
    Chronic_Illnesses: int
    Therapy: int
    Meditation: int
    
    # Psychological Scores (Scale 1-10 or 1-9)
    Financial_Stress: int     
    Work_Stress: int          
    Self_Esteem_Score: int    
    Life_Satisfaction_Score: int
    Loneliness_Score: int

# --- 3. HELPER FUNCTION: PREDICT ---
@router.post("/predict")
async def predict_mental_health(data: UserData):
    """
    Receives user data, processes it, and returns Stress/Anxiety/Depression risks.
    """
    if model is None:
        raise HTTPException(status_code=503, detail="Model not loaded. Check server logs.")

    try:
        # A. Convert Input JSON to DataFrame
        input_data = data.dict()
        df = pd.DataFrame([input_data])
        
        # B. Define Feature Columns (ORDER MATTERS - Must match training exactly)
        feature_cols = [
            'Age', 'Gender', 'Education_Level', 'Employment_Status', 
            'Sleep_Hours', 'Physical_Activity_Hrs', 'Social_Support_Score', 
            'Family_History_Mental_Illness', 'Chronic_Illnesses', 
            'Therapy', 'Meditation', 
            'Financial_Stress', 'Work_Stress', 'Self_Esteem_Score', 
            'Life_Satisfaction_Score', 'Loneliness_Score'
        ]
        
        # C. Preprocessing (The "Translator")
        # 1. Ensure all columns exist
        for col in feature_cols:
            if col not in df.columns:
                df[col] = 0
        
        # 2. Encode Categoricals (Male -> 1)
        for col, le in encoders.items():
            if col in df.columns:
                val = str(df[col].iloc[0])
                # Handle unseen labels (e.g., if user sends "Other" but model only knows "Male/Female")
                if val in le.classes_:
                    df[col] = le.transform([val])
                else:
                    df[col] = 0 # Default to 0
        
        # 3. Scale Numerics (The "Ruler")
        scaled_data = scaler.transform(df[feature_cols])

        # D. Predict (The "Brain")
        preds = model.predict(scaled_data)
        # preds[0] = Stress Probs, preds[1] = Anxiety Probs, preds[2] = Depression Probs
       
       # E. Calculate Risk Scores (0-100%)
        def get_score(probs):
            # probs shape is (1, 3) -> [[Low, Med, High]]
            score = (probs[0][1] * 50) + (probs[0][2] * 100)
            
            return float(min(round(score, 1), 100.0))

        stress_score = get_score(preds[0])
        anxiety_score = get_score(preds[1])
        depression_score = get_score(preds[2])

        # F. Determine Text Labels
        def get_label(score):
            if score < 35: return "Low"
            elif score < 70: return "Moderate"
            else: return "High"

        # G. Return JSON Response
        return {
            "status": "success",
            "results": {
                "stress": {
                    "score": stress_score,
                    "risk_level": get_label(stress_score)
                },
                "anxiety": {
                    "score": anxiety_score,
                    "risk_level": get_label(anxiety_score)
                },
                "depression": {
                    "score": depression_score,
                    "risk_level": get_label(depression_score)
                }
            }
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction Error: {str(e)}")