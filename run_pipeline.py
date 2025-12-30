"""
MANO Project: End-to-End Execution Pipeline
Demonstrates the full "Patient Journey" from Generation to Cured.

STEPS:
1. GENERATE: Create a synthetic patient (Static + Dynamic).
2. DIAGNOSE: Use LSTM to predict Mental Health Risk.
3. PRESCRIBE: Use RL Agent to select optimal intervention.
4. SIMULATE: Use Seq2Seq to predict post-treatment health.
5. VALIDATE: Re-run LSTM to confirm risk reduction.
"""
import torch
import numpy as np
import sys
import os
from pathlib import Path

# --- SETUP PATHS ---
# Add all component paths so we can import their modules
PROJECT_ROOT = Path(__file__).resolve().parent
sys.path.append(
    str(PROJECT_ROOT / 'ml-services/privacy-preserving-gan/config'))
sys.path.append(
    str(PROJECT_ROOT / 'ml-services/privacy-preserving-lstm/config'))
sys.path.append(
    str(PROJECT_ROOT / 'ml-services/intervention-simulation/config'))
sys.path.append(str(PROJECT_ROOT / 'ml-services/privacy-preserving-lstm/src'))
sys.path.append(str(PROJECT_ROOT / 'ml-services/intervention-simulation/src'))

# Import Configs
from intervention_config import config as int_config
from rl_config import config as rl_config
from lstm_config import config as lstm_config

# Import Models
from rl_agent import ActorCritic
from seq2seq_model import InterventionSimulator
from lstm_model import RiskPredictionModel


class ManoPipeline:
    def __init__(self):
        self.device = 'cuda' if torch.cuda.is_available() else 'cpu'
        print(f"\n🚀 Initializing MANO Pipeline on {self.device}...")

        # 1. Load LSTM Predictor (The Doctor)
        print("   Loading Risk Predictor...")
        self.lstm = RiskPredictionModel(lstm_config)
        self.lstm.load_state_dict(torch.load(
            rl_config.paths.LSTM_PATH, map_location=self.device))
        self.lstm.to(self.device).eval()

        # 2. Load Seq2Seq Simulator (The Physics Engine)
        print("   Loading Outcome Simulator...")
        self.simulator = InterventionSimulator(int_config)
        self.simulator.load_state_dict(torch.load(
            rl_config.paths.SIMULATOR_PATH, map_location=self.device))
        self.simulator.to(self.device).eval()

        # 3. Load RL Agent (The Specialist)
        print("   Loading AI Intervention Agent...")
        self.agent = ActorCritic(
            rl_config.env.STATE_DIM,
            rl_config.env.NUM_INTERVENTIONS,
            rl_config.ppo.HIDDEN_DIM,
            self.device
        )
        self.agent.load_state_dict(torch.load(
            rl_config.paths.BEST_AGENT_PATH, map_location=self.device))
        self.agent.to(self.device).eval()

        print("✅ System Ready.\n")

    def get_risk_score(self, dyn, stat):
        """Helper to get probability of High Risk"""
        with torch.no_grad():
            logits = self.lstm(dyn, stat)
            probs = torch.softmax(logits, dim=1)
            # Return tuple: (Risk Class, Probability of High Risk)
            return torch.argmax(probs).item(), probs[0, 2].item()

    def run_demo(self):
        print("="*60)
        print("PATIENT CASE STUDY")
        print("="*60)

        # 1. GENERATE PATIENT (Load random High Risk sample)
        data = np.load(rl_config.paths.PATIENT_DATA)
        # Find a high risk patient index
        high_risk_indices = np.where(data['y'] == 2)[0]
        idx = np.random.choice(high_risk_indices)

        patient_dyn = torch.FloatTensor(
            data['X_dynamic'][idx]).unsqueeze(0).to(self.device)
        patient_stat = torch.FloatTensor(
            data['X_static'][idx]).unsqueeze(0).to(self.device)

        # Verify Initial Risk
        risk_class, risk_prob = self.get_risk_score(patient_dyn, patient_stat)
        print(f"\n👤 PATIENT ID: {idx}")
        print(
            f"   Initial Diagnosis: {'HIGH RISK' if risk_class==2 else 'MEDIUM'}")
        print(f"   High Risk Probability: {risk_prob:.1%}")

        # 2. PRESCRIBE INTERVENTION
        # Flatten state for Agent
        state_flat = torch.cat([patient_dyn.flatten(), patient_stat.flatten()])

        with torch.no_grad():
            (action_cat, action_cont), _, _ = self.agent.act(state_flat)

        intervention_name = int_config.interventions.INTERVENTION_TYPES[action_cat]
        print(f"\n💊 AI PRESCRIPTION:")
        print(f"   Treatment: {intervention_name}")
        print(f"   Intensity: {action_cont:.1%}")

        # 3. SIMULATE OUTCOME
        # Create condition vector
        one_hot = torch.zeros(1, 5).to(self.device)
        one_hot[0, action_cat] = 1.0
        intensity = torch.tensor([[action_cont]]).to(self.device)
        condition = torch.cat([one_hot, intensity], dim=1)

        with torch.no_grad():
            future_dyn = self.simulator(
                patient_dyn, condition, teacher_forcing_ratio=0.0)

        # 4. RE-EVALUATE RISK
        new_risk_class, new_risk_prob = self.get_risk_score(
            future_dyn, patient_stat)

        print(f"\n🔮 PROGNOSIS (After 7 Days):")
        print(
            f"   New Risk Level: {['Low', 'Medium', 'High'][new_risk_class]}")
        print(f"   New Risk Probability: {new_risk_prob:.1%}")

        # 5. IMPACT ANALYSIS
        drop = risk_prob - new_risk_prob
        print(f"\n📊 CLINICAL IMPACT:")
        print(f"   Risk Reduction: {drop*100:+.1f} points")
        if new_risk_class < risk_class:
            print("   ✅ SUCCESS: Patient condition improved.")
        else:
            print(
                "   ⚠️ WARNING: Intervention insufficient. Consider alternative treatment.")


if __name__ == "__main__":
    pipeline = ManoPipeline()
    pipeline.run_demo()
