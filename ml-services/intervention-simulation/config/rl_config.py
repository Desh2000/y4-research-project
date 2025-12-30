"""
MANO Component 3: Reinforcement Learning Configuration
Hyperparameters for the PPO Agent and Medical Environment.
"""
import torch
import os
from pathlib import Path

# Resolve paths relative to project root
# Structure: project/ml-services/intervention-simulation/config/rl_config.py
PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent.parent


class PathConfig:
    """Model and Data Paths"""
    # The models we consult (The "World")
    LSTM_PATH = PROJECT_ROOT / \
        'ml-services/privacy-preserving-lstm/models/risk_lstm_best.pth'
    SIMULATOR_PATH = PROJECT_ROOT / \
        'ml-services/intervention-simulation/models/seq2seq_simulator.pth'

    # Patient Data (To sample episodes)
    PATIENT_DATA = PROJECT_ROOT / 'data/synthetic/synthetic_labeled_dataset.npz'

    # Output Directory for Agent
    AGENT_DIR = PROJECT_ROOT / 'ml-services/intervention-simulation/models/rl_agent'
    BEST_AGENT_PATH = AGENT_DIR / 'ppo_agent.pth'

    # Ensure dir exists
    AGENT_DIR.mkdir(parents=True, exist_ok=True)


class EnvConfig:
    """Environment Settings"""
    # State Space:
    # Dynamic (7 days * 4 signals = 28) + Static (20 demographics) = 48
    STATE_DIM = 28 + 20

    # Action Space:
    # 1. Intervention Type (Discrete 0-4) -> 5 logits
    # 2. Intensity (Continuous 0.1-1.0) -> 1 value
    NUM_INTERVENTIONS = 5

    # Limits
    MAX_STEPS = 5  # Max attempts to cure a patient per episode

    # Reward Function Weights
    REWARD_RISK_REDUCTION = 10.0  # +10 for full cure
    PENALTY_INTENSITY = -0.1      # Small cost for high dosage
    PENALTY_FAILURE = -1.0        # Cost for not curing


class PPOConfig:
    """PPO Hyperparameters (Industry Standard)"""
    DEVICE = 'cuda' if torch.cuda.is_available() else 'cpu'

    # Model Architecture
    HIDDEN_DIM = 128      # Neurons in Actor/Critic layers

    # Learning
    LR_ACTOR = 3e-4       # 0.0003
    LR_CRITIC = 1e-3      # 0.001
    GAMMA = 0.99          # Discount factor
    K_EPOCHS = 4          # Updates per batch
    EPS_CLIP = 0.2        # PPO Clipping (Stability)
    ENTROPY_COEFF = 0.01  # Encourage exploration

    # Training Loop
    MAX_EPISODES = 5000   # Total episodes to train
    UPDATE_TIMESTEP = 2000  # Update policy every N timesteps
    PRINT_INTERVAL = 100   # Log progress


class Config:
    def __init__(self):
        self.paths = PathConfig()
        self.env = EnvConfig()
        self.ppo = PPOConfig()


config = Config()
