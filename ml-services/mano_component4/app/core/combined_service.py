# app/core/combined_service.py
# Combines scoring and clustering services

"""
Combined Service
================
This module combines scoring and clustering to provide
a complete analysis of a user.

Flow:
1. User data comes in
2. Calculate scores (ScoringService)
3. Use scores for clustering (ClusteringService)
4. Return complete analysis
"""

import pandas as pd
from typing import Dict, List, Optional
from datetime import datetime
from app.core.recommender import RecommenderService

from app.core.scoring import ScoringService
from app.core.clustering import ClusteringService


class CombinedAnalysisService:
    """
    Service that combines scoring and clustering for complete user analysis.
    """

    def __init__(self):
        """Initialize both services."""
        self.scorer = ScoringService()
        self.clusterer = ClusteringService()
        self.model_loaded = False

    # In __init__, add:
    def __init__(self):
        """Initialize all services."""
        self.scorer = ScoringService()
        self.clusterer = ClusteringService()
        self.recommender = RecommenderService()  # ADD THIS
        self.model_loaded = False

    # Add new method:
    def get_full_analysis_with_recommendations(self, user_data: Dict) -> Dict:
        """
        Get complete analysis including recommendations.

        This is the COMPLETE analysis:
        1. Calculate scores
        2. Get interpretation
        3. Assign peer group
        4. Get activity recommendations

        Args:
            user_data: User's raw data

        Returns:
            Complete analysis with recommendations
        """
        # Get base analysis
        analysis = self.analyze_user(user_data)

        # Add recommendations
        score_data = {
            'overall_score': analysis['scores']['overall'],
            'stress_level': analysis['scores']['stress_level'],
            'category_scores': analysis['scores']['categories']
        }

        recommendations = self.recommender.get_recommendations(score_data)

        analysis['recommendations'] = {
            'activities': recommendations['recommendations'],
            'identified_problems': recommendations['identified_problems'],
            'total_matching': recommendations.get('total_matching_activities', 0)
        }

        return analysis

    def load_clustering_model(self, model_path: str = None) -> bool:
        """
        Load the pre-trained clustering model.

        Args:
            model_path: Path to the model file

        Returns:
            True if loaded successfully
        """
        try:
            self.clusterer.load_model(model_path)
            self.model_loaded = True
            return True
        except Exception as e:
            print(f"Error loading model: {e}")
            return False

    def analyze_user(self, user_data: Dict) -> Dict:
        """
        Perform complete analysis on a user.

        This:
        1. Calculates all scores
        2. Determines stress level
        3. Assigns to a peer group (if model loaded)
        4. Provides interpretation

        Args:
            user_data: Dictionary with user's data

        Returns:
            Complete analysis results
        """
        # Step 1: Calculate scores
        score_result = self.scorer.calculate_overall_score(user_data)

        # Step 2: Get interpretation
        interpretation = self.scorer.get_score_interpretation(score_result)

        # Step 3: Prepare result
        analysis = {
            'user_id': user_data.get('user_id', 'unknown'),
            'analyzed_at': datetime.now().isoformat(),

            # Scoring results
            'scores': {
                'overall': score_result['overall_score'],
                'stress_level': score_result['stress_level'],
                'categories': score_result['category_scores']
            },

            # Interpretation
            'interpretation': interpretation,

            # Areas needing attention
            'areas_of_concern': score_result['areas_of_concern'],
        }

        # Step 4: Add clustering results if model is loaded
        if self.model_loaded:
            try:
                # Prepare data for clustering
                # We need to use the scores as features for clustering
                cluster_features = self._prepare_cluster_features(user_data, score_result)

                cluster_df = pd.DataFrame([cluster_features])
                cluster_result = self.clusterer.predict(cluster_df)

                analysis['peer_group'] = {
                    'group_id': cluster_result['assigned_group'],
                    'group_name': cluster_result['group_name'],
                    'confidence': round(cluster_result['confidence'] * 100, 2),
                    'all_group_probabilities': cluster_result['all_probabilities']
                }
            except Exception as e:
                analysis['peer_group'] = {
                    'error': str(e),
                    'message': 'Could not determine peer group'
                }
        else:
            analysis['peer_group'] = {
                'message': 'Clustering model not loaded'
            }

        return analysis

    def _prepare_cluster_features(self, user_data: Dict, score_result: Dict) -> Dict:
        """
        Prepare features for clustering from user data and scores.

        Args:
            user_data: Raw user data
            score_result: Calculated scores

        Returns:
            Dictionary with features for clustering
        """
        # Use a combination of raw data and calculated scores
        features = {
            'user_id': user_data.get('user_id', 'unknown'),

            # Use calculated category scores
            'stress_score': 100 - score_result['category_scores']['emotional'],  # Invert for GMM
            'anxiety_score': user_data.get('anxiety_level', 5) * 10,
            'depression_score': user_data.get('depression_level', 5) * 10,
            'sleep_score': score_result['category_scores']['body'],
            'social_score': score_result['category_scores']['social'],
            'work_balance_score': score_result['category_scores']['behavior'],
            'physical_activity_score': user_data.get('steps', 5000) / 100,
            'hrv_score': user_data.get('hrv', 50),
            'wearable_stress': user_data.get('stress_level', 5) * 10,
            'positive_emotion': user_data.get('mood_score', 5) * 10,
            'negative_emotion': user_data.get('stress_level', 5) * 10,
        }

        return features

    def analyze_batch(self, users_data: List[Dict]) -> List[Dict]:
        """
        Analyze multiple users.

        Args:
            users_data: List of user data dictionaries

        Returns:
            List of analysis results
        """
        results = []
        for user_data in users_data:
            result = self.analyze_user(user_data)
            results.append(result)
        return results

    def get_group_summary(self) -> List[Dict]:
        """
        Get summary of all peer groups.

        Returns:
            List of group information
        """
        if not self.model_loaded:
            return [{'error': 'Clustering model not loaded'}]

        return self.clusterer.get_group_info()

    def compare_users(self, user1_data: Dict, user2_data: Dict) -> Dict:
        """
        Compare two users' scores and groups.

        Args:
            user1_data: First user's data
            user2_data: Second user's data

        Returns:
            Comparison results
        """
        analysis1 = self.analyze_user(user1_data)
        analysis2 = self.analyze_user(user2_data)

        comparison = {
            'user1': {
                'id': analysis1['user_id'],
                'overall_score': analysis1['scores']['overall'],
                'stress_level': analysis1['scores']['stress_level']
            },
            'user2': {
                'id': analysis2['user_id'],
                'overall_score': analysis2['scores']['overall'],
                'stress_level': analysis2['scores']['stress_level']
            },
            'score_difference': abs(
                analysis1['scores']['overall'] - analysis2['scores']['overall']
            ),
            'same_stress_level': (
                    analysis1['scores']['stress_level'] == analysis2['scores']['stress_level']
            ),
            'category_comparison': {}
        }

        # Compare each category
        for category in analysis1['scores']['categories']:
            score1 = analysis1['scores']['categories'][category]
            score2 = analysis2['scores']['categories'][category]
            comparison['category_comparison'][category] = {
                'user1': score1,
                'user2': score2,
                'difference': round(score1 - score2, 2)
            }

        # Check if same peer group
        if 'group_id' in analysis1.get('peer_group', {}) and 'group_id' in analysis2.get('peer_group', {}):
            comparison['same_peer_group'] = (
                    analysis1['peer_group']['group_id'] == analysis2['peer_group']['group_id']
            )

        return comparison