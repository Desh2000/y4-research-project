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
import numpy as np
import pandas as pd
import os # Import os for path manipulation

# --- 1. Define Loss Functions ---
# Loss functions tell our models how well they are performing.
# For GANs, we use Binary Crossentropy because it's a binary classification problem
# (real vs. fake).

# This loss function is for the Discriminator when it tries to classify real images.
# It should output 1 for real images.
cross_entropy = tf.keras.losses.BinaryCrossentropy(from_logits=True)

def discriminator_loss(real_output, fake_output):
    """
    Calculates the Discriminator's loss.
    The Discriminator wants to classify real data as 'real' (1) and fake data as 'fake' (0).
    """
    # Loss for real data: how far is the discriminator's prediction from 1 (real)?
    real_loss = cross_entropy(tf.ones_like(real_output), real_output)
    # Loss for fake data: how far is the discriminator's prediction from 0 (fake)?
    fake_loss = cross_entropy(tf.zeros_like(fake_output), fake_output)
    # Total discriminator loss is the sum of these two.
    total_loss = real_loss + fake_loss
    return total_loss

def generator_loss(fake_output):
    """
    Calculates the Generator's loss.
    The Generator wants its fake data to be classified as 'real' (1) by the Discriminator.
    """
    # The generator wants the discriminator to output 1 for its fake data.
    # So, we calculate how far the discriminator's prediction for fake data is from 1.
    return cross_entropy(tf.ones_like(fake_output), fake_output)


# --- 2. Define Optimizers ---
# Optimizers are algorithms that adjust the weights of our neural networks
# to minimize the loss functions. Adam is a popular choice for GANs.

generator_optimizer = tf.keras.optimizers.Adam(1e-4)  # Learning rate of 0.0001
discriminator_optimizer = tf.keras.optimizers.Adam(
    1e-4)  # Learning rate of 0.0001


# --- 3. Define Training Step ---
# This function describes one single step of the GAN training process.
# It's decorated with @tf.function for performance optimization (compiles it into a TensorFlow graph).

@tf.function
def train_step(real_data, generator, discriminator, latent_dim):
    """
    Performs one training step for both the Generator and Discriminator.

    Args:
        real_data (tf.Tensor): A batch of real data samples.
        generator (tf.keras.Model): The Generator model.
        discriminator (tf.keras.Model): The Discriminator model.
        latent_dim (int): The dimension of the latent noise vector.
    """
    # Generate random noise for the Generator to create fake data.
    noise = tf.random.normal([real_data.shape[0], latent_dim])

    # Use tf.GradientTape to record operations for automatic differentiation.
    # This allows us to calculate gradients for updating model weights.
    with tf.GradientTape() as gen_tape, tf.GradientTape() as disc_tape:
        # Generator creates fake data from noise.
        generated_data = generator(noise, training=True)

        # Discriminator tries to classify real and fake data.
        real_output = discriminator(real_data, training=True)
        fake_output = discriminator(generated_data, training=True)

        # Calculate losses for both models.
        gen_loss = generator_loss(fake_output)
        disc_loss = discriminator_loss(real_output, fake_output)

    # Calculate gradients for the Generator and Discriminator.
    # Gradients tell us how much to change each weight to reduce the loss.
    gradients_of_generator = gen_tape.gradient(
        gen_loss, generator.trainable_variables)
    gradients_of_discriminator = disc_tape.gradient(
        disc_loss, discriminator.trainable_variables)

    # Apply the gradients to update the model weights using their respective optimizers.
    generator_optimizer.apply_gradients(
        zip(gradients_of_generator, generator.trainable_variables))
    discriminator_optimizer.apply_gradients(
        zip(gradients_of_discriminator, discriminator.trainable_variables))
    
    return gen_loss, disc_loss # Return losses for monitoring


# --- 4. Main Training Loop ---
# This function orchestrates the entire training process over multiple epochs.

def train_gan(dataset, epochs, generator, discriminator, latent_dim, batch_size, monitor):
    """
    Trains the GAN for a specified number of epochs.

    Args:
        dataset (tf.data.Dataset): The preprocessed dataset to train on.
        epochs (int): The number of training epochs.
        generator (tf.keras.Model): The Generator model.
        discriminator (tf.keras.Model): The Discriminator model.
        latent_dim (int): The dimension of the latent noise vector.
        batch_size (int): The number of samples per training batch.
        monitor (GANMonitor): The monitor object to record and save training progress.
    """
    for epoch in range(epochs):
        print(f"\nEpoch {epoch + 1}/{epochs}")
        epoch_gen_loss = 0.0
        epoch_disc_loss = 0.0
        num_batches = 0
        # Iterate over each batch of the dataset.
        for batch_num, data_batch in enumerate(dataset):
            # Perform one training step for the current batch.
            gen_loss, disc_loss = train_step(data_batch, generator, discriminator, latent_dim)
            epoch_gen_loss += gen_loss
            epoch_disc_loss += disc_loss
            num_batches += 1
            if batch_num % 100 == 0: # Print progress every 100 batches
                print(f"  Batch {batch_num} completed. Gen Loss: {gen_loss:.4f}, Disc Loss: {disc_loss:.4f}")
        
        # Calculate average losses for the epoch
        avg_gen_loss = epoch_gen_loss / num_batches
        avg_disc_loss = epoch_disc_loss / num_batches
        monitor.record_losses(avg_gen_loss, avg_disc_loss)
        print(f"Epoch {epoch + 1} completed. Avg Gen Loss: {avg_gen_loss:.4f}, Avg Disc Loss: {avg_disc_loss:.4f}")
        
        # Call the monitor's on_epoch_end method
        monitor.on_epoch_end(epoch, generator, discriminator)
        
    print("\nTraining complete!")
    monitor.plot_losses() # Plot losses after training


# --- Example Usage (for testing purposes) ---
if __name__ == "__main__":
    # First, let's load some data using our DataLoader.
    # For simplicity, we'll use the DASS dataset as our primary training data.
    # In a real scenario, you'd preprocess and combine datasets.

    # Define the path to your data folder (relative to the project root)
    data_path = "data"
    data_loader = MentalHealthDataLoader(data_path)
    dass_data = data_loader.load_dass_dataset()

    if dass_data is not None:
        # Preprocessing: For a simple GAN, we need numerical data and a consistent range.
        # The DASS dataset has many columns. For this initial test, let's select
        # a subset of numerical columns and normalize them to [-1, 1].

        # Identify numerical columns (assuming DASS responses are numerical)
        # You might need to adjust this based on actual DASS column types.
        numerical_cols = dass_data.select_dtypes(include=np.number).columns
        processed_data = dass_data[numerical_cols].copy()

        # Handle missing values: fill with mean for simplicity for now.
        # In a real project, you'd use more sophisticated imputation.
        processed_data = processed_data.fillna(processed_data.mean())

        # Normalize data to [-1, 1] range, which is suitable for Generator's tanh output.
        # This is a common practice for GANs with tabular data.
        processed_data = processed_data.apply(
            lambda x: 2 * ((x - x.min()) / (x.max() - x.min())) - 1)

        # Convert pandas DataFrame to TensorFlow Dataset.
        # This is efficient for training large models.
        BATCH_SIZE = 64
        BUFFER_SIZE = len(processed_data)  # Shuffle the data

        # Convert to float32, which is standard for TensorFlow models.
        train_dataset = tf.data.Dataset.from_tensor_slices(processed_data.values.astype(np.float32)) \
            .shuffle(BUFFER_SIZE).batch(BATCH_SIZE)

        # Define GAN parameters
        LATENT_DIM = 100  # Dimension of the noise vector
        # Number of features in our processed data
        OUTPUT_DIM = processed_data.shape[1]
        EPOCHS = 50       # Number of training epochs
        
        # Define log directory for GANMonitor
        # IMPORTANT FIX: Use a relative path from the project root for LOG_DIR
        # This ensures the 'gan_logs' folder is created in the project root
        # regardless of where the script is executed from.
        current_script_dir = os.path.dirname(os.path.abspath(__file__))
        project_root = os.path.abspath(os.path.join(current_script_dir, "..", "..", ".."))
        LOG_DIR = os.path.join(project_root, "gan_logs")

        # Initialize Generator and Discriminator
        generator = Generator(LATENT_DIM, OUTPUT_DIM)
        discriminator = Discriminator(OUTPUT_DIM)
        
        # Initialize GANMonitor
        monitor = GANMonitor(LOG_DIR, LATENT_DIM, OUTPUT_DIM, sample_interval=5, checkpoint_interval=10)

        # Print model summaries to confirm their structure
        print("\n--- Generator Summary ---")
        generator.build(input_shape=(None, LATENT_DIM))
        generator.summary()

        print("\n--- Discriminator Summary ---")
        discriminator.build(input_shape=(None, OUTPUT_DIM))
        discriminator.summary()

        # Start training the GAN
        print("\n--- Starting GAN Training ---")
        train_gan(train_dataset, EPOCHS, generator,
                  discriminator, LATENT_DIM, BATCH_SIZE, monitor)

        # After training, you can generate some synthetic samples to see the results.
        print("\n--- Generating Synthetic Samples After Training ---")
        num_generate = 10  # Number of synthetic samples to generate
        test_noise = tf.random.normal([num_generate, LATENT_DIM])
        synthetic_samples = generator(test_noise, training=False)

        # Print the first few generated samples (they will be in [-1, 1] range)
        print("First 5 synthetic samples (normalized to [-1, 1]):")
        print(synthetic_samples.numpy()[:5])

    else:
        print("Failed to load DASS dataset. Cannot proceed with GAN training example.")