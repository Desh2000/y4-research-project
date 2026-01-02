"""
Manō Component 4: Complete System with Activity Recommendations

Integrates clustering, community management, and activity recommendations.

Author: [Your Name]
Date: [Current Date]
"""

import pandas as pd
import os

# Import all modules
from src.data_processing.data_loader import DataLoader
from src.data_processing.data_preprocessor import DataPreprocessor
from src.clustering.gmm_model import GMMClusterer
from src.visualization.cluster_visualizer import ClusterVisualizer
from src.community.community_manager import CommunityManager
from src.community.peer_matcher import PeerMatcher
from src.clustering.cluster_updater import ClusterUpdater

# Activity modules
from src.activities.activity_database import ActivityDatabase
from src.activities.activity_recommender import ActivityRecommender
from src.activities.social_activity_matcher import SocialActivityMatcher
from src.activities.progress_tracker import ProgressTracker
from src.activities.activity_system import ActivitySystem

from src.utils.logger import ManoCommunityLogger
from src.utils.config_loader import ConfigLoader


def main():
    """
    Main execution function with complete activity recommendations
    """
    print("\n" + "=" * 70)
    print(" MANŌ COMPONENT 4: COMPLETE SYSTEM")
    print(" Community-Driven Resilience Clustering + Activity Recommendations")
    print("=" * 70 + "\n")

    # Initialize logger and config
    logger = ManoCommunityLogger()
    config = ConfigLoader()

    logger.info("Starting Mano Component 4 - Complete System")

    # ==================== PHASE 1-5: CLUSTERING & COMMUNITIES ====================

    print("\n[PHASE 1-5] CLUSTERING & COMMUNITY FORMATION")
    print("-" * 70)

    # Get feature columns from config
    feature_cols = config.get('data.feature_columns')

    # Load and preprocess data
    loader = DataLoader()
    raw_data = loader.load_and_validate("data/sample/sample_users.csv")

    preprocessor = DataPreprocessor(feature_columns=feature_cols)
    normalized_data = preprocessor.fit_transform(raw_data)
    clusterer = GMMClusterer(n_components=5)

    if os.path.exists("models/gmm_model_v1.pkl"):
        clusterer = GMMClusterer.load_model("models/gmm_model_v1.pkl")
        logger.info("Loaded existing GMM model")
    else:
        clusterer.fit(normalized_data[feature_cols])
        clusterer.save_model("models/gmm_model_v1.pkl")

    # Get cluster assignments
    cluster_labels = clusterer.predict(normalized_data[feature_cols])
    cluster_probs = clusterer.predict_proba(normalized_data[feature_cols])

    # Visualize
    visualizer = ClusterVisualizer(output_dir="results/visualizations")
    visualizer.plot_clusters_2d(
        X=normalized_data[feature_cols].values,
        labels=cluster_labels
    )
    visualizer.plot_cluster_distribution(labels=cluster_labels)

    # Community management
    assignments_df = pd.DataFrame({
        'user_id': normalized_data['user_id'],
        'cluster': cluster_labels,
        'primary_probability': cluster_probs.max(axis=1)
    })

    cluster_info = clusterer.get_cluster_info()

    community_manager = CommunityManager(
        assignments_df=assignments_df,
        cluster_centers=cluster_info,
        feature_columns=feature_cols
    )

    communities = community_manager.create_communities()
    community_manager.save_communities("results/communities/communities.json")

    print("\n✓ Clustering and communities complete")

    # ==================== PHASE 6-9: ACTIVITY RECOMMENDATIONS ====================

    print("\n[PHASE 6-9] ACTIVITY RECOMMENDATION SYSTEM")
    print("-" * 70)

    # Initialize activity components
    activity_db = ActivityDatabase()
    activity_recommender = ActivityRecommender(activity_db)
    progress_tracker = ProgressTracker()

    # Generate recommendations for each community
    print("\nGenerating activity recommendations for all communities...")

    for community_id, community in communities.items():
        print(f"\nProcessing: {community.name}")

        # Get community members
        community_members = normalized_data[normalized_data['user_id'].isin(community.members)]

        # Initialize peer matcher for this community
        peer_matcher = PeerMatcher(
            user_features=community_members,
            feature_columns=feature_cols
        )

        # Create peer pairs and convert to DataFrame
        pairs_list = peer_matcher.create_peer_pairs(community.members)
        peer_pairs = pd.DataFrame(pairs_list, columns=['user_id', 'peer_id', 'similarity_score'])

        # Initialize social matcher for this community
        social_matcher = SocialActivityMatcher(
            activity_database=activity_db,
            activity_recommender=activity_recommender,
            peer_matcher=peer_matcher
        )

        # Create accountability partnerships
        if len(peer_pairs) > 0:
            partnerships = social_matcher.create_accountability_partnerships(
                peer_pairs,
                normalized_data,
                output_file=f"results/communities/partnerships_{community_id}.csv"
            )

            print(f"  ✓ Created {len(partnerships)} accountability partnerships")

        # Group recommendations for community
        if len(community.members) >= 4:
            group_recs = social_matcher.recommend_group_activities(
                community.members[:10],  # Max 10 for group activities
                normalized_data,
                n_recommendations=5
            )

            if len(group_recs) > 0:
                group_recs.to_csv(
                    f"results/communities/group_activities_{community_id}.csv",
                    index=False
                )
                print(f"  ✓ Generated {len(group_recs)} group activity recommendations")

    # Initialize activity system for individual recommendations
    # Use the last community's peer matcher (or create a generic one)
    first_community = list(communities.values())[0]
    first_community_members = normalized_data[normalized_data['user_id'].isin(first_community.members)]
    default_peer_matcher = PeerMatcher(
        user_features=first_community_members,
        feature_columns=feature_cols
    )
    default_social_matcher = SocialActivityMatcher(
        activity_database=activity_db,
        activity_recommender=activity_recommender,
        peer_matcher=default_peer_matcher
    )
    activity_system = ActivitySystem(
        activity_database=activity_db,
        activity_recommender=activity_recommender,
        social_matcher=default_social_matcher,
        progress_tracker=progress_tracker
    )

    # Individual recommendations for sample users
    print("\nGenerating individual recommendations for sample users...")

    sample_users = normalized_data['user_id'].head(5)

    for user_id in sample_users:
        recommendations = activity_system.get_complete_recommendations(
            user_id=user_id,
            user_features=normalized_data,
            n_individual=5
        )

        # Save individual recommendations
        if len(recommendations['individual_activities']) > 0:
            recommendations['individual_activities'].to_csv(
                f"results/communities/individual_recs_{user_id}.csv",
                index=False
            )

    print("\n✓ Activity recommendations complete")

    # ==================== FINAL REPORTS ====================

    print("\n[FINAL REPORTS] GENERATING COMPREHENSIVE REPORTS")
    print("-" * 70)

    # System-wide report
    activity_system.generate_system_report(output_dir="results/activity_system")

    # Summary statistics
    stats = {
        'total_users': len(normalized_data),
        'total_communities': len(communities),
        'total_activities': len(activity_db.activities),
        'social_activities': len(social_matcher.social_activities),
        'tracking_records': len(progress_tracker.progress_data)
    }

    pd.DataFrame([stats]).to_csv("results/system_summary.csv", index=False)

    print("\n" + "=" * 70)
    print(" ✓ MANŌ COMPONENT 4: COMPLETE")
    print("=" * 70)
    print("\nOutputs generated:")
    print("  • Cluster visualizations: results/visualizations/")
    print("  • Communities: results/communities/")
    print("  • Activity recommendations: results/communities/")
    print("  • Activity system reports: results/activity_system/")
    print("  • System summary: results/system_summary.csv")
    print("\n" + "=" * 70 + "\n")


if __name__ == "__main__":
    main()