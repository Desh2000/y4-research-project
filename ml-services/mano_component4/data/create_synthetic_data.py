# data/create_synthetic_data.py
# Creates synthetic data for testing

"""
Synthetic Data Generator
========================
Creates fake but realistic test data.
"""

import pandas as pd
import numpy as np
import os

os.makedirs('data', exist_ok=True)

print("=" * 50)
print("SYNTHETIC DATA GENERATOR")
print("=" * 50)

np.random.seed(123)  # Different seed from training data

# Number of test samples
n_test = 200

print(f"\nGenerating {n_test} synthetic test records...")

# ============================================
# CREATE 5 DISTINCT USER PROFILES
# These represent the groups GMM should find
# ============================================

profiles = {
    'stressed_worker': {
        'stress_score': (70, 90),
        'anxiety_score': (60, 85),
        'depression_score': (50, 75),
        'sleep_score': (20, 40),
        'social_score': (20, 40),
        'work_balance_score': (10, 30),
        'physical_activity_score': (10, 30),
        'hrv_score': (25, 40),
        'wearable_stress': (70, 95),
        'positive_emotion': (15, 35),
        'negative_emotion': (50, 75),
    },
    'healthy_active': {
        'stress_score': (10, 30),
        'anxiety_score': (10, 25),
        'depression_score': (10, 20),
        'sleep_score': (75, 95),
        'social_score': (70, 90),
        'work_balance_score': (70, 90),
        'physical_activity_score': (70, 95),
        'hrv_score': (60, 80),
        'wearable_stress': (10, 30),
        'positive_emotion': (65, 85),
        'negative_emotion': (10, 25),
    },
    'lonely_struggling': {
        'stress_score': (50, 70),
        'anxiety_score': (55, 75),
        'depression_score': (60, 80),
        'sleep_score': (35, 55),
        'social_score': (10, 25),
        'work_balance_score': (40, 60),
        'physical_activity_score': (20, 40),
        'hrv_score': (30, 50),
        'wearable_stress': (50, 70),
        'positive_emotion': (25, 40),
        'negative_emotion': (45, 65),
    },
    'moderate_coping': {
        'stress_score': (40, 60),
        'anxiety_score': (35, 55),
        'depression_score': (30, 50),
        'sleep_score': (50, 70),
        'social_score': (50, 70),
        'work_balance_score': (45, 65),
        'physical_activity_score': (40, 60),
        'hrv_score': (45, 60),
        'wearable_stress': (40, 60),
        'positive_emotion': (45, 60),
        'negative_emotion': (30, 45),
    },
    'sleep_deprived': {
        'stress_score': (45, 65),
        'anxiety_score': (40, 60),
        'depression_score': (35, 55),
        'sleep_score': (10, 30),
        'social_score': (45, 65),
        'work_balance_score': (30, 50),
        'physical_activity_score': (25, 45),
        'hrv_score': (30, 45),
        'wearable_stress': (55, 75),
        'positive_emotion': (35, 50),
        'negative_emotion': (40, 55),
    }
}

# Generate data for each profile
all_data = []
samples_per_profile = n_test // len(profiles)

for profile_name, ranges in profiles.items():
    print(f"  Creating {samples_per_profile} '{profile_name}' users...")

    for i in range(samples_per_profile):
        user_data = {
            'user_id': f'TEST_{profile_name.upper()}_{i:03d}',
            'true_profile': profile_name  # We know the real group (for testing)
        }

        for feature, (low, high) in ranges.items():
            # Add some randomness
            value = np.random.uniform(low, high)
            user_data[feature] = round(value, 2)

        all_data.append(user_data)

# Create DataFrame
synthetic_df = pd.DataFrame(all_data)

# Shuffle the data
synthetic_df = synthetic_df.sample(frac=1, random_state=42).reset_index(drop=True)

# Save full test data (with true labels)
synthetic_df.to_csv('data/synthetic_test_data_with_labels.csv', index=False)
print(f"\n✓ Saved: data/synthetic_test_data_with_labels.csv ({len(synthetic_df)} records)")

# Save test data without labels (for actual testing)
test_features = synthetic_df.drop(columns=['true_profile'])
test_features.to_csv('data/synthetic_test_data.csv', index=False)
print(f"✓ Saved: data/synthetic_test_data.csv ({len(test_features)} records)")

# ============================================
# CREATE SINGLE USER EXAMPLES
# ============================================

print("\nCreating individual test cases...")

test_cases = [
    {
        'user_id': 'TEST_CASE_001',
        'name': 'Very Stressed Person',
        'stress_score': 85,
        'anxiety_score': 80,
        'depression_score': 70,
        'sleep_score': 25,
        'social_score': 30,
        'work_balance_score': 20,
        'physical_activity_score': 15,
        'hrv_score': 30,
        'wearable_stress': 88,
        'positive_emotion': 20,
        'negative_emotion': 70,
    },
    {
        'user_id': 'TEST_CASE_002',
        'name': 'Very Healthy Person',
        'stress_score': 15,
        'anxiety_score': 12,
        'depression_score': 10,
        'sleep_score': 90,
        'social_score': 85,
        'work_balance_score': 88,
        'physical_activity_score': 92,
        'hrv_score': 75,
        'wearable_stress': 12,
        'positive_emotion': 80,
        'negative_emotion': 15,
    },
    {
        'user_id': 'TEST_CASE_003',
        'name': 'Average Person',
        'stress_score': 50,
        'anxiety_score': 45,
        'depression_score': 40,
        'sleep_score': 55,
        'social_score': 60,
        'work_balance_score': 50,
        'physical_activity_score': 55,
        'hrv_score': 50,
        'wearable_stress': 48,
        'positive_emotion': 52,
        'negative_emotion': 38,
    },
]

test_cases_df = pd.DataFrame(test_cases)
test_cases_df.to_csv('data/individual_test_cases.csv', index=False)
print(f"✓ Saved: data/individual_test_cases.csv ({len(test_cases_df)} records)")

# ============================================
# SUMMARY
# ============================================

print("\n" + "=" * 50)
print("SYNTHETIC DATA GENERATION COMPLETE!")
print("=" * 50)
print("\nFiles created:")
print("  1. data/synthetic_test_data_with_labels.csv (for evaluation)")
print("  2. data/synthetic_test_data.csv (for testing)")
print("  3. data/individual_test_cases.csv (single user tests)")
print("\nProfile distribution:")
for profile in profiles.keys():
    print(f"  - {profile}: {samples_per_profile} users")