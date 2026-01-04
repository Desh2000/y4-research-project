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

predictor = RiskPredictor()