"""
Community Manager Module

Manages the formation and organization of peer support communities
based on GMM clustering results.

Author: [Your Name]
Date: [Current Date]
"""

import numpy as np
import pandas as pd
from typing import List, Dict, Optional, Tuple
import json
import os
from dataclasses import dataclass, asdict


@dataclass
class Community:
    """
    Represents a peer support community

    Attributes:
        community_id (str): Unique identifier
        name (str): Human-readable name
        cluster_id (int): Source cluster from GMM
        members (list): List of user IDs
        profile (dict): Average resilience profile
        size (int): Number of members
        recommended_activities (list): Suggested activities
    """
    community_id: str
    name: str
    cluster_id: int
    members: List[str]
    profile: Dict[str, float]
    size: int
    recommended_activities: List[str]

    def add_member(self, user_id: str):
        """Add a member to the community"""
        if user_id not in self.members:
            self.members.append(user_id)
            self.size = len(self.members)

    def remove_member(self, user_id: str):
        """Remove a member from the community"""
        if user_id in self.members:
            self.members.remove(user_id)
            self.size = len(self.members)

    def to_dict(self) -> dict:
        """Convert to dictionary for serialization"""
        return asdict(self)


class CommunityManager:
    """
    Manages peer support communities formed from GMM clusters
    """

    # Community names for each cluster type
    CLUSTER_NAMES = {
        'low': "Building Resilience Together",
        'developing': "Foundations Group",
        'moderate': "Steady Growth Circle",
        'above_average': "Rising Resilience",
        'high': "Resilience Champions"
    }

    # Optimal community size range
    MIN_COMMUNITY_SIZE = 8
    MAX_COMMUNITY_SIZE = 12
    TARGET_COMMUNITY_SIZE = 10

    def __init__(self,
                 assignments_df: pd.DataFrame,
                 cluster_centers: pd.DataFrame,
                 feature_columns: List[str]):
        """
        Initialize the community manager

        Parameters:
            assignments_df (pd.DataFrame): User cluster assignments
            cluster_centers (pd.DataFrame): Cluster center information
            feature_columns (list): Names of resilience indicator features
        """
        self.assignments_df = assignments_df
        self.cluster_centers = cluster_centers
        self.feature_columns = feature_columns
        self.communities = {}

        print(f"✓ CommunityManager initialized")
        print(f"  Users: {len(assignments_df)}")
        print(f"  Clusters: {len(cluster_centers)}")
        print(f"  Target community size: {self.MIN_COMMUNITY_SIZE}-{self.MAX_COMMUNITY_SIZE}")

    def _classify_cluster_type(self, cluster_id: int) -> str:
        """
        Classify cluster type based on resilience scores

        Parameters:
            cluster_id (int): Cluster ID

        Returns:
            str: Cluster type ('low', 'developing', 'moderate', 'above_average', 'high')
        """
        # Get cluster center
        cluster_row = self.cluster_centers[
            self.cluster_centers['cluster_id'] == cluster_id
            ]

        if len(cluster_row) == 0:
            return 'moderate'  # Default

        # Calculate average resilience score
        feature_scores = cluster_row[self.feature_columns].values[0]
        avg_score = np.mean(feature_scores)

        # Classify based on thresholds
        if avg_score < 0.35:
            return 'low'
        elif avg_score < 0.50:
            return 'developing'
        elif avg_score < 0.65:
            return 'moderate'
        elif avg_score < 0.80:
            return 'above_average'
        else:
            return 'high'

    def _get_community_name(self, cluster_id: int, sub_id: int = 0) -> str:
        """
        Generate community name

        Parameters:
            cluster_id (int): Cluster ID
            sub_id (int): Sub-community ID if cluster is split

        Returns:
            str: Community name
        """
        cluster_type = self._classify_cluster_type(cluster_id)
        base_name = self.CLUSTER_NAMES[cluster_type]

        if sub_id > 0:
            # Add letter suffix for sub-communities
            suffix = chr(65 + sub_id)  # A, B, C, etc.
            return f"{base_name} - Group {suffix}"

        return base_name

    def _split_large_cluster(self,
                             cluster_users: pd.DataFrame,
                             cluster_id: int) -> List[List[str]]:
        """
        Split a large cluster into multiple communities

        Parameters:
            cluster_users (pd.DataFrame): Users in the cluster
            cluster_id (int): Cluster ID

        Returns:
            List[List[str]]: List of user ID lists (one per community)
        """
        n_users = len(cluster_users)

        # Calculate number of communities needed
        n_communities = max(1, round(n_users / self.TARGET_COMMUNITY_SIZE))

        # If only one community needed
        if n_communities == 1:
            return [cluster_users['user_id'].tolist()]

        print(f"  Splitting Cluster {cluster_id} ({n_users} users) → {n_communities} communities")

        # Sort users by primary probability (most confident assignments first)
        cluster_users_sorted = cluster_users.sort_values(
            'primary_probability',
            ascending=False
        ).copy()

        # Split into roughly equal groups
        communities = []
        users_per_community = n_users // n_communities

        for i in range(n_communities):
            if i == n_communities - 1:
                # Last community gets remaining users
                community_users = cluster_users_sorted.iloc[i * users_per_community:]
            else:
                start_idx = i * users_per_community
                end_idx = (i + 1) * users_per_community
                community_users = cluster_users_sorted.iloc[start_idx:end_idx]

            communities.append(community_users['user_id'].tolist())

        return communities

    def create_communities(self) -> Dict[str, Community]:
        """
        Create communities from cluster assignments

        Returns:
            dict: Dictionary of Community objects keyed by community_id
        """
        print(f"\n{'=' * 60}")
        print(f"CREATING PEER SUPPORT COMMUNITIES")
        print(f"{'=' * 60}")

        self.communities = {}
        community_counter = 0

        # Process each cluster
        for cluster_id in sorted(self.assignments_df['cluster'].unique()):
            print(f"\nProcessing Cluster {cluster_id}...")

            # Get users in this cluster
            cluster_users = self.assignments_df[
                self.assignments_df['cluster'] == cluster_id
                ].copy()

            n_users = len(cluster_users)
            print(f"  Users in cluster: {n_users}")

            # Split into communities if needed
            user_groups = self._split_large_cluster(cluster_users, cluster_id)

            # Get cluster profile
            cluster_profile = self.cluster_centers[
                self.cluster_centers['cluster_id'] == cluster_id
                ][self.feature_columns].iloc[0].to_dict()

            # Create communities
            for sub_id, user_list in enumerate(user_groups):
                community_id = f"COMM_{str(community_counter).zfill(3)}"
                community_name = self._get_community_name(cluster_id, sub_id)

                # Get recommended activities based on cluster type
                cluster_type = self._classify_cluster_type(cluster_id)
                activities = self._get_default_activities(cluster_type)

                # Create community object
                community = Community(
                    community_id=community_id,
                    name=community_name,
                    cluster_id=cluster_id,
                    members=user_list,
                    profile=cluster_profile,
                    size=len(user_list),
                    recommended_activities=activities
                )

                self.communities[community_id] = community

                print(f"  ✓ Created: {community_id} - '{community_name}' ({len(user_list)} members)")

                community_counter += 1

        print(f"\n{'=' * 60}")
        print(f"✓ COMMUNITY CREATION COMPLETE")
        print(f"{'=' * 60}")
        print(f"Total communities created: {len(self.communities)}")
        print(f"Average community size: {np.mean([c.size for c in self.communities.values()]):.1f}")

        return self.communities

    def _get_default_activities(self, cluster_type: str) -> List[str]:
        """
        Get default activities based on cluster type

        Parameters:
            cluster_type (str): Type of cluster

        Returns:
            list: Recommended activities
        """
        activity_templates = {
            'low': [
                "Daily Check-in Circle",
                "Mindfulness Basics Workshop",
                "Peer Support Sessions",
                "Stress Management Fundamentals",
                "Building Healthy Routines"
            ],
            'developing': [
                "Weekly Reflection Group",
                "Coping Skills Exchange",
                "Guided Meditation Sessions",
                "Resilience Building Exercises",
                "Peer Mentorship Program"
            ],
            'moderate': [
                "Growth-Focused Discussions",
                "Advanced Coping Strategies",
                "Community Wellness Activities",
                "Skill Sharing Sessions",
                "Goal-Setting Workshops"
            ],
            'above_average': [
                "Leadership Development",
                "Mentoring Opportunities",
                "Advanced Resilience Training",
                "Community Service Projects",
                "Wellness Innovation Labs"
            ],
            'high': [
                "Peer Mentorship (as mentors)",
                "Resilience Champions Network",
                "Community Leadership Roles",
                "Advanced Wellness Workshops",
                "Giving Back Initiatives"
            ]
        }

        return activity_templates.get(cluster_type, activity_templates['moderate'])

    def get_community(self, community_id: str) -> Optional[Community]:
        """
        Get a specific community

        Parameters:
            community_id (str): Community ID

        Returns:
            Community: Community object or None
        """
        return self.communities.get(community_id)

    def get_user_community(self, user_id: str) -> Optional[Community]:
        """
        Find which community a user belongs to

        Parameters:
            user_id (str): User ID

        Returns:
            Community: Community object or None
        """
        for community in self.communities.values():
            if user_id in community.members:
                return community
        return None

    def list_all_communities(self) -> pd.DataFrame:
        """
        Get summary of all communities

        Returns:
            pd.DataFrame: Community summary
        """
        summary_data = []

        for comm_id, community in self.communities.items():
            summary_data.append({
                'community_id': community.community_id,
                'name': community.name,
                'cluster_id': community.cluster_id,
                'size': community.size,
                'avg_emotional_regulation': community.profile.get('emotional_regulation_score', 0),
                'avg_social_connectivity': community.profile.get('social_connectivity_score', 0),
                'avg_behavioral_stability': community.profile.get('behavioral_stability_score', 0),
                'avg_cognitive_flexibility': community.profile.get('cognitive_flexibility_score', 0),
                'avg_stress_coping': community.profile.get('stress_coping_mechanism', 0)
            })

        return pd.DataFrame(summary_data)

    def _convert_to_serializable(self, obj):
        """
        Convert numpy types to native Python types for JSON serialization

        Parameters:
            obj: Object to convert

        Returns:
            JSON-serializable object
        """
        if isinstance(obj, dict):
            return {k: self._convert_to_serializable(v) for k, v in obj.items()}
        elif isinstance(obj, list):
            return [self._convert_to_serializable(item) for item in obj]
        elif isinstance(obj, (np.integer,)):
            return int(obj)
        elif isinstance(obj, (np.floating,)):
            return float(obj)
        elif isinstance(obj, np.ndarray):
            return obj.tolist()
        else:
            return obj

    def save_communities(self, filepath: str):
        """
        Save communities to JSON file

        Parameters:
            filepath (str): Path to save file
        """
        print(f"\n--- Saving Communities ---")

        # Create directory
        os.makedirs(os.path.dirname(filepath), exist_ok=True)

        # Convert to serializable format (handle numpy types)
        communities_data = {
            comm_id: self._convert_to_serializable(community.to_dict())
            for comm_id, community in self.communities.items()
        }

        # Save
        with open(filepath, 'w') as f:
            json.dump(communities_data, f, indent=2)

        print(f"✓ Saved to: {filepath}")
        print(f"  Communities: {len(communities_data)}")

    @classmethod
    def load_communities(cls, filepath: str):
        """
        Load communities from JSON file

        Parameters:
            filepath (str): Path to load from

        Returns:
            dict: Dictionary of Community objects
        """
        print(f"\n--- Loading Communities ---")

        with open(filepath, 'r') as f:
            communities_data = json.load(f)

        # Convert back to Community objects
        communities = {}
        for comm_id, comm_dict in communities_data.items():
            communities[comm_id] = Community(**comm_dict)

        print(f"✓ Loaded from: {filepath}")
        print(f"  Communities: {len(communities)}")

        return communities

    def print_community_summary(self):
        """
        Print a formatted summary of all communities
        """
        print(f"\n{'=' * 80}")
        print(f"COMMUNITY SUMMARY")
        print(f"{'=' * 80}\n")

        for comm_id, community in sorted(self.communities.items()):
            print(f"🏘️  {community.name}")
            print(f"   ID: {community.community_id}")
            print(f"   Cluster: {community.cluster_id}")
            print(f"   Members: {community.size}")
            print(f"   Profile:")
            for feature, score in community.profile.items():
                print(f"      • {feature.replace('_', ' ').title()}: {score:.2f}")
            print(f"   Recommended Activities:")
            for i, activity in enumerate(community.recommended_activities[:3], 1):
                print(f"      {i}. {activity}")
            print()


# Testing code
if __name__ == "__main__":
    """
    Test the Community Manager module
    """
    import sys

    # Get the project root directory (Component4_GMM)
    SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
    PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))

    # Add src directory to path for imports
    sys.path.append(os.path.join(PROJECT_ROOT, "src", "clustering"))

    print("Testing Community Manager Module")
    print("=" * 60)

    # Load data
    print("\n1. Loading cluster data...")
    assignments_path = os.path.join(PROJECT_ROOT, "results", "clusters", "cluster_assignments.csv")
    assignments = pd.read_csv(assignments_path)

    # Load cluster centers
    from gmm_model import GMMClusterer

    model_path = os.path.join(PROJECT_ROOT, "models", "gmm_model_v1.pkl")
    clusterer = GMMClusterer.load_model(model_path)
    cluster_centers = clusterer.get_cluster_info()

    feature_cols = [
        'emotional_regulation_score',
        'social_connectivity_score',
        'behavioral_stability_score',
        'cognitive_flexibility_score',
        'stress_coping_mechanism'
    ]

    print(f"✓ Loaded {len(assignments)} user assignments")
    print(f"✓ Loaded {len(cluster_centers)} clusters")

    # Initialize community manager
    print("\n2. Initializing Community Manager...")
    manager = CommunityManager(
        assignments_df=assignments,
        cluster_centers=cluster_centers,
        feature_columns=feature_cols
    )

    # Create communities
    print("\n3. Creating communities...")
    communities = manager.create_communities()

    # Print summary
    print("\n4. Community Summary:")
    manager.print_community_summary()

    # Get community list
    print("\n5. Community List:")
    community_list = manager.list_all_communities()
    print(community_list.to_string(index=False))

    # Test getting specific community
    print("\n6. Testing community lookup...")
    test_user = assignments.iloc[0]['user_id']
    user_community = manager.get_user_community(test_user)
    if user_community:
        print(f"✓ User {test_user} is in: {user_community.name}")

    # Save communities
    print("\n7. Saving communities...")
    communities_output_path = os.path.join(PROJECT_ROOT, "results", "communities", "communities.json")
    manager.save_communities(communities_output_path)

    # Save community list
    community_list_path = os.path.join(PROJECT_ROOT, "results", "communities", "community_list.csv")
    os.makedirs(os.path.dirname(community_list_path), exist_ok=True)
    community_list.to_csv(community_list_path, index=False)
    print("✓ Saved community list to CSV")

    print("\n" + "=" * 60)
    print("✓ ALL TESTS PASSED!")
    print("=" * 60)