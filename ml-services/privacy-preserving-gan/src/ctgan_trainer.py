"""
This script implements the training and synthetic data generation using CTGAN.
It handles data preprocessing specific to CTGAN, trains the model,
and saves the generated synthetic data.
"""
import pandas as pd
import numpy as np
import os
# Import the specific CTGAN class from the library
# *** FIX APPLIED HERE ***
# Based on the latest version of the `ctgan` library, the main class
# has been renamed from `CTGANSynthesizer` to `CTGAN` and is now
# available at the top level of the library.
from ctgan import CTGAN
from data_loader import MentalHealthDataLoader # To load the real DASS data

# --- Configuration --- #
# Get the current script's directory
current_script_dir = os.path.dirname(os.path.abspath(__file__))
# Navigate up to the project root (assuming script is in src/)
project_root = os.path.abspath(os.path.join(current_script_dir, "..", "..", ".."))

DATA_PATH = os.path.join(project_root, "data") # Path to your raw data
LOG_DIR = os.path.join(project_root, "gan_logs") # Path where synthetic data will be saved
CTGAN_MODEL_PATH = os.path.join(LOG_DIR, "ctgan_model.pkl") # Path to save the trained CTGAN model
SYNTHETIC_DATA_PATH = os.path.join(LOG_DIR, "ctgan_synthetic_data_for_validation.csv") # Path for CTGAN generated data

# CTGAN Training Parameters
EPOCHS = 300 # WHY: Number of training iterations. CTGAN often requires more epochs than basic GANs. We start with 300, but this might need tuning based on validation results.
BATCH_SIZE = 500 # WHY: Number of samples processed before updating model weights. Affects training stability and speed.
NUM_SYNTHETIC_SAMPLES = 10000 # WHY: Number of synthetic samples to generate for validation. This should be consistent with the previous GAN for fair comparison.

# --- Main Logic --- #
if __name__ == "__main__":
    print("\n--- Starting CTGAN Training and Data Generation ---")
    
    # 1. Load Real DASS Data
    # WHY: CTGAN needs real data to learn the underlying distributions and relationships.
    data_loader = MentalHealthDataLoader(DATA_PATH)
    real_dass_data = data_loader.load_dass_dataset()
    if real_dass_data is None:
        print("Error: Could not load real DASS dataset. Exiting.")
        exit()

    # 2. Preprocess Data for CTGAN
    # WHY: CTGAN requires specific preprocessing, especially for categorical features.
    # For the DASS dataset, all features are numerical, but some might be treated
    # as discrete categories (e.g., survey responses on a Likert scale).
    # For now, we'll treat all as numerical and let CTGAN handle the binning.
    # We'll also handle missing values as CTGAN expects a clean dataset.
    
    # Select only numerical columns, as the DASS dataset has some non-numerical ones.
    numerical_cols = real_dass_data.select_dtypes(include=np.number).columns
    processed_data = real_dass_data[numerical_cols].copy()

    # WHY: Fill missing values. CTGAN expects no NaNs. Filling with mean is a simple strategy.
    # More sophisticated imputation can be explored later if needed.
    processed_data = processed_data.fillna(processed_data.mean())
    
    # WHY: CTGAN does its own internal normalization/transformation. We don't need
    # to normalize to [-1, 1] here, as the library handles it.
    
    # Define categorical features if any. For DASS, all are numerical, but some
    # are effectively categorical (e.g., Q1A, Q1I, Q1E are typically 0-3 scales).
    # For simplicity in this first CTGAN implementation, we'll treat all as numerical.
    # If we had true string/object categorical columns, we'd list them here.
    categorical_features = [] # Example: ["Gender", "Education_Level"]
    print(f"Loaded and preprocessed real DASS data for CTGAN. Shape: {processed_data.shape}")

    # 3. Initialize and Train CTGAN Synthesizer
    # WHY: CTGAN is the core class for training the CTGAN model.
    # It learns the data distribution and relationships.
    # *** FIX APPLIED HERE ***
    # The class name is now `CTGAN`, not `CTGANSynthesizer`.
    # Added `verbose=True` to print the training loss at each epoch, which is very helpful for monitoring.
    synthesizer = CTGAN(epochs=EPOCHS, batch_size=BATCH_SIZE, verbose=True)
    print(f"\nTraining CTGAN for {EPOCHS} epochs with batch size {BATCH_SIZE}...")
    
    # The fit method trains the GAN on the real data.
    synthesizer.fit(processed_data, categorical_features)
    print("CTGAN training complete.")

    # 4. Save the trained CTGAN model
    # WHY: Saving the model allows us to reuse it later without retraining.
    # CTGAN models are typically saved using pickle.
    import pickle
    with open(CTGAN_MODEL_PATH, "wb") as f:
        pickle.dump(synthesizer, f)
    print(f"Trained CTGAN model saved to: {CTGAN_MODEL_PATH}")
    
    # 5. Generate Synthetic Data
    # WHY: Once trained, the synthesizer can generate new synthetic samples
    # that mimic the statistical properties of the real data.
    print(f"\nGenerating {NUM_SYNTHETIC_SAMPLES} synthetic samples using CTGAN...")
    synthetic_data = synthesizer.sample(NUM_SYNTHETIC_SAMPLES)
    
    # 6. Save the Synthetic Data to CSV
    # WHY: Save the generated data for subsequent validation steps.
    synthetic_data.to_csv(SYNTHETIC_DATA_PATH, index=False)
    print(f"Synthetic data saved to: {SYNTHETIC_DATA_PATH}")
    print("\nCTGAN data generation complete.")

    # Optional: Print a few samples to verify
    print("\nFirst 5 generated synthetic samples:")
    print(synthetic_data.head())