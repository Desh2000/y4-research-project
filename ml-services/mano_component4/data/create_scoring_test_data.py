# data/create_scoring_test_data.py
# Creates test data specifically for testing the scoring service

"""
Scoring Test Data Generator
===========================
Creates test users with known characteristics to verify scoring works correctly.
"""

import pandas as pd
import os

os.makedirs('data', exist_ok=True)

print("="*60)
print("CREATING SCORING TEST DATA")
print("="*60)

# Create test cases with different profiles
test_users = [
    {
        'user_id': 'SCORE_TEST_001',
        'name': 'Very Healthy Person',
        'description': 'All metrics are good - should get HIGH score',
        # Body metrics (all good)
        'heart_rate': 68,
        'sleep_hours': 7.5,
        'sleep_quality': 8,
        'steps': 10000,
        'hrv': 65,
        'exercise_days': 5,
        # Behavior metrics (all good)
        'phone_usage_hours': 3,
        'routine_stability': 85,
        'work_hours': 8,
        'social_media_hours': 1,
        # Emotional metrics (all good - low stress)
        'stress_level': 2,
        'anxiety_level': 2,
        'depression_level': 1,
        'mood_score': 8,
        # Social metrics (all good)
        'messages_sent': 50,
        'friends_contacted': 10,
        'social_support_score': 9,
        'family_relationship': 8,
        'expected_result': 'very_low stress (score 80+)'
    },
    {
        'user_id': 'SCORE_TEST_002',
        'name': 'Very Stressed Person',
        'description': 'All metrics are bad - should get LOW score',
        # Body metrics (all bad)
        'heart_rate': 95,
        'sleep_hours': 4,
        'sleep_quality': 3,
        'steps': 1500,
        'hrv': 25,
        'exercise_days': 0,
        # Behavior metrics (all bad)
        'phone_usage_hours': 12,
        'routine_stability': 20,
        'work_hours': 14,
        'social_media_hours': 6,
        # Emotional metrics (all bad - high stress)
        'stress_level': 9,
        'anxiety_level': 8,
        'depression_level': 7,
        'mood_score': 2,
        # Social metrics (all bad)
        'messages_sent': 2,
        'friends_contacted': 0,
        'social_support_score': 2,
        'family_relationship': 3,
        'expected_result': 'very_high stress (score below 20)'
    },
    {
        'user_id': 'SCORE_TEST_003',
        'name': 'Average Person',
        'description': 'All metrics are medium - should get MEDIUM score',
        # Body metrics (medium)
        'heart_rate': 78,
        'sleep_hours': 6,
        'sleep_quality': 5,
        'steps': 5000,
        'hrv': 45,
        'exercise_days': 2,
        # Behavior metrics (medium)
        'phone_usage_hours': 6,
        'routine_stability': 50,
        'work_hours': 10,
        'social_media_hours': 3,
        # Emotional metrics (medium)
        'stress_level': 5,
        'anxiety_level': 5,
        'depression_level': 4,
        'mood_score': 5,
        # Social metrics (medium)
        'messages_sent': 15,
        'friends_contacted': 3,
        'social_support_score': 5,
        'family_relationship': 5,
        'expected_result': 'medium stress (score 40-60)'
    },
    {
        'user_id': 'SCORE_TEST_004',
        'name': 'Sleep Deprived Worker',
        'description': 'Good emotional/social but bad sleep/work balance',
        # Body metrics (bad sleep)
        'heart_rate': 82,
        'sleep_hours': 4.5,
        'sleep_quality': 3,
        'steps': 3000,
        'hrv': 35,
        'exercise_days': 1,
        # Behavior metrics (overworking)
        'phone_usage_hours': 8,
        'routine_stability': 40,
        'work_hours': 12,
        'social_media_hours': 2,
        # Emotional metrics (okay)
        'stress_level': 6,
        'anxiety_level': 5,
        'depression_level': 4,
        'mood_score': 5,
        # Social metrics (okay)
        'messages_sent': 20,
        'friends_contacted': 4,
        'social_support_score': 6,
        'family_relationship': 6,
        'expected_result': 'high stress (body/behavior low, others medium)'
    },
    {
        'user_id': 'SCORE_TEST_005',
        'name': 'Lonely but Healthy',
        'description': 'Good physical health but socially isolated',
        # Body metrics (good)
        'heart_rate': 65,
        'sleep_hours': 7,
        'sleep_quality': 7,
        'steps': 8000,
        'hrv': 55,
        'exercise_days': 4,
        # Behavior metrics (good)
        'phone_usage_hours': 4,
        'routine_stability': 75,
        'work_hours': 8,
        'social_media_hours': 1,
        # Emotional metrics (medium - affected by loneliness)
        'stress_level': 5,
        'anxiety_level': 6,
        'depression_level': 5,
        'mood_score': 5,
        # Social metrics (very bad)
        'messages_sent': 3,
        'friends_contacted': 0,
        'social_support_score': 2,
        'family_relationship': 3,
        'expected_result': 'medium-high stress (social pulling down overall)'
    },
]

# Create DataFrame
df = pd.DataFrame(test_users)

# Save to CSV
df.to_csv('data/scoring_test_data.csv', index=False)

print(f"\n✓ Created {len(test_users)} test users")
print(f"✓ Saved to: data/scoring_test_data.csv")

print("\n" + "-"*60)
print("TEST USERS CREATED:")
print("-"*60)

for user in test_users:
    print(f"\n{user['user_id']}: {user['name']}")
    print(f"  Description: {user['description']}")
    print(f"  Expected: {user['expected_result']}")

print("\n" + "="*60)
print("SCORING TEST DATA READY!")
print("="*60)
