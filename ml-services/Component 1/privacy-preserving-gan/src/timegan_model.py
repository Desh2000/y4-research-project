"""
MANO Component 1: TimeGAN Model Architecture (PyTorch)
Contains the 4 core networks: Embedder, Recovery, Generator, Discriminator

This architecture is based on the paper "Time-series Generative Adversarial Networks" (Yoon et al., 2019).
It combines the benefits of GANs (generative power) with autoregressive models (temporal dynamics).
"""
import torch
import torch.nn as nn
import torch.nn.init as init


class TimeGAN(nn.Module):
    """
    TimeGAN Main Class
    Encapsulates the four distinct neural networks required for the framework.
    """

    def __init__(self, config):
        super(TimeGAN, self).__init__()
        # Configuration parameters extracted for easier access
        # Number of input features (e.g., 4: Sleep, HR, etc.)
        self.feature_dim = config.model.N_SIGNALS
        # Size of the internal memory state (Latent Space size)
        self.hidden_dim = config.model.HIDDEN_DIM
        # Depth of the networks (number of stacked GRUs)
        self.num_layers = config.model.NUM_LAYERS
        # Hardware device (CPU or CUDA GPU)
        self.device = config.training.DEVICE

        # --- 1. EMBEDDER NETWORK (Autoencoder Encoder) ---
        # GOAL: Map high-dimensional temporal features into a lower-dimensional "Latent Space".
        # WHY GRU?: Gated Recurrent Units capture time-dependencies (like LSTM) but are
        #           computationally lighter and faster to train for simple sensor data.
        self.embedder = nn.GRU(
            input_size=self.feature_dim,
            hidden_size=self.hidden_dim,
            num_layers=self.num_layers,
            # Standard PyTorch format: [Batch Size, Sequence Length, Features]
            batch_first=True
        )
        # Linear layer maps the GRU output to the exact latent dimension size
        self.embedder_out = nn.Linear(self.hidden_dim, self.hidden_dim)

        # --- 2. RECOVERY NETWORK (Autoencoder Decoder) ---
        # GOAL: Map the "Latent Space" back to the original feature space.
        # WHY?: This ensures the Embedder preserves vital information. If we can't reconstruct
        #       the original data from the latent code, the latent code is useless.
        self.recovery = nn.GRU(
            input_size=self.hidden_dim,
            hidden_size=self.hidden_dim,
            num_layers=self.num_layers,
            batch_first=True
        )
        self.recovery_out = nn.Linear(self.hidden_dim, self.feature_dim)

        # --- 3. GENERATOR NETWORK ---
        # GOAL: Take random noise (Z) and produce synthetic "Latent Codes".
        # WHY?: The Generator learns to mimic the Embedder's output, not the raw data directly.
        #       This "Latent GAN" approach is more stable for time-series.
        self.generator = nn.GRU(
            input_size=self.feature_dim,  # We use feature_dim size for noise input too
            hidden_size=self.hidden_dim,
            num_layers=self.num_layers,
            batch_first=True
        )
        self.generator_out = nn.Linear(self.hidden_dim, self.hidden_dim)

        # --- 4. SUPERVISOR NETWORK ---
        # GOAL: The "Teacher". It takes latent code at time (t) and predicts latent code at (t+1).
        # WHY?: Standard GANs ignore time. This network forces the Generator to learn
        #       "Physics" or "Causality" (e.g., poor sleep leads to high stress next day).
        self.supervisor = nn.GRU(
            input_size=self.hidden_dim,
            hidden_size=self.hidden_dim,
            num_layers=self.num_layers - 1,  # Typically 1 layer shallower than others
            batch_first=True
        )
        self.supervisor_out = nn.Linear(self.hidden_dim, self.hidden_dim)

        # --- 5. DISCRIMINATOR NETWORK ---
        # GOAL: The "Critic". It classifies sequences as Real or Fake.
        # WHY?: It provides the feedback signal (gradients) that trains the Generator.
        self.discriminator = nn.GRU(
            input_size=self.hidden_dim,
            hidden_size=self.hidden_dim,
            num_layers=self.num_layers,
            batch_first=True
        )
        # Output: Single probability score
        self.discriminator_out = nn.Linear(self.hidden_dim, 1)

        # Move model to GPU/CPU immediately
        self.to(self.device)
        # Apply specific weight initialization
        self._init_weights()

    def _init_weights(self):
        """
        Custom Weight Initialization
        WHY?: Neural networks are sensitive to starting random weights.
        'Xavier Normal' is optimal for Tanh/Sigmoid activations used in RNNs.
        It prevents the 'Vanishing Gradient' problem where the model stops learning early.
        """
        for m in self.modules():
            if isinstance(m, nn.Linear):
                init.xavier_normal_(m.weight)
                if m.bias is not None:
                    init.constant_(m.bias, 0)

    # --- Forward Passes ---
    # These methods define how data flows through each sub-network.

    def forward_embedder(self, X):
        """Input: Real Data -> Output: Latent Code"""
        H, _ = self.embedder(X)
        # WHY SIGMOID?: In data_preprocessor.py, we normalized data to [0, 1].
        # Sigmoid forces the neural network output to stay exactly in that [0, 1] range.
        # Tanh (-1 to 1) or ReLU (0 to inf) would produce invalid values.
        H = torch.sigmoid(self.embedder_out(H))
        return H

    def forward_recovery(self, H):
        """Input: Latent Code -> Output: Reconstructed Data"""
        X_tilde, _ = self.recovery(H)
        # WHY SIGMOID?: To match the input data range [0, 1].
        X_tilde = torch.sigmoid(self.recovery_out(X_tilde))
        return X_tilde

    def forward_generator(self, Z):
        """Input: Random Noise -> Output: Fake Latent Code"""
        E, _ = self.generator(Z)
        # WHY SIGMOID?: To mimic the Embedder's output range [0, 1].
        E = torch.sigmoid(self.generator_out(E))
        return E

    def forward_supervisor(self, H):
        """Input: Latent Code(t) -> Output: Latent Code(t+1)"""
        S, _ = self.supervisor(H)
        # WHY SIGMOID?: To maintain the latent space constraints [0, 1].
        S = torch.sigmoid(self.supervisor_out(S))
        return S

    def forward_discriminator(self, H):
        """Input: Latent Code -> Output: Real/Fake Score"""
        Y_hat, _ = self.discriminator(H)
        # WHY NO SIGMOID?: The loss function `BCEWithLogitsLoss` applies Sigmoid internally.
        # Doing it twice would break the math (numerical instability).
        # We output raw 'logits' here.
        Y_hat = self.discriminator_out(Y_hat)
        return Y_hat
