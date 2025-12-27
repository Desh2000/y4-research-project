
"""
This module defines a GANMonitor class to track training progress, save models,
and generate synthetic samples during GAN training.
"""

import tensorflow as tf
import numpy as np
import pandas as pd
import os
import matplotlib.pyplot as plt

class GANMonitor:
    """
    A class to monitor GAN training progress, save models, and generate samples.
    
    This monitor will:
    - Record Generator and Discriminator losses.
    - Save model checkpoints periodically.
    - Generate and save synthetic samples to observe data evolution.
    - (Future) Provide basic evaluation metrics.
    """

    def __init__(self, log_dir, latent_dim, output_dim, sample_interval=10, checkpoint_interval=10):
        """
        Initializes the GANMonitor.
        
        Args:
            log_dir (str): Directory to save logs, checkpoints, and generated samples.
            latent_dim (int): Dimension of the latent noise vector for the Generator.
            output_dim (int): Dimension of the output data (number of features).
            sample_interval (int): How often (in epochs) to generate and save samples.
            checkpoint_interval (int): How often (in epochs) to save model checkpoints.
        """
        self.log_dir = log_dir
        self.latent_dim = latent_dim
        self.output_dim = output_dim
        self.sample_interval = sample_interval
        self.checkpoint_interval = checkpoint_interval

        # Create directories if they don't exist
        self.samples_dir = os.path.join(log_dir, "generated_samples")
        self.checkpoints_dir = os.path.join(log_dir, "checkpoints")
        os.makedirs(self.samples_dir, exist_ok=True)
        os.makedirs(self.checkpoints_dir, exist_ok=True)

        # Lists to store losses over epochs
        self.gen_losses = []
        self.disc_losses = []

    def on_epoch_end(self, epoch, generator, discriminator):
        """
        Callback function to be called at the end of each training epoch.
        
        Args:
            epoch (int): Current epoch number.
            generator (tf.keras.Model): The Generator model.
            discriminator (tf.keras.Model): The Discriminator model.
        """
        # Record losses (assuming losses are calculated and accessible in train_gan)
        # For now, we'll just append dummy values or rely on train_gan to pass them.
        # We will modify train_gan to return losses per epoch.
        
        # Generate and save samples periodically
        if (epoch + 1) % self.sample_interval == 0:
            self._generate_and_save_samples(epoch + 1, generator)

        # Save model checkpoints periodically
        if (epoch + 1) % self.checkpoint_interval == 0:
            self._save_checkpoint(epoch + 1, generator, discriminator)
            
        # Plot losses (after we start recording them properly)
        # self._plot_losses()

    def _generate_and_save_samples(self, epoch, generator, num_samples=10):
        """
        Generates synthetic samples and saves them to a CSV file.
        """
        noise = tf.random.normal([num_samples, self.latent_dim])
        synthetic_samples = generator(noise, training=False).numpy()
        
        # Create a DataFrame for better readability and saving
        sample_df = pd.DataFrame(synthetic_samples)
        sample_path = os.path.join(self.samples_dir, f"synthetic_samples_epoch_{epoch:04d}.csv")
        sample_df.to_csv(sample_path, index=False)
        print(f"Generated and saved {num_samples} synthetic samples to {sample_path}")

    def _save_checkpoint(self, epoch, generator, discriminator):
        """
        Saves the Generator and Discriminator models.
        """
        # Corrected: Added .keras extension for saving models
        gen_checkpoint_path = os.path.join(self.checkpoints_dir, f"generator_epoch_{epoch:04d}.keras")
        disc_checkpoint_path = os.path.join(self.checkpoints_dir, f"discriminator_epoch_{epoch:04d}.keras")
        generator.save(gen_checkpoint_path)
        discriminator.save(disc_checkpoint_path)
        print(f"Saved models checkpoints for epoch {epoch} to {self.checkpoints_dir}")

    def record_losses(self, gen_loss, disc_loss):
        """
        Records the average Generator and Discriminator losses for the current epoch.
        """
        self.gen_losses.append(gen_loss)
        self.disc_losses.append(disc_loss)

    def plot_losses(self):
        """
        Plots the Generator and Discriminator losses over epochs.
        """
        if not self.gen_losses or not self.disc_losses:
            print("No losses recorded to plot.")
            return

        epochs = range(1, len(self.gen_losses) + 1)
        plt.figure(figsize=(10, 6))
        plt.plot(epochs, self.gen_losses, label="Generator Loss")
        plt.plot(epochs, self.disc_losses, label="Discriminator Loss")
        plt.xlabel("Epoch")
        plt.ylabel("Loss")
        plt.title("GAN Training Losses")
        plt.legend()
        plt.grid(True)
        plot_path = os.path.join(self.log_dir, "gan_losses.png")
        plt.savefig(plot_path)
        print(f"Saved loss plot to {plot_path}")
        plt.close() # Close the plot to free memory


# Example Usage (for testing purposes)
if __name__ == "__main__":
    # Dummy models for testing GANMonitor
    class DummyGenerator(tf.keras.Model):
        def __init__(self, latent_dim, output_dim):
            super().__init__()
            self.output_dim = output_dim
        def call(self, inputs, training=False):
            return tf.random.normal([inputs.shape[0], self.output_dim])

    class DummyDiscriminator(tf.keras.Model):
        def __init__(self, input_dim):
            super().__init__()
        def call(self, inputs, training=False):
            return tf.random.uniform([inputs.shape[0], 1])

    LATENT_DIM = 100
    OUTPUT_DIM = 172
    EPOCHS = 5
    LOG_DIR = "./gan_logs_test"

    dummy_gen = DummyGenerator(LATENT_DIM, OUTPUT_DIM)
    dummy_disc = DummyDiscriminator(OUTPUT_DIM)

    monitor = GANMonitor(LOG_DIR, LATENT_DIM, OUTPUT_DIM, sample_interval=1, checkpoint_interval=2)

    print("\n--- Testing GANMonitor ---")
    for epoch in range(EPOCHS):
        # Simulate training step and get losses
        gen_loss_val = np.random.rand()
        disc_loss_val = np.random.rand()
        monitor.record_losses(gen_loss_val, disc_loss_val)
        
        print(f"Epoch {epoch + 1}: Gen Loss = {gen_loss_val:.4f}, Disc Loss = {disc_loss_val:.4f}")
        monitor.on_epoch_end(epoch, dummy_gen, dummy_disc)

    monitor.plot_losses()
    print("GANMonitor test complete. Check the gan_logs_test directory.")