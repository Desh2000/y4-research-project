"""
MANO Component 1: TimeGAN Training Logic (PyTorch)

This script manages the 'Classroom'. It teaches the model in 3 distinct semesters (Phases).
"""
import torch
import torch.nn as nn
import torch.optim as optim
import numpy as np
import json


class TimeGANTrainer:
    def __init__(self, model, config):
        self.model = model
        self.config = config
        self.device = config.training.DEVICE

        # --- Loss Functions ---
        # MSE (Mean Squared Error): For continuous data (e.g. Sleep 0.5 vs 0.6)
        self.mse_loss = nn.MSELoss()
        # BCE (Binary Cross Entropy): For classification (Real vs Fake)
        self.bce_loss = nn.BCEWithLogitsLoss()

        # --- Optimizers ---
        # We need distinct optimizers because we update different parts of the brain
        # at different times.

        # 1. Autoencoder Optimizer (Embedder + Recovery)
        self.e_opt = optim.Adam(
            list(model.embedder.parameters()) + list(model.embedder_out.parameters()) +
            list(model.recovery.parameters()) +
            list(model.recovery_out.parameters()),
            lr=config.training.LEARNING_RATE
        )

        # 2. Generator/Supervisor Optimizer
        self.g_opt = optim.Adam(
            list(model.generator.parameters()) + list(model.generator_out.parameters()) +
            list(model.supervisor.parameters()) +
            list(model.supervisor_out.parameters()),
            lr=config.training.LEARNING_RATE
        )

        # 3. Discriminator Optimizer
        self.d_opt = optim.Adam(
            list(model.discriminator.parameters()) +
            list(model.discriminator_out.parameters()),
            lr=config.training.DISCRIMINATOR_LR
        )

        self.loss_history = {'embedder_loss': [],
                             'supervisor_loss': [], 'g_loss': [], 'd_loss': []}

    def _get_batch(self, data, batch_size):
        """Randomly select a batch of real data sequences"""
        idx = np.random.permutation(len(data))[:batch_size]
        return torch.tensor(data[idx], dtype=torch.float32).to(self.device)

    def _generate_noise(self, batch_size):
        """Generate random 'static' noise for the Generator to shape"""
        return torch.rand((batch_size, self.config.model.SEQ_LEN, self.config.model.N_SIGNALS),
                          dtype=torch.float32).to(self.device)

    def _compute_variational_loss(self, H_real, H_fake):
        """
        Moments Matching Loss:
        Standard GANs might generate valid individual samples but miss the 'Big Picture'.
        This forces the Mean and Variance of the Fake data to match the Real data.
        """
        H_real_flat = H_real.reshape(H_real.size(0), -1)
        H_fake_flat = H_fake.reshape(H_fake.size(0), -1)

        mean_loss = self.mse_loss(H_real_flat.mean(0), H_fake_flat.mean(0))
        std_loss = self.mse_loss(H_real_flat.std(0), H_fake_flat.std(0))
        return mean_loss + std_loss

    def train(self, data):
        print(
            f"\n{'='*80}\nSTARTING TIMEGAN TRAINING ON {self.device.upper()}\n{'='*80}")
        batch_size = self.config.training.BATCH_SIZE

        # --- PHASE 1: AUTOENCODER TRAINING ---
        # Goal: Minimize reconstruction error (Input X -> Latent -> Output X)
        print("\nPhase 1: Autoencoder (Embedder + Recovery)...")
        for epoch in range(self.config.training.EMBEDDER_EPOCHS):
            X = self._get_batch(data, batch_size)

            # Forward pass
            H = self.model.forward_embedder(X)
            X_tilde = self.model.forward_recovery(H)

            # Calculate error (Original vs Reconstructed)
            e_loss = self.mse_loss(X_tilde, X)

            # Update weights
            self.e_opt.zero_grad()
            e_loss.backward()
            self.e_opt.step()
            self.loss_history['embedder_loss'].append(e_loss.item())

            if (epoch+1) % 100 == 0:
                print(
                    f"  Epoch {epoch+1}/{self.config.training.EMBEDDER_EPOCHS} | E_Loss: {e_loss.item():.6f}")

        # --- PHASE 2: SUPERVISOR TRAINING ---
        # Goal: Minimize temporal error (Latent[t] -> Supervisor -> Latent[t+1])
        print("\nPhase 2: Supervisor (Temporal Dynamics)...")
        for epoch in range(self.config.training.SUPERVISOR_EPOCHS):
            X = self._get_batch(data, batch_size)
            H = self.model.forward_embedder(X)

            # Supervisor tries to predict the latent vector H
            H_hat = self.model.forward_supervisor(H)

            # Compare Prediction (H_hat) vs Reality (H shifted by 1 step)
            # We want H_hat[t] to equal H[t+1]
            s_loss = self.mse_loss(H_hat[:, :-1, :], H[:, 1:, :])

            self.g_opt.zero_grad()
            s_loss.backward()
            self.g_opt.step()
            self.loss_history['supervisor_loss'].append(s_loss.item())

            if (epoch+1) % 100 == 0:
                print(
                    f"  Epoch {epoch+1}/{self.config.training.SUPERVISOR_EPOCHS} | S_Loss: {s_loss.item():.6f}")

        # --- PHASE 3: JOINT TRAINING (THE GAN) ---
        # Goal: Train Generator to fool Discriminator AND maintain temporal logic
        print("\nPhase 3: Joint Training (Adversarial)...")
        for epoch in range(self.config.training.JOINT_EPOCHS):

            # 1. TRAIN GENERATOR (Twice per loop for stability)
            for _ in range(2):
                X = self._get_batch(data, batch_size)
                Z = self._generate_noise(batch_size)

                # Generator Path: Noise -> Latent -> Supervisor -> Fake Label
                E_hat = self.model.forward_generator(Z)
                H_hat = self.model.forward_supervisor(E_hat)
                Y_fake = self.model.forward_discriminator(H_hat)

                # Embedder Path (for Supervised Loss reference)
                H = self.model.forward_embedder(X)
                H_hat_supervise = self.model.forward_supervisor(H)
                X_tilde = self.model.forward_recovery(H)

                # --- Loss Calculation ---
                # A. Fool the Discriminator (Adversarial)
                g_loss_u = self.bce_loss(Y_fake, torch.ones_like(Y_fake))

                # B. Maintain Temporal Consistency (Supervised)
                g_loss_s = self.mse_loss(
                    H_hat_supervise[:, :-1, :], H[:, 1:, :])

                # C. Match Statistics (Moments)
                g_loss_v = self._compute_variational_loss(H, H_hat)

                # D. Reconstruction Quality
                e_loss = self.mse_loss(X_tilde, X)

                # Combine losses
                g_loss = g_loss_u + g_loss_s + g_loss_v + e_loss

                self.g_opt.zero_grad()
                self.e_opt.zero_grad()
                g_loss.backward()
                self.g_opt.step()
                self.e_opt.step()

            # 2. TRAIN DISCRIMINATOR
            X = self._get_batch(data, batch_size)
            Z = self._generate_noise(batch_size)

            # Real Path
            H = self.model.forward_embedder(X)
            # Fake Path
            E_hat = self.model.forward_generator(Z)
            H_hat = self.model.forward_supervisor(E_hat)

            # Classification
            Y_real = self.model.forward_discriminator(H)
            # Detach! Don't update G here.
            Y_fake = self.model.forward_discriminator(H_hat.detach())

            # Loss: Real should be 1, Fake should be 0
            d_loss = self.bce_loss(Y_real, torch.ones_like(
                Y_real)) + self.bce_loss(Y_fake, torch.zeros_like(Y_fake))

            self.d_opt.zero_grad()
            d_loss.backward()
            self.d_opt.step()

            self.loss_history['g_loss'].append(g_loss.item())
            self.loss_history['d_loss'].append(d_loss.item())

            if (epoch+1) % 50 == 0:
                print(
                    f"  Epoch {epoch+1}/{self.config.training.JOINT_EPOCHS} | G: {g_loss.item():.4f} | D: {d_loss.item():.4f}")

        print("✅ TRAINING COMPLETE")
        torch.save(self.model.state_dict(), self.config.data.FINAL_MODEL_PATH)
        print(f"Model saved to {self.config.data.FINAL_MODEL_PATH}")
