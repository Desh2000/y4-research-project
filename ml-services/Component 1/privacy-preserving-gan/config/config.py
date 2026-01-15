"""
Configuration Management for Privacy-Preserving Mental Health GAN System
This module centralizes all configuration settings for easy management and scalability.

Directory Structure:
ml-services/privacy-preserving-gan/
├── config/
│   └── config.py  (THIS FILE)
├── src/
│   ├── gan_model.py
│   ├── data_loader.py
│   └── ... (other source files)
└── gan_logs/

Updated: November 30, 2025
Status: Production Ready
"""

import os
from pathlib import Path

# ==================== PROJECT PATHS ====================
# Current file: ml-services/privacy-preserving-gan/config/config.py
# Resolution chain:
# .resolve() = absolute path
# .parent = config
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
for directory in [CHECKPOINTS_DIR, PLOTS_DIR, REPORTS_DIR, SYNTHETIC_DATA_DIR, PROCESSED_DATA_DIR, MODELS_DIR]:
    directory.mkdir(parents=True, exist_ok=True)

# ==================== HARDWARE CONFIGURATION ====================
# ASUS ROG G15 G513QE Specifications
HARDWARE_CONFIG = {
    "laptop_model": "ASUS ROG G15 G513QE",
    "ram_gb": 16,
    "cpu": "Ryzen 9 5900HX (12 cores)",
    "gpu": "RTX 3050 Ti (4GB VRAM)",
    "secondary_gpu": "AMD Radeon Graphics",
    "vram_gb": 4,
    "vram_available_for_training": 3.5,
}

# ==================== DATASET CONFIGURATION ====================

DATASETS = {
    "DASS": {
        "name": "Depression Anxiety Stress Scales",
        "file_path": RAW_DATA_DIR / "Depression anxiety stress scales",
        "data_type": "tabular",
        "modality": "survey",
        "description": "DASS-21 standardized psychological assessment",
    },

    "MENTAL_HEALTH_TECH": {
        "name": "Mental Health in Tech Survey",
        "file_path": RAW_DATA_DIR / "Mental Health in Tech Survey.csv",
        # CORRECTED: Matches output from data_unification.py
        "processed_path": PROCESSED_DATA_DIR / "mental_health_tech_survey_PROCESSED.csv",
        # CORRECTED: Matches output from production_ctgan_trainer.py
        "synthetic_path": SYNTHETIC_DATA_DIR / "synthetic_mental_health_data_v1.csv",
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
        "description": "Sleep metrics from wearable devices and lifestyle data",
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

CTGAN_TRAINER_CONFIG = {
    "dataset_key": "MENTAL_HEALTH_TECH",
    "epochs": 600,
    "batch_size": 500,
    "verbose": True,
    "log_frequency": 50,
    "embedding_dim": 128,
    "generator_dim": (256, 256),
    "discriminator_dim": (256, 256),
    "generator_lr": 2e-4,
    "discriminator_lr": 2e-4,
    "discriminator_decay": 1e-6,
    "batch_normalization": True,
    "dropout_discriminator": 0.2,
    "synthetic_samples": 10000,
    "sampling_seed": 42,
}

# ==================== VALIDATION CONFIGURATION ====================

VALIDATION_CONFIG = {
    "test_size": 0.2,
    "val_size": 0.1,
    "random_state": 42,
    "statistical_metrics": [
        "ks_complement",
        "tv_complement",
        "correlation_similarity",
    ],
    "quality_thresholds": {
        "minimum_quality_score": 0.70,
        "minimum_correlation_similarity": 0.85,
    },
    "plot_heatmap": True,
    "plot_distributions": True,
    "num_distributions_to_plot": 10,
}

# ==================== DIFFERENTIAL PRIVACY CONFIGURATION ====================

DP_CONFIG = {
    "enabled": False,
    "l2_norm_clip": 1.0,
    "noise_multiplier": 1.1,
    "num_microbatches": 1,
    "delta": 1e-5,
    "target_epsilon": 5.0,
}

# ==================== MULTI-MODAL CONFIGURATION ====================

MULTIMODAL_CONFIG = {
    "architecture_choice": "B_separate_gans_with_fusion",
    "modalities": {
        "tabular": {
            "enabled": True,
            "generator_type": "CTGAN",
            "status": "IMPLEMENTED (Phase 1)",
            "vram_estimate_mb": 800,
        },
        "time_series": {
            "enabled": False,
            "generator_type": "TimeGAN",
            "status": "PLANNED (Phase 2)",
            "sequence_length": 168,
            "output_features": ["heart_rate", "sleep", "activity", "steps", "calories"],
            "vram_estimate_mb": 600,
        },
        "text": {
            "enabled": False,
            "generator_type": "SeqGAN",
            "status": "PLANNED (Phase 3)",
            "max_length": 200,
            "vocab_size": 5000,
            "vram_estimate_mb": 1000,
        },
    },
    "fusion_layer": {
        "enabled": False,
        "fusion_type": "concatenation_with_attention",
        "vram_estimate_mb": 200,
    },
}

# ==================== INTERVENTION SIMULATION CONFIGURATION ====================

INTERVENTION_CONFIG = {
    "approach": "C_hybrid_rules_and_learned",
    "intervention_types": [
        "CBT",
        "Medication",
        "Exercise",
        "Mindfulness",
        "Crisis",
    ],
    "simulation_duration_weeks": [1, 2, 4, 8, 12],
    "enable_causal_inference": True,
    "enable_rl_optimization": True,
    "rules_based_effects": {
        "CBT": {"anxiety_reduction": 0.30, "depression_reduction": 0.25},
        "Medication": {"anxiety_reduction": 0.40, "depression_reduction": 0.35},
        "Exercise": {"anxiety_reduction": 0.20, "depression_reduction": 0.22},
        "Mindfulness": {"anxiety_reduction": 0.15, "stress_reduction": 0.25},
    },
}

# ==================== EVALUATION PRIORITY ====================

EVALUATION_PRIORITY = {
    "rank_1": "C_novelty_intervention_simulation",
    "rank_2": "B_utility_downstream_task_performance",
    "rank_3": "D_integration_component_feeding",
    "rank_4": "A_privacy_guarantees_mathematical_rigor",
    "defense_focus": "Intervention simulation novelty demonstrates research contribution",
}

# ==================== DEVICE & PERFORMANCE CONFIGURATION ====================

DEVICE_CONFIG = {
    "use_gpu": True,
    "gpu_memory_fraction": 0.875,
    "allow_memory_growth": True,
    "mixed_precision": True,
    "max_batch_size_for_4gb_vram": 64,
    "recommended_batch_size_for_safety": 32,
}

# ==================== LOGGING CONFIGURATION ====================

LOGGING_CONFIG = {
    "log_level": "INFO",
    "log_file": LOGS_DIR / "gan_training.log",
    "console_output": True,
    "save_checkpoints": True,
    "checkpoint_frequency": 50,
    "save_plots": True,
    "plot_frequency": 50,
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
