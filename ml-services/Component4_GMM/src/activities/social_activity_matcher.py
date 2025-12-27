"""
Social Activity Matcher Module

Matches users with social activities based on peer pairs
and creates accountability partnerships.

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
from activities.activity_recommender import ActivityRecommender
from community.peer_matcher import PeerMatcher


class SocialActivityMatcher:
    """
    Matches peer pairs/groups with appropriate social activities
    """

    def __init__(self,
                 activity_database: ActivityDatabase,
                 activity_recommender: ActivityRecommender,
                 peer_matcher: PeerMatcher):
        """
        Initialize social activity matcher

        Parameters:
            activity_database (ActivityDatabase): Activity database
            activity_recommender (ActivityRecommender): Activity recommender
            peer_matcher (PeerMatcher): Peer matcher instance
        """
        self.activity_db = activity_database
        self.recommender = activity_recommender
        self.peer_matcher = peer_matcher

        # Load social activities
        self.social_activities = self._load_social_activities()

        print(f"✓ SocialActivityMatcher initialized")
        print(f"  Social activities available: {len(self.social_activities)}")

    def _load_social_activities(self) -> pd.DataFrame:
        """Load activities suitable for pairs/groups"""
        all_activities = self.activity_db.get_all_activities()

        # Filter for social activities (Pair, Group, or Individual/Group)
        social = all_activities[
            (all_activities['social_format'].str.contains('Pair', na=False)) |
            (all_activities['social_format'].str.contains('Group', na=False))
            ].copy()

        return social

    def find_shared_weaknesses(self,
                               user1_profile: Dict[str, float],
                               user2_profile: Dict[str, float]) -> List[Dict]:
        """
        Find resilience dimensions where both users need improvement

        Parameters:
            user1_profile (dict): First user's resilience profile
            user2_profile (dict): Second user's resilience profile

        Returns:
            list: Shared weaknesses with average scores
        """
        WEAKNESS_THRESHOLD = 0.50
        dimensions = self.recommender.DIMENSIONS

        shared_weaknesses = []

        for dimension in dimensions:
            score1 = user1_profile.get(dimension, 0.5)
            score2 = user2_profile.get(dimension, 0.5)

            # Both below threshold = shared weakness
            if score1 < WEAKNESS_THRESHOLD and score2 < WEAKNESS_THRESHOLD:
                avg_score = (score1 + score2) / 2
                gap = WEAKNESS_THRESHOLD - avg_score

                shared_weaknesses.append({
                    'dimension': dimension,
                    'user1_score': score1,
                    'user2_score': score2,
                    'avg_score': avg_score,
                    'gap': gap
                })

        # Sort by gap (biggest gap = highest priority)
        shared_weaknesses = sorted(shared_weaknesses, key=lambda x: x['gap'], reverse=True)

        return shared_weaknesses

    def find_shared_strengths(self,
                              user1_profile: Dict[str, float],
                              user2_profile: Dict[str, float]) -> List[Dict]:
        """
        Find resilience dimensions where both users are strong

        Parameters:
            user1_profile (dict): First user's resilience profile
            user2_profile (dict): Second user's resilience profile

        Returns:
            list: Shared strengths
        """
        STRENGTH_THRESHOLD = 0.60
        dimensions = self.recommender.DIMENSIONS

        shared_strengths = []

        for dimension in dimensions:
            score1 = user1_profile.get(dimension, 0.5)
            score2 = user2_profile.get(dimension, 0.5)

            # Both above threshold = shared strength
            if score1 > STRENGTH_THRESHOLD and score2 > STRENGTH_THRESHOLD:
                avg_score = (score1 + score2) / 2

                shared_strengths.append({
                    'dimension': dimension,
                    'user1_score': score1,
                    'user2_score': score2,
                    'avg_score': avg_score
                })

        return shared_strengths

    def recommend_pair_activities(self,
                                  user1_id: str,
                                  user2_id: str,
                                  user_features: pd.DataFrame,
                                  n_recommendations: int = 5) -> pd.DataFrame:
        """
        Recommend activities suitable for a peer pair

        Parameters:
            user1_id (str): First user ID
            user2_id (str): Second user ID
            user_features (pd.DataFrame): User feature data
            n_recommendations (int): Number of recommendations

        Returns:
            pd.DataFrame: Recommended pair activities
        """
        print(f"\n{'=' * 70}")
        print(f"PAIR ACTIVITY RECOMMENDATIONS")
        print(f"{'=' * 70}")
        print(f"User 1: {user1_id}")
        print(f"User 2: {user2_id}\n")

        # Get user profiles
        user1_row = user_features[user_features['user_id'] == user1_id]
        user2_row = user_features[user_features['user_id'] == user2_id]

        if len(user1_row) == 0 or len(user2_row) == 0:
            print("One or both users not found")
            return pd.DataFrame()

        user1_profile = {
            'emotional_regulation_score': user1_row.iloc[0]['emotional_regulation_score'],
            'social_connectivity_score': user1_row.iloc[0]['social_connectivity_score'],
            'behavioral_stability_score': user1_row.iloc[0]['behavioral_stability_score'],
            'cognitive_flexibility_score': user1_row.iloc[0]['cognitive_flexibility_score'],
            'stress_coping_mechanism': user1_row.iloc[0]['stress_coping_mechanism']
        }

        user2_profile = {
            'emotional_regulation_score': user2_row.iloc[0]['emotional_regulation_score'],
            'social_connectivity_score': user2_row.iloc[0]['social_connectivity_score'],
            'behavioral_stability_score': user2_row.iloc[0]['behavioral_stability_score'],
            'cognitive_flexibility_score': user2_row.iloc[0]['cognitive_flexibility_score'],
            'stress_coping_mechanism': user2_row.iloc[0]['stress_coping_mechanism']
        }

        # Find shared needs
        shared_weaknesses = self.find_shared_weaknesses(user1_profile, user2_profile)
        shared_strengths = self.find_shared_strengths(user1_profile, user2_profile)

        print(f"Shared Weaknesses: {len(shared_weaknesses)}")
        for i, weakness in enumerate(shared_weaknesses[:3], 1):
            dim_name = weakness['dimension'].replace('_', ' ').title()
            print(f"  {i}. {dim_name}: Avg {weakness['avg_score']:.2f}")

        print(f"\nShared Strengths: {len(shared_strengths)}")
        for strength in shared_strengths:
            dim_name = strength['dimension'].replace('_', ' ').title()
            print(f"  • {dim_name}: Avg {strength['avg_score']:.2f}")

        # Calculate average profile
        avg_profile = {
            dim: (user1_profile[dim] + user2_profile[dim]) / 2
            for dim in self.recommender.DIMENSIONS
        }

        # Score all social activities
        print(f"\nScoring {len(self.social_activities)} social activities...")

        recommendations = []

        for _, activity in self.social_activities.iterrows():
            # Calculate base score using average profile
            score, explanation = self.recommender.calculate_activity_score(
                activity,
                avg_profile,
                self.recommender.analyze_user_profile(avg_profile)
            )

            # Bonus for pair/group format
            social_bonus = 0.0
            if 'Pair' in activity['social_format']:
                social_bonus = 0.15  # Perfect for pairs
            elif 'Group' in activity['social_format'] and 'Group 4-8' in activity['social_format']:
                social_bonus = 0.10  # Good for small groups (can include pairs)

            # Bonus if both users have high social connectivity
            if (user1_profile['social_connectivity_score'] > 0.60 and
                    user2_profile['social_connectivity_score'] > 0.60):
                social_bonus += 0.05

            total_score = score + social_bonus

            # Check if both users meet requirements
            user1_meets_req = (
                    user1_profile.get('behavioral_stability_score', 0) >= activity['required_min_behavioral'] and
                    user1_profile.get('social_connectivity_score', 0) >= activity['required_min_social']
            )

            user2_meets_req = (
                    user2_profile.get('behavioral_stability_score', 0) >= activity['required_min_behavioral'] and
                    user2_profile.get('social_connectivity_score', 0) >= activity['required_min_social']
            )

            both_meet_requirements = user1_meets_req and user2_meets_req

            recommendations.append({
                'activity_id': activity['activity_id'],
                'name': activity['name'],
                'category': activity['category'],
                'score': total_score,
                'base_score': score,
                'social_bonus': social_bonus,
                'difficulty': activity['difficulty'],
                'duration_minutes': activity['duration_minutes'],
                'social_format': activity['social_format'],
                'both_meet_requirements': both_meet_requirements,
                'user1_id': user1_id,
                'user2_id': user2_id,
                'partnership_type': 'Accountability Pair'
            })

        # Convert to DataFrame and sort
        recommendations_df = pd.DataFrame(recommendations)

        # Prioritize activities both users can do
        recommendations_df['sort_key'] = (
                recommendations_df['score'] +
                (recommendations_df['both_meet_requirements'].astype(int) * 0.2)
        )

        recommendations_df = recommendations_df.sort_values('sort_key', ascending=False)

        # Get top N
        top_recommendations = recommendations_df.head(n_recommendations)

        print(f"\n{'=' * 70}")
        print(f"TOP {n_recommendations} PAIR ACTIVITIES")
        print(f"{'=' * 70}\n")

        for idx, rec in top_recommendations.iterrows():
            req_status = "✓ Both ready" if rec['both_meet_requirements'] else "⚠ May be challenging"
            print(f"{rec.name + 1}. {rec['name']}")
            print(f"   Score: {rec['score']:.2f} | {rec['difficulty']} | {rec['duration_minutes']} min | {req_status}")
            print(f"   Format: {rec['social_format']}\n")

        return top_recommendations.drop('sort_key', axis=1)

    def recommend_group_activities(self,
                                   user_ids: List[str],
                                   user_features: pd.DataFrame,
                                   n_recommendations: int = 5) -> pd.DataFrame:
        """
        Recommend activities suitable for a group

        Parameters:
            user_ids (list): List of user IDs in the group
            user_features (pd.DataFrame): User feature data
            n_recommendations (int): Number of recommendations

        Returns:
            pd.DataFrame: Recommended group activities
        """
        print(f"\n{'=' * 70}")
        print(f"GROUP ACTIVITY RECOMMENDATIONS")
        print(f"{'=' * 70}")
        print(f"Group size: {len(user_ids)} members\n")

        # Get all user profiles
        group_profiles = []
        for user_id in user_ids:
            user_row = user_features[user_features['user_id'] == user_id]
            if len(user_row) > 0:
                profile = {
                    'user_id': user_id,
                    'emotional_regulation_score': user_row.iloc[0]['emotional_regulation_score'],
                    'social_connectivity_score': user_row.iloc[0]['social_connectivity_score'],
                    'behavioral_stability_score': user_row.iloc[0]['behavioral_stability_score'],
                    'cognitive_flexibility_score': user_row.iloc[0]['cognitive_flexibility_score'],
                    'stress_coping_mechanism': user_row.iloc[0]['stress_coping_mechanism']
                }
                group_profiles.append(profile)

        if len(group_profiles) == 0:
            print("No valid users found")
            return pd.DataFrame()

        # Calculate group average profile
        avg_profile = {
            dim: np.mean([p[dim] for p in group_profiles])
            for dim in self.recommender.DIMENSIONS
        }

        print(f"Group Average Resilience Profile:")
        for dim, score in avg_profile.items():
            print(f"  {dim.replace('_', ' ').title()}: {score:.2f}")

        # Filter for group activities with appropriate size
        group_size = len(user_ids)
        suitable_activities = self.social_activities[
            self.social_activities['social_format'].str.contains('Group', na=False)
        ].copy()

        print(f"\nScoring {len(suitable_activities)} group activities...")

        recommendations = []

        for _, activity in suitable_activities.iterrows():
            # Parse group size from format (e.g., "Group 6-8")
            activity_fits_size = True
            social_format = activity['social_format']

            if 'Group' in social_format and any(char.isdigit() for char in social_format):
                # Extract numbers from format
                import re
                numbers = re.findall(r'\d+', social_format)
                if len(numbers) >= 2:
                    min_size = int(numbers[0])
                    max_size = int(numbers[1])
                    activity_fits_size = min_size <= group_size <= max_size

            if not activity_fits_size:
                continue  # Skip activities not suitable for this group size

            # Calculate score
            score, explanation = self.recommender.calculate_activity_score(
                activity,
                avg_profile,
                self.recommender.analyze_user_profile(avg_profile)
            )

            # Bonus for matching group size
            size_bonus = 0.10 if activity_fits_size else 0.0

            total_score = score + size_bonus

            # Check how many members meet requirements
            members_ready = sum([
                1 for p in group_profiles
                if (p.get('behavioral_stability_score', 0) >= activity['required_min_behavioral'] and
                    p.get('social_connectivity_score', 0) >= activity['required_min_social'])
            ])

            readiness_percentage = (members_ready / len(group_profiles)) * 100

            recommendations.append({
                'activity_id': activity['activity_id'],
                'name': activity['name'],
                'category': activity['category'],
                'score': total_score,
                'difficulty': activity['difficulty'],
                'duration_minutes': activity['duration_minutes'],
                'social_format': activity['social_format'],
                'members_ready': members_ready,
                'total_members': len(group_profiles),
                'readiness_percentage': readiness_percentage,
                'group_ids': ','.join(user_ids)
            })

        # Convert to DataFrame and sort
        recommendations_df = pd.DataFrame(recommendations)

        if len(recommendations_df) == 0:
            print("No suitable group activities found for this group size")
            return pd.DataFrame()

        recommendations_df = recommendations_df.sort_values('score', ascending=False)

        # Get top N
        top_recommendations = recommendations_df.head(n_recommendations)

        print(f"\n{'=' * 70}")
        print(f"TOP {n_recommendations} GROUP ACTIVITIES")
        print(f"{'=' * 70}\n")

        for idx, rec in top_recommendations.iterrows():
            print(f"{rec.name + 1}. {rec['name']}")
            print(f"   Score: {rec['score']:.2f} | {rec['difficulty']} | {rec['duration_minutes']} min")
            print(f"   Format: {rec['social_format']}")
            print(
                f"   Readiness: {rec['members_ready']}/{rec['total_members']} members ready ({rec['readiness_percentage']:.0f}%)\n")

        return top_recommendations

    def create_accountability_partnerships(self,
                                           peer_pairs: pd.DataFrame,
                                           user_features: pd.DataFrame,
                                           output_file: str = "results/communities/accountability_partnerships.csv") -> pd.DataFrame:
        """
        Create accountability partnerships from peer pairs

        Parameters:
            peer_pairs (pd.DataFrame): Peer pairs from PeerMatcher
            user_features (pd.DataFrame): User feature data
            output_file (str): Output file path

        Returns:
            pd.DataFrame: Accountability partnerships with recommended activities
        """
        print(f"\n{'=' * 70}")
        print(f"CREATING ACCOUNTABILITY PARTNERSHIPS")
        print(f"{'=' * 70}\n")

        print(f"Processing {len(peer_pairs)} peer pairs...")

        partnerships = []

        for idx, pair in peer_pairs.iterrows():
            user1_id = pair['user_id']
            user2_id = pair['peer_id']
            similarity = pair['similarity_score']

            # Get top 3 activities for this pair
            pair_activities = self.recommend_pair_activities(
                user1_id,
                user2_id,
                user_features,
                n_recommendations=3
            )

            if len(pair_activities) > 0:
                # Create partnership record
                partnership = {
                    'partnership_id': f"PARTNER_{user1_id}_{user2_id}",
                    'user1_id': user1_id,
                    'user2_id': user2_id,
                    'similarity_score': similarity,
                    'recommended_activity_1': pair_activities.iloc[0]['name'],
                    'activity_1_id': pair_activities.iloc[0]['activity_id'],
                    'activity_1_score': pair_activities.iloc[0]['score'],
                    'status': 'Proposed',
                    'created_date': pd.Timestamp.now().strftime('%Y-%m-%d')
                }

                if len(pair_activities) > 1:
                    partnership['recommended_activity_2'] = pair_activities.iloc[1]['name']
                    partnership['activity_2_id'] = pair_activities.iloc[1]['activity_id']

                if len(pair_activities) > 2:
                    partnership['recommended_activity_3'] = pair_activities.iloc[2]['name']
                    partnership['activity_3_id'] = pair_activities.iloc[2]['activity_id']

                partnerships.append(partnership)

        partnerships_df = pd.DataFrame(partnerships)

        # Save partnerships
        os.makedirs(os.path.dirname(output_file), exist_ok=True)
        partnerships_df.to_csv(output_file, index=False)

        print(f"\n{'=' * 70}")
        print(f"✓ PARTNERSHIPS CREATED: {len(partnerships_df)}")
        print(f"  Saved to: {output_file}")
        print(f"{'=' * 70}\n")

        return partnerships_df


# Testing code
if __name__ == "__main__":
    """
    Test the Social Activity Matcher
    """
    # Get the project root directory (Component4_GMM)
    SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
    PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))

    # Add paths for imports
    sys.path.append(os.path.join(PROJECT_ROOT, "src", "clustering"))

    print("Testing Social Activity Matcher Module")
    print("=" * 70)

    # Load data
    print("\n1. Loading data...")
    user_data_path = os.path.join(PROJECT_ROOT, "data", "processed", "resilience_indicators_normalized.csv")
    assignments_path = os.path.join(PROJECT_ROOT, "results", "clusters", "cluster_assignments.csv")
    user_data = pd.read_csv(user_data_path)
    assignments = pd.read_csv(assignments_path)

    # Initialize components
    print("\n2. Initializing components...")

    # Activity system
    from activities.activity_database import ActivityDatabase
    from activities.activity_recommender import ActivityRecommender

    activity_db = ActivityDatabase()
    activity_recommender = ActivityRecommender(activity_db)

    # Community system
    from community.community_manager import CommunityManager
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

    community_manager = CommunityManager(
        assignments_df=assignments,
        cluster_centers=cluster_info,
        feature_columns=feature_cols
    )

    # Create communities if not already done
    if not community_manager.communities:
        community_manager.create_communities()

    # Find peers for first community
    first_community_id = list(community_manager.communities.keys())[0]
    first_community = community_manager.communities[first_community_id]

    print(f"\n3. Finding peer pairs in community: {first_community.name}")
    community_members = user_data[user_data['user_id'].isin(first_community.members)]

    # Peer matcher
    peer_matcher = PeerMatcher(
        user_features=community_members,
        feature_columns=feature_cols
    )

    # Create peer pairs and convert to DataFrame
    pairs_list = peer_matcher.create_peer_pairs(first_community.members)
    peer_pairs = pd.DataFrame(pairs_list, columns=['user_id', 'peer_id', 'similarity_score'])

    # Initialize social activity matcher
    print("\n4. Initializing Social Activity Matcher...")
    social_matcher = SocialActivityMatcher(
        activity_database=activity_db,
        activity_recommender=activity_recommender,
        peer_matcher=peer_matcher
    )

    # Test 1: Pair recommendations
    print("\n" + "=" * 70)
    print("TEST 1: Pair Activity Recommendations")
    print("=" * 70)

    results_dir = os.path.join(PROJECT_ROOT, "results", "communities")
    os.makedirs(results_dir, exist_ok=True)

    if len(peer_pairs) > 0:
        test_pair = peer_pairs.iloc[0]
        pair_activities = social_matcher.recommend_pair_activities(
            test_pair['user_id'],
            test_pair['peer_id'],
            user_data,
            n_recommendations=5
        )

        # Save
        pair_output = os.path.join(results_dir, "pair_activities_sample.csv")
        pair_activities.to_csv(pair_output, index=False)
        print(f"\n✓ Saved: {pair_output}")

    # Test 2: Group recommendations
    print("\n" + "=" * 70)
    print("TEST 2: Group Activity Recommendations")
    print("=" * 70)

    # Take first 8 members of community
    test_group = first_community.members[:8]

    group_activities = social_matcher.recommend_group_activities(
        test_group,
        user_data,
        n_recommendations=5
    )

    if len(group_activities) > 0:
        group_output = os.path.join(results_dir, "group_activities_sample.csv")
        group_activities.to_csv(group_output, index=False)
        print(f"\n✓ Saved: {group_output}")

    # Test 3: Create accountability partnerships
    print("\n" + "=" * 70)
    print("TEST 3: Create Accountability Partnerships")
    print("=" * 70)

    partnerships = social_matcher.create_accountability_partnerships(
        peer_pairs,
        user_data
    )

    print(f"\nSample Partnerships:")
    print(partnerships.head())

    print("\n" + "=" * 70)
    print("✓ ALL TESTS PASSED!")
    print("=" * 70)