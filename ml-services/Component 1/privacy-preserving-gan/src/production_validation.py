"""
Production-Ready Validation Script for Synthetic Data Quality
Validates CTGAN-generated synthetic mental health data against real data.

Uses SDMetrics QualityReport for comprehensive statistical assessment.

Author: Privacy-Preserving Mental Health Simulation Component
Date: November 30, 2025
Status: Production Ready
"""

import sys
import os
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from pathlib import Path
from datetime import datetime
import logging

# --- CRITICAL FIX FOR WINDOWS TERMINAL ---
# Forces Python to use UTF-8 for printing emojis (✅, ❌)
# Without this, the script will crash on Windows PowerShell
sys.stdout.reconfigure(encoding='utf-8')

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        # Force UTF-8 for the file so emojis save correctly
        logging.FileHandler('validation_execution.log', encoding='utf-8'),
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)

# ==================== 1. SYSTEM SETUP & CONFIG IMPORT ====================

logger.info("=" * 80)
logger.info(
    "PRODUCTION VALIDATION: Synthetic Mental Health Data Quality Assessment")
logger.info("=" * 80)

# Add config path
current_dir = os.getcwd()
# Points to 'ml-services/privacy-preserving-gan/config'
config_path = os.path.join(current_dir, 'ml-services',
                           'privacy-preserving-gan', 'config')
sys.path.append(config_path)

try:
    import config
    logger.info("OK: Successfully imported system configuration")
    logger.info(f"   Project Root: {config.PROJECT_ROOT}")
    logger.info(f"   Data Directory: {config.DATA_DIR}")
    logger.info(f"   Hardware: {config.HARDWARE_CONFIG['laptop_model']}")
except ImportError as e:
    logger.error(f"ERROR: Could not import 'config.py'")
    logger.error(f"   Expected location: {config_path}/config.py")
    logger.error(f"   Error: {e}")
    sys.exit(1)

# Import SDMetrics
try:
    from sdmetrics.reports.single_table import QualityReport
    logger.info("OK: SDMetrics library imported successfully")
except ImportError:
    logger.error("ERROR: SDMetrics not installed. Run: pip install sdmetrics")
    sys.exit(1)


# ==================== 2. DATA LOADING FUNCTIONS ====================

def load_real_data(dataset_key="MENTAL_HEALTH_TECH"):
    """Load real (processed) data from config path."""
    logger.info(f"\n--- Loading Real Data: {dataset_key} ---")

    dataset_info = config.DATASETS[dataset_key]
    real_path = dataset_info.get("processed_path")

    if real_path is None:
        logger.error(
            f"ERROR: No 'processed_path' defined for {dataset_key} in config")
        return None

    logger.info(f"   Path: {real_path}")

    if not real_path.exists():
        logger.error(f"ERROR: Real data file not found at: {real_path}")
        logger.info("   Have you run the data preprocessing step?")
        return None

    try:
        real_data = pd.read_csv(real_path)
        logger.info(
            f"OK: Loaded real data: {real_data.shape[0]} rows x {real_data.shape[1]} columns")
        return real_data
    except Exception as e:
        logger.error(f"ERROR: {e}")
        return None


def load_synthetic_data(dataset_key="MENTAL_HEALTH_TECH"):
    """Load synthetic data from config path."""
    logger.info(f"\n--- Loading Synthetic Data: {dataset_key} ---")

    dataset_info = config.DATASETS[dataset_key]
    synthetic_path = dataset_info.get("synthetic_path")

    if synthetic_path is None:
        logger.error(
            f"ERROR: No 'synthetic_path' defined for {dataset_key} in config")
        return None

    logger.info(f"   Path: {synthetic_path}")

    if not synthetic_path.exists():
        logger.error(
            f"ERROR: Synthetic data file not found at: {synthetic_path}")
        logger.info("   Have you run production_ctgan_trainer.py?")
        return None

    try:
        synthetic_data = pd.read_csv(synthetic_path)
        logger.info(
            f"OK: Loaded synthetic data: {synthetic_data.shape[0]} rows x {synthetic_data.shape[1]} columns")
        return synthetic_data
    except Exception as e:
        logger.error(f"ERROR: {e}")
        return None


# ==================== 3. DATA PREPROCESSING ====================

def get_metadata(dataset_key="MENTAL_HEALTH_TECH"):
    """Build SDMetrics metadata dictionary from config."""
    logger.info(f"\n--- Building Metadata for {dataset_key} ---")

    dataset_info = config.DATASETS[dataset_key]
    metadata = {
        'columns': {}
    }

    # Add categorical features
    categorical_features = dataset_info.get("categorical_features", [])
    for col in categorical_features:
        metadata['columns'][col] = {'sdtype': 'categorical'}

    # Add numerical features
    numerical_features = dataset_info.get("numerical_features", [])
    for col in numerical_features:
        metadata['columns'][col] = {'sdtype': 'numerical'}

    logger.info(f"OK: Metadata built: {len(metadata['columns'])} columns")
    logger.info(f"   Categorical: {len(categorical_features)}")
    logger.info(f"   Numerical: {len(numerical_features)}")

    return metadata


def ensure_column_consistency(real_data, synthetic_data):
    """Ensure real and synthetic data have same columns."""
    logger.info("\n--- Ensuring Column Consistency ---")

    real_cols = set(real_data.columns)
    synth_cols = set(synthetic_data.columns)

    if real_cols != synth_cols:
        logger.warning("WARNING: Column mismatch detected!")
        logger.warning(
            f"   Columns in real but not synthetic: {real_cols - synth_cols}")
        logger.warning(
            f"   Columns in synthetic but not real: {synth_cols - real_cols}")

        # Keep only common columns
        common_cols = real_cols & synth_cols
        logger.info(f"   Using {len(common_cols)} common columns")

        real_data = real_data[list(common_cols)]
        synthetic_data = synthetic_data[list(common_cols)]
    else:
        logger.info("OK: Column consistency verified")

    return real_data, synthetic_data


# ==================== 4. VISUALIZATION FUNCTIONS ====================

def plot_correlation_comparison(real_data, synthetic_data, dataset_key="MENTAL_HEALTH_TECH"):
    """Generate and save correlation heatmap comparison."""
    logger.info("\n--- Generating Correlation Heatmaps ---")

    try:
        # Encode categorical columns numerically for correlation
        real_encoded = real_data.copy()
        synth_encoded = synthetic_data.copy()

        for col in real_encoded.columns:
            if real_encoded[col].dtype == 'object':
                real_encoded[col] = pd.Categorical(real_encoded[col]).codes
                synth_encoded[col] = pd.Categorical(synth_encoded[col]).codes

        # Create figure
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(20, 8))

        # Real data correlation
        sns.heatmap(
            real_encoded.corr(),
            ax=ax1,
            cmap='coolwarm',
            vmin=-1,
            vmax=1,
            square=True,
            cbar_kws={'label': 'Correlation'},
            annot=False
        )
        ax1.set_title(
            f"Real Data - Correlation Matrix ({real_data.shape[0]} samples)", fontsize=14, fontweight='bold')

        # Synthetic data correlation
        sns.heatmap(
            synth_encoded.corr(),
            ax=ax2,
            cmap='coolwarm',
            vmin=-1,
            vmax=1,
            square=True,
            cbar_kws={'label': 'Correlation'},
            annot=False
        )
        ax2.set_title(
            f"Synthetic Data - Correlation Matrix ({synthetic_data.shape[0]} samples)", fontsize=14, fontweight='bold')

        # Save
        save_path = config.PLOTS_DIR / \
            f"correlation_comparison_{dataset_key}.png"
        plt.tight_layout()
        plt.savefig(save_path, dpi=300, bbox_inches='tight')
        logger.info(f"OK: Correlation plot saved: {save_path}")
        plt.close()

    except Exception as e:
        logger.error(f"ERROR: {e}")


def plot_distribution_comparison(real_data, synthetic_data, dataset_key="MENTAL_HEALTH_TECH", n_features=6):
    """Plot distribution comparison for numerical features."""
    logger.info(
        f"\n--- Generating Distribution Plots (top {n_features} features) ---")

    try:
        # Get numerical columns
        numerical_cols = real_data.select_dtypes(
            include=[np.number]).columns[:n_features]

        fig, axes = plt.subplots(
            len(numerical_cols), 1, figsize=(12, 4 * len(numerical_cols)))
        if len(numerical_cols) == 1:
            axes = [axes]

        for idx, col in enumerate(numerical_cols):
            axes[idx].hist(real_data[col].dropna(), bins=30,
                           alpha=0.6, label='Real', color='blue')
            axes[idx].hist(synthetic_data[col].dropna(), bins=30,
                           alpha=0.6, label='Synthetic', color='orange')
            axes[idx].set_title(f"Distribution: {col}", fontweight='bold')
            axes[idx].set_xlabel("Value")
            axes[idx].set_ylabel("Frequency")
            axes[idx].legend()
            axes[idx].grid(alpha=0.3)

        # Save
        save_path = config.PLOTS_DIR / f"distributions_{dataset_key}.png"
        plt.tight_layout()
        plt.savefig(save_path, dpi=300, bbox_inches='tight')
        logger.info(f"OK: Distribution plots saved: {save_path}")
        plt.close()

    except Exception as e:
        logger.error(f"ERROR: {e}")


# ==================== 5. QUALITY VALIDATION ====================

def validate_quality(real_data, synthetic_data, metadata, dataset_key="MENTAL_HEALTH_TECH"):
    """Run SDMetrics QualityReport validation."""
    logger.info("\n--- Running Statistical Quality Validation (SDMetrics) ---")

    try:
        logger.info("Initializing QualityReport...")
        report = QualityReport()

        logger.info("Computing quality metrics (this may take a moment)...")
        report.generate(real_data, synthetic_data, metadata)

        # Get overall score
        score = report.get_score()
        logger.info(f"\nOVERALL QUALITY SCORE: {score:.2%}")

        # Interpret score
        if score >= 0.90:
            logger.info("   Rating: EXCELLENT - Production-ready")
        elif score >= 0.80:
            logger.info("   Rating: VERY GOOD - High quality")
        elif score >= 0.70:
            logger.info("   Rating: GOOD - Acceptable")
        elif score >= 0.50:
            logger.info("   Rating: FAIR - Needs improvement")
        else:
            logger.warning("   Rating: POOR - Requires retraining")

        # Get detailed properties
        logger.info("\n--- Detailed Quality Metrics ---")
        properties = report.get_properties()
        # Handle properties output properly if it's a dataframe or dict
        if hasattr(properties, 'to_string'):
            logger.info(f"\n{properties.to_string()}")
        else:
            logger.info(f"\n{properties}")

        # Save report
        save_path = config.REPORTS_DIR / f"validation_report_{dataset_key}.pkl"
        report.save(save_path)
        logger.info(f"\nOK: Full validation report saved: {save_path}")

        return score, report

    except Exception as e:
        logger.error(f"ERROR: {e}")
        import traceback
        traceback.print_exc()
        return None, None


# ==================== 6. SUMMARY REPORT ====================

def generate_summary_report(real_data, synthetic_data, quality_score, dataset_key="MENTAL_HEALTH_TECH"):
    """Generate human-readable summary report."""
    logger.info("\n" + "=" * 80)
    logger.info("VALIDATION SUMMARY REPORT")
    logger.info("=" * 80)

    report_text = f"""
========================================================================
SYNTHETIC DATA QUALITY VALIDATION REPORT
========================================================================

Dataset: {dataset_key}
Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}

DATA STATISTICS:
Real Data Shape:       {real_data.shape[0]} rows x {real_data.shape[1]} columns
Synthetic Data Shape:  {synthetic_data.shape[0]} rows x {synthetic_data.shape[1]} columns
Missing Values (Real):       {real_data.isnull().sum().sum()}
Missing Values (Synthetic): {synthetic_data.isnull().sum().sum()}

QUALITY ASSESSMENT:
Overall Quality Score: {quality_score:.2%}
Status: {"PASSED" if quality_score >= 0.70 else "FAILED"}
Threshold: 70% (Minimum)
Recommendation: {"Ready for deployment" if quality_score >= 0.80 else "Consider retraining" if quality_score >= 0.70 else "Requires retraining"}

NEXT STEPS:
"""

    if quality_score >= 0.80:
        report_text += """1. Deploy synthetic data to other components
2. Begin Phase 2: Time-Series Data Generation (TimeGAN)
3. Prepare for multi-modal expansion with fusion layer
"""
    else:
        report_text += """1. Review training hyperparameters
2. Increase training epochs or adjust batch size
3. Re-run production_ctgan_trainer.py
4. Validate again
"""

    report_text += f"""
Generated by: production_validation.py
Timestamp: {datetime.now()}
"""

    # Save report
    report_path = config.REPORTS_DIR / f"validation_summary_{dataset_key}.txt"
    with open(report_path, 'w', encoding='utf-8') as f:
        f.write(report_text)

    logger.info(report_text)
    logger.info(f"\nOK: Summary report saved: {report_path}")


# ==================== 7. MAIN EXECUTION ====================

def main():
    """Main validation pipeline."""
    logger.info("\nStarting Production Validation Pipeline...\n")

    # Configuration
    DATASET_KEY = "MENTAL_HEALTH_TECH"

    # Step 1: Load data
    real_df = load_real_data(DATASET_KEY)
    if real_df is None:
        logger.error("ERROR: Failed to load real data. Exiting.")
        return

    synth_df = load_synthetic_data(DATASET_KEY)
    if synth_df is None:
        logger.error("ERROR: Failed to load synthetic data. Exiting.")
        return

    # Step 2: Ensure consistency
    real_df, synth_df = ensure_column_consistency(real_df, synth_df)

    # Step 3: Build metadata
    metadata = get_metadata(DATASET_KEY)

    # Step 4: Generate visualizations
    plot_correlation_comparison(real_df, synth_df, DATASET_KEY)
    plot_distribution_comparison(real_df, synth_df, DATASET_KEY)

    # Step 5: Run validation
    quality_score, report = validate_quality(
        real_df, synth_df, metadata, DATASET_KEY)

    if quality_score is not None:
        # Step 6: Generate summary
        generate_summary_report(real_df, synth_df, quality_score, DATASET_KEY)

        logger.info("\n" + "=" * 80)
        logger.info("VALIDATION COMPLETE")
        logger.info("=" * 80)
    else:
        logger.error("\nERROR: Validation failed. Check logs for details.")


if __name__ == "__main__":
    main()
