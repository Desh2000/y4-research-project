"""
This script loads a trained Generator model and generates a large number of synthetic samples.
These samples will be used for statistical validation and ML utility assessment.
""" 

import tensorflow as tf
import numpy as np
import pandas as pd
import os
# gan_model.py is no longer needed here because load_model restores the architecture
# from gan_model import Generator 

# --- Configuration --- #
current_script_dir = os.path.dirname(os.path.abspath(__file__))
project_root = os.path.abspath(os.path.join(
    current_script_dir, "..", "..", ".."))
LOG_DIR = os.path.join(project_root, "gan_logs")

CHECKPOINTS_DIR = os.path.join(LOG_DIR, "checkpoints")
OUTPUT_SAMPLES_PATH = os.path.join(
    LOG_DIR, "synthetic_data_for_validation.csv")

# These parameters are for generating new data, they don't need to be exact
# as the loaded model already knows its own dimensions.
LATENT_DIM = 100
OUTPUT_DIM = 170 
NUM_SYNTHETIC_SAMPLES = 10000

# --- Main Logic --- #
if __name__ == "__main__":
    print("\n--- Generating Synthetic Data for Validation ---")

    # 1. Find and load the latest trained generator model
    try:
        # List all files in the checkpoints directory that are generator models
        checkpoint_files = [f for f in os.listdir(CHECKPOINTS_DIR) if f.startswith('generator_epoch_') and f.endswith('.keras')]
        if not checkpoint_files:
            raise FileNotFoundError("No generator checkpoint files (.keras) found.")
        
        # Sort the files alphabetically/numerically to find the latest one
        latest_generator_file = sorted(checkpoint_files)[-1]
        latest_generator_path = os.path.join(CHECKPOINTS_DIR, latest_generator_file)
        
        print(f"Loading trained generator from: {latest_generator_path}")
        
        # Use tf.keras.models.load_model to restore the entire model, including architecture and weights
        generator = tf.keras.models.load_model(latest_generator_path)
        
        print("Generator model loaded successfully.")
        generator.summary()

    except Exception as e:
        print(f"Error: Could not load the generator model. {e}")
        print(f"Please ensure the GAN has been trained and checkpoints are saved in '{CHECKPOINTS_DIR}'.")
        exit()

    # 2. Generate a large number of synthetic samples
    print(f"\nGenerating {NUM_SYNTHETIC_SAMPLES} synthetic samples...")
    noise = tf.random.normal([NUM_SYNTHETIC_SAMPLES, LATENT_DIM])
    synthetic_data_tensor = generator(noise, training=False)

    # 3. Convert to DataFrame and save
    synthetic_data_np = synthetic_data_tensor.numpy()
    synthetic_df = pd.DataFrame(synthetic_data_np)
    
    # Optional: Assign column names if you have them for better readability
    # synthetic_df.columns = [f'feature_{i}' for i in range(synthetic_data_np.shape[1])]

    synthetic_df.to_csv(OUTPUT_SAMPLES_PATH, index=False)
    print(f"\nSynthetic data saved to: {OUTPUT_SAMPLES_PATH}")
    print("Synthetic data generation complete.")

    # 4. Optional: Print a few samples to verify
    print("\nFirst 5 generated synthetic samples (normalized to [-1, 1]):")
    print(synthetic_df.head())


