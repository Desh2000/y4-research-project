"""
MANO Component 1: Signal Normalization Configuration
Based on actual data ranges from Week 1 exploration
Date: December 18, 2025
"""

# Signal ranges from wearable_data_explorer.py results
NORMALIZATION_CONFIG = {
    'Sleep Duration': {
        'min': 5.8,
        'max': 8.5,
        'method': 'minmax',
        'unit': 'hours',
        'description': 'Nightly sleep duration'
    },
    'Quality of Sleep': {
        'min': 4,
        'max': 9,
        'method': 'minmax',
        'unit': '1-10 scale',
        'description': 'Subjective sleep quality rating'
    },
    'Heart Rate': {
        'min': 65,
        'max': 86,
        'method': 'minmax',
        'unit': 'bpm',
        'description': 'Resting heart rate'
    },
    'Stress Level': {
        'min': 3,
        'max': 8,
        'method': 'minmax',
        'unit': '1-10 scale',
        'description': 'Perceived stress level'
    }
}

# Sequence generation parameters
SEQUENCE_CONFIG = {
    'sequence_length': 7,           # 7-day sequences
    'granularity': 'daily',         # 1 measurement per day
    'total_timepoints': 7,          # 7 daily points
    'noise_std': 0.08,              # ±8% daily variation
    'num_sequences': 374,           # 374 people
    'train_test_split': 0.8,        # 80% train, 20% test
}

# Quality validation thresholds
VALIDATION_THRESHOLDS = {
    'min_value': 0.0,               # All normalized >= 0
    'max_value': 1.0,               # All normalized <= 1
    'missing_value_tolerance': 0.0,  # No missing values
    'sequence_shape': (374, 7, 4),  # Expected output shape
}
