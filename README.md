Manō: Privacy-Preserving Synthetic Mental Health AI & Adaptive Intervention Engine"Solving the Mental Health Data Scarcity Crisis with Generative Digital Twins."Manō is an end-to-end Generative AI ecosystem that creates high-fidelity synthetic patient data, predicts mental health risks with clinical precision, and autonomously simulates personalized therapeutic interventions using Deep Reinforcement Learning. It allows researchers to develop and test medical AI algorithms without ever touching sensitive PII (Personally Identifiable Information).📑 Table of ContentsArchitectureKey ComponentsTechnical InnovationsPerformance BenchmarksInstallationUsage GuideProject Structure🏗️ System ArchitectureThe system operates as a closed-loop "Digital Twin" pipeline, moving from data generation to active intervention.graph TD
    subgraph "Phase 1: Synthetic Generation"
    A[Real Data Sources] -->|Cleaning| B(Component 1: Generators)
    B -->|CTGAN| C[Synthetic Static Profiles]
    B -->|TimeGAN| D[Synthetic 7-Day Rhythms]
    end

    subgraph "Phase 2: Prediction"
    C & D -->|Data Fusion| E{Labeled Synthetic Dataset}
    E --> F[Component 2: Hybrid LSTM Predictor]
    end

    subgraph "Phase 3: Intervention (AMISE)"
    F -->|Risk Score| G[RL Agent (PPO)]
    G -->|Action: Treatment + Intensity| H[Seq2Seq World Model]
    H -->|Simulated Outcome| F
    end
🧩 Key Components1. The Generator (Component 1)Static Engine (CTGAN): Uses Variational Gaussian Mixtures to model complex, multi-modal demographic distributions (Age, Gender, Job).Dynamic Engine (TimeGAN): A 4-network architecture (Embedder, Recovery, Generator, Supervisor) that learns the temporal "physics" of biological rhythms (Sleep, Heart Rate, Stress) over 7 days.2. The Predictor (Component 2)Hybrid LSTM: A Dual-Branch neural network that fuses static demographics (Dense layers) with temporal sequences (Stacked LSTM).Capabilities: Predicts High/Medium/Low mental health risk with 96% accuracy using weighted Cross-Entropy Loss to handle class imbalance.3. The Intervenor (Component 3 - AMISE)World Model: An Attention-based Seq2Seq network trained via Model Distillation to simulate the physiological effects of treatments (e.g., CBT, Medication).AI Doctor: A Proximal Policy Optimization (PPO) agent with a Dual-Head Actor (Discrete Action + Continuous Intensity) that learns to prescribe the minimum effective dose to cure patients.🛠️ Technical InnovationsThis project followed a rigorous R&D lifecycle involving pivots based on empirical failure modes.📉 Phase 1 Pivot: Solving Mode CollapseProblem: Vanilla GANs failed to generate realistic survey data (generated continuous values for discrete categories).Solution: Migrated to CTGAN, utilizing mode-specific normalization to handle non-Gaussian distributions perfectly.⏱️ Phase 2 Optimization: TimeGAN StabilizationProblem: Standard TimeGAN training is notoriously unstable.Solution: Implemented a 3-Phase Training Loop (Embedding → Supervisor → Joint) and added a custom Moments Matching Loss (Mean/Std) to prevent spectral collapse.🏥 Phase 3 Innovation: Hybrid Action SpaceProblem: Medical treatments aren't just "Type" (What to do), but "Intensity" (How much). Standard RL agents struggle with mixed action spaces.Solution: Designed a custom Dual-Head PPO Actor that simultaneously outputs a Categorical distribution (Treatment Type) and a Gaussian distribution (Intensity), allowing for precise dosage optimization.📊 Performance BenchmarksHardware: ASUS ROG G15 (Ryzen 9 5900HX, RTX 3050 Ti 4GB)ComponentMetricResultInterpretationCTGANColumn Shape Score90.05%Synthetic demographics are statistically identical to real populations.TimeGANDistribution Score83.85%Synthetic biological rhythms preserve realistic variance and trends.Hybrid LSTMF1-Score (High Risk)0.98The model detects 98% of high-risk cases with near-zero false negatives.PPO AgentAvg Reward (Ep 5000)8.42The AI learned to cure patients efficiently without over-prescribing.💻 InstallationPrerequisitesPython 3.10+NVIDIA GPU with CUDA 11.8 or 12.1 (Highly Recommended)Setup StepsClone the Repository:git clone [https://github.com/yourusername/mano-project.git](https://github.com/yourusername/mano-project.git)
cd mano-project
Create Virtual Environment:python -m venv venv
# Windows
.\venv\Scripts\activate
# Linux/Mac
source venv/bin/activate
Install Dependencies:Crucial: Install PyTorch with CUDA support first to avoid CPU bottlenecks.pip install torch torchvision torchaudio --index-url [https://download.pytorch.org/whl/cu121](https://download.pytorch.org/whl/cu121)
pip install -r requirements.txt
⏯️ Usage Guide1. End-to-End Demo (The "Magic" Button)To see the entire pipeline generate a patient, diagnose them, prescribe treatment, and simulate the outcome:python run_pipeline.py
2. Manual Reproduction (Step-by-Step)Step A: Generate Synthetic Data# Train TimeGAN & Generate 10k Sequences (GPU Accelerated)
python ml-services/privacy-preserving-gan/src/timegan_main.py

# Fuse Static & Dynamic Data into Labeled Dataset
python ml-services/privacy-preserving-lstm/src/data_fusion.py
Step B: Train Predictor# Train Hybrid LSTM with Stratified Splits
python ml-services/privacy-preserving-lstm/src/lstm_main.py --mode train --epochs 50
Step C: Train Intervention Engine# Generate Virtual Clinical Trials (Data Augmentation)
python ml-services/intervention-simulation/src/intervention_data_prep.py

# Train Seq2Seq World Model (Mixed Precision)
python ml-services/intervention-simulation/src/seq2seq_trainer.py

# Train PPO Agent (Reinforcement Learning)
python ml-services/intervention-simulation/src/rl_trainer.py
📂 Project Structuremano-project/
├── data/                       # Data Artifacts (Excluded from Git)
│   ├── raw/                    # Original DASS/Sleep datasets
│   └── synthetic/              # Generated .npz datasets (10k patients)
├── ml-services/
│   ├── privacy-preserving-gan/ # COMPONENT 1: GENERATORS
│   │   ├── config/             # CTGAN/TimeGAN Configs
│   │   └── src/                # Generator Source Code (PyTorch)
│   ├── privacy-preserving-lstm/# COMPONENT 2: PREDICTOR
│   │   ├── config/             # LSTM Hyperparameters
│   │   └── src/                # Hybrid Network & Trainer
│   └── intervention-simulation/# COMPONENT 3: AMISE
│       ├── config/             # RL & Simulation Rules
│       └── src/                # PPO Agent & Seq2Seq Simulator
├── requirements.txt            # Dependency list
└── run_pipeline.py             # Main Execution Entry Point
📜 LicenseThis project is licensed under the MIT License - see the LICENSE file for details.🤝 AcknowledgmentsOriginal Papers: CTGAN (Xu et al., 2019), TimeGAN (Yoon et al., 2019), PPO (Schulman et al., 2017).
