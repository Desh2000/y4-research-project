# test_gmm.py
# Script to test the trained GMM model

"""
GMM Testing Script
==================
Tests the trained model on synthetic data.
"""

import pandas as pd
from app.core.clustering import ClusteringService

print("=" * 60)
print("GMM MODEL TESTING")
print("=" * 60)

# Step 1: Load the trained model
print("\nStep 1: Loading trained model...")
clustering = ClusteringService()
clustering.load_model()
print("Model loaded successfully!")

# Step 2: Show group information
print("\nStep 2: Group Information")
print("-" * 40)
groups = clustering.get_group_info()
for group in groups:
    print(f"  {group['group_id']}: {group['group_name']}")

# Step 3: Test with individual cases
print("\nStep 3: Testing Individual Cases")
print("-" * 40)

test_cases = pd.read_csv('data/individual_test_cases.csv')

for _, user in test_cases.iterrows():
    user_df = pd.DataFrame([user])
    prediction = clustering.predict(user_df)

    print(f"\n  User: {user['name']}")
    print(f"  Stress Score: {user['stress_score']}")
    print(f"  Sleep Score: {user['sleep_score']}")
    print(f"  Social Score: {user['social_score']}")
    print(f"  → Assigned to: {prediction['group_name']}")
    print(f"  → Confidence: {prediction['confidence'] * 100:.1f}%")

# Step 4: Test with synthetic batch data
print("\n\nStep 4: Batch Testing with Synthetic Data")
print("-" * 40)

test_data = pd.read_csv('data/synthetic_test_data.csv')
results = clustering.predict_batch(test_data)

# Show distribution
print("\nPredicted group distribution:")
distribution = results['group_name'].value_counts()
for group_name, count in distribution.items():
    print(f"  {group_name}: {count} users")

# Step 5: Compare with true labels (if available)
print("\n\nStep 5: Accuracy Check (with labeled test data)")
print("-" * 40)

labeled_data = pd.read_csv('data/synthetic_test_data_with_labels.csv')
results_labeled = clustering.predict_batch(labeled_data)

# Show how synthetic profiles were grouped
print("\nHow synthetic profiles were grouped:")
for profile in labeled_data['true_profile'].unique():
    profile_data = results_labeled[results_labeled['true_profile'] == profile]
    most_common_group = profile_data['group_name'].mode()[0]
    count = len(profile_data)
    print(f"  '{profile}' ({count} users) → mostly assigned to '{most_common_group}'")

# Step 6: Test similar users function
print("\n\nStep 6: Finding Similar Users")
print("-" * 40)

# Use first user in test data
first_user_id = test_data.iloc[0]['user_id']
similar = clustering.get_similar_users(first_user_id, test_data, top_n=3)

print(f"\nUsers similar to {first_user_id}:")
for user in similar:
    print(f"  - {user['user_id']}: {user['similarity_score']}% similar")

print("\n" + "=" * 60)
print("TESTING COMPLETE!")
print("=" * 60)