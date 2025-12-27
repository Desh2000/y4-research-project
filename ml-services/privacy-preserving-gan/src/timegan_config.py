"""
MANO Component 1: TimeGAN Configuration
Phase 2: Temporal Sequence Generation

This file acts as the 'Control Center' for the neural network.
"""
import os
from pathlib import Path
import torch

class DataConfig:
    """Configuration for input/output data paths"""
    REAL_DATA_FILE = 'data/processed/wearable_sequences.npz'
    SYNTHETIC_DATA_FILE = 'data/synthetic/synthetic_wearable_sequences.npz'
    
    # Model artifacts (Where we save the "Brain" of the AI)
    MODEL_DIR = 'ml-services/privacy-preserving-gan/models/timegan'
    CHECKPOINT_DIR = f'{MODEL_DIR}/checkpoints'
    FINAL_MODEL_PATH = f'{MODEL_DIR}/timegan_final.pth'
    
    # Create directories immediately to prevent "FileNotFound" errors later
    Path(CHECKPOINT_DIR).mkdir(parents=True, exist_ok=True)
    Path('data/synthetic').mkdir(parents=True, exist_ok=True)

class ModelConfig:
    """
    Architectural Hyperparameters
    These define the 'shape' and 'size' of the neural network.
    """
    SEQ_LEN = 7                    # The model looks at 7 days of history.
    N_SIGNALS = 4                  # Inputs: Sleep, Quality, HR, Stress.
    
    # NOISE_DIM: The size of the random vector Z. 
    # Why 24? It needs to be large enough to capture variations, but not so large 
    # that the model overfits. Usually matches Hidden Dim.
    NOISE_DIM = 24                 
    
    # HIDDEN_DIM: The memory capacity of the GRU.
    # Higher = Smarter but slower and risks overfitting.
    HIDDEN_DIM = 24                
    
    # NUM_LAYERS: How many stacked GRUs.
    # 3 layers allows the model to learn: 
    # 1. Simple features -> 2. Combinations -> 3. Abstract temporal patterns.
    NUM_LAYERS = 3                 

class TrainingConfig:
    """
    Training Hyperparameters
    Controls the 3-Phase learning process.
    """
    # Auto-detect GPU. CUDA is much faster than CPU for RNNs.
    DEVICE = 'cuda' if torch.cuda.is_available() else 'cpu'
    
    BATCH_SIZE = 128
    LEARNING_RATE = 1e-3
    DISCRIMINATOR_LR = 1e-3
    
    # --- THE 3 PHASES ---
    # TimeGAN is hard to train. We split it into 3 easier homework assignments:
    
    # Phase 1: Embedder (Autoencoder)
    # Goal: Learn to compress data and uncompress it perfectly.
    EMBEDDER_EPOCHS = 600  
    
    # Phase 2: Supervisor
    # Goal: Learn that "Monday predicts Tuesday". 
    # We teach this BEFORE the GAN game starts so the generator isn't confused.
    SUPERVISOR_EPOCHS = 600 
    
    # Phase 3: Joint (The GAN)
    # Goal: The Generator fights the Discriminator to make realistic data.
    JOINT_EPOCHS = 600
    
    CHECKPOINT_FREQ = 100 # Save progress every 100 epochs
    
class GenerationConfig:
    NUM_SAMPLES = 10000            # We want 10k synthetic users.
    BATCH_SIZE = 100               

class EvaluationConfig:
    REPORT_FILE = 'ml-services/privacy-preserving-gan/gan_logs/reports/timegan_evaluation.json'
    PLOTS_DIR = 'ml-services/privacy-preserving-gan/gan_logs/plots/timegan'
    Path(PLOTS_DIR).mkdir(parents=True, exist_ok=True)

class SignalConfig:
    # Metadata for graphs
    SIGNAL_NAMES = ['Sleep Duration', 'Quality of Sleep', 'Heart Rate', 'Stress Level']

class TimeGANConfig:
    """Master wrapper"""
    def __init__(self):
        self.data = DataConfig()
        self.model = ModelConfig()
        self.training = TrainingConfig()
        self.generation = GenerationConfig()
        self.evaluation = EvaluationConfig()
        self.signals = SignalConfig()

config = TimeGANConfig()