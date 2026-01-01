# data/download_datasets.py
# Script to download and prepare datasets

"""
Dataset Downloader
==================
Downloads mental health datasets from online sources.
"""

import pandas as pd
import numpy as np
import os

# Create data folder if not exists
os.makedirs('data', exist_ok=True)

print("=" * 50)
print("DATASET DOWNLOADER")
print("=" * 50)

# ============================================
# DATASET 1: Mental Health Survey Data
# We will create a realistic dataset based on
# common mental health survey patterns
# ============================================

print("\nCreating Dataset 1: Mental Health Survey Data...")

# Set random seed for reproducibility
np.random.seed(42)

# Number of samples
n_samples = 1000

# Generate realistic mental health survey data
dataset1 = pd.DataFrame({
    'user_id': [f'USER_{i:04d}' for i in range(n_samples)],

    # Demographics
    'age': np.random.randint(18, 65, n_samples),
    'gender': np.random.choice(['Male', 'Female', 'Other'], n_samples, p=[0.48, 0.48, 0.04]),
    'occupation': np.random.choice(
        ['Engineer', 'Developer', 'Designer', 'Manager', 'Researcher', 'Student', 'Other'],
        n_samples,
        p=[0.20, 0.25, 0.10, 0.15, 0.10, 0.15, 0.05]
    ),

    # Stress indicators (1-10 scale)
    'stress_level': np.random.randint(1, 11, n_samples),
    'anxiety_level': np.random.randint(1, 11, n_samples),
    'depression_level': np.random.randint(1, 11, n_samples),

    # Sleep data
    'sleep_hours': np.round(np.random.uniform(4, 10, n_samples), 1),
    'sleep_quality': np.random.randint(1, 11, n_samples),

    # Physical activity
    'exercise_days_per_week': np.random.randint(0, 8, n_samples),
    'daily_steps': np.random.randint(1000, 15000, n_samples),

    # Social indicators
    'social_support_score': np.random.randint(1, 11, n_samples),
    'friends_count': np.random.randint(0, 50, n_samples),
    'family_relationship': np.random.randint(1, 11, n_samples),

    # Work-related
    'work_hours_per_day': np.round(np.random.uniform(4, 14, n_samples), 1),
    'work_satisfaction': np.random.randint(1, 11, n_samples),
    'work_life_balance': np.random.randint(1, 11, n_samples),

    # Technology usage
    'screen_time_hours': np.round(np.random.uniform(2, 16, n_samples), 1),
    'social_media_hours': np.round(np.random.uniform(0, 8, n_samples), 1),
})

# Add some realistic correlations
# People with high stress tend to have worse sleep
high_stress_idx = dataset1['stress_level'] > 7
dataset1.loc[high_stress_idx, 'sleep_hours'] = np.clip(
    dataset1.loc[high_stress_idx, 'sleep_hours'] - np.random.uniform(1, 2, high_stress_idx.sum()),
    4, 10
)
dataset1.loc[high_stress_idx, 'sleep_quality'] = np.clip(
    dataset1.loc[high_stress_idx, 'sleep_quality'] - np.random.randint(1, 4, high_stress_idx.sum()),
    1, 10
)

# People with good social support tend to have lower stress
good_social_idx = dataset1['social_support_score'] > 7
dataset1.loc[good_social_idx, 'stress_level'] = np.clip(
    dataset1.loc[good_social_idx, 'stress_level'] - np.random.randint(1, 3, good_social_idx.sum()),
    1, 10
)

# Save dataset
dataset1.to_csv('data/mental_health_survey.csv', index=False)
print(f"✓ Saved: data/mental_health_survey.csv ({len(dataset1)} records)")

# ============================================
# DATASET 2: Wearable Device Data
# Simulated smart watch data
# ============================================

print("\nCreating Dataset 2: Wearable Device Data...")

dataset2 = pd.DataFrame({
    'user_id': [f'USER_{i:04d}' for i in range(n_samples)],

    # Heart rate data
    'resting_heart_rate': np.random.randint(55, 100, n_samples),
    'avg_heart_rate': np.random.randint(60, 110, n_samples),
    'max_heart_rate': np.random.randint(100, 180, n_samples),
    'heart_rate_variability': np.round(np.random.uniform(20, 80, n_samples), 1),

    # Sleep data from wearable
    'sleep_duration_mins': np.random.randint(240, 600, n_samples),
    'deep_sleep_mins': np.random.randint(30, 150, n_samples),
    'light_sleep_mins': np.random.randint(120, 300, n_samples),
    'rem_sleep_mins': np.random.randint(30, 120, n_samples),
    'times_woken': np.random.randint(0, 10, n_samples),

    # Activity data
    'steps': np.random.randint(1000, 20000, n_samples),
    'active_minutes': np.random.randint(10, 180, n_samples),
    'calories_burned': np.random.randint(1500, 3500, n_samples),
    'distance_km': np.round(np.random.uniform(1, 15, n_samples), 2),

    # Stress indicators from wearable
    'stress_score': np.random.randint(1, 100, n_samples),
    'relaxation_score': np.random.randint(1, 100, n_samples),
})

# Add correlations
# Low HRV often indicates stress
low_hrv_idx = dataset2['heart_rate_variability'] < 40
dataset2.loc[low_hrv_idx, 'stress_score'] = np.clip(
    dataset2.loc[low_hrv_idx, 'stress_score'] + np.random.randint(10, 30, low_hrv_idx.sum()),
    1, 100
)

# Save dataset
dataset2.to_csv('data/wearable_data.csv', index=False)
print(f"✓ Saved: data/wearable_data.csv ({len(dataset2)} records)")

# ============================================
# DATASET 3: Social Media Sentiment
# Simulated emotional text analysis results
# ============================================

print("\nCreating Dataset 3: Social Media Sentiment Data...")

dataset3 = pd.DataFrame({
    'user_id': [f'USER_{i:04d}' for i in range(n_samples)],

    # Sentiment scores (from text analysis)
    'positive_sentiment': np.round(np.random.uniform(0, 1, n_samples), 3),
    'negative_sentiment': np.round(np.random.uniform(0, 1, n_samples), 3),
    'neutral_sentiment': np.round(np.random.uniform(0, 1, n_samples), 3),

    # Emotion scores
    'joy_score': np.round(np.random.uniform(0, 1, n_samples), 3),
    'sadness_score': np.round(np.random.uniform(0, 1, n_samples), 3),
    'anger_score': np.round(np.random.uniform(0, 1, n_samples), 3),
    'fear_score': np.round(np.random.uniform(0, 1, n_samples), 3),

    # Activity metrics
    'posts_per_week': np.random.randint(0, 50, n_samples),
    'comments_per_week': np.random.randint(0, 100, n_samples),
    'likes_received': np.random.randint(0, 500, n_samples),

    # Language indicators
    'negative_words_ratio': np.round(np.random.uniform(0, 0.5, n_samples), 3),
    'positive_words_ratio': np.round(np.random.uniform(0, 0.5, n_samples), 3),
})

# Normalize sentiment to sum to 1
total_sentiment = dataset3['positive_sentiment'] + dataset3['negative_sentiment'] + dataset3['neutral_sentiment']
dataset3['positive_sentiment'] = dataset3['positive_sentiment'] / total_sentiment
dataset3['negative_sentiment'] = dataset3['negative_sentiment'] / total_sentiment
dataset3['neutral_sentiment'] = dataset3['neutral_sentiment'] / total_sentiment

# Save dataset
dataset3.to_csv('data/social_sentiment.csv', index=False)
print(f"✓ Saved: data/social_sentiment.csv ({len(dataset3)} records)")

# ============================================
# DATASET 4: Combined Features (for GMM training)
# Merge all datasets into one feature set
# ============================================

print("\nCreating Dataset 4: Combined Features for GMM...")

# Merge datasets on user_id
combined = dataset1.merge(dataset2, on='user_id', how='inner')
combined = combined.merge(dataset3, on='user_id', how='inner')

# Select features for GMM clustering
gmm_features = pd.DataFrame({
    'user_id': combined['user_id'],

    # Normalized scores (0-100 scale)
    'stress_score': combined['stress_level'] * 10,
    'anxiety_score': combined['anxiety_level'] * 10,
    'depression_score': combined['depression_level'] * 10,
    'sleep_score': combined['sleep_quality'] * 10,
    'social_score': combined['social_support_score'] * 10,
    'work_balance_score': combined['work_life_balance'] * 10,

    # Wearable scores
    'physical_activity_score': np.clip(combined['steps'] / 100, 0, 100),
    'hrv_score': combined['heart_rate_variability'],
    'wearable_stress': combined['stress_score'],

    # Sentiment scores (scaled to 0-100)
    'positive_emotion': combined['positive_sentiment'] * 100,
    'negative_emotion': combined['negative_sentiment'] * 100,
})

# Save combined dataset
gmm_features.to_csv('data/gmm_training_data.csv', index=False)
print(f"✓ Saved: data/gmm_training_data.csv ({len(gmm_features)} records)")

# ============================================
# SUMMARY
# ============================================

print("\n" + "=" * 50)
print("DOWNLOAD COMPLETE!")
print("=" * 50)
print("\nFiles created:")
print("  1. data/mental_health_survey.csv")
print("  2. data/wearable_data.csv")
print("  3. data/social_sentiment.csv")
print("  4. data/gmm_training_data.csv (for GMM training)")
print("\nTotal records per file:", n_samples)
print("\nYou can now proceed with Phase 3 Step 3!")
