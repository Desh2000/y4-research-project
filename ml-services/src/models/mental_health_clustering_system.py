"""
Complete Mental Health Clustering System
- 9-cluster GMM with dynamic centroid updates
- Professional support recommendations
- User assignment and risk assessment
- Integration ready for Java backend
"""

import numpy as np
import pandas as pd
from sklearn.mixture import GaussianMixture
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import adjusted_rand_score, silhouette_score
from typing import Dict, List, Tuple, Optional
import json
import joblib
import matplotlib.pyplot as plt
import seaborn as sns
from datetime import datetime
import os


class MentalHealthClusteringSystem:
    """
    Production-ready Mental Health Clustering System
    """

    def __init__(self, n_clusters: int = 9, random_state: int = 42):
        """
        Initialize the clustering system

        Args:
            n_clusters: Number of clusters (default 9 for 3x3 matrix)
            random_state: For reproducible results
        """
        self.n_clusters = n_clusters
        self.random_state = random_state
        self.gmm = None
        self.scaler = StandardScaler()
        self.is_fitted = False

        # Define cluster interpretation mapping
        self.cluster_info = {}

        # Professional support level mapping
        self.support_levels = {
            'MINIMAL': 'Self-help resources, monitoring',
            'MODERATE': 'Counseling, therapy sessions',
            'INTENSIVE': 'Intensive therapy, possible medication',
            'CRITICAL': 'Immediate intervention, crisis support'
        }

        # Model metadata
        self.model_version = "1.0.0"
        self.training_date = None
        self.performance_metrics = {}

    def fit(self, data: np.ndarray, true_labels: Optional[List[str]] = None) -> Dict:
        """
        Fit the GMM model and analyze clusters

        Args:
            data: Mental health scores (n_samples, 3) [stress, depression, anxiety]
            true_labels: Optional ground truth labels for evaluation

        Returns:
            Dict with training results and metrics
        """
        print("🧠 Training Mental Health Clustering System...")
        print(f"📊 Data shape: {data.shape}")

        # Step 1: Preprocessing
        print("🔄 Preprocessing data...")
        data_scaled = self.scaler.fit_transform(data)

        # Step 2: Fit GMM model
        print(f"🤖 Fitting GMM with {self.n_clusters} clusters...")
        self.gmm = GaussianMixture(
            n_components=self.n_clusters,
            covariance_type='full',  # Allow elliptical clusters
            init_params='kmeans',  # Initialize with k-means
            max_iter=200,
            tol=1e-3,
            random_state=self.random_state
        )

        self.gmm.fit(data_scaled)
        self.is_fitted = True
        self.training_date = datetime.now()

        # Step 3: Predict clusters
        cluster_labels = self.gmm.predict(data_scaled)
        cluster_probs = self.gmm.predict_proba(data_scaled)

        # Step 4: Analyze clusters
        self._analyze_clusters(data, cluster_labels)

        # Step 5: Calculate performance metrics
        metrics = self._calculate_metrics(data_scaled, cluster_labels, true_labels)
        self.performance_metrics = metrics

        print("✅ Training complete!")

        return {
            'cluster_labels': cluster_labels,
            'cluster_probabilities': cluster_probs,
            'metrics': metrics,
            'cluster_info': self.cluster_info
        }

    def predict_user_cluster(self, stress: float, depression: float, anxiety: float) -> Dict:
        """
        Predict cluster for a single user

        Args:
            stress, depression, anxiety: Mental health scores (0.0 - 1.0)

        Returns:
            Dict with cluster assignment and recommendations
        """
        if not self.is_fitted:
            raise ValueError("Model must be fitted before prediction")

        # Validate input scores
        for score_name, score in [('stress', stress), ('depression', depression), ('anxiety', anxiety)]:
            if not 0.0 <= score <= 1.0:
                raise ValueError(f"{score_name} score must be between 0.0 and 1.0")

        # Prepare data
        user_data = np.array([[stress, depression, anxiety]])
        user_data_scaled = self.scaler.transform(user_data)

        # Predict cluster
        cluster_id = self.gmm.predict(user_data_scaled)[0]
        cluster_probs = self.gmm.predict_proba(user_data_scaled)[0]

        # Get cluster information
        cluster_info = self.cluster_info.get(cluster_id, {})

        # Calculate overall risk
        overall_risk = (stress * 0.4) + (depression * 0.35) + (anxiety * 0.25)

        # Determine support level
        support_level = self._determine_support_level(stress, depression, anxiety, overall_risk)

        return {
            'user_scores': {
                'stress': stress,
                'depression': depression,
                'anxiety': anxiety,
                'overall_risk': overall_risk
            },
            'cluster_assignment': {
                'cluster_id': int(cluster_id),
                'cluster_identifier': cluster_info.get('identifier', f'CLUSTER_{cluster_id}'),
                'cluster_category': cluster_info.get('category', 'MIXED'),
                'cluster_level': cluster_info.get('level', 'MEDIUM'),
                'confidence': float(cluster_probs.max())
            },
            'recommendations': {
                'support_level': support_level,
                'support_description': self.support_levels.get(support_level, 'Standard care'),
                'interventions': cluster_info.get('interventions', []),
                'peer_activities': cluster_info.get('peer_activities', [])
            },
            'risk_assessment': {
                'is_high_risk': overall_risk >= 0.8,
                'crisis_flag': any(score >= 0.9 for score in [stress, depression, anxiety]),
                'requires_intervention': overall_risk >= 0.8 or any(
                    score >= 0.85 for score in [stress, depression, anxiety])
            }
        }

    def update_centroids(self, new_data: np.ndarray, learning_rate: float = 0.1):
        """
        Dynamically update cluster centroids with new data

        Args:
            new_data: New user data (n_samples, 3)
            learning_rate: How much to adapt to new data (0.0 - 1.0)
        """
        if not self.is_fitted:
            raise ValueError("Model must be fitted before updating centroids")

        print(f"🔄 Updating centroids with {len(new_data)} new data points...")

        # Scale new data
        new_data_scaled = self.scaler.transform(new_data)

        # Get cluster assignments for new data
        new_labels = self.gmm.predict(new_data_scaled)

        # Update means (centroids) using exponential moving average
        current_means = self.gmm.means_.copy()

        for cluster_id in range(self.n_clusters):
            # Find new data points assigned to this cluster
            cluster_mask = new_labels == cluster_id
            cluster_new_data = new_data_scaled[cluster_mask]

            if len(cluster_new_data) > 0:
                # Calculate new centroid for this cluster's new data
                new_cluster_mean = np.mean(cluster_new_data, axis=0)

                # Update using exponential moving average
                self.gmm.means_[cluster_id] = (
                        (1 - learning_rate) * current_means[cluster_id] +
                        learning_rate * new_cluster_mean
                )

        print("✅ Centroids updated!")

        # Re-analyze clusters with updated centroids
        combined_data = np.vstack([self.scaler.inverse_transform(self.gmm.means_), new_data])
        combined_labels = self.gmm.predict(self.scaler.transform(combined_data))
        self._analyze_clusters(combined_data, combined_labels)

    def get_cluster_statistics(self) -> Dict:
        """
        Get comprehensive cluster statistics

        Returns:
            Dict with cluster statistics and metadata
        """
        if not self.is_fitted:
            raise ValueError("Model must be fitted before getting statistics")

        return {
            'model_info': {
                'version': self.model_version,
                'n_clusters': self.n_clusters,
                'training_date': self.training_date.isoformat() if self.training_date else None,
                'is_fitted': self.is_fitted
            },
            'cluster_info': self.cluster_info,
            'performance_metrics': self.performance_metrics,
            'centroids': self.gmm.means_.tolist() if self.gmm else [],
            'weights': self.gmm.weights_.tolist() if self.gmm else []
        }

    def save_model(self, filepath: str):
        """
        Save the trained model to disk

        Args:
            filepath: Path to save the model
        """
        if not self.is_fitted:
            raise ValueError("Model must be fitted before saving")

        # Create parent directory if it doesn't exist
        dir_path = os.path.dirname(filepath)
        if dir_path:
            os.makedirs(dir_path, exist_ok=True)

        model_data = {
            'gmm': self.gmm,
            'scaler': self.scaler,
            'cluster_info': self.cluster_info,
            'model_version': self.model_version,
            'training_date': self.training_date,
            'performance_metrics': self.performance_metrics,
            'n_clusters': self.n_clusters
        }

        joblib.dump(model_data, filepath)
        print(f"💾 Model saved to: {filepath}")

    def load_model(self, filepath: str):
        """
        Load a trained model from disk

        Args:
            filepath: Path to load the model from
        """
        model_data = joblib.load(filepath)

        self.gmm = model_data['gmm']
        self.scaler = model_data['scaler']
        self.cluster_info = model_data['cluster_info']
        self.model_version = model_data['model_version']
        self.training_date = model_data['training_date']
        self.performance_metrics = model_data['performance_metrics']
        self.n_clusters = model_data['n_clusters']
        self.is_fitted = True

        print(f"📁 Model loaded from: {filepath}")

    def _analyze_clusters(self, data: np.ndarray, labels: np.ndarray):
        """
        Analyze clusters and assign interpretations
        """
        print("🔍 Analyzing cluster characteristics...")

        self.cluster_info = {}

        for cluster_id in range(self.n_clusters):
            cluster_mask = labels == cluster_id
            cluster_data = data[cluster_mask]

            if len(cluster_data) == 0:
                continue

            # Calculate cluster statistics
            mean_scores = np.mean(cluster_data, axis=0)
            std_scores = np.std(cluster_data, axis=0)

            # Determine dominant issue and severity
            dominant_issue = self._determine_dominant_issue(mean_scores)
            severity_level = self._determine_severity_level(mean_scores)

            # Generate cluster identifier
            cluster_identifier = f"{dominant_issue}_{severity_level}"

            # Store cluster information
            self.cluster_info[cluster_id] = {
                'identifier': cluster_identifier,
                'category': dominant_issue,
                'level': severity_level,
                'member_count': len(cluster_data),
                'mean_scores': {
                    'stress': float(mean_scores[0]),
                    'depression': float(mean_scores[1]),
                    'anxiety': float(mean_scores[2])
                },
                'std_scores': {
                    'stress': float(std_scores[0]),
                    'depression': float(std_scores[1]),
                    'anxiety': float(std_scores[2])
                },
                'interventions': self._get_interventions(dominant_issue, severity_level),
                'peer_activities': self._get_peer_activities(dominant_issue, severity_level)
            }

        print(f"✅ Analyzed {len(self.cluster_info)} clusters")

    def _determine_dominant_issue(self, mean_scores: np.ndarray) -> str:
        """Determine dominant mental health issue"""
        stress, depression, anxiety = mean_scores

        if stress >= depression and stress >= anxiety:
            return "STRESS"
        elif depression >= stress and depression >= anxiety:
            return "DEPRESSION"
        else:
            return "ANXIETY"

    def _determine_severity_level(self, mean_scores: np.ndarray) -> str:
        """Determine severity level"""
        max_score = np.max(mean_scores)

        if max_score >= 0.7:
            return "HIGH"
        elif max_score >= 0.4:
            return "MEDIUM"
        else:
            return "LOW"

    def _determine_support_level(self, stress: float, depression: float, anxiety: float, overall_risk: float) -> str:
        """Determine required support level"""
        if any(score >= 0.9 for score in [stress, depression, anxiety]):
            return "CRITICAL"
        elif overall_risk >= 0.8:
            return "INTENSIVE"
        elif overall_risk >= 0.5:
            return "MODERATE"
        else:
            return "MINIMAL"

    def _get_interventions(self, issue: str, level: str) -> List[str]:
        """Get recommended interventions"""
        interventions = {
            'STRESS_LOW': ['Stress management techniques', 'Time management'],
            'STRESS_MEDIUM': ['Cognitive behavioral therapy', 'Relaxation training'],
            'STRESS_HIGH': ['Intensive therapy', 'Medical evaluation', 'Crisis support'],
            'DEPRESSION_LOW': ['Behavioral activation', 'Social activities'],
            'DEPRESSION_MEDIUM': ['Psychotherapy', 'Support groups'],
            'DEPRESSION_HIGH': ['Psychiatric evaluation', 'Medication review', 'Crisis intervention'],
            'ANXIETY_LOW': ['Mindfulness training', 'Exercise programs'],
            'ANXIETY_MEDIUM': ['Exposure therapy', 'Anxiety management'],
            'ANXIETY_HIGH': ['Intensive therapy', 'Medical consultation', 'Crisis support']
        }

        return interventions.get(f"{issue}_{level}", ['Standard mental health support'])

    def _get_peer_activities(self, issue: str, level: str) -> List[str]:
        """Get recommended peer activities"""
        activities = {
            'STRESS_LOW': ['Stress relief workshops', 'Meditation groups'],
            'STRESS_MEDIUM': ['Support groups', 'Stress management classes'],
            'STRESS_HIGH': ['Intensive support groups', 'Crisis peer support'],
            'DEPRESSION_LOW': ['Social activities', 'Hobby groups'],
            'DEPRESSION_MEDIUM': ['Depression support groups', 'Peer counseling'],
            'DEPRESSION_HIGH': ['Intensive peer support', 'Crisis companion'],
            'ANXIETY_LOW': ['Relaxation groups', 'Mindfulness classes'],
            'ANXIETY_MEDIUM': ['Anxiety support groups', 'Coping skills groups'],
            'ANXIETY_HIGH': ['Intensive anxiety support', 'Crisis peer network']
        }

        return activities.get(f"{issue}_{level}", ['General peer support'])

    def _calculate_metrics(self, data_scaled: np.ndarray, labels: np.ndarray, true_labels: Optional[List[str]]) -> Dict:
        """Calculate clustering performance metrics"""
        metrics = {}

        # Silhouette score (internal metric)
        metrics['silhouette_score'] = silhouette_score(data_scaled, labels)

        # Log-likelihood (GMM specific)
        metrics['log_likelihood'] = self.gmm.score(data_scaled)

        # AIC and BIC (model selection)
        metrics['aic'] = self.gmm.aic(data_scaled)
        metrics['bic'] = self.gmm.bic(data_scaled)

        # If ground truth available, calculate external metrics
        if true_labels is not None:
            from sklearn.preprocessing import LabelEncoder
            le = LabelEncoder()
            true_labels_encoded = le.fit_transform(true_labels)
            metrics['adjusted_rand_score'] = adjusted_rand_score(true_labels_encoded, labels)

        return metrics


# Example usage and testing
if __name__ == "__main__":
    from src.data.mental_health_generator import MentalHealthDataGenerator

    print("🧠 Mental Health Clustering System - Complete Test")
    print("=" * 60)

    # Step 1: Generate realistic data
    generator = MentalHealthDataGenerator(random_state=42)
    data, true_clusters, df = generator.generate_full_dataset(samples_per_cluster=100)

    # Step 2: Create and train clustering system
    clustering_system = MentalHealthClusteringSystem(n_clusters=9, random_state=42)
    results = clustering_system.fit(data, true_clusters)

    # Step 3: Test single user prediction
    print("\n🧑 Testing Single User Prediction:")
    test_user = clustering_system.predict_user_cluster(
        stress=0.8, depression=0.3, anxiety=0.6
    )

    print(f"User Cluster: {test_user['cluster_assignment']['cluster_identifier']}")
    print(f"Support Level: {test_user['recommendations']['support_level']}")
    print(f"High Risk: {test_user['risk_assessment']['is_high_risk']}")

    # Step 4: Get cluster statistics
    stats = clustering_system.get_cluster_statistics()
    print(f"\nModel Performance:")
    print(f"Silhouette Score: {stats['performance_metrics']['silhouette_score']:.3f}")

    # Step 5: Save model
    clustering_system.save_model('models/mental_health_gmm_v1.pkl')

    print("\n🎉 Complete system test finished!")
