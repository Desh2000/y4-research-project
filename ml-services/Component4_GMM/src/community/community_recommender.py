"""
Community Recommender Module

Integrates activity recommendations with community management.

Author: [Your Name]
Date: [Current Date]
"""

import pandas as pd
import numpy as np
from typing import List, Dict
import sys
import os

# Add src to path
sys.path.append(os.path.dirname(os.path.dirname(__file__)))

from activities.activity_database import ActivityDatabase
from activities.activity_recommender import ActivityRecommender
from community.community_manager import CommunityManager


class CommunityRecommender:
    """
    Provides activity recommendations at both individual and community levels
    """

    def __init__(self,
                 community_manager: CommunityManager,
                 activity_recommender: ActivityRecommender):
        """
        Initialize community recommender

        Parameters:
            community_manager (CommunityManager): Community manager instance
            activity_recommender (ActivityRecommender): Activity recommender instance
        """
        self.community_manager = community_manager
        self.activity_recommender = activity_recommender

        print(f"✓ CommunityRecommender initialized")

    def recommend_for_user(self,
                           user_id: str,
                           user_features: pd.DataFrame,
                           n_recommendations: int = 5) -> pd.DataFrame:
        """
        Get personalized recommendations for a specific user

        Parameters:
            user_id (str): User ID
            user_features (pd.DataFrame): User feature data
            n_recommendations (int): Number of recommendations

        Returns:
            pd.DataFrame: Recommended activities
        """
        print(f"\n{'=' * 60}")
        print(f"RECOMMENDATIONS FOR USER: {user_id}")
        print(f"{'=' * 60}\n")

        # Get user's resilience profile
        user_row = user_features[user_features['user_id'] == user_id]

        if len(user_row) == 0:
            print(f"User {user_id} not found")
            return pd.DataFrame()

        user_profile = {
            'emotional_regulation_score': user_row.iloc[0]['emotional_regulation_score'],
            'social_connectivity_score': user_row.iloc[0]['social_connectivity_score'],
            'behavioral_stability_score': user_row.iloc[0]['behavioral_stability_score'],
            'cognitive_flexibility_score': user_row.iloc[0]['cognitive_flexibility_score'],
            'stress_coping_mechanism': user_row.iloc[0]['stress_coping_mechanism']
        }

        # Get user's community
        community = self.community_manager.get_user_community(user_id)

        if community:
            print(f"User is in community: {community.name}")
            print(f"Community size: {community.size} members\n")

        # Get recommendations
        recommendations = self.activity_recommender.recommend_activities(
            user_profile,
            n_recommendations=n_recommendations
        )

        # Add community context
        recommendations['user_id'] = user_id
        if community:
            recommendations['community_id'] = community.community_id
            recommendations['community_name'] = community.name

        return recommendations

    def recommend_for_community(self,
                                community_id: str,
                                user_features: pd.DataFrame,
                                n_recommendations: int = 5) -> pd.DataFrame:
        """
        Get recommendations suitable for entire community

        Parameters:
            community_id (str): Community ID
            user_features (pd.DataFrame): User feature data
            n_recommendations (int): Number of recommendations

        Returns:
            pd.DataFrame: Recommended community activities
        """
        print(f"\n{'=' * 60}")
        print(f"COMMUNITY RECOMMENDATIONS: {community_id}")
        print(f"{'=' * 60}\n")

        # Get community
        community = self.community_manager.get_community(community_id)

        if not community:
            print(f"Community {community_id} not found")
            return pd.DataFrame()

        print(f"Community: {community.name}")
        print(f"Members: {community.size}")

        # Calculate average resilience profile for community
        community_members = user_features[user_features['user_id'].isin(community.members)]

        avg_profile = {
            'emotional_regulation_score': community_members['emotional_regulation_score'].mean(),
            'social_connectivity_score': community_members['social_connectivity_score'].mean(),
            'behavioral_stability_score': community_members['behavioral_stability_score'].mean(),
            'cognitive_flexibility_score': community_members['cognitive_flexibility_score'].mean(),
            'stress_coping_mechanism': community_members['stress_coping_mechanism'].mean()
        }

        print(f"\nCommunity Average Resilience Profile:")
        for dim, score in avg_profile.items():
            print(f"  {dim.replace('_', ' ').title()}: {score:.2f}")

        # Get recommendations for community profile
        # Filter to group activities only
        recommendations = self.activity_recommender.recommend_activities(
            avg_profile,
            n_recommendations=n_recommendations * 2,  # Get more to filter
            format_filter="Group"
        )

        # Take top N group activities
        recommendations = recommendations.head(n_recommendations)

        # Add community info
        recommendations['community_id'] = community_id
        recommendations['community_name'] = community.name
        recommendations['recommended_for'] = 'Community Group Activity'

        return recommendations


# Testing code
if __name__ == "__main__":
    """
    Test the Community Recommender
    """
    # Get the project root directory (Component4_GMM)
    SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
    PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))

    # Add clustering to path
    sys.path.append(os.path.join(PROJECT_ROOT, "src", "clustering"))

    print("Testing Community Recommender Module")
    print("=" * 60)

    # Load data
    print("\n1. Loading data...")
    user_data_path = os.path.join(PROJECT_ROOT, "data", "processed", "resilience_indicators_normalized.csv")
    assignments_path = os.path.join(PROJECT_ROOT, "results", "clusters", "cluster_assignments.csv")
    user_data = pd.read_csv(user_data_path)
    assignments = pd.read_csv(assignments_path)

    # Load models
    print("\n2. Loading models...")
    from gmm_model import GMMClusterer

    model_path = os.path.join(PROJECT_ROOT, "models", "gmm_model_v1.pkl")
    clusterer = GMMClusterer.load_model(model_path)
    cluster_info = clusterer.get_cluster_info()

    feature_cols = [
        'emotional_regulation_score',
        'social_connectivity_score',
        'behavioral_stability_score',
        'cognitive_flexibility_score',
        'stress_coping_mechanism'
    ]

    # Initialize systems
    print("\n3. Initializing systems...")

    # Activity system
    activity_db = ActivityDatabase()
    activity_recommender = ActivityRecommender(activity_db)

    # Community system
    community_manager = CommunityManager(
        assignments_df=assignments,
        cluster_centers=cluster_info,
        feature_columns=feature_cols
    )

    # Create communities if not already created
    if not community_manager.communities:
        community_manager.create_communities()

    # Community recommender
    community_recommender = CommunityRecommender(
        community_manager=community_manager,
        activity_recommender=activity_recommender
    )

    # Test 1: Individual user recommendations
    print("\n" + "=" * 60)
    print("TEST 1: Individual User Recommendations")
    print("=" * 60)

    test_user = user_data.iloc[0]['user_id']
    user_recs = community_recommender.recommend_for_user(
        test_user,
        user_data,
        n_recommendations=5
    )

    print(f"\nSaving recommendations...")
    user_recs_path = os.path.join(PROJECT_ROOT, "results", "communities", f"recommendations_{test_user}.csv")
    os.makedirs(os.path.dirname(user_recs_path), exist_ok=True)
    user_recs.to_csv(user_recs_path, index=False)
    print(f"✓ Saved to: {user_recs_path}")

    # Test 2: Community recommendations
    print("\n" + "=" * 60)
    print("TEST 2: Community Group Recommendations")
    print("=" * 60)

    test_community = list(community_manager.communities.keys())[0]
    community_recs = community_recommender.recommend_for_community(
        test_community,
        user_data,
        n_recommendations=5
    )

    print(f"\nSaving community recommendations...")
    comm_recs_path = os.path.join(PROJECT_ROOT, "results", "communities", f"community_recommendations_{test_community}.csv")
    community_recs.to_csv(comm_recs_path, index=False)
    print(f"✓ Saved to: {comm_recs_path}")

    print("\n" + "=" * 60)
    print("✓ ALL TESTS PASSED!")
    print("=" * 60)