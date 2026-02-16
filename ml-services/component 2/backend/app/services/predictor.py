import os
import joblib
import numpy as np
import tensorflow as tf
from app.models.schemas import PatientData

class RiskPredictor:
    def __init__(self):
        self.model = None
        self.scaler = None
        self.encoders = None
        self.labels = {0: "🟢 Low Risk", 1: "🟡 Medium Risk", 2: "🔴 High Risk"}
        self.load_artifacts()

    def load_artifacts(self):
        try:
            base = "model_artifacts"
            self.model = tf.keras.models.load_model(os.path.join(base, "risk_model_3class.keras"))
            self.scaler = joblib.load(os.path.join(base, "scaler.pkl"))
            self.encoders = joblib.load(os.path.join(base, "encoders.pkl"))
            print("✅ Artifacts Loaded.")
        except Exception as e:
            print(f"❌ Error loading artifacts: {e}")

    def preprocess(self, data: PatientData):
        # 1. Parse BP
        try:
            sys, dia = map(float, data.blood_pressure.split('/'))
        except:
            sys, dia = 120.0, 80.0

        # 2. Encode Text (Using saved encoders)
        def encode(col, val):
            enc = self.encoders.get(col)
            return enc.transform([val])[0] if enc and val in enc.classes_ else 0

        features = [
            encode('Gender', data.gender),
            data.age,
            encode('Occupation', data.occupation),
            data.sleep_duration,
            data.sleep_quality,
            data.physical_activity,
            encode('BMI Category', data.bmi_category),
            data.heart_rate,
            data.daily_steps,
            encode('Sleep Disorder', data.sleep_disorder),
            sys, dia
        ]
        
        # 3. Scale & Reshape
        scaled = self.scaler.transform([features])
        return scaled.reshape((1, 1, 12))

    def predict(self, data: PatientData):
        if not self.model: return {"error": "Model missing"}
        probs = self.model.predict(self.preprocess(data), verbose=0)[0]
        cls = int(np.argmax(probs))
        return {
            "risk_class": cls,
            "risk_label": self.labels[cls],
            "confidence": float(probs[cls]),
            "probabilities": {
                "low": float(probs[0]), "medium": float(probs[1]), "high": float(probs[2])
            }
        }

    def _encode(self, col_name, value):
        """Helper to safely encode text values."""
        encoder = self.encoders.get(col_name)
        if encoder and value in encoder.classes_:
            return encoder.transform([value])[0]
        return 0

    def predict_trajectory(self, request):
        """
        Standalone Forecasting Logic:
        1. Analyzes 3-day trend.
        2. Simulates 'Tomorrow's' data.
        3. Runs risk prediction on that simulated future.
        """
        if not self.model: return {"error": "Model missing"}
        
        history = request.history # List of 3 days
        
        # 1. EXTRACT TRENDS
        # We focus on Sleep and Heart Rate as primary drivers
        sleep_vals = [d.sleep_duration for d in history] # e.g. [7.0, 6.0, 5.0]
        hr_vals = [d.heart_rate for d in history]        # e.g. [70, 75, 80]
        
        # 2. CALCULATE SLOPE (The Trajectory)
        # Formula: (Last Day - First Day) / 2
        sleep_slope = (sleep_vals[2] - sleep_vals[0]) / 2
        hr_slope = (hr_vals[2] - hr_vals[0]) / 2
        
        # 3. PROJECT TOMORROW (Day 4)
        # Projected = Today + Slope
        proj_sleep = max(3.0, sleep_vals[2] + sleep_slope) # Don't go below 3h
        proj_hr = hr_vals[2] + hr_slope
        
        # 4. BUILD SYNTHETIC 'FUTURE' USER
        # We copy static traits (Age, Job) from the latest day
        last_day = history[2]
        
        # Parse BP for the projected day
        try:
            sys, dia = map(float, last_day.blood_pressure.split('/'))
        except:
            sys, dia = 120.0, 80.0
            
        future_features = [
            self._encode('Gender', last_day.gender),
            last_day.age,
            self._encode('Occupation', last_day.occupation),
            proj_sleep,                 # <--- USE PROJECTED VALUE
            last_day.sleep_quality,     # Assume quality stays similar
            last_day.physical_activity,
            self._encode('BMI Category', last_day.bmi_category),
            proj_hr,                    # <--- USE PROJECTED VALUE
            last_day.daily_steps,
            self._encode('Sleep Disorder', last_day.sleep_disorder),
            sys, dia
        ]
        
        # 5. RUN PREDICTION ON FUTURE DATA
        scaled = self.scaler.transform([future_features])
        input_tensor = scaled.reshape((1, 1, 12))
        
        probs = self.model.predict(input_tensor, verbose=0)[0]
        cls = int(np.argmax(probs))
        
        # 6. GENERATE INSIGHT TEXT
        analysis = "Stable biometrics."
        if sleep_slope < -0.5:
            analysis = f" Sleep is trending down ({sleep_slope}h/day)."
        elif hr_slope > 5:
            analysis = f" Heart Rate is spiking (+{hr_slope} bpm/day)."
            
        return {
            "forecast_target": "Tomorrow (Day T+1)",
            "predicted_risk_class": cls,
            "predicted_risk_label": self.labels[cls],
            "trend_analysis": analysis,
            "confidence": float(probs[cls])
        }

predictor = RiskPredictor()