"""
MANO Component 2: LSTM Configuration
Defines hyperparameters for the Risk Prediction Model.
"""
import os
import torch
from pathlib import Path

class DataConfig:
    # Input: The fused dataset created in Phase 4
    DATASET_PATH = 'data/synthetic/synthetic_labeled_dataset.npz'
    REAL_DATA_PATH = 'data/processed/wearable_sequences.npz'
    
    # Model Artifacts
    MODEL_DIR = 'ml-services/privacy-preserving-lstm/models'
    CHECKPOINT_DIR = 'ml-services/privacy-preserving-lstm/checkpoints'
    BEST_MODEL_PATH = f'{MODEL_DIR}/risk_lstm_best.pth'
    
    # Split Ratios
    TRAIN_RATIO = 0.70
    VAL_RATIO = 0.15
    TEST_RATIO = 0.15
    
    # DataLoader Settings
    BATCH_SIZE = 64
    SHUFFLE = True
    NUM_WORKERS = 0 # Set to 0 on Windows to avoid multiprocessing errors
    PIN_MEMORY = True
    
    # Ensure dirs exist
    Path(MODEL_DIR).mkdir(parents=True, exist_ok=True)
    Path(CHECKPOINT_DIR).mkdir(parents=True, exist_ok=True)

class ModelConfig:
    """
    Hybrid Architecture Settings
    Dual-Branch Network: Time-Series Branch + Static Demographics Branch
    """
    # --- Dynamic Branch (Time Series) ---
    SEQ_LEN = 7             # 7 Days
    DYNAMIC_INPUT_DIM = 4   # [Sleep, Quality, HR, Stress]
    LSTM_HIDDEN_DIM = 64    # Size of LSTM memory
    LSTM_LAYERS = 2         # Stacked LSTMs for complex pattern recognition
    DROPOUT = 0.3           # Prevent overfitting
    
    # --- Static Branch (Demographics) ---
    STATIC_INPUT_DIM = 20   # Default (will be updated dynamically based on data)
    STATIC_HIDDEN_DIM = 32  # Dense embedding for demographics
    
    # --- Fusion & Output ---
    # Fusion = LSTM Output (64) + Static Output (32) = 96
    FUSION_HIDDEN_DIM = 64  # Intermediate dense layer after concatenation
    NUM_CLASSES = 3         # 0=Low, 1=Medium, 2=High Risk

class TrainingConfig:
    DEVICE = 'cuda' if torch.cuda.is_available() else 'cpu'
    
    LEARNING_RATE = 0.001
    WEIGHT_DECAY = 1e-5     # L2 Regularization
    EPOCHS = 50
    
    # Early Stopping
    PATIENCE = 10           # Stop if validation loss doesn't improve for 10 epochs
    
    # Loss Function
    # We use CrossEntropyLoss for Multi-class classification (0, 1, 2)
    # Class weights will be calculated dynamically to handle imbalance
    USE_CLASS_WEIGHTS = True

class EvalConfig:
    """Evaluation parameters"""
    BATCH_SIZE = 64
    CONFIDENCE_THRESHOLD = 0.5
    
    # Metrics to track
    METRICS = ['accuracy', 'precision', 'recall', 'f1', 'confusion_matrix']

class LSTMConfig:
    def __init__(self):
        self.data = DataConfig()
        self.model = ModelConfig()
        self.training = TrainingConfig()
        self.eval = EvalConfig()

config = LSTMConfig()