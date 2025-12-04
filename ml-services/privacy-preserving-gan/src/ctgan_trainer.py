"""
This script implements the training and synthetic data generation using CTGAN.
It handles data preprocessing specific to CTGAN, trains the model,
and saves the generated synthetic data.
"""
import pandas as pd
import numpy as np
import os
import sys
from ctgan import CTGAN
import pickle

# --- Configuration --- #
# Add config path to system path
current_dir = os.path.dirname(os.path.abspath(__file__))
# Points to 'ml-services/privacy-preserving-gan/config'
config_path = os.path.join(current_dir, '..', 'config')
sys.path.append(config_path)

try:
    import config
except ImportError as e:
    print(f"ERROR: Could not import 'config.py'. Ensure it is in {config_path}")
    sys.exit(1)


# Get dataset configuration
DATASET_KEY = "MENTAL_HEALTH_TECH"
dataset_info = config.DATASETS[DATASET_KEY]

# Paths from config
REAL_DATA_PATH = dataset_info['processed_path']
SYNTHETIC_DATA_PATH = dataset_info['synthetic_path']
CTGAN_MODEL_PATH = config.MODELS_DIR / f"ctgan_model_{DATASET_KEY}.pkl"

# CTGAN Training Parameters from config
trainer_config = config.CTGAN_TRAINER_CONFIG
EPOCHS = trainer_config['epochs']
BATCH_SIZE = trainer_config['batch_size']
NUM_SYNTHETIC_SAMPLES = trainer_config['synthetic_samples']


# --- Main Logic --- #
if __name__ == "__main__":
    print("\n--- Starting CTGAN Training and Data Generation ---")
    print(f"Dataset: {DATASET_KEY}")

    # 1. Load Real Data
    print(f"Loading real data from: {REAL_DATA_PATH}")
    if not REAL_DATA_PATH.exists():
        print(f"ERROR: Real data file not found at '{REAL_DATA_PATH}'")
        print("Please run the data unification/preprocessing script first.")
        sys.exit(1)
    
    real_data = pd.read_csv(REAL_DATA_PATH)
    print(f"OK: Loaded real data. Shape: {real_data.shape}")

    # 2. Identify Categorical Features from Config
    categorical_features = dataset_info.get("categorical_features", [])
    
    # Ensure all specified categorical features are actually in the dataframe
    missing_cols = [col for col in categorical_features if col not in real_data.columns]
    if missing_cols:
        print(f"ERROR: The following categorical columns from config are missing in the data: {missing_cols}")
        sys.exit(1)
        
    print(f"Identified {len(categorical_features)} categorical features.")
    
    # Preprocessing: Handle missing values.
    # For simplicity, we fill numerical with median and categorical with mode.
    for col in real_data.columns:
        if col in categorical_features:
            real_data[col] = real_data[col].fillna(real_data[col].mode()[0])
        else: # It's a numerical column
            real_data[col] = real_data[col].fillna(real_data[col].median())
            
    print("OK: Handled missing values.")

    # 3. Initialize and Train CTGAN Synthesizer
    synthesizer = CTGAN(
        epochs=EPOCHS, 
        batch_size=BATCH_SIZE, 
        verbose=True
    )
    
    print(f"\nTraining CTGAN for {EPOCHS} epochs with batch size {BATCH_SIZE}...")
    
    # The fit method trains the GAN on the real data, specifying which columns are categorical
    synthesizer.fit(real_data, categorical_features)
    print("OK: CTGAN training complete.")

    # 4. Save the trained CTGAN model
    print(f"Saving trained CTGAN model to: {CTGAN_MODEL_PATH}")
    with open(CTGAN_MODEL_PATH, "wb") as f:
        pickle.dump(synthesizer, f)
    print("OK: Model saved.")
    
    # 5. Generate Synthetic Data
    print(f"\nGenerating {NUM_SYNTHETIC_SAMPLES} synthetic samples...")
    synthetic_data = synthesizer.sample(NUM_SYNTHETIC_SAMPLES)
    print("OK: Synthetic data generated.")
    
    # 6. Save the Synthetic Data to CSV
    # Ensure the directory exists
    SYNTHETIC_DATA_PATH.parent.mkdir(parents=True, exist_ok=True)
    synthetic_data.to_csv(SYNTHETIC_DATA_PATH, index=False)
    print(f"OK: Synthetic data saved to: {SYNTHETIC_DATA_PATH}")
    
    print("\n--- CTGAN Process Finished ---")

    # Optional: Print a few samples to verify
    print("\nFirst 5 generated synthetic samples:")
    print(synthetic_data.head())