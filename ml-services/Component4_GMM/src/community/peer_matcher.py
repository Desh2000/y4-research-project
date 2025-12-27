"""
Peer Matcher Module

Implements algorithms for matching users within communities for
optimal peer support relationships.

Author: [Your Name]
Date: [Current Date]
"""

import numpy as np
import pandas as pd
from typing import List, Dict, Tuple
from scipy.spatial.distance import cosine


class PeerMatcher:
    """
    Matches users within communities for peer support
    """

    def __init__(self,
                 user_features: pd.DataFrame,
                 feature_columns: List[str]):
        """
        Initialize the peer matcher

        Parameters:
            user_features (pd.DataFrame): User resilience profiles
            feature_columns (list): Feature column names
        """
        self.user_features = user_features
        self.feature_columns = feature_columns

        print(f"✓ PeerMatcher initialized")
        print(f"  Users: {len(user_features)}")
        print(f"  Features: {len(feature_columns)}")

    def calculate_similarity(self,
                             user1_id: str,
                             user2_id: str) -> float:
        """
        Calculate similarity between two users

        Uses cosine similarity on resilience profiles.

        Parameters:
            user1_id (str): First user ID
            user2_id (str): Second user ID

        Returns:
            float: Similarity score (0 to 1, higher = more similar)
        """
        # Get feature vectors
        user1_features = self.user_features[
            self.user_features['user_id'] == user1_id
            ][self.feature_columns].values[0]

        user2_features = self.user_features[
            self.user_features['user_id'] == user2_id
            ][self.feature_columns].values[0]

        # Calculate cosine similarity (1 - cosine distance)
        similarity = 1 - cosine(user1_features, user2_features)

        return max(0, similarity)  # Ensure non-negative

    def find_best_matches(self,
                          user_id: str,
                          candidate_ids: List[str],
                          top_n: int = 5) -> List[Tuple[str, float]]:
        """
        Find best peer matches for a user

        Parameters:
            user_id (str): Target user ID
            candidate_ids (list): List of potential match IDs
            top_n (int): Number of matches to return

        Returns:
            list: List of (user_id, similarity_score) tuples
        """
        # Calculate similarity with all candidates
        similarities = []

        for candidate_id in candidate_ids:
            if candidate_id != user_id:  # Don't match with self
                similarity = self.calculate_similarity(user_id, candidate_id)
                similarities.append((candidate_id, similarity))

        # Sort by similarity (descending)
        similarities.sort(key=lambda x: x[1], reverse=True)

        # Return top N
        return similarities[:top_n]

    def create_peer_pairs(self,
                          community_members: List[str]) -> List[Tuple[str, str, float]]:
        """
        Create optimal peer pairs within a community

        Uses greedy matching to pair users with high similarity.

        Parameters:
            community_members (list): List of user IDs in community

        Returns:
            list: List of (user1_id, user2_id, similarity) tuples
        """
        print(f"\n--- Creating Peer Pairs ---")
        print(f"Community size: {len(community_members)}")

        pairs = []
        unmatched = set(community_members)

        while len(unmatched) >= 2:
            # Get first unmatched user
            user1 = unmatched.pop()

            # Find best match among remaining users
            best_match = None
            best_similarity = -1

            for user2 in unmatched:
                similarity = self.calculate_similarity(user1, user2)
                if similarity > best_similarity:
                    best_similarity = similarity
                    best_match = user2

            # Create pair
            if best_match:
                unmatched.remove(best_match)
                pairs.append((user1, best_match, best_similarity))

        print(f"✓ Created {len(pairs)} peer pairs")
        print(f"  Average similarity: {np.mean([p[2] for p in pairs]):.3f}")

        return pairs

    def get_community_recommendations(self,
                                      user_id: str,
                                      community_members: List[str],
                                      n_recommendations: int = 3) -> pd.DataFrame:
        """
        Get peer recommendations for a user within their community

        Parameters:
            user_id (str): Target user ID
            community_members (list): Community member IDs
            n_recommendations (int): Number of recommendations

        Returns:
            pd.DataFrame: Recommended peers with similarity scores
        """
        # Find best matches
        matches = self.find_best_matches(
            user_id,
            community_members,
            top_n=n_recommendations
        )

        # Create DataFrame
        recommendations = pd.DataFrame(matches, columns=['user_id', 'similarity'])
        recommendations['recommendation_rank'] = range(1, len(recommendations) + 1)

        return recommendations


# Testing code
if __name__ == "__main__":
    """
    Test the Peer Matcher module
    """
    print("Testing Peer Matcher Module")
    print("=" * 60)

    # Load data
    print("\n1. Loading data...")
    user_data = pd.read_csv("data/processed/resilience_indicators_normalized.csv")

    feature_cols = [
        'emotional_regulation_score',
        'social_connectivity_score',
        'behavioral_stability_score',
        'cognitive_flexibility_score',
        'stress_coping_mechanism'
    ]

    # Initialize matcher
    print("\n2. Initializing Peer Matcher...")
    matcher = PeerMatcher(user_data, feature_cols)

    # Test similarity calculation
    print("\n3. Testing similarity calculation...")
    user1 = user_data.iloc[0]['user_id']
    user2 = user_data.iloc[1]['user_id']
    similarity = matcher.calculate_similarity(user1, user2)
    print(f"Similarity between {user1} and {user2}: {similarity:.3f}")

    # Test finding matches
    print("\n4. Finding best matches for a user...")
    test_user = user_data.iloc[0]['user_id']
    all_users = user_data['user_id'].tolist()
    matches = matcher.find_best_matches(test_user, all_users, top_n=5)

    print(f"\nTop 5 matches for {test_user}:")
    for i, (match_id, sim) in enumerate(matches, 1):
        print(f"  {i}. {match_id}: {sim:.3f}")

    # Test peer pairing
    print("\n5. Creating peer pairs for a community...")
    test_community = user_data.iloc[:10]['user_id'].tolist()
    pairs = matcher.create_peer_pairs(test_community)

    print(f"\nPeer pairs:")
    for user1, user2, sim in pairs:
        print(f"  {user1} ↔ {user2}: {sim:.3f}")

    print("\n" + "=" * 60)
    print("✓ ALL TESTS PASSED!")
    print("=" * 60)