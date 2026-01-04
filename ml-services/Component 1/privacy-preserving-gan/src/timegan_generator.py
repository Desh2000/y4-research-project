"""
MANO Component 1: Synthetic Data Generator

This script acts as the "Inference Engine". 
It loads the saved weights and produces new, never-before-seen patient profiles.
"""
import torch
import numpy as np
from pathlib import Path


class SyntheticDataGenerator:
    def __init__(self, model, config):
        self.model = model
        self.config = config
        self.device = config.training.DEVICE

        # Load the trained weights if they exist
        if Path(config.data.FINAL_MODEL_PATH).exists():
            print(f"Loading weights from {config.data.FINAL_MODEL_PATH}")
            self.model.load_state_dict(torch.load(
                config.data.FINAL_MODEL_PATH, map_location=self.device))
        else:
            print(
                "⚠️ WARNING: No trained model found. Using random weights (for testing only).")

        # Switch model to Evaluation Mode (disables Dropout/Batch Norm updates)
        self.model.eval()

    def generate(self, num_samples=None):
        if num_samples is None:
            num_samples = self.config.generation.NUM_SAMPLES
        print(f"\nGenerating {num_samples} synthetic sequences...")

        batch_size = self.config.generation.BATCH_SIZE
        generated_data = []

        # Turn off gradient calculation to save memory/speed
        with torch.no_grad():
            for i in range(0, num_samples, batch_size):
                # Calculate current batch size (handles the last partial batch)
                current_batch = min(batch_size, num_samples - i)

                # 1. Create Random Noise (The "Seed")
                Z = torch.rand((current_batch, self.config.model.SEQ_LEN, self.config.model.N_SIGNALS),
                               dtype=torch.float32).to(self.device)

                # 2. Generator: Noise -> Raw Latent
                E_hat = self.model.forward_generator(Z)

                # 3. Supervisor: Raw Latent -> Temporally Coherent Latent
                # This ensures the generated week looks like a continuous week, not 7 random days.
                H_hat = self.model.forward_supervisor(E_hat)

                # 4. Recovery: Latent -> Readable Features (Sleep, HR)
                X_hat = self.model.forward_recovery(H_hat)

                generated_data.append(X_hat.cpu().numpy())

        # Stitch batches together
        final_data = np.concatenate(generated_data, axis=0)

        # Save as compressed NumPy array
        save_path = self.config.data.SYNTHETIC_DATA_FILE
        np.savez_compressed(save_path, sequences=final_data)

        print(f"✅ Generated shape: {final_data.shape}")
        print(f"💾 Saved to: {save_path}")
        return final_data
