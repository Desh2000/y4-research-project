"""
Activity Recommender Module

Implements personalized activity recommendation using
content-based filtering matched to user resilience profiles.

Author: [Your Name]
Date: [Current Date]
"""

import pandas as pd
import numpy as np
from typing import List, Dict, Tuple, Optional
import sys
import os

# Add src to path
sys.path.append(os.path.dirname(os.path.dirname(__file__)))

from activities.activity_database import ActivityDatabase


class ActivityRecommender:
    """
    Recommends personalized activities based on user resilience profiles
    """

    # Resilience dimension names
    DIMENSIONS = [
        'emotional_regulation_score',
        'social_connectivity_score',
        'behavioral_stability_score',
        'cognitive_flexibility_score',
        'stress_coping_mechanism'
    ]

    # Mapping to activity target columns
    TARGET_MAPPING = {
        'emotional_regulation_score': 'target_emotional',
        'social_connectivity_score': 'target_social',
        'behavioral_stability_score': 'target_behavioral',
        'cognitive_flexibility_score': 'target_cognitive',
        'stress_coping_mechanism': 'target_stress'
    }

    # Thresholds
    WEAKNESS_THRESHOLD = 0.50  # Below this = weakness
    STRENGTH_THRESHOLD = 0.60  # Above this = strength

    def __init__(self, activity_database: ActivityDatabase):
        """
        Initialize recommender

        Parameters:
            activity_database (ActivityDatabase): Activity database instance
        """
        self.db = activity_database
        self.activities = self.db.get_all_activities()

        print(f"✓ ActivityRecommender initialized")
        print(f"  Available activities: {len(self.activities)}")

    def analyze_user_profile(self, user_profile: Dict[str, float]) -> Dict:
        """
        Analyze user's resilience profile to identify strengths and weaknesses

        Parameters:
            user_profile (dict): User's resilience scores

        Returns:
            dict: Analysis with weaknesses, strengths, priorities
        """
        weaknesses = []
        strengths = []
        moderate = []

        for dimension in self.DIMENSIONS:
            score = user_profile.get(dimension, 0.5)

            if score < self.WEAKNESS_THRESHOLD:
                weaknesses.append({
                    'dimension': dimension,
                    'score': score,
                    'gap': self.WEAKNESS_THRESHOLD - score
                })
            elif score > self.STRENGTH_THRESHOLD:
                strengths.append({
                    'dimension': dimension,
                    'score': score
                })
            else:
                moderate.append({
                    'dimension': dimension,
                    'score': score
                })

        # Sort weaknesses by gap (biggest gap = highest priority)
        weaknesses = sorted(weaknesses, key=lambda x: x['gap'], reverse=True)

        analysis = {
            'weaknesses': weaknesses,
            'strengths': strengths,
            'moderate': moderate,
            'primary_weakness': weaknesses[0] if weaknesses else None,
            'primary_strength': strengths[0] if strengths else None
        }

        return analysis

    def calculate_activity_score(self,
                                 activity: pd.Series,
                                 user_profile: Dict[str, float],
                                 analysis: Dict) -> Tuple[float, Dict]:
        """
        Calculate recommendation score for an activity

        Parameters:
            activity (pd.Series): Activity data
            user_profile (dict): User resilience profile
            analysis (dict): User profile analysis

        Returns:
            tuple: (score, explanation_dict)
        """
        score = 0.0
        explanation = {
            'base_impact': 0.0,
            'weakness_bonus': 0.0,
            'strength_leverage': 0.0,
            'feasibility_bonus': 0.0,
            'multi_dimension_bonus': 0.0,
            'reasons': []
        }

        # 1. BASE IMPACT SCORE (0-0.20)
        # Sum of improvements to all dimensions
        total_impact = 0
        for dimension, target_col in self.TARGET_MAPPING.items():
            impact = activity[target_col]
            total_impact += impact

        explanation['base_impact'] = min(total_impact, 0.20)
        score += explanation['base_impact']

        # 2. WEAKNESS TARGETING BONUS (0-0.40)
        # High bonus for targeting user's weaknesses
        if analysis['weaknesses']:
            for i, weakness in enumerate(analysis['weaknesses'][:3]):  # Top 3 weaknesses
                dimension = weakness['dimension']
                target_col = self.TARGET_MAPPING[dimension]
                impact = activity[target_col]

                if impact > 0:
                    # Priority bonus: 1st weakness = 0.20, 2nd = 0.15, 3rd = 0.10
                    priority_bonus = [0.20, 0.15, 0.10][i]
                    # Scale by impact size
                    bonus = priority_bonus * (impact / 0.20)  # Normalize to max impact
                    explanation['weakness_bonus'] += bonus

                    weakness_name = dimension.replace('_', ' ').title()
                    explanation['reasons'].append(
                        f"Targets weakness: {weakness_name} (+{impact:.2f})"
                    )

        score += explanation['weakness_bonus']

        # 3. STRENGTH LEVERAGE BONUS (0-0.15)
        # Bonus for activities that leverage user's strengths
        if analysis['strengths']:
            for strength in analysis['strengths']:
                dimension = strength['dimension']

                # Check if activity requires this strength
                if dimension == 'behavioral_stability_score':
                    required = activity['required_min_behavioral']
                    if required > 0 and user_profile[dimension] >= required:
                        explanation['strength_leverage'] += 0.08
                        strength_name = dimension.replace('_', ' ').title()
                        explanation['reasons'].append(
                            f"Leverages strength: {strength_name}"
                        )

                elif dimension == 'social_connectivity_score':
                    required = activity['required_min_social']
                    if required > 0 and user_profile[dimension] >= required:
                        explanation['strength_leverage'] += 0.08
                        strength_name = dimension.replace('_', ' ').title()
                        explanation['reasons'].append(
                            f"Leverages strength: {strength_name}"
                        )

        score += explanation['strength_leverage']

        # 4. FEASIBILITY BONUS (0-0.15)
        # Check if user meets requirements
        behavioral_ok = user_profile.get('behavioral_stability_score', 0) >= activity['required_min_behavioral']
        social_ok = user_profile.get('social_connectivity_score', 0) >= activity['required_min_social']

        if behavioral_ok and social_ok:
            explanation['feasibility_bonus'] = 0.15
            explanation['reasons'].append("Meets all requirements (achievable)")
        elif behavioral_ok or social_ok:
            explanation['feasibility_bonus'] = 0.08
            explanation['reasons'].append("Meets some requirements")
        else:
            explanation['feasibility_bonus'] = 0.0
            explanation['reasons'].append("⚠ May be challenging (doesn't meet requirements)")

        score += explanation['feasibility_bonus']

        # 5. MULTI-DIMENSION BONUS (0-0.10)
        # Bonus for activities that improve multiple dimensions
        dimensions_improved = sum([
            1 for col in self.TARGET_MAPPING.values()
            if activity[col] > 0.05  # Meaningful impact
        ])

        if dimensions_improved >= 3:
            explanation['multi_dimension_bonus'] = 0.10
            explanation['reasons'].append(f"Improves {dimensions_improved} dimensions")
        elif dimensions_improved == 2:
            explanation['multi_dimension_bonus'] = 0.05

        score += explanation['multi_dimension_bonus']

        # Add activity metadata to explanation
        explanation['activity_name'] = activity['name']
        explanation['difficulty'] = activity['difficulty']
        explanation['duration'] = activity['duration_minutes']
        explanation['social_format'] = activity['social_format']

        return score, explanation

    def recommend_activities(self,
                             user_profile: Dict[str, float],
                             n_recommendations: int = 5,
                             difficulty_filter: Optional[str] = None,
                             format_filter: Optional[str] = None) -> pd.DataFrame:
        """
        Generate personalized activity recommendations

        Parameters:
            user_profile (dict): User's resilience scores
            n_recommendations (int): Number of recommendations to return
            difficulty_filter (str): Optional difficulty filter
            format_filter (str): Optional social format filter

        Returns:
            pd.DataFrame: Recommended activities with scores and explanations
        """
        print(f"\n{'=' * 60}")
        print(f"GENERATING PERSONALIZED ACTIVITY RECOMMENDATIONS")
        print(f"{'=' * 60}\n")

        # Analyze user profile
        print("Analyzing user profile...")
        analysis = self.analyze_user_profile(user_profile)

        print(f"\nWeaknesses identified: {len(analysis['weaknesses'])}")
        for i, weakness in enumerate(analysis['weaknesses'][:3], 1):
            dim_name = weakness['dimension'].replace('_', ' ').title()
            print(f"  {i}. {dim_name}: {weakness['score']:.2f} (Priority {i})")

        print(f"\nStrengths identified: {len(analysis['strengths'])}")
        for strength in analysis['strengths']:
            dim_name = strength['dimension'].replace('_', ' ').title()
            print(f"  • {dim_name}: {strength['score']:.2f}")

        # Filter activities
        activities = self.activities.copy()

        if difficulty_filter:
            activities = activities[activities['difficulty'] == difficulty_filter]
            print(f"\nFiltered to {difficulty_filter} activities: {len(activities)}")

        if format_filter:
            activities = activities[activities['social_format'].str.contains(format_filter)]
            print(f"Filtered to {format_filter} format: {len(activities)}")

        # Calculate scores for all activities
        print(f"\nScoring {len(activities)} activities...")

        recommendations = []

        for _, activity in activities.iterrows():
            score, explanation = self.calculate_activity_score(
                activity,
                user_profile,
                analysis
            )

            recommendations.append({
                'activity_id': activity['activity_id'],
                'name': activity['name'],
                'category': activity['category'],
                'score': score,
                'difficulty': activity['difficulty'],
                'duration_minutes': activity['duration_minutes'],
                'social_format': activity['social_format'],
                'base_impact': explanation['base_impact'],
                'weakness_bonus': explanation['weakness_bonus'],
                'strength_leverage': explanation['strength_leverage'],
                'feasibility_bonus': explanation['feasibility_bonus'],
                'multi_dimension_bonus': explanation['multi_dimension_bonus'],
                'reasons': ' | '.join(explanation['reasons'][:3])  # Top 3 reasons
            })

        # Convert to DataFrame and sort by score
        recommendations_df = pd.DataFrame(recommendations)
        recommendations_df = recommendations_df.sort_values('score', ascending=False)

        # Get top N
        top_recommendations = recommendations_df.head(n_recommendations)

        print(f"\n{'=' * 60}")
        print(f"TOP {n_recommendations} RECOMMENDATIONS")
        print(f"{'=' * 60}\n")

        for i, rec in top_recommendations.iterrows():
            print(f"{rec.name + 1}. {rec['name']}")
            print(f"   Score: {rec['score']:.2f} | {rec['difficulty']} | {rec['duration_minutes']} min")
            print(f"   Reasons: {rec['reasons']}")
            print()

        return top_recommendations

    def explain_recommendation(self, activity_id: str, user_profile: Dict[str, float]):
        """
        Provide detailed explanation for why an activity is recommended

        Parameters:
            activity_id (str): Activity ID
            user_profile (dict): User resilience profile
        """
        # Get activity
        activity = self.activities[self.activities['activity_id'] == activity_id]

        if len(activity) == 0:
            print(f"Activity {activity_id} not found")
            return

        activity = activity.iloc[0]

        # Analyze user
        analysis = self.analyze_user_profile(user_profile)

        # Calculate score
        score, explanation = self.calculate_activity_score(
            activity,
            user_profile,
            analysis
        )

        # Print detailed explanation
        print(f"\n{'=' * 70}")
        print(f"RECOMMENDATION EXPLANATION: {activity['name']}")
        print(f"{'=' * 70}\n")

        print(f"Overall Recommendation Score: {score:.2f}/1.00")
        print(f"Difficulty: {activity['difficulty']}")
        print(f"Duration: {activity['duration_minutes']} minutes")
        print(f"Social Format: {activity['social_format']}")

        print(f"\nScore Breakdown:")
        print(f"  Base Impact Score:        {explanation['base_impact']:.2f}")
        print(f"  Weakness Targeting Bonus: {explanation['weakness_bonus']:.2f}")
        print(f"  Strength Leverage Bonus:  {explanation['strength_leverage']:.2f}")
        print(f"  Feasibility Bonus:        {explanation['feasibility_bonus']:.2f}")
        print(f"  Multi-Dimension Bonus:    {explanation['multi_dimension_bonus']:.2f}")
        print(f"  ─────────────────────────────────")
        print(f"  Total:                    {score:.2f}")

        print(f"\nWhy This Activity is Recommended:")
        for reason in explanation['reasons']:
            print(f"  ✓ {reason}")

        print(f"\nExpected Resilience Improvements:")
        for dimension, target_col in self.TARGET_MAPPING.items():
            impact = activity[target_col]
            if impact > 0:
                dim_name = dimension.replace('_', ' ').title()
                current = user_profile.get(dimension, 0)
                expected = min(current + impact, 1.0)
                improvement_pct = (impact / current * 100) if current > 0 else 0

                print(f"  • {dim_name}:")
                print(
                    f"      Current: {current:.2f} → Expected: {expected:.2f} (+{impact:.2f}, {improvement_pct:.1f}% improvement)")

        print(f"\n{'=' * 70}\n")


# Testing code
if __name__ == "__main__":
    """
    Test the Activity Recommender
    """
    print("Testing Activity Recommender Module")
    print("=" * 60)

    # Initialize database and recommender
    print("\n1. Initializing system...")
    db = ActivityDatabase()
    recommender = ActivityRecommender(db)

    # Test Case 1: User with low emotional regulation and behavioral stability
    print("\n" + "=" * 60)
    print("TEST CASE 1: Low emotional & behavioral, high social")
    print("=" * 60)

    user_profile_1 = {
        'emotional_regulation_score': 0.30,
        'social_connectivity_score': 0.65,
        'behavioral_stability_score': 0.28,
        'cognitive_flexibility_score': 0.50,
        'stress_coping_mechanism': 0.35
    }

    print("\nUser Profile:")
    for dim, score in user_profile_1.items():
        print(f"  {dim.replace('_', ' ').title()}: {score:.2f}")

    recommendations_1 = recommender.recommend_activities(
        user_profile_1,
        n_recommendations=5
    )

    # Explain top recommendation
    print("\n" + "=" * 60)
    print("DETAILED EXPLANATION: Top Recommendation")
    print("=" * 60)
    top_activity_id = recommendations_1.iloc[0]['activity_id']
    recommender.explain_recommendation(top_activity_id, user_profile_1)

    # Test Case 2: User with low social connectivity
    print("\n" + "=" * 60)
    print("TEST CASE 2: Low social, everything else moderate")
    print("=" * 60)

    user_profile_2 = {
        'emotional_regulation_score': 0.55,
        'social_connectivity_score': 0.25,
        'behavioral_stability_score': 0.52,
        'cognitive_flexibility_score': 0.58,
        'stress_coping_mechanism': 0.50
    }

    print("\nUser Profile:")
    for dim, score in user_profile_2.items():
        print(f"  {dim.replace('_', ' ').title()}: {score:.2f}")

    recommendations_2 = recommender.recommend_activities(
        user_profile_2,
        n_recommendations=5
    )

    # Test Case 3: High resilience user
    print("\n" + "=" * 60)
    print("TEST CASE 3: High resilience across all dimensions")
    print("=" * 60)

    user_profile_3 = {
        'emotional_regulation_score': 0.85,
        'social_connectivity_score': 0.80,
        'behavioral_stability_score': 0.82,
        'cognitive_flexibility_score': 0.78,
        'stress_coping_mechanism': 0.75
    }

    print("\nUser Profile:")
    for dim, score in user_profile_3.items():
        print(f"  {dim.replace('_', ' ').title()}: {score:.2f}")

    recommendations_3 = recommender.recommend_activities(
        user_profile_3,
        n_recommendations=5
    )

    # Test filtering
    print("\n" + "=" * 60)
    print("TEST CASE 4: Filter by difficulty (Beginner only)")
    print("=" * 60)

    recommendations_beginner = recommender.recommend_activities(
        user_profile_1,
        n_recommendations=5,
        difficulty_filter="Beginner"
    )

    print("\n" + "=" * 60)
    print("✓ ALL TESTS PASSED!")
    print("=" * 60)