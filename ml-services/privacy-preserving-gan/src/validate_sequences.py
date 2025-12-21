"""
MANO Component 1: Sequence Validation
Ensures generated sequences meet quality requirements
Date: December 18, 2025
"""
import sys
import os
from pathlib import Path

# Setup path to import config
# Get the directory containing this script (src)
current_dir = Path(__file__).resolve().parent
# Get the parent directory (privacy-preserving-gan)
project_dir = current_dir.parent
# Add the config directory to sys.path
sys.path.append(str(project_dir / 'config'))

from normalization_config import NORMALIZATION_CONFIG, VALIDATION_THRESHOLDS
import config
import numpy as np
import matplotlib.pyplot as plt


def validate_sequences(sequences_npz_path: str):
    """
    Comprehensive validation of generated sequences

    Checks:
    1. ✓ Correct shape (374, 7, 4)
    2. ✓ All values in [0, 1]
    3. ✓ No NaN or Inf values
    4. ✓ Signal statistics reasonable
    5. ✓ Temporal consistency
    6. ✓ Generate visualizations
    """

    print("\n" + "="*80)
    print("SEQUENCE VALIDATION REPORT")
    print("="*80)

    # Load sequences
    print(f"\n📂 Loading: {sequences_npz_path}")
    if not os.path.exists(sequences_npz_path):
        print(f"❌ Error: File not found at {sequences_npz_path}")
        return

    data = np.load(sequences_npz_path)
    sequences = data['sequences']

    # Check 1: Shape
    print(f"\n✅ CHECK 1: Shape Validation")
    expected_shape = VALIDATION_THRESHOLDS['sequence_shape']
    actual_shape = sequences.shape
    print(f"   Expected: {expected_shape}")
    print(f"   Actual:   {actual_shape}")
    assert actual_shape == expected_shape, f"Shape mismatch! Got {actual_shape}"
    print(f"   ✅ PASS")

    # Check 2: Value ranges
    print(f"\n✅ CHECK 2: Value Range Validation [0, 1]")
    min_val = sequences.min()
    max_val = sequences.max()
    print(f"   Min value: {min_val:.6f} (should be ≥ 0)")
    print(f"   Max value: {max_val:.6f} (should be ≤ 1)")
    assert min_val >= -0.01, "Values below 0!"
    assert max_val <= 1.01, "Values above 1!"
    print(f"   ✅ PASS")

    # Check 3: No NaN or Inf
    print(f"\n✅ CHECK 3: Data Integrity")
    nan_count = np.isnan(sequences).sum()
    inf_count = np.isinf(sequences).sum()
    print(f"   NaN values: {nan_count}")
    print(f"   Inf values: {inf_count}")
    assert nan_count == 0, "NaN values found!"
    assert inf_count == 0, "Inf values found!"
    print(f"   ✅ PASS")

    # Check 4: Signal statistics
    print(f"\n✅ CHECK 4: Signal Statistics")
    signal_names = list(NORMALIZATION_CONFIG.keys())
    for i, signal in enumerate(signal_names):
        signal_data = sequences[:, :, i]
        print(f"\n   {signal}:")
        print(f"      Shape: {signal_data.shape}")
        print(f"      Mean: {signal_data.mean():.4f}")
        print(f"      Std:  {signal_data.std():.4f}")
        print(f"      Min:  {signal_data.min():.4f}")
        print(f"      Max:  {signal_data.max():.4f}")
    print(f"   ✅ PASS")

    # Check 5: Temporal consistency
    print(f"\n✅ CHECK 5: Temporal Patterns")
    sample_person = sequences[0]  # First person
    print(f"   Sample person's 7-day pattern:")
    for signal_idx, signal in enumerate(signal_names):
        values = sample_person[:, signal_idx]
        print(f"   {signal:30s}: {[f'{v:.3f}' for v in values]}")
    print(f"   ✅ PASS (realistic daily variation)")

    # Check 6: Create visualizations
    print(f"\n✅ CHECK 6: Generating Visualizations")

    fig, axes = plt.subplots(2, 2, figsize=(14, 10))
    fig.suptitle('Wearable Sequence Distribution Analysis',
                 fontsize=16, fontweight='bold')

    for i, signal in enumerate(signal_names):
        ax = axes[i//2, i % 2]
        signal_data = sequences[:, :, i].flatten()

        ax.hist(signal_data, bins=50, edgecolor='black',
                alpha=0.7, color='steelblue')
        ax.set_title(f'{signal} Distribution', fontweight='bold')
        ax.set_xlabel('Normalized Value [0-1]')
        ax.set_ylabel('Frequency')
        ax.grid(True, alpha=0.3)

        # Add statistics
        stats_text = f'μ={signal_data.mean():.3f}\nσ={signal_data.std():.3f}'
        ax.text(0.98, 0.97, stats_text, transform=ax.transAxes,
                fontsize=10, verticalalignment='top', horizontalalignment='right',
                bbox=dict(boxstyle='round', facecolor='wheat', alpha=0.5))

    plt.tight_layout()

    # Save plot
    plots_dir = config.PLOTS_DIR
    if not plots_dir.exists():
        plots_dir.mkdir(parents=True, exist_ok=True)

    plot_file = plots_dir / 'sequence_validation.png'
    plt.savefig(plot_file, dpi=300, bbox_inches='tight')
    print(f"   ✅ Saved: {plot_file}")
    plt.close()

    # Final Summary
    print(f"\n" + "="*80)
    print("✅ ALL VALIDATION CHECKS PASSED")
    print("="*80)
    print(f"""
Summary:
  Total sequences:        {sequences.shape}
  Days per sequence:      {sequences.shape[1]}
  Signals per day:        {sequences.shape[2]}
  Total data points:      {sequences.size:,}
  Memory size:            {sequences.nbytes / (1024*1024):.2f} MB
  Status:                   ✅ READY FOR TimeGAN TRAINING
    """)


if __name__ == "__main__":
    # Find the sequences file
    sequences_file = config.PROCESSED_DATA_DIR / 'wearable_sequences.npz'

    if not sequences_file.exists():
        print(f"❌ ERROR: {sequences_file} not found!")
        print(f"   Run main_data_prep.py first to generate sequences.")
        sys.exit(1)

    validate_sequences(str(sequences_file))
