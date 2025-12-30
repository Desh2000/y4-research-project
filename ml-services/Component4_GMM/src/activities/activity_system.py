"""
Activity System - Master Integration Module

Integrates all activity recommendation components:
- Individual recommendations
- Pair/social recommendations
- Progress tracking
- Effectiveness analysis

Author: [Your Name]
Date: [Current Date]
"""

import pandas as pd
import numpy as np
from typing import Dict, List, Optional
import os
import sys

# Add src to path for imports
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from activities.activity_database import ActivityDatabase
from activities.activity_recommender import ActivityRecommender
from activities.social_activity_matcher import SocialActivityMatcher
from activities.progress_tracker import ProgressTracker
from activities.effectiveness_analyzer import EffectivenessAnalyzer


class ActivitySystem:
    """
    Master activity recommendation and tracking system
    """

    def __init__(self,
                 activity_database: ActivityDatabase,
                 activity_recommender: ActivityRecommender,
                 social_matcher: SocialActivityMatcher,
                 progress_tracker: ProgressTracker):
        """
        Initialize complete activity system

        Parameters:
            activity_database: ActivityDatabase instance
            activity_recommender: ActivityRecommender instance
            social_matcher: SocialActivityMatcher instance
            progress_tracker: ProgressTracker instance
        """
        self.db = activity_database
        self.recommender = activity_recommender
        self.social_matcher = social_matcher
        self.tracker = progress_tracker
        self.analyzer = EffectivenessAnalyzer(progress_tracker)

        print(f"\n{'=' * 60}")
        print(f"ACTIVITY SYSTEM INITIALIZED")
        print(f"{'=' * 60}")
        print(f"✓ Activity Database: {len(self.db.activities)} activities")
        print(f"✓ Social Activities: {len(self.social_matcher.social_activities)}")
        print(f"✓ Progress Tracking: {len(self.tracker.progress_data)} records")
        print(f"{'=' * 60}\n")

    def get_complete_recommendations(self,
                                     user_id: str,
                                     user_features: pd.DataFrame,
                                     peer_id: Optional[str] = None,
                                     n_individual: int = 5,
                                     n_pair: int = 3) -> Dict:
        """
        Get complete recommendation suite for a user

        Parameters:
            user_id (str): User ID
            user_features (pd.DataFrame): User feature data
            peer_id (str): Optional peer partner ID
            n_individual (int): Number of individual recommendations
            n_pair (int): Number of pair recommendations

        Returns:
            dict: Complete recommendations package
        """
        print(f"\n{'=' * 70}")
        print(f"COMPLETE RECOMMENDATIONS FOR USER: {user_id}")
        print(f"{'=' * 70}\n")

        # Get user profile
        user_row = user_features[user_features['user_id'] == user_id]

        if len(user_row) == 0:
            print(f"User {user_id} not found")
            return {}

        user_profile = {
            'emotional_regulation_score': user_row.iloc[0]['emotional_regulation_score'],
            'social_connectivity_score': user_row.iloc[0]['social_connectivity_score'],
            'behavioral_stability_score': user_row.iloc[0]['behavioral_stability_score'],
            'cognitive_flexibility_score': user_row.iloc[0]['cognitive_flexibility_score'],
            'stress_coping_mechanism': user_row.iloc[0]['stress_coping_mechanism']
        }

        # 1. Individual recommendations
        print("1. Getting individual recommendations...")
        individual_recs = self.recommender.recommend_activities(
            user_profile,
            n_recommendations=n_individual
        )

        recommendations = {
            'user_id': user_id,
            'individual_activities': individual_recs,
            'pair_activities': None,
            'current_progress': None,
            'profile': user_profile
        }

        # 2. Pair recommendations (if peer provided)
        if peer_id:
            print(f"\n2. Getting pair recommendations with {peer_id}...")
            pair_recs = self.social_matcher.recommend_pair_activities(
                user_id,
                peer_id,
                user_features,
                n_recommendations=n_pair
            )
            recommendations['pair_activities'] = pair_recs
            recommendations['peer_id'] = peer_id

        # 3. Current progress
        print("\n3. Getting current progress...")
        current_progress = self.tracker.get_user_progress(user_id)
        recommendations['current_progress'] = current_progress

        print(f"\n{'=' * 70}")
        print(f"RECOMMENDATIONS SUMMARY")
        print(f"{'=' * 70}")
        print(f"Individual Activities: {len(individual_recs)}")
        if peer_id and recommendations['pair_activities'] is not None:
            print(f"Pair Activities: {len(recommendations['pair_activities'])}")
        print(f"Activities in Progress: {len(current_progress[current_progress['status'] == 'in_progress'])}")
        print(f"Completed Activities: {len(current_progress[current_progress['status'] == 'completed'])}")
        print(f"{'=' * 70}\n")

        return recommendations

    def start_user_activity(self,
                            user_id: str,
                            activity_id: str,
                            user_features: pd.DataFrame,
                            duration_days: int = 14) -> str:
        """
        Start an activity for a user with full tracking

        Parameters:
            user_id (str): User ID
            activity_id (str): Activity ID
            user_features (pd.DataFrame): User feature data
            duration_days (int): Expected duration

        Returns:
            str: Tracking ID
        """
        # Get activity details
        activity = self.db.get_activity(activity_id)

        if not activity:
            print(f"Activity {activity_id} not found")
            return None

        # Get user baseline
        user_row = user_features[user_features['user_id'] == user_id]

        if len(user_row) == 0:
            print(f"User {user_id} not found")
            return None

        baseline_scores = {
            'emotional_regulation_score': user_row.iloc[0]['emotional_regulation_score'],
            'social_connectivity_score': user_row.iloc[0]['social_connectivity_score'],
            'behavioral_stability_score': user_row.iloc[0]['behavioral_stability_score'],
            'cognitive_flexibility_score': user_row.iloc[0]['cognitive_flexibility_score'],
            'stress_coping_mechanism': user_row.iloc[0]['stress_coping_mechanism']
        }

        # Get expected improvements
        expected_improvements = {
            'target_emotional': activity['target_emotional'],
            'target_social': activity['target_social'],
            'target_behavioral': activity['target_behavioral'],
            'target_cognitive': activity['target_cognitive'],
            'target_stress': activity['target_stress']
        }

        # Start tracking
        tracking_id = self.tracker.start_activity(
            user_id=user_id,
            activity_id=activity_id,
            activity_name=activity['name'],
            baseline_scores=baseline_scores,
            expected_improvements=expected_improvements,
            duration_days=duration_days
        )

        return tracking_id

    def generate_system_report(self, output_dir: str = "results/activity_system"):
        """
        Generate comprehensive system-wide report

        Parameters:
            output_dir (str): Output directory
        """
        os.makedirs(output_dir, exist_ok=True)

        print(f"\n{'=' * 70}")
        print(f"GENERATING COMPREHENSIVE ACTIVITY SYSTEM REPORT")
        print(f"{'=' * 70}\n")

        # 1. Activity catalog summary
        print("1. Activity Catalog Analysis...")
        all_activities = self.db.get_all_activities()

        catalog_summary = {
            'total_activities': len(all_activities),
            'by_category': all_activities['category'].value_counts().to_dict(),
            'by_difficulty': all_activities['difficulty'].value_counts().to_dict(),
            'by_social_format': all_activities['social_format'].value_counts().to_dict(),
            'avg_duration': all_activities['duration_minutes'].mean()
        }

        pd.DataFrame([catalog_summary]).to_csv(f"{output_dir}/catalog_summary.csv", index=False)

        # 2. Effectiveness analysis
        print("2. Effectiveness Analysis...")
        self.analyzer.generate_effectiveness_report(output_dir=f"{output_dir}/effectiveness")

        # 3. Overall statistics
        print("3. Overall Statistics...")
        overall_stats = self.tracker.get_overall_statistics()

        pd.DataFrame([overall_stats]).to_csv(f"{output_dir}/overall_statistics.csv", index=False)

        print(f"\n{'=' * 70}")
        print(f"✓ REPORT COMPLETE")
        print(f"  Location: {output_dir}/")
        print(f"{'=' * 70}\n")


# Testing code
if __name__ == "__main__":
    """
    Test the complete Activity System
    """
    # Get the project root directory (Component4_GMM)
    SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
    PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))

    # Add clustering to path
    sys.path.append(os.path.join(PROJECT_ROOT, "src", "clustering"))

    print("Testing Complete Activity System")
    print("=" * 70)

    # Load data
    print("\n1. Loading data...")
    user_data_path = os.path.join(PROJECT_ROOT, "data", "processed", "resilience_indicators_normalized.csv")
    user_data = pd.read_csv(user_data_path)

    # Initialize all components
    print("\n2. Initializing all components...")

    from gmm_model import GMMClusterer
    from community.community_manager import CommunityManager
    from community.peer_matcher import PeerMatcher

    # Activity components
    activity_db = ActivityDatabase()
    activity_recommender = ActivityRecommender(activity_db)
    progress_tracker = ProgressTracker()

    # Community components
    assignments_path = os.path.join(PROJECT_ROOT, "results", "clusters", "cluster_assignments.csv")
    model_path = os.path.join(PROJECT_ROOT, "models", "gmm_model_v1.pkl")
    assignments = pd.read_csv(assignments_path)
    clusterer = GMMClusterer.load_model(model_path)
    cluster_info = clusterer.get_cluster_info()

    feature_cols = [
        'emotional_regulation_score',
        'social_connectivity_score',
        'behavioral_stability_score',
        'cognitive_flexibility_score',
        'stress_coping_mechanism'
    ]

    community_manager = CommunityManager(
        assignments_df=assignments,
        cluster_centers=cluster_info,
        feature_columns=feature_cols
    )

    if not community_manager.communities:
        community_manager.create_communities()

    # Get first community members for peer matcher
    first_community_id = list(community_manager.communities.keys())[0]
    first_community = community_manager.communities[first_community_id]
    community_members = user_data[user_data['user_id'].isin(first_community.members)]

    peer_matcher = PeerMatcher(
        user_features=community_members,
        feature_columns=feature_cols
    )

    # Social activity matcher
    social_matcher = SocialActivityMatcher(
        activity_database=activity_db,
        activity_recommender=activity_recommender,
        peer_matcher=peer_matcher
    )

    # Complete system
    print("\n3. Initializing Activity System...")
    activity_system = ActivitySystem(
        activity_database=activity_db,
        activity_recommender=activity_recommender,
        social_matcher=social_matcher,
        progress_tracker=progress_tracker
    )

    # Test: Complete recommendations
    print("\n4. Testing complete recommendations...")
    test_user = user_data.iloc[0]['user_id']
    test_peer = user_data.iloc[1]['user_id']

    complete_recs = activity_system.get_complete_recommendations(
        user_id=test_user,
        user_features=user_data,
        peer_id=test_peer,
        n_individual=5,
        n_pair=3
    )

    # Test: Start activity
    print("\n5. Testing activity start...")
    if len(complete_recs['individual_activities']) > 0:
        top_activity_id = complete_recs['individual_activities'].iloc[0]['activity_id']

        tracking_id = activity_system.start_user_activity(
            user_id=test_user,
            activity_id=top_activity_id,
            user_features=user_data,
            duration_days=14
        )

        print(f"\n✓ Activity started: {tracking_id}")

    # Test: System report
    print("\n6. Generating system report...")
    report_dir = os.path.join(PROJECT_ROOT, "results", "activity_system")
    activity_system.generate_system_report(output_dir=report_dir)

    print("\n" + "=" * 70)
    print("✓ ALL TESTS PASSED!")
    print("=" * 70)