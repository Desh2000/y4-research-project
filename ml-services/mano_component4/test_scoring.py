# test_scoring.py
# Script to test the scoring service

"""
Scoring Service Test
====================
Tests the scoring calculations with known test cases.
"""

import pandas as pd
from app.core.scoring import ScoringService

print("=" * 60)
print("SCORING SERVICE TEST")
print("=" * 60)

# Create scoring service
scorer = ScoringService()

# Load test data
print("\nLoading test data...")
test_data = pd.read_csv('data/data/scoring_test_data.csv')
print(f"Loaded {len(test_data)} test users")

# Test each user
print("\n" + "=" * 60)
print("TESTING EACH USER")
print("=" * 60)

for idx, row in test_data.iterrows():
    user_data = row.to_dict()

    print(f"\n{'─' * 60}")
    print(f"USER: {user_data['name']} ({user_data['user_id']})")
    print(f"{'─' * 60}")
    print(f"Description: {user_data['description']}")
    print(f"Expected: {user_data['expected_result']}")

    # Calculate scores
    result = scorer.calculate_overall_score(user_data)

    print(f"\nRESULTS:")
    print(f"  Overall Score: {result['overall_score']}/100")
    print(f"  Stress Level: {result['stress_level'].upper()}")

    print(f"\n  Category Scores:")
    for category, score in result['category_scores'].items():
        bar = '█' * int(score / 5) + '░' * (20 - int(score / 5))
        print(f"    {category:12}: {bar} {score:.1f}")

    if result['areas_of_concern']:
        print(f"\n  ⚠️  Areas of Concern: {', '.join(result['areas_of_concern'])}")

    # Get interpretation
    interpretation = scorer.get_score_interpretation(result)
    print(f"\n  💬 Interpretation: {interpretation['overall_interpretation']}")

# Summary
print("\n" + "=" * 60)
print("TEST SUMMARY")
print("=" * 60)

summary_data = []
for idx, row in test_data.iterrows():
    user_data = row.to_dict()
    result = scorer.calculate_overall_score(user_data)
    summary_data.append({
        'User': user_data['name'],
        'Overall Score': result['overall_score'],
        'Stress Level': result['stress_level'],
        'Expected': user_data['expected_result']
    })

summary_df = pd.DataFrame(summary_data)
print("\n")
print(summary_df.to_string(index=False))

print("\n" + "=" * 60)
print("SCORING TEST COMPLETE!")
print("=" * 60)
