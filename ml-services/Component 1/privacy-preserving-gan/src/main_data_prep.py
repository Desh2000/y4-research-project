"""
MANO Component 1: Data Preparation Main Execution Script
Run this to generate wearable_sequences.npz
Date: December 18, 2025
"""
from data_preprocessor import WearableSequenceGenerator
import config
import sys
import os
from pathlib import Path

# Import configuration
config_path = os.path.join(os.getcwd(), 'ml-services',
                           'privacy-preserving-gan', 'config')
sys.path.append(config_path)


def main():
    """Execute the data preparation pipeline"""

    print("\n" + "█"*80)
    print("█ MANO COMPONENT 1: WEARABLE DATA PREPARATION PIPELINE")
    print("█"*80)

    # Initialize generator
    generator = WearableSequenceGenerator()

    # Paths
    input_csv = config.DATASETS['SLEEP_HEALTH']['file_path']
    output_npz = config.PROCESSED_DATA_DIR / 'wearable_sequences.npz'

    # Ensure output directory exists
    if not output_npz.parent.exists():
        output_npz.parent.mkdir(parents=True, exist_ok=True)

    # Run pipeline
    sequences = generator.process_dataset(
        input_csv=str(input_csv),
        output_npz=str(output_npz)
    )

    if sequences is not None:
        print(f"\n" + "█"*80)
        print("█ DATA PREPARATION COMPLETE ✅")
        print("█"*80)
        print(f"""
Output Location: {output_npz}
Generated File: wearable_sequences.npz
  Shape: {sequences.shape}
  Ready for TimeGAN training

Next Steps:
1. Run validation: python ml-services/privacy-preserving-gan/src/validate_sequences.py
2. Verify all checks pass
3. Then proceed to Week 3-4 (TimeGAN training)
        """)


if __name__ == "__main__":
    main()
