# test_recommendations.py
# Tests the recommendation service

"""
Recommendation Service Test
===========================
Tests that recommendations are appropriate for different user profiles.
"""

import json
from app.core.recommender import RecommenderService

print("=" * 60)
print("RECOMMENDATION SERVICE TEST")
print("=" * 60)

# Create recommender service
recommender = RecommenderService()

# Load test cases
print("\nLoading test cases...")
with open('data/recommendation_test_cases.json', 'r') as f:
    test_cases = json.load(f)
print(f"Loaded {len(test_cases)} test cases")

# Test each case
print("\n" + "=" * 60)
print("TESTING EACH USER")
print("=" * 60)

results_summary = []

for case in test_cases:
    print(f"\n{'─' * 60}")
    print(f"TEST: {case['name']} ({case['test_id']})")
    print(f"{'─' * 60}")
    print(f"Description: {case['description']}")
    print(f"Stress Level: {case['scores']['stress_level']}")

    print(f"\nCategory Scores:")
    for cat, score in case['scores']['category_scores'].items():
        bar = '█' * int(score / 5) + '░' * (20 - int(score / 5))
        status = "⚠️ LOW" if score < 40 else "✓ OK"
        print(f"  {cat:12}: {bar} {score:3} {status}")

    # Get recommendations
    result = recommender.get_recommendations(case['scores'])

    print(f"\n📋 IDENTIFIED PROBLEMS:")
    if result['identified_problems']:
        for problem in result['identified_problems']:
            print(f"  - {problem['category']}: {problem['severity']} (score: {problem['score']})")
    else:
        print("  None - user is doing well!")

    print(f"\n💡 RECOMMENDATIONS ({len(result['recommendations'])}):")
    recommended_categories = []
    for i, rec in enumerate(result['recommendations'], 1):
        activity = rec['activity']
        print(f"\n  {i}. {activity['name']}")
        print(f"     Category: {activity['category']}")
        print(f"     Duration: {activity['duration_minutes']} minutes")
        print(f"     Difficulty: {activity['difficulty']}")
        print(f"     Why: {rec['why_recommended']}")
        recommended_categories.append(activity['category'])

    # Check if expected categories are included
    print(f"\n✅ VERIFICATION:")
    expected = case['expected_categories']

    # Check for professional help
    has_professional = any(r['activity']['category'] == 'professional'
                           for r in result['recommendations'])

    if case['should_include_professional']:
        if has_professional:
            print(f"  ✓ Professional help included (as expected)")
        else:
            print(f"  ⚠️ Professional help NOT included (but should be!)")

    # Check category matches
    matches = sum(1 for exp in expected if exp in recommended_categories or
                  (exp == 'general_wellness' and not result['identified_problems']))
    print(f"  Expected categories: {expected}")
    print(f"  Got categories: {recommended_categories}")

    results_summary.append({
        'test_id': case['test_id'],
        'name': case['name'],
        'num_recommendations': len(result['recommendations']),
        'stress_level': result['user_stress_level'],
        'problems_found': len(result['identified_problems'])
    })

# Summary
print("\n" + "=" * 60)
print("TEST SUMMARY")
print("=" * 60)

print("\n")
for r in results_summary:
    print(f"{r['test_id']}: {r['name']}")
    print(f"  → {r['num_recommendations']} recommendations, "
          f"{r['problems_found']} problems found, "
          f"stress: {r['stress_level']}")

# Test specific features
print("\n" + "=" * 60)
print("TESTING ADDITIONAL FEATURES")
print("=" * 60)

# Test quick activities
print("\n🚀 Quick Activities (under 10 minutes):")
quick = recommender.get_quick_activities(3)
for activity in quick:
    print(f"  - {activity['name']} ({activity['duration_minutes']} min)")

# Test search
print("\n🔍 Search for 'breathing':")
search_results = recommender.search_activities('breathing')
for activity in search_results:
    print(f"  - {activity['name']}")

# Test categories
print("\n📂 All Categories:")
categories = recommender.get_all_categories()
for cat in categories:
    print(f"  - {cat['name']}: {cat['count']} activities")

print("\n" + "=" * 60)
print("RECOMMENDATION TEST COMPLETE!")
print("=" * 60)
