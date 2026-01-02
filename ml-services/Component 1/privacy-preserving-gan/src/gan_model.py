"""
This module defines two main classes for a Generative Adversarial Network (GAN):
- Generator: Produces synthetic data from random noise, simulating real data distribution.
- Discriminator: Attempts to distinguish between real and synthetic (generated) data.
Key Components:
---------------
1. Generator Class:
    - Inherits from tf.keras.Model.
    - Takes a latent noise vector as input.
    - Uses several Dense layers with ReLU activations and BatchNormalization for stable training.
    - Final Dense layer uses 'tanh' activation to output data in the range [-1, 1].
    - Output dimension matches the real dataset's feature size.
2. Discriminator Class:
    - Inherits from tf.keras.Model.
    - Takes data samples (real or synthetic) as input.
    - Uses several Dense layers with ReLU activations and Dropout for regularization.
    - Final Dense layer uses 'sigmoid' activation for binary classification (real/fake).
3. Example Usage (Main Block):
    - Instantiates Generator and Discriminator with example dimensions.
    - Builds models and prints their summaries.
    - Generates synthetic data from random noise.
    - Passes both synthetic and real data through the Discriminator to verify output shapes.
Usage Notes:
------------
- The Generator and Discriminator are designed for tabular data (e.g., DASS dataset with 172 features).
- BatchNormalization in the Generator helps stabilize training by normalizing activations.
- Dropout in the Discriminator helps prevent overfitting.
- The code is structured for easy extension and integration into a GAN training loop.
GAN Model for Privacy-Preserving Mental Health Simulation System
This module defines the Generator and Discriminator networks for the GAN.
"""

import tensorflow as tf
from tensorflow.keras import layers, Model


class Generator(Model):
    """
    The Generator network takes random noise as input and generates synthetic data.
    """

    def __init__(self, latent_dim, output_dim):
        super(Generator, self).__init__()
        self.latent_dim = latent_dim
        self.output_dim = output_dim

        self.model = tf.keras.Sequential([
            layers.Dense(256, activation='relu', input_shape=(latent_dim,)),
            layers.BatchNormalization(),
            layers.Dense(512, activation='relu'),
            layers.BatchNormalization(),
            layers.Dense(1024, activation='relu'),
            layers.BatchNormalization(),
            # Tanh for output in range [-1, 1]
            layers.Dense(output_dim, activation='tanh')
        ])

    def call(self, inputs):
        return self.model(inputs)


class Discriminator(Model):
    """
    The Discriminator network takes real or synthetic data as input and tries to classify it as real or fake.
    """

    def __init__(self, input_dim):
        super(Discriminator, self).__init__()
        self.input_dim = input_dim

        self.model = tf.keras.Sequential([
            layers.Dense(1024, activation='relu', input_shape=(input_dim,)),
            layers.Dropout(0.3),
            layers.Dense(512, activation='relu'),
            layers.Dropout(0.3),
            layers.Dense(256, activation='relu'),
            layers.Dropout(0.3),
            # *** FIX APPLIED HERE ***
            # Removed activation='sigmoid'. The model will now output raw logits.
            # This is more numerically stable when used with a loss function
            # that has `from_logits=True`.
            layers.Dense(1)
        ])

    def call(self, inputs):
        return self.model(inputs)


# Example usage (for testing purposes)
if __name__ == "__main__":
    latent_dim = 100  # Dimension of the random noise vector
    output_dim = 172  # Example: DASS dataset has 172 features

    # Create a Generator instance
    generator = Generator(latent_dim, output_dim)
    generator.build(input_shape=(None, latent_dim))
    print("\nGenerator Summary:")
    generator.summary()

    # Create a Discriminator instance
    discriminator = Discriminator(output_dim)
    discriminator.build(input_shape=(None, output_dim))
    print("\nDiscriminator Summary:")
    discriminator.summary()

    # Test with dummy data
    dummy_noise = tf.random.normal([1, latent_dim])
    synthetic_data = generator(dummy_noise)
    print(f"\nSynthetic data shape: {synthetic_data.shape}")

    discriminator_output = discriminator(synthetic_data)
    print(
        f"Discriminator output shape (for synthetic data): {discriminator_output.shape}")

    dummy_real_data = tf.random.normal([1, output_dim])
    discriminator_output_real = discriminator(dummy_real_data)
    print(
        f"Discriminator output shape (for real data): {discriminator_output_real.shape}")
