# test_full_system.py
# Tests the complete system: scoring + clustering + recommendations

"""
Full System Integration Test
============================
Tests the entire pipeline from raw user data to recommendations.
"""

import json
from app.core.combined_service import CombinedAnalysisService

print("=" * 70)
print("FULL SYSTEM INTEGRATION TEST")
print("=" * 70)

# Initialize combined service
print("\nInitializing all services...")
service = CombinedAnalysisService()

# Load clustering model
print("Loading clustering model...")
if service.load_clustering_model():
    print("✓ Clustering model loaded")
else:
    print("⚠ Clustering model not loaded (some features disabled)")

# Test users with raw data
test_users = [
    {
        'user_id': 'FULL_TEST_001',
        'name': 'Stressed & Isolated Worker',
        # Body metrics
        'heart_rate': 88,
        'sleep_hours': 5,
        'sleep_quality': 4,
        'steps': 3000,
        'exercise_days': 1,
        # Behavior metrics
        'phone_usage_hours': 10,
        'work_hours': 12,
        'routine_stability': 30,
        # Emotional metrics
        'stress_level': 8,
        'anxiety_level': 7,
        'depression_level': 5,
        'mood_score': 3,
        # Social metrics
        'friends_contacted': 1,
        'social_support_score': 3,
        'family_relationship': 4,
    },
    {
        'user_id': 'FULL_TEST_002',
        'name': 'Healthy but Lonely',
        # Body metrics (good)
        'heart_rate': 68,
        'sleep_hours': 7.5,
        'sleep_quality': 8,
        'steps': 10000,
        'exercise_days': 5,
        # Behavior metrics (good)
        'phone_usage_hours': 3,
        'work_hours': 8,
        'routine_stability': 80,
        # Emotional metrics (medium)
        'stress_level': 4,
        'anxiety_level': 5,
        'depression_level': 4,
        'mood_score': 6,
        # Social metrics (poor)
        'friends_contacted': 0,
        'social_support_score': 2,
        'family_relationship': 3,
    },
    {
        'user_id': 'FULL_TEST_003',
        'name': 'Well-Balanced Person',
        # All metrics good
        'heart_rate': 65,
        'sleep_hours': 7.5,
        'sleep_quality': 8,
        'steps': 12000,
        'exercise_days': 4,
        'phone_usage_hours': 3,
        'work_hours': 8,
        'routine_stability': 85,
        'stress_level': 2,
        'anxiety_level': 2,
        'depression_level': 1,
        'mood_score': 8,
        'friends_contacted': 8,
        'social_support_score': 9,
        'family_relationship': 8,
    },
]

# Analyze each user
print("\n" + "=" * 70)
print("ANALYZING USERS")
print("=" * 70)

for user in test_users:
    print(f"\n{'━' * 70}")
    print(f"👤 USER: {user['name']} ({user['user_id']})")
    print(f"{'━' * 70}")

    # Get full analysis with recommendations
    analysis = service.get_full_analysis_with_recommendations(user)

    # Print scores
    print(f"\n📊 SCORES:")
    print(f"   Overall Score: {analysis['scores']['overall']}/100")
    print(f"   Stress Level: {analysis['scores']['stress_level'].upper()}")

    print(f"\n   Category Breakdown:")
    for cat, score in analysis['scores']['categories'].items():
        bar = '█' * int(score / 5) + '░' * (20 - int(score / 5))
        status = "⚠️" if score < 40 else "✓"
        print(f"   {cat:12}: {bar} {score:5.1f} {status}")

    # Print interpretation
    print(f"\n💬 INTERPRETATION:")
    print(f"   {analysis['interpretation']['overall_interpretation']}")

    # Print peer group
    if 'group_id' in analysis.get('peer_group', {}):
        print(f"\n👥 PEER GROUP:")
        print(f"   Group: {analysis['peer_group']['group_name']}")
        print(f"   Confidence: {analysis['peer_group']['confidence']}%")

    # Print areas of concern
    if analysis['areas_of_concern']:
        print(f"\n⚠️  AREAS OF CONCERN:")
        for area in analysis['areas_of_concern']:
            print(f"   • {area}")

    # Print recommendations
    print(f"\n💡 RECOMMENDED ACTIVITIES:")
    recs = analysis['recommendations']['activities']
    for i, rec in enumerate(recs[:3], 1):
        activity = rec['activity']
        print(f"\n   {i}. {activity['name']}")
        print(f"      ├─ Category: {activity['category']}")
        print(f"      ├─ Duration: {activity['duration_minutes']} min")
        print(f"      ├─ Difficulty: {activity['difficulty']}")
        print(f"      └─ Why: {rec['why_recommended']}")

    # Print identified problems
    if analysis['recommendations']['identified_problems']:
        print(f"\n📋 IDENTIFIED PROBLEMS:")
        for problem in analysis['recommendations']['identified_problems']:
            print(f"   • {problem['category']}: {problem['severity']} (score: {problem['score']})")

# Summary
print("\n" + "=" * 70)
print("SYSTEM TEST SUMMARY")
print("=" * 70)

print("\n✅ COMPONENTS TESTED:")
print("   • Scoring Service - Calculate scores from raw data")
print("   • Clustering Service - Assign to peer groups")
print("   • Recommender Service - Suggest activities")
print("   • Combined Service - Full pipeline integration")

print("\n📊 RESULTS:")
for user in test_users:
    analysis = service.get_full_analysis_with_recommendations(user)
    print(f"\n   {user['name']}:")
    print(f"   → Score: {analysis['scores']['overall']}/100 ({analysis['scores']['stress_level']})")
    print(f"   → Problems: {len(analysis['recommendations']['identified_problems'])}")
    print(f"   → Recommendations: {len(analysis['recommendations']['activities'])}")

print("\n" + "=" * 70)
print("FULL SYSTEM TEST COMPLETE!")
print("=" * 70)
