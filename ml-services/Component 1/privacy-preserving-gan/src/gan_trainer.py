"""
This module defines the GAN training loop, including loss functions, optimizers,
and the training step for the Generator and Discriminator.
"""

import tensorflow as tf
from tensorflow.keras import layers, Model
# Import our previously defined GAN models
from gan_model import Generator, Discriminator
from data_loader import MentalHealthDataLoader  # Import our data loader
from gan_monitor import GANMonitor  # Import the GANMonitor
# Import our differentially private optimizer
from dp_optimizer import make_dp_adam_optimizer
import numpy as np
import pandas as pd
import os  # Import os for path manipulation

# --- 1. Define Loss Functions ---
cross_entropy = tf.keras.losses.BinaryCrossentropy(from_logits=True)


def discriminator_loss(real_output, fake_output):
    """
    Calculates the Discriminator's loss.
    """
    real_loss = cross_entropy(tf.ones_like(real_output), real_output)
    fake_loss = cross_entropy(tf.zeros_like(fake_output), fake_output)
    total_loss = real_loss + fake_loss
    return total_loss


def generator_loss(fake_output):
    """
    Calculates the Generator's loss.
    """
    return cross_entropy(tf.ones_like(fake_output), fake_output)


# --- 2. Define Optimizers (Now Differentially Private) ---
# These will be initialized within the main block to pass DP parameters.
generator_optimizer = None
discriminator_optimizer = None

# --- 3. Define Training Step ---
@tf.function
def train_step(real_data, generator, discriminator, latent_dim, generator_optimizer, discriminator_optimizer):
    """
    Performs one training step for both the Generator and Discriminator.
    """
    noise = tf.random.normal([real_data.shape[0], latent_dim])

    with tf.GradientTape() as gen_tape, tf.GradientTape() as disc_tape:
        generated_data = generator(noise, training=True)

        real_output = discriminator(real_data, training=True)
        fake_output = discriminator(generated_data, training=True)

        gen_loss = generator_loss(fake_output)
        disc_loss = discriminator_loss(real_output, fake_output)

    # *** FIX APPLIED HERE ***
    # Instead of using tape.gradient(), we let the DP optimizer compute the gradients.
    # This is the crucial step where clipping and noising happens.
    # The `_compute_gradients` method is internal but is the correct way to use
    # DP optimizers in a custom training loop like this.
    
    # For the Generator
    grads_and_vars_generator = generator_optimizer._compute_gradients(
        gen_loss, generator.trainable_variables, tape=gen_tape)
    
    # For the Discriminator
    grads_and_vars_discriminator = discriminator_optimizer._compute_gradients(
        disc_loss, discriminator.trainable_variables, tape=disc_tape)

    # Apply the (now private) gradients to the models.
    generator_optimizer.apply_gradients(grads_and_vars_generator)
    discriminator_optimizer.apply_gradients(grads_and_vars_discriminator)

    return gen_loss, disc_loss


# --- 4. Main Training Loop ---
def train_gan(dataset, epochs, generator, discriminator, latent_dim, batch_size, monitor,
              l2_norm_clip, noise_multiplier, num_microbatches, learning_rate):
    """
    Trains the GAN for a specified number of epochs with differential privacy.
    """
    global generator_optimizer, discriminator_optimizer
    generator_optimizer = make_dp_adam_optimizer(
        l2_norm_clip=l2_norm_clip,
        noise_multiplier=noise_multiplier,
        num_microbatches=num_microbatches,
        learning_rate=learning_rate
    )
    discriminator_optimizer = make_dp_adam_optimizer(
        l2_norm_clip=l2_norm_clip,
        noise_multiplier=noise_multiplier,
        num_microbatches=num_microbatches,
        learning_rate=learning_rate
    )

    for epoch in range(epochs):
        print(f"\nEpoch {epoch + 1}/{epochs}")
        epoch_gen_loss = 0.0
        epoch_disc_loss = 0.0
        num_batches = 0
        for batch_num, data_batch in enumerate(dataset):
            gen_loss, disc_loss = train_step(
                data_batch, generator, discriminator, latent_dim, generator_optimizer, discriminator_optimizer)
            epoch_gen_loss += gen_loss
            epoch_disc_loss += disc_loss
            num_batches += 1
            if batch_num % 100 == 0:
                print(
                    f"  Batch {batch_num} completed. Gen Loss: {gen_loss:.4f}, Disc Loss: {disc_loss:.4f}")

        avg_gen_loss = epoch_gen_loss / num_batches
        avg_disc_loss = epoch_disc_loss / num_batches
        monitor.record_losses(avg_gen_loss, avg_disc_loss)
        print(
            f"Epoch {epoch + 1} completed. Avg Gen Loss: {avg_gen_loss:.4f}, Avg Disc Loss: {avg_disc_loss:.4f}")

        monitor.on_epoch_end(epoch, generator, discriminator)

    print("\nTraining complete!")
    monitor.plot_losses()


# --- Example Usage (for testing purposes) ---
if __name__ == "__main__":
    data_path = "data"
    data_loader = MentalHealthDataLoader(data_path)
    dass_data = data_loader.load_dass_dataset()

    if dass_data is not None:
        numerical_cols = dass_data.select_dtypes(include=np.number).columns
        processed_data = dass_data[numerical_cols].copy()
        processed_data = processed_data.fillna(processed_data.mean())
        processed_data = processed_data.apply(
            lambda x: 2 * ((x - x.min()) / (x.max() - x.min())) - 1 if x.max() > x.min() else 0
        )

        BATCH_SIZE = 64
        BUFFER_SIZE = len(processed_data)

        train_dataset = tf.data.Dataset.from_tensor_slices(processed_data.values.astype(np.float32)) \
            .shuffle(BUFFER_SIZE).batch(BATCH_SIZE)

        LATENT_DIM = 100
        OUTPUT_DIM = processed_data.shape[1]
        EPOCHS = 50
        LEARNING_RATE = 1e-4

        L2_NORM_CLIP = 1.0
        NOISE_MULTIPLIER = 1.1
        # Recommendation: Use num_microbatches=1 for per-example gradient clipping
        NUM_MICROBATCHES = 1

        current_script_dir = os.path.dirname(os.path.abspath(__file__))
        project_root = os.path.abspath(os.path.join(
            current_script_dir, "..", "..", ".."))
        LOG_DIR = os.path.join(project_root, "gan_logs")

        generator = Generator(LATENT_DIM, OUTPUT_DIM)
        discriminator = Discriminator(OUTPUT_DIM)

        monitor = GANMonitor(LOG_DIR, LATENT_DIM, OUTPUT_DIM,
                             sample_interval=5, checkpoint_interval=10)

        print("\n--- Generator Summary ---")
        generator.build(input_shape=(None, LATENT_DIM))
        generator.summary()

        print("\n--- Discriminator Summary ---")
        discriminator.build(input_shape=(None, OUTPUT_DIM))
        discriminator.summary()

        print("\n--- Starting GAN Training with Differential Privacy ---")
        train_gan(train_dataset, EPOCHS, generator,
                  discriminator, LATENT_DIM, BATCH_SIZE, monitor,
                  L2_NORM_CLIP, NOISE_MULTIPLIER, NUM_MICROBATCHES, LEARNING_RATE)

        print("\n--- Generating Synthetic Samples After Training ---")
        num_generate = 10
        test_noise = tf.random.normal([num_generate, LATENT_DIM])
        synthetic_samples = generator(test_noise, training=False)

        print("First 5 synthetic samples (normalized to [-1, 1]):")
        print(synthetic_samples.numpy()[:5])

    else:
        print("Failed to load DASS dataset. Cannot proceed with GAN training example.")
