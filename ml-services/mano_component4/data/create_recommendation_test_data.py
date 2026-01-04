# data/create_recommendation_test_data.py
# Creates test data for testing the recommendation service

"""
Recommendation Test Data Generator
==================================
Creates test users with different problem profiles to verify
recommendations are appropriate.
"""

import pandas as pd
import os

os.makedirs('data', exist_ok=True)

print("="*60)
print("CREATING RECOMMENDATION TEST DATA")
print("="*60)

# Test cases with different problem profiles
test_cases = [
    {
        'test_id': 'REC_TEST_001',
        'name': 'High Stress Worker',
        'description': 'Very stressed, needs calming activities',
        'scores': {
            'overall_score': 35,
            'stress_level': 'high',
            'category_scores': {
                'body': 55,
                'behavior': 40,
                'emotional': 20,  # Very low - main problem
                'social': 45
            }
        },
        'expected_categories': ['stress_relief', 'mindfulness', 'emotional'],
        'should_include_professional': True
    },
    {
        'test_id': 'REC_TEST_002',
        'name': 'Sleep Deprived Student',
        'description': 'Poor sleep, tired all day',
        'scores': {
            'overall_score': 42,
            'stress_level': 'medium',
            'category_scores': {
                'body': 25,  # Very low - main problem
                'behavior': 50,
                'emotional': 55,
                'social': 60
            }
        },
        'expected_categories': ['sleep', 'physical'],
        'should_include_professional': False
    },
    {
        'test_id': 'REC_TEST_003',
        'name': 'Lonely Remote Worker',
        'description': 'Isolated, few social connections',
        'scores': {
            'overall_score': 48,
            'stress_level': 'medium',
            'category_scores': {
                'body': 70,
                'behavior': 55,
                'emotional': 45,
                'social': 18  # Very low - main problem
            }
        },
        'expected_categories': ['social'],
        'should_include_professional': False
    },
    {
        'test_id': 'REC_TEST_004',
        'name': 'Chaotic Lifestyle',
        'description': 'No routine, bad habits',
        'scores': {
            'overall_score': 45,
            'stress_level': 'medium',
            'category_scores': {
                'body': 50,
                'behavior': 22,  # Very low - main problem
                'emotional': 55,
                'social': 65
            }
        },
        'expected_categories': ['routine'],
        'should_include_professional': False
    },
    {
        'test_id': 'REC_TEST_005',
        'name': 'Multiple Problems',
        'description': 'Struggling in many areas',
        'scores': {
            'overall_score': 25,
            'stress_level': 'very_high',
            'category_scores': {
                'body': 30,
                'behavior': 25,
                'emotional': 15,
                'social': 20
            }
        },
        'expected_categories': ['professional', 'stress_relief', 'emotional'],
        'should_include_professional': True
    },
    {
        'test_id': 'REC_TEST_006',
        'name': 'Healthy Person',
        'description': 'All scores good - general wellness',
        'scores': {
            'overall_score': 82,
            'stress_level': 'very_low',
            'category_scores': {
                'body': 85,
                'behavior': 80,
                'emotional': 82,
                'social': 78
            }
        },
        'expected_categories': ['general_wellness'],
        'should_include_professional': False
    },
]

# Save test cases
import json

with open('data/recommendation_test_cases.json', 'w') as f:
    json.dump(test_cases, f, indent=2)

print(f"\n✓ Created {len(test_cases)} test cases")
print(f"✓ Saved to: data/recommendation_test_cases.json")

print("\n" + "-"*60)
print("TEST CASES CREATED:")
print("-"*60)

for case in test_cases:
    print(f"\n{case['test_id']}: {case['name']}")
    print(f"  Description: {case['description']}")
    print(f"  Stress Level: {case['scores']['stress_level']}")
    print(f"  Expected Categories: {', '.join(case['expected_categories'])}")
    print(f"  Should Include Professional: {case['should_include_professional']}")

print("\n" + "="*60)
print("RECOMMENDATION TEST DATA READY!")
print("="*60)
