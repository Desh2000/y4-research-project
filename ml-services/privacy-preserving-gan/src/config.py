"""
Configuration Management for Privacy-Preserving Mental Health GAN System
This module centralizes all configuration settings for easy management and scalability.

Updated: November 24, 2025
Status: Production Ready
"""

import os
from pathlib import Path

# ==================== PROJECT PATHS ====================
# Current file: ml-services/privacy-preserving-gan/src/config.py
# Resolution chain:
# .resolve() = absolute path
# .parent = src
# .parent.parent = privacy-preserving-gan
# .parent.parent.parent = ml-services
# .parent.parent.parent.parent = y4-research-project (ROOT)

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent.parent
ML_SERVICES_ROOT = PROJECT_ROOT / "ml-services" / "privacy-preserving-gan"

# Data directories
DATA_DIR = PROJECT_ROOT / "data"
RAW_DATA_DIR = DATA_DIR / "raw"
PROCESSED_DATA_DIR = DATA_DIR / "processed"
SYNTHETIC_DATA_DIR = DATA_DIR / "synthetic"

# ML-Services directories
LOGS_DIR = ML_SERVICES_ROOT / "gan_logs"
CHECKPOINTS_DIR = LOGS_DIR / "checkpoints"
PLOTS_DIR = LOGS_DIR / "plots"
REPORTS_DIR = LOGS_DIR / "reports"
MODELS_DIR = LOGS_DIR / "models"

# Create all directories
for directory in [
    CHECKPOINTS_DIR, PLOTS_DIR, REPORTS_DIR, SYNTHETIC_DATA_DIR,
    PROCESSED_DATA_DIR, MODELS_DIR
]:
    directory.mkdir(parents=True, exist_ok=True)

# ==================== DATASET CONFIGURATION ====================
# Maps dataset keys to actual file paths and metadata
# All paths are verified against actual files on disk

DATASETS = {
    "DASS": {
        "name": "Depression Anxiety Stress Scales",
        "file_path": RAW_DATA_DIR / "Depression anxiety stress scales",
        "data_type": "tabular",
        "modality": "survey",
        "description": "DASS-21 standardized psychological assessment",
        "numerical_features": [
            # Depression (Q1A-Q1G)
            "Q1A", "Q1B", "Q1C", "Q1D", "Q1E", "Q1F", "Q1G",
            # Anxiety (Q2A-Q2G)
            "Q2A", "Q2B", "Q2C", "Q2D", "Q2E", "Q2F", "Q2G",
            # Stress (Q3A-Q3G)
            "Q3A", "Q3B", "Q3C", "Q3D", "Q3E", "Q3F", "Q3G",
        ]
    },

    "MENTAL_HEALTH_TECH": {
        "name": "Mental Health in Tech Survey",
        "file_path": RAW_DATA_DIR / "Mental Health in Tech Survey.csv",
        "processed_path": PROCESSED_DATA_DIR / "mental_health_tech_PROCESSED.csv",
        "synthetic_path": SYNTHETIC_DATA_DIR / "mental_health_tech_SYNTHETIC.csv",
        "data_type": "tabular",
        "modality": "survey",
        "description": "OSMI Tech Industry Mental Health Survey",
        "categorical_features": [
            "Gender", "Country", "self_employed", "family_history", "treatment",
            "work_interfere", "no_employees", "remote_work", "tech_company",
            "benefits", "care_options", "wellness_program", "seek_help",
            "anonymity", "leave", "mental_health_consequence",
            "phys_health_consequence", "coworkers", "supervisor"
        ],
        "numerical_features": ["Age"],
    },

    "SLEEP_HEALTH": {
        "name": "Sleep Health and Lifestyle Dataset",
        "file_path": RAW_DATA_DIR / "Sleep_health_and_lifestyle_dataset.csv",
        "processed_path": PROCESSED_DATA_DIR / "sleep_health_PROCESSED.csv",
        "synthetic_path": SYNTHETIC_DATA_DIR / "sleep_health_SYNTHETIC.csv",
        "data_type": "tabular",
        "modality": "wearable",
        "description": "Sleep metrics from wearable devices + lifestyle data",
        "numerical_features": [
            "Age", "Sleep_Duration", "Quality_of_Sleep", "Heart_Rate",
            "Daily_Steps", "Stress_Level"
        ],
        "categorical_features": ["Gender", "Sleep_Disorder"]
    },

    "WEARABLE_IOT": {
        "name": "Mental Health Monitor Using Wearable IoT Sensors",
        "file_path": RAW_DATA_DIR / "Mental Health Monitor Using Wearable IoT Sensors.csv",
        "processed_path": PROCESSED_DATA_DIR / "wearable_iot_PROCESSED.csv",
        "synthetic_path": SYNTHETIC_DATA_DIR / "wearable_iot_SYNTHETIC.csv",
        "data_type": "tabular",
        "modality": "wearable",
        "description": "IoT wearable sensor data for mental health monitoring",
    },

    "DEPRESSION_REDDIT": {
        "name": "Depression Dataset from Reddit",
        "file_path": RAW_DATA_DIR / "Depression Dataset from reddit.csv",
        "data_type": "text",
        "modality": "emotional_text",
        "description": "Emotional text posts from Reddit depression communities",
    },
}

# ==================== CTGAN TRAINER CONFIGURATION ====================
# Settings specifically for CTGAN model training
CTGAN_TRAINER_CONFIG = {
    "dataset_key": "MENTAL_HEALTH_TECH",  # Primary dataset
    "epochs": 600,
    "batch_size": 500,
    "verbose": True,
    "log_frequency": 50,  # Log every N epochs

    # CTGAN specific parameters
    "embedding_dim": 128,
    "generator_dim": (256, 256),
    "discriminator_dim": (256, 256),
    "generator_lr": 2e-4,
    "discriminator_lr": 2e-4,
    "discriminator_decay": 1e-6,
    "batch_normalization": True,
    "dropout_discriminator": 0.2,

    # Synthetic data generation
    "synthetic_samples": 10000,  # Number of synthetic samples to generate
    "sampling_seed": 42,
}

# ==================== VALIDATION CONFIGURATION ====================
# Settings for synthetic data validation
VALIDATION_CONFIG = {
    "test_size": 0.2,
    "val_size": 0.1,
    "random_state": 42,

    # Statistical metrics
    "statistical_metrics": [
        "ks_complement",           # Kolmogorov-Smirnov
        "tv_complement",           # Total Variation
        "correlation_similarity",  # Feature correlation preservation
    ],

    # Quality thresholds
    "quality_thresholds": {
        "minimum_quality_score": 0.70,  # 70% minimum
        "minimum_correlation_similarity": 0.85,  # 85% similarity in correlations
    },

    # Visualization settings
    "plot_heatmap": True,
    "plot_distributions": True,
    "num_distributions_to_plot": 10,  # Show top 10 features
}

# ==================== DIFFERENTIAL PRIVACY CONFIGURATION ====================
# Privacy budget and DP-SGD settings for future use
DP_CONFIG = {
    "enabled": False,  # Enable when implementing DP-CTGAN
    "l2_norm_clip": 1.0,
    "noise_multiplier": 1.1,
    "num_microbatches": 1,
    "delta": 1e-5,
    "target_epsilon": 5.0,
}

# ==================== MULTI-MODAL CONFIGURATION ====================
# Roadmap for multi-modal synthetic data generation
MULTIMODAL_CONFIG = {
    "modalities": {
        "tabular": {
            "enabled": True,
            "generator_type": "CTGAN",
            "status": "✅ IMPLEMENTED (Phase 1)",
        },
        "time_series": {
            "enabled": False,
            "generator_type": "TimeGAN",
            "status": "⏳ PLANNED (Phase 2)",
            "sequence_length": 168,  # 7 days * 24 hours
            "output_features": ["heart_rate", "sleep", "activity", "steps", "calories"],
        },
        "text": {
            "enabled": False,
            "generator_type": "SeqGAN",
            "status": "⏳ PLANNED (Phase 3)",
            "max_length": 200,
            "vocab_size": 5000,
        },
    },
    "unified_discriminator": False,  # Will enable in Phase 3
}

# ==================== DEVICE & PERFORMANCE CONFIGURATION ====================
DEVICE_CONFIG = {
    "use_gpu": True,
    "gpu_memory_fraction": 0.8,
    "allow_memory_growth": True,
    "mixed_precision": False,  # Enable for faster training (fp16)
}

# ==================== LOGGING CONFIGURATION ====================
LOGGING_CONFIG = {
    "log_level": "INFO",
    "log_file": LOGS_DIR / "gan_training.log",
    "console_output": True,

    # Checkpoint settings
    "save_checkpoints": True,
    "checkpoint_frequency": 50,  # Save every N epochs

    # Visualization settings
    "save_plots": True,
    "plot_frequency": 50,

    # Report generation
    "generate_reports": True,
    "report_frequency": 100,
}

# ==================== FEEDBACK LOOP CONFIGURATION (PHASE 4) ====================
FEEDBACK_LOOP_CONFIG = {
    "enabled": False,
    "max_feedback_cycles": 5,
    "quality_threshold": 0.75,
    "gap_identification_method": "clustering",
}

# ==================== API CONFIGURATION (PHASE 3) ====================
API_CONFIG = {
    "flask_port": 5000,
    "fastapi_port": 8000,
    "enable_authentication": False,
    "enable_rate_limiting": False,
    "max_requests_per_minute": 100,
}

# ==================== DEBUG & DEVELOPMENT ====================
DEBUG_MODE = False
VERBOSE = True
RANDOM_SEED = 42
