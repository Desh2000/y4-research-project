"""
MANO Component 1: Unit Tests for Data Preparation
Run with: python ml-services/privacy-preserving-gan/src/test_data_preparation.py
Date: December 18, 2025
"""
from normalization_config import NORMALIZATION_CONFIG
from data_preprocessor import WearableSequenceGenerator
import numpy as np
import sys
import os

# Setup path to find local modules
config_path = os.path.join(os.getcwd(), 'ml-services',
                           'privacy-preserving-gan', 'config')
sys.path.append(config_path)

# Also append src to find sibling modules if running from root
src_path = os.path.join(os.getcwd(), 'ml-services',
                        'privacy-preserving-gan', 'src')
sys.path.append(src_path)


def test_initialization():
    """Test that generator initializes correctly"""
    generator = WearableSequenceGenerator()
    assert generator is not None
    assert len(generator.signals) == 4
    assert generator.seq_len == 7
    print("✅ Test 1: Initialization test passed")


def test_normalization_bounds():
    """Test that normalization keeps values in [0, 1]"""
    generator = WearableSequenceGenerator()

    # Test minimum
    norm_min = generator.normalize_signal('Sleep Duration', 5.8)
    assert 0 <= norm_min <= 0.01, f"Min normalization failed: {norm_min}"

    # Test maximum
    norm_max = generator.normalize_signal('Sleep Duration', 8.5)
    assert 0.99 <= norm_max <= 1, f"Max normalization failed: {norm_max}"

    # Test middle
    norm_mid = generator.normalize_signal('Sleep Duration', 7.15)
    assert 0.4 <= norm_mid <= 0.6, f"Mid normalization failed: {norm_mid}"

    print("✅ Test 2: Normalization bounds test passed")


def test_denormalization_reciprocal():
    """Test that denormalization reverses normalization"""
    generator = WearableSequenceGenerator()
    original_value = 7.5

    # Normalize then denormalize
    normalized = generator.normalize_signal('Sleep Duration', original_value)
    recovered = generator.denormalize_signal('Sleep Duration', normalized)

    # Should be very close
    assert abs(recovered - original_value) < 0.01, \
        f"Denormalization failed: {original_value} → {normalized} → {recovered}"

    print("✅ Test 3: Denormalization reciprocal test passed")


def test_sequence_shape():
    """Test that generated sequences have correct shape"""
    generator = WearableSequenceGenerator()
    person = {
        'Sleep Duration': 7.5,
        'Quality of Sleep': 7,
        'Heart Rate': 75,
        'Stress Level': 5
    }

    seq = generator.generate_sequence_for_person(person, add_noise=False)
    assert seq.shape == (7, 4), f"Wrong shape: {seq.shape}"
    print("✅ Test 4: Sequence shape test passed")


def test_sequence_values_in_range():
    """Test that all sequence values are in [0, 1]"""
    generator = WearableSequenceGenerator()
    person = {
        'Sleep Duration': 7.5,
        'Quality of Sleep': 7,
        'Heart Rate': 75,
        'Stress Level': 5
    }

    seq = generator.generate_sequence_for_person(person, add_noise=True)
    assert np.all(seq >= -0.01), "Values below 0"
    assert np.all(seq <= 1.01), "Values above 1"

    print("✅ Test 5: Sequence value range test passed")


def test_noise_addition():
    """Test that noise actually adds variation"""
    generator = WearableSequenceGenerator()
    person = {
        'Sleep Duration': 7.5,
        'Quality of Sleep': 7,
        'Heart Rate': 75,
        'Stress Level': 5
    }

    seq_no_noise = generator.generate_sequence_for_person(
        person, add_noise=False)
    seq_with_noise = generator.generate_sequence_for_person(
        person, add_noise=True)

    # With noise, sequences should differ
    diff = np.abs(seq_with_noise - seq_no_noise)
    max_diff = diff.max()

    assert max_diff > 0.01, "Noise not added effectively"
    assert max_diff < 0.2, "Noise too large"

    print(f"✅ Test 6: Noise addition test passed (max diff: {max_diff:.4f})")


def run_all_tests():
    """Run all unit tests"""
    print("\n" + "="*80)
    print("RUNNING UNIT TESTS FOR DATA PREPARATION")
    print("="*80 + "\n")

    test_initialization()
    test_normalization_bounds()
    test_denormalization_reciprocal()
    test_sequence_shape()
    test_sequence_values_in_range()
    test_noise_addition()

    print("\n" + "="*80)
    print("✅ ALL UNIT TESTS PASSED (6/6)")
    print("="*80)


if __name__ == "__main__":
    run_all_tests()
