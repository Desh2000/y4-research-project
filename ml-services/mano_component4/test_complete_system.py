# test_complete_system.py
# Tests the complete scoring + clustering system

"""
Complete System Test
====================
Tests scoring and clustering working together.
"""

import pandas as pd
from app.core.combined_service import CombinedAnalysisService

print("=" * 60)
print("COMPLETE SYSTEM TEST")
print("=" * 60)

# Initialize combined service
print("\nInitializing services...")
service = CombinedAnalysisService()

# Load clustering model
print("Loading clustering model...")
if service.load_clustering_model():
    print("✓ Clustering model loaded")
else:
    print("⚠ Could not load clustering model")

# Test users
test_users = [
    {
        'user_id': 'COMPLETE_TEST_001',
        'name': 'Stressed Worker',
        'heart_rate': 92,
        'sleep_hours': 5,
        'sleep_quality': 4,
        'steps': 2500,
        'hrv': 30,
        'exercise_days': 1,
        'phone_usage_hours': 10,
        'routine_stability': 30,
        'work_hours': 12,
        'social_media_hours': 4,
        'stress_level': 8,
        'anxiety_level': 7,
        'depression_level': 6,
        'mood_score': 3,
        'messages_sent': 5,
        'friends_contacted': 1,
        'social_support_score': 3,
        'family_relationship': 4,
    },
    {
        'user_id': 'COMPLETE_TEST_002',
        'name': 'Healthy Active',
        'heart_rate': 65,
        'sleep_hours': 7.5,
        'sleep_quality': 8,
        'steps': 12000,
        'hrv': 60,
        'exercise_days': 5,
        'phone_usage_hours': 3,
        'routine_stability': 80,
        'work_hours': 8,
        'social_media_hours': 1,
        'stress_level': 2,
        'anxiety_level': 2,
        'depression_level': 1,
        'mood_score': 8,
        'messages_sent': 40,
        'friends_contacted': 8,
        'social_support_score': 8,
        'family_relationship': 9,
    },
]

# Analyze each user
print("\n" + "=" * 60)
print("ANALYZING USERS")
print("=" * 60)

for user in test_users:
    print(f"\n{'─' * 60}")
    print(f"USER: {user['name']} ({user['user_id']})")
    print(f"{'─' * 60}")

    # Get complete analysis
    analysis = service.analyze_user(user)

    # Print results
    print(f"\n📊 SCORES:")
    print(f"   Overall Score: {analysis['scores']['overall']}/100")
    print(f"   Stress Level: {analysis['scores']['stress_level'].upper()}")

    print(f"\n📈 CATEGORY BREAKDOWN:")
    for cat, score in analysis['scores']['categories'].items():
        bar = '█' * int(score / 5) + '░' * (20 - int(score / 5))
        print(f"   {cat:12}: {bar} {score:.1f}")

    if 'group_id' in analysis.get('peer_group', {}):
        print(f"\n👥 PEER GROUP:")
        print(f"   Group: {analysis['peer_group']['group_name']}")
        print(f"   Confidence: {analysis['peer_group']['confidence']}%")

    if analysis['areas_of_concern']:
        print(f"\n⚠️  AREAS OF CONCERN:")
        for area in analysis['areas_of_concern']:
            print(f"   - {area}")

    print(f"\n💬 INTERPRETATION:")
    print(f"   {analysis['interpretation']['overall_interpretation']}")

# Compare users
print("\n" + "=" * 60)
print("COMPARING USERS")
print("=" * 60)

comparison = service.compare_users(test_users[0], test_users[1])

print(f"\n{test_users[0]['name']} vs {test_users[1]['name']}:")
print(f"   Score difference: {comparison['score_difference']:.1f} points")
print(f"   Same stress level: {'Yes' if comparison['same_stress_level'] else 'No'}")

print(f"\n   Category differences:")
for cat, data in comparison['category_comparison'].items():
    diff = data['difference']
    direction = "↑" if diff > 0 else "↓" if diff < 0 else "="
    print(f"   {cat:12}: {data['user1']:.1f} vs {data['user2']:.1f} ({direction} {abs(diff):.1f})")

# Show all groups
print("\n" + "=" * 60)
print("ALL PEER GROUPS")
print("=" * 60)

groups = service.get_group_summary()
for group in groups:
    if 'error' not in group:
        print(f"\n   Group {group['group_id']}: {group['group_name']}")

print("\n" + "=" * 60)
print("COMPLETE SYSTEM TEST FINISHED!")
print("=" * 60)
