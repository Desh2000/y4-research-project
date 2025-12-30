Manō: Privacy-Preserving Synthetic Mental Health Data Generation and Adaptive Mutlimodal Intervention Simulation Engine

Manō (Component 1 ) is an end-to-end generative AI ecosystem designed to solve the critical data scarcity problem in mental health research. It generates high-fidelity synthetic patient data, predicts mental health risks with clinical precision, and autonomously simulates therapeutic interventions using Deep Reinforcement Learning.

🏗️ System Architecture

The system operates as a closed-loop "Digital Twin" pipeline:

graph TD
    A[Real Data Sources] -->|Pre-processing| B(Component 1: Generators)
    B -->|CTGAN| C[Synthetic Static Profiles]
    B -->|TimeGAN| D[Synthetic 7-Day Rhythms]
    C & D -->|Data Fusion| E{Labeled Synthetic Dataset}
    E --> F[Component 2: Hybrid LSTM Predictor]
    F -->|Risk Score| G[Component 3: AMISE Intervention Engine]
    G -->|Seq2Seq Simulation| H[Projected Health Outcomes]
    H -->|RL Optimization| I[Personalized Treatment Plan]


🚀 Key Features

1. Synthetic Data Generation (Privacy-First)

Static Generator (CTGAN): Generates demographic profiles (Age, Gender, Job) preserving complex categorical correlations.

Metric: 90% Column Shape Similarity.

Dynamic Generator (TimeGAN): Generates 7-day longitudinal biological rhythms (Sleep, Heart Rate, Stress).

Metric: 0.8385 Distribution Score (KS-Test).

Result: A dataset of 10,000 synthetic patients that statistically mirrors reality but contains zero PII (Personally Identifiable Information).

2. Risk Prediction (Hybrid LSTM)

Architecture: Dual-Branch Neural Network fusing temporal (LSTM) and static (Dense) features.

Performance: Achieved 0.98 F1-Score for High-Risk detection on held-out test data.

Safety: Implements Class Balancing to prioritize high-risk detection.

3. AMISE (The AI Doctor)

World Model: An Attention-based Seq2Seq Simulator that predicts future health states based on interventions.

Agent: A PPO (Proximal Policy Optimization) Reinforcement Learning agent.

Capability: Prescribes optimal treatments (e.g., CBT Therapy) and intensities (e.g., 80% dosage) to minimize patient risk while minimizing intervention cost.

🛠️ Technical Evolution & Decisions

This project followed a rigorous R&D lifecycle involving pivots based on empirical failure modes.

Phase 1: The Static Generator

Initial Failure: We first attempted a Vanilla GAN for survey data. It suffered severe Mode Collapse (generating continuous values for discrete categories).

The Pivot: We migrated to CTGAN (Conditional Tabular GAN).

Why? CTGAN uses Variational Gaussian Mixtures (VGM) to handle multi-modal distributions, solving the mode collapse issue.

Phase 2: The Time-Series Bridge

Challenge: Lack of longitudinal datasets linking surveys to wearables.

Solution: Developed a Gaussian Noise Injection Pipeline. We transformed static averages into 7-day sequences ($N=374 \to N=10,000$) to seed the TimeGAN training.

Phase 3: TimeGAN Implementation

Optimization: Standard TimeGAN is unstable. We implemented a 3-Phase Training Loop (Embedding $\to$ Supervisor $\to$ Joint) using PyTorch.

Loss Engineering: Added a custom Moments Matching Loss (Mean/Std) to prevent spectral collapse.

Result: The Discriminator reached Nash Equilibrium (D_loss ≈ 1.38).

Phase 5: The Intervention Engine

Architecture: We rejected simple Rule-Based logic for a Dual-Head Actor-Critic architecture.

Innovation: The Agent creates continuous ("Intensity") and discrete ("Treatment Type") actions simultaneously.

Optimization: Utilized Mixed Precision Training (AMP) to train the Seq2Seq simulator on a 4GB VRAM GPU.

💻 Installation

Prerequisites

Python 3.10+

NVIDIA GPU (Recommended: RTX 3050 Ti or better) with CUDA 11.8/12.1

Setup

Clone the Repository:

git clone [https://github.com/yourusername/mano-project.git](https://github.com/yourusername/mano-project.git)
cd mano-project


Install Dependencies:

pip install -r requirements.txt


Note: Ensure PyTorch is installed with CUDA support:

pip install torch torchvision torchaudio --index-url [https://download.pytorch.org/whl/cu121](https://download.pytorch.org/whl/cu121)


⏯️ Usage Guide

1. End-to-End Demo (The "Magic" Button)

To see the entire pipeline generate a patient, diagnose them, and cure them:

python run_pipeline.py


2. Training the Modules (Manual Reproduction)

Step A: Generate Synthetic Data

# Train TimeGAN & Generate 10k Sequences
python ml-services/privacy-preserving-gan/src/timegan_main.py
# Fuse Static & Dynamic Data
python ml-services/privacy-preserving-lstm/src/data_fusion.py


Step B: Train Predictor

# Train Hybrid LSTM
python ml-services/privacy-preserving-lstm/src/lstm_main.py --mode train --epochs 50


Step C: Train Intervention Engine

# Generate Virtual Clinical Trials
python ml-services/intervention-simulation/src/intervention_data_prep.py
# Train Seq2Seq World Model
python ml-services/intervention-simulation/src/seq2seq_trainer.py
# Train PPO Agent
python ml-services/intervention-simulation/src/rl_trainer.py


📊 Performance Benchmarks

Hardware: ASUS ROG G15 (Ryzen 9 5900HX, RTX 3050 Ti 4GB)

Component

Metric

Result

Training Time

CTGAN

Column Shape Score

90.05%

~10 mins

TimeGAN

Distribution Score

83.85%

~5 mins (GPU)

Hybrid LSTM

F1-Score (High Risk)

0.98

~2 mins

PPO Agent

Avg Reward (Ep 5000)

8.42

~45 mins

📂 Project Structure

mano-project/
├── data/                       # Storage for Raw & Synthetic artifacts
│   ├── raw/                    # Original DASS/Sleep datasets
│   └── synthetic/              # The 10k generated patients (.npz)
├── ml-services/
│   ├── privacy-preserving-gan/ # Component 1 (Generators)
│   │   ├── config/             # CTGAN/TimeGAN Configs
│   │   └── src/                # Generator Source Code
│   ├── privacy-preserving-lstm/# Component 2 (Predictor)
│   │   ├── config/             # LSTM Hyperparameters
│   │   └── src/                # Hybrid Network & Trainer
│   └── intervention-simulation/# Component 3 (AMISE)
│       ├── config/             # RL & Simulation Rules
│       └── src/                # PPO Agent & Seq2Seq Simulator
└── run_pipeline.py             # Main Execution Entry Point



📜 License
This project is licensed under the MIT License - see the LICENSE file for details.
