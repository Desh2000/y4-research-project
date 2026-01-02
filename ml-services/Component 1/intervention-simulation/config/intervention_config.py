"""
MANO Component 3: Intervention Simulation Configuration
Defines the "Physics" of the simulation world: Effects, Constraints, and Model Hyperparameters.

ARCHITECTURE:
- Class-based config for type safety and IDE autocompletion.
- Defines Probabilistic Effects (Mean + Variance) for realistic simulation.
- Defines Constraints for the Reinforcement Learning Action Space.
"""
import os
import torch
from pathlib import Path


class DataConfig:
    # Input: The 10k synthetic users we generated in Phase 3
    # We use the NPZ because we need the raw time-series data
    INPUT_DATA_PATH = 'data/synthetic/synthetic_labeled_dataset.npz'

    # Output: Where we save the "Counterfactual" training pairs
    # (Patient_State_t, Intervention) -> (Patient_State_t+1)
    SIMULATION_DATA_PATH = 'data/synthetic/intervention_training_data.npz'

    # Model Artifacts
    MODEL_DIR = 'ml-services/intervention-simulation/models'
    SIMULATOR_PATH = f'{MODEL_DIR}/seq2seq_simulator.pth'

    # Ensure directory exists
    Path(MODEL_DIR).mkdir(parents=True, exist_ok=True)


class InterventionConfig:
    """
    Defines the Action Space for the AI Agent.
    """
    # The Signals we are modifying (Must match TimeGAN output order)
    # 0: Sleep Duration, 1: Quality, 2: Heart Rate, 3: Stress Level
    SIGNAL_MAP = ['Sleep Duration', 'Quality of Sleep',
                  'Heart Rate', 'Stress Level']

    # Action Space: 5 Discrete Interventions
    INTERVENTION_TYPES = ['Control', 'Wellness_App',
                          'CBT', 'Exercise', 'Medication']
    NUM_INTERVENTIONS = len(INTERVENTION_TYPES)

    # Intensity Constraints (For the Bandit/RL Agent)
    MIN_INTENSITY = 0.1  # Low dose
    MAX_INTENSITY = 1.0  # High dose

    # --- EFFECT MATRIX (Ground Truth for Simulation Training) ---
    # Values represent the Mean % change to the normalized [0,1] signal per unit of intensity.
    # We also add VARIANCE to simulate that patients respond differently.

    BASE_EFFECTS = {
        'Control':       [0.00,  0.00,  0.00,  0.00],  # Do nothing

        'Wellness_App':  [+0.05, +0.05, -0.02, -0.05],  # Mild improvement
        # (Sleep+, Quality+, HR-, Stress-)

        # Targeted Stress/Quality boost
        'CBT':           [+0.05, +0.15, -0.05, -0.20],

        # Physical impact (HR drops)
        'Exercise':      [+0.10, +0.10, -0.15, -0.10],

        # Drastic Stress drop, slight HR increase side-effect
        'Medication':    [0.00, +0.05, +0.05, -0.30]
    }

    # How much random noise to add to the effect? (Realism factor)
    EFFECT_VARIANCE = 0.02


class EnvironmentConfig:
    """
    Constraints for the Reinforcement Learning Environment
    """
    # Safety Constraints (Normalized 0-1 values)
    # The AI gets a negative reward if it pushes patients beyond these limits
    SAFETY_LIMITS = {
        'min_sleep': 0.1,  # Don't simulate < 2 hours sleep
        'max_hr': 0.95,    # Don't simulate dangerous Tachycardia
        'max_stress': 1.0  # Cap stress at max
    }

    # Reward Weights for RL
    REWARD_WEIGHTS = {
        'risk_reduction': 10.0,  # Big reward for lowering LSTM Risk Score
        # Small penalty for high intensity (efficiency)
        'effort_penalty': -0.1,
        'side_effect': -5.0      # Penalty for violating safety limits
    }


class ModelConfig:
    """Seq2Seq Simulator Architecture"""
    # Input Dimensions
    SEQ_LEN = 7             # History: 7 Days
    PRED_LEN = 7            # Forecast: 7 Days
    INPUT_DIM = 4           # 4 Signals

    # Conditional Input (Intervention is concatenated to the input)
    # Input Size = 4 (Features) + 5 (One-Hot Intervention Vector) + 1 (Intensity) = 10
    ENC_INPUT_DIM = 4
    DEC_INPUT_DIM = 4 + 5 + 1

    # Network Size
    HIDDEN_DIM = 64
    LAYERS = 2
    
    # Encoder/Decoder specific configs
    ENC_HIDDEN_DIM = HIDDEN_DIM
    DEC_HIDDEN_DIM = HIDDEN_DIM
    ENC_LAYERS = LAYERS
    DEC_LAYERS = LAYERS

    DROPOUT = 0.2


class TrainingConfig:
    DEVICE = 'cuda' if torch.cuda.is_available() else 'cpu'
    BATCH_SIZE = 64
    LEARNING_RATE = 0.001
    EPOCHS = 100
    PATIENCE = 15  # Early stopping


class Config:
    """Master Config Object"""

    def __init__(self):
        self.data = DataConfig()
        self.interventions = InterventionConfig()
        self.env = EnvironmentConfig()
        self.model = ModelConfig()
        self.training = TrainingConfig()


config = Config()
