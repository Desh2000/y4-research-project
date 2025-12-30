"""
MANO Component 3: Medical RL Environment (OpenAI Gym style)
Simulates patient response to interventions.

FLOW:
1. Agent chooses Action (Treatment + Intensity)
2. Simulator predicts Future Health (Seq2Seq)
3. Risk Predictor calculates Risk Score (LSTM)
4. Environment returns Reward (Risk Reduction)
"""
import torch
import numpy as np
import sys
import os
from pathlib import Path

# --- SETUP PATHS ---
# This ensures we can import modules from sibling directories
current_file = Path(__file__).resolve()
src_dir = current_file.parent
config_dir = src_dir.parent / "config"
lstm_config_dir = src_dir.parent.parent / "privacy-preserving-lstm" / "config"

# Add paths to Python's search list
sys.path.insert(0, str(src_dir))
sys.path.insert(0, str(config_dir))
sys.path.insert(0, str(lstm_config_dir))

try:
    from rl_config import config as rl_config
    from intervention_config import config as int_config
    from lstm_config import config as lstm_config
    from seq2seq_model import InterventionSimulator
    from lstm_model import RiskPredictionModel
except ImportError as e:
    print(f"❌ Import Error: {e}")
    sys.exit(1)


class MedicalEnvironment:
    def __init__(self):
        self.device = rl_config.ppo.DEVICE
        print(f"🏥 Initializing Medical Environment on {self.device}...")

        # 1. Load Models (The Physics Engine & Referee)
        self.simulator = self._load_simulator()
        self.risk_predictor = self._load_risk_predictor()

        # 2. Load Population (High Risk Patients Only)
        self.patients_dyn, self.patients_stat = self._load_high_risk_patients()

        # State variables
        self.current_step = 0
        self.current_idx = 0
        self.state_dyn = None
        self.state_stat = None
        self.initial_risk = 0.0

    def _load_simulator(self):
        print("   Loading Seq2Seq Simulator...")
        model = InterventionSimulator(int_config)
        model.load_state_dict(torch.load(
            rl_config.paths.SIMULATOR_PATH, map_location=self.device))
        model.to(self.device)
        model.eval()
        return model

    def _load_risk_predictor(self):
        print("   Loading LSTM Risk Predictor...")
        model = RiskPredictionModel(lstm_config)
        model.load_state_dict(torch.load(
            rl_config.paths.LSTM_PATH, map_location=self.device))
        model.to(self.device)
        model.eval()
        return model

    def _load_high_risk_patients(self):
        print("   Loading Patient Population...")
        data = np.load(rl_config.paths.PATIENT_DATA)
        X_dyn = torch.FloatTensor(data['X_dynamic']).to(self.device)
        X_stat = torch.FloatTensor(data['X_static']).to(self.device)
        y = data['y']

        # Filter: Only train on Medium (1) or High (2) risk patients
        # We don't need to 'cure' people who are already Label 0 (Low Risk)
        mask = y > 0
        print(
            f"   Selected {mask.sum()} High/Medium risk patients for training.")
        return X_dyn[mask], X_stat[mask]

    def _calculate_risk(self, dyn, stat):
        """Helper to get risk score (0.0 - 1.0)"""
        with torch.no_grad():
            # LSTM expects batch dim
            logits = self.risk_predictor(dyn, stat)
            probs = torch.softmax(logits, dim=1)

            # Weighted Risk Score: 0*Low + 0.5*Med + 1.0*High
            # Result is strictly 0.0 to 1.0
            # If prob of High Risk is 0.8, score is 0.8.
            risk_score = (probs[0, 1] * 0.5) + (probs[0, 2] * 1.0)
        return risk_score.item()

    def reset(self):
        """Start new episode with random patient"""
        self.current_step = 0
        self.current_idx = np.random.randint(0, len(self.patients_dyn))

        # Select patient and add batch dimension [1, ...]
        self.state_dyn = self.patients_dyn[self.current_idx].unsqueeze(0)
        self.state_stat = self.patients_stat[self.current_idx].unsqueeze(0)

        self.initial_risk = self._calculate_risk(
            self.state_dyn, self.state_stat)

        return self._get_observation()

    def step(self, action_type, action_intensity):
        """
        Execute Action:
        1. Construct Intervention Vector
        2. Simulate Future (Seq2Seq)
        3. Calculate Reward (Risk Delta)
        """
        self.current_step += 1

        # 1. Prepare Inputs
        one_hot = torch.zeros(1, 5).to(self.device)
        one_hot[0, int(action_type)] = 1.0

        intensity = torch.tensor([[action_intensity]]).to(self.device)

        # Condition Vector [1, 6]
        condition = torch.cat([one_hot, intensity], dim=1)

        # 2. Simulate Outcome
        with torch.no_grad():
            # Predict the FUTURE 7 days based on Current + Intervention
            # Teacher Forcing = 0.0 (We don't know the future, we predict it)
            next_dyn = self.simulator(
                self.state_dyn,
                condition,
                target=None,
                teacher_forcing_ratio=0.0
            )

        # 3. Calculate Reward
        current_risk = self._calculate_risk(next_dyn, self.state_stat)

        # Reward = Improvement - Cost
        risk_drop = self.initial_risk - current_risk

        # We penalize high intensity slightly to encourage "Minimum Effective Dose"
        cost = action_intensity * abs(rl_config.env.PENALTY_INTENSITY)

        reward = (risk_drop * rl_config.env.REWARD_RISK_REDUCTION) - cost

        # 4. Check Termination
        done = False
        if current_risk < 0.2:  # Cured (Low Risk)
            reward += 5.0  # Bonus for cure
            done = True
        elif self.current_step >= rl_config.env.MAX_STEPS:
            done = True  # Failed to cure in time

        # Update State (Patient moves forward in time to the new simulated reality)
        self.state_dyn = next_dyn

        return self._get_observation(), reward, done, {'risk': current_risk}

    def _get_observation(self):
        """Flatten state for PPO (Dynamic + Static)"""
        dyn_flat = self.state_dyn.flatten()
        stat_flat = self.state_stat.flatten()
        return torch.cat([dyn_flat, stat_flat])  # Size: 48
