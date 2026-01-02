#
# This module provides a differentially private (DP) optimizer for TensorFlow,
# based on the DP-SGD algorithm.
#

# Import the necessary libraries.
# tensorflow is the core machine learning framework.
import tensorflow as tf
# tensorflow_privacy is the library that provides the differential privacy tools.
import tensorflow_privacy as tfp

# --- 1. Define Differentially Private Optimizer ---
# We will use the DPKerasAdam optimizer from TensorFlow Privacy.
# This optimizer is a special version of the standard Adam optimizer
# that has differential privacy mechanisms built directly into it.

# These are the key parameters for controlling the differential privacy guarantees:
#
# l2_norm_clip: The maximum Euclidean (L2) norm for each individual gradient.
#               Before the model's weights are updated, the gradients are "clipped"
#               or scaled down to this maximum value. This is a crucial step because
#               it limits how much influence any single data point can have on the model,
#               which is the core idea behind differential privacy.
#
# noise_multiplier: This controls how much random noise is added to the clipped gradients.
#                   More noise means stronger privacy protection, but it can sometimes
#                   make it harder for the model to learn effectively (the privacy-utility trade-off).
#
# num_microbatches: Gradients are calculated on small subsets of the data called microbatches.
#                   Clipping happens per-microbatch. This technique improves both privacy
#                   and model performance compared to clipping the gradient of the entire batch.
#

# This function creates and returns the differentially private optimizer.


def make_dp_adam_optimizer(l2_norm_clip, noise_multiplier, num_microbatches, learning_rate):
    """
    Creates a differentially private Adam optimizer for a Keras model.

    Args:
        l2_norm_clip (float): The clipping norm for gradients.
        noise_multiplier (float): The multiplier for the noise added to gradients.
        num_microbatches (int): The number of microbatches per batch.
        learning_rate (float): The learning rate for the Adam optimizer.

    Returns:
        A differentially private Adam optimizer instance.
    """
    # Instantiate the differentially private Adam optimizer for Keras.
    dp_optimizer = tfp.DPKerasAdamOptimizer(
        l2_norm_clip=l2_norm_clip,
        noise_multiplier=noise_multiplier,
        num_microbatches=num_microbatches,
        learning_rate=learning_rate
    )
    # Return the newly created optimizer object.
    return dp_optimizer


# --- Example Usage (for testing this script directly) ---

# The `if __name__ == "__main__"` block ensures this code only runs
# when you execute this file directly (e.g., `python dp_optimizer.py`).
# It won't run if this file is imported as a module into another script.
if __name__ == "__main__":
    # Print a message to confirm the script is running.
    print("Testing dp_optimizer.py")

    # Define some example DP parameters for our test.
    TEST_L2_NORM_CLIP = 1.0           # Set a test value for gradient clipping.
    TEST_NOISE_MULTIPLIER = 1.1       # Set a test value for the noise.
    TEST_NUM_MICROBATCHES = 1         # Set a test value for microbatches.
    TEST_LEARNING_RATE = 1e-4         # Set a test value for the learning rate.

    # Call our function to create the DP Adam optimizer with the test parameters.
    dp_adam = make_dp_adam_optimizer(
        l2_norm_clip=TEST_L2_NORM_CLIP,
        noise_multiplier=TEST_NOISE_MULTIPLIER,
        num_microbatches=TEST_NUM_MICROBATCHES,
        learning_rate=TEST_LEARNING_RATE
    )

    # Print a confirmation message that the optimizer was created.
    print(f"\nSuccessfully created a differentially private Adam optimizer:")
    # Print the type of the created object to verify it's the correct class.
    print(f"  Type: {type(dp_adam)}")

    # *** FIX APPLIED HERE ***
    # The internal '_dp_sum_query' object does not store the 'noise_multiplier' directly.
    # Instead, it uses it to calculate the standard deviation of the noise and stores that.
    # The attribute for the standard deviation is `_stddev`.
    print(f"  L2 Norm Clip: {dp_adam._dp_sum_query._l2_norm_clip}")
    print(f"  Noise Standard Deviation: {dp_adam._dp_sum_query._stddev}")

    # The number of microbatches is stored on the main optimizer object.
    print(f"  Number of Microbatches: {dp_adam._num_microbatches}")

    # The learning rate is a special TensorFlow variable, so we call `.numpy()` to see its numerical value.
    print(f"  Learning Rate: {dp_adam.learning_rate.numpy()}")

    # Print a final message explaining what to do next with the optimizer.
    print("\nThis optimizer can now be compiled with a Keras model to train it with differential privacy guarantees.")
