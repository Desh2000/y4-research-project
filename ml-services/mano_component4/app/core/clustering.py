# app/core/clustering.py
# GMM Clustering implementation

"""
Clustering Module
=================
What is this file?
The BRAIN of your system. Contains GMM clustering logic.
Why we need it?
This is the MAIN machine learning code!

This module handles all clustering operations using GMM (Gaussian Mixture Model).

What this does:
1. Takes user data (stress, sleep, social scores, etc.)
2. Finds natural groups of similar users
3. Assigns each user to a group
4. Finds similar users within groups
"""

import numpy as np
import pandas as pd
from sklearn.mixture import GaussianMixture
from sklearn.preprocessing import StandardScaler
import pickle
import os
from typing import List, Dict, Tuple, Optional
from datetime import datetime

# Import our config
import sys

sys.path.append(os.path.dirname(os.path.dirname(os.path.dirname(__file__))))
from config import NUM_CLUSTERS, ML_MODELS_DIR


class ClusteringService:
    """
    Service class for clustering users into groups using GMM.

    GMM = Gaussian Mixture Model
    - Finds hidden groups in data
    - Gives probability of belonging to each group
    - Handles overlapping groups (soft clustering)
    """

    def __init__(self, n_clusters: int = NUM_CLUSTERS):
        """
        Initialize the clustering service.

        Args:
            n_clusters: Number of groups to create (default from config)
        """
        self.n_clusters = n_clusters
        self.model = None
        self.scaler = StandardScaler()
        self.is_trained = False
        self.feature_names = None
        self.training_date = None

        # Group names (will be updated based on characteristics)
        self.group_names = {
            0: "Group 0",
            1: "Group 1",
            2: "Group 2",
            3: "Group 3",
            4: "Group 4"
        }

        # Group descriptions (will be updated after training)
        self.group_descriptions = {}

    def prepare_features(self, data: pd.DataFrame) -> Tuple[np.ndarray, List[str]]:
        """
        Prepare features for GMM.

        Args:
            data: DataFrame with user data

        Returns:
            Tuple of (feature_array, feature_names)
        """
        # Select only numeric columns (exclude user_id, name, etc.)
        numeric_columns = data.select_dtypes(include=[np.number]).columns.tolist()

        # Remove any ID columns
        feature_columns = [col for col in numeric_columns
                           if 'id' not in col.lower() and 'index' not in col.lower()]

        features = data[feature_columns].values

        return features, feature_columns

    def train(self, data: pd.DataFrame) -> Dict:
        """
        Train the GMM model on user data.

        Args:
            data: DataFrame with user features

        Returns:
            Dictionary with training results
        """
        print("\n" + "=" * 50)
        print("TRAINING GMM MODEL")
        print("=" * 50)

        # Step 1: Prepare features
        print("\nStep 1: Preparing features...")
        features, self.feature_names = self.prepare_features(data)
        print(f"  - Number of samples: {features.shape[0]}")
        print(f"  - Number of features: {features.shape[1]}")
        print(f"  - Features: {self.feature_names}")

        # Step 2: Scale features (important for GMM!)
        print("\nStep 2: Scaling features...")
        features_scaled = self.scaler.fit_transform(features)
        print("  - Features scaled to mean=0, std=1")

        # Step 3: Create and train GMM
        print(f"\nStep 3: Training GMM with {self.n_clusters} clusters...")
        self.model = GaussianMixture(
            n_components=self.n_clusters,
            covariance_type='full',  # Allow different shapes for each cluster
            n_init=10,  # Try 10 different starting points
            max_iter=200,  # Maximum iterations
            random_state=42  # For reproducibility
        )

        self.model.fit(features_scaled)
        self.is_trained = True
        self.training_date = datetime.now()
        print("  - GMM training complete!")

        # Step 4: Analyze clusters
        print("\nStep 4: Analyzing clusters...")
        labels = self.model.predict(features_scaled)

        # Count members in each cluster
        cluster_counts = {}
        for i in range(self.n_clusters):
            count = np.sum(labels == i)
            cluster_counts[i] = count
            print(f"  - Cluster {i}: {count} members")

        # Step 5: Characterize each cluster
        print("\nStep 5: Characterizing clusters...")
        data_with_labels = data.copy()
        data_with_labels['cluster'] = labels

        for i in range(self.n_clusters):
            cluster_data = data_with_labels[data_with_labels['cluster'] == i]
            characteristics = self._get_cluster_characteristics(cluster_data, self.feature_names)
            self.group_names[i] = characteristics['name']
            self.group_descriptions[i] = characteristics['description']
            print(f"  - Cluster {i}: {characteristics['name']}")

        # Calculate model score
        score = self.model.score(features_scaled)

        print("\n" + "=" * 50)
        print("TRAINING COMPLETE!")
        print("=" * 50)

        return {
            'n_clusters': self.n_clusters,
            'n_samples': features.shape[0],
            'n_features': features.shape[1],
            'feature_names': self.feature_names,
            'cluster_counts': cluster_counts,
            'group_names': self.group_names,
            'model_score': score,
            'training_date': self.training_date.isoformat()
        }

    def _get_cluster_characteristics(self, cluster_data: pd.DataFrame,
                                     feature_names: List[str]) -> Dict:
        """
        Determine the main characteristics of a cluster.

        Args:
            cluster_data: Data for one cluster
            feature_names: Names of features

        Returns:
            Dictionary with name and description
        """
        # Calculate mean values for this cluster
        means = cluster_data[feature_names].mean()

        # Determine dominant characteristics
        characteristics = []

        # Check stress level
        if 'stress_score' in means:
            if means['stress_score'] > 70:
                characteristics.append('High Stress')
            elif means['stress_score'] < 30:
                characteristics.append('Low Stress')

        # Check sleep
        if 'sleep_score' in means:
            if means['sleep_score'] < 40:
                characteristics.append('Poor Sleep')
            elif means['sleep_score'] > 70:
                characteristics.append('Good Sleep')

        # Check social
        if 'social_score' in means:
            if means['social_score'] < 40:
                characteristics.append('Low Social')
            elif means['social_score'] > 70:
                characteristics.append('High Social')

        # Check activity
        if 'physical_activity_score' in means:
            if means['physical_activity_score'] > 70:
                characteristics.append('Active')
            elif means['physical_activity_score'] < 30:
                characteristics.append('Inactive')

        # Generate name
        if characteristics:
            name = ' & '.join(characteristics[:2])  # Take top 2
        else:
            name = 'Mixed Profile'

        # Generate description
        description = f"Average stress: {means.get('stress_score', 0):.1f}, "
        description += f"sleep: {means.get('sleep_score', 0):.1f}, "
        description += f"social: {means.get('social_score', 0):.1f}"

        return {
            'name': name,
            'description': description,
            'mean_values': means.to_dict()
        }

    def predict(self, user_data: pd.DataFrame) -> Dict:
        """
        Predict which group a user belongs to.

        Args:
            user_data: DataFrame with single user's data

        Returns:
            Dictionary with prediction results
        """
        if not self.is_trained:
            raise ValueError("Model not trained! Call train() first.")

        # Prepare features
        features, _ = self.prepare_features(user_data)

        # Scale features
        features_scaled = self.scaler.transform(features)

        # Get prediction (hard assignment)
        cluster = self.model.predict(features_scaled)[0]

        # Get probabilities (soft assignment)
        probabilities = self.model.predict_proba(features_scaled)[0]

        return {
            'assigned_group': int(cluster),
            'group_name': self.group_names.get(cluster, f"Group {cluster}"),
            'confidence': float(probabilities[cluster]),
            'all_probabilities': {
                self.group_names.get(i, f"Group {i}"): float(prob)
                for i, prob in enumerate(probabilities)
            }
        }

    def predict_batch(self, data: pd.DataFrame) -> pd.DataFrame:
        """
        Predict groups for multiple users.

        Args:
            data: DataFrame with multiple users

        Returns:
            DataFrame with predictions added
        """
        if not self.is_trained:
            raise ValueError("Model not trained! Call train() first.")

        # Prepare features
        features, _ = self.prepare_features(data)

        # Scale features
        features_scaled = self.scaler.transform(features)

        # Get predictions
        clusters = self.model.predict(features_scaled)
        probabilities = self.model.predict_proba(features_scaled)

        # Add to dataframe
        result = data.copy()
        result['predicted_group'] = clusters
        result['group_name'] = [self.group_names.get(c, f"Group {c}") for c in clusters]
        result['confidence'] = [probabilities[i][clusters[i]] for i in range(len(clusters))]

        return result

    def get_similar_users(self, user_id: str, data: pd.DataFrame,
                          top_n: int = 5) -> List[Dict]:
        """
        Find users similar to the given user.

        Args:
            user_id: ID of the user
            data: Full dataset with all users
            top_n: Number of similar users to return

        Returns:
            List of similar users with similarity scores
        """
        if not self.is_trained:
            raise ValueError("Model not trained! Call train() first.")

        # Find the target user
        if 'user_id' not in data.columns:
            raise ValueError("Data must have 'user_id' column")

        target_user = data[data['user_id'] == user_id]
        if target_user.empty:
            raise ValueError(f"User {user_id} not found")

        # Get target user's group
        target_prediction = self.predict(target_user)
        target_group = target_prediction['assigned_group']

        # Get all users in same group
        all_predictions = self.predict_batch(data)
        same_group = all_predictions[all_predictions['predicted_group'] == target_group]

        # Remove target user
        same_group = same_group[same_group['user_id'] != user_id]

        # Calculate similarity (using confidence as proxy)
        # Higher confidence = more central to group = more representative
        similar_users = same_group.nlargest(top_n, 'confidence')

        result = []
        for _, user in similar_users.iterrows():
            result.append({
                'user_id': user['user_id'],
                'group_name': user['group_name'],
                'similarity_score': round(user['confidence'] * 100, 2)
            })

        return result

    def save_model(self, filepath: str = None) -> str:
        """
        Save the trained model to disk.

        Args:
            filepath: Path to save the model

        Returns:
            Path where model was saved
        """
        if not self.is_trained:
            raise ValueError("Model not trained! Nothing to save.")

        if filepath is None:
            os.makedirs(ML_MODELS_DIR, exist_ok=True)
            filepath = os.path.join(ML_MODELS_DIR, 'gmm_model.pkl')

        model_data = {
            'model': self.model,
            'scaler': self.scaler,
            'n_clusters': self.n_clusters,
            'feature_names': self.feature_names,
            'group_names': self.group_names,
            'group_descriptions': self.group_descriptions,
            'training_date': self.training_date
        }

        with open(filepath, 'wb') as f:
            pickle.dump(model_data, f)

        print(f"Model saved to: {filepath}")
        return filepath

    def load_model(self, filepath: str = None) -> bool:
        """
        Load a trained model from disk.

        Args:
            filepath: Path to the saved model

        Returns:
            True if loaded successfully
        """
        if filepath is None:
            filepath = os.path.join(ML_MODELS_DIR, 'gmm_model.pkl')

        if not os.path.exists(filepath):
            raise FileNotFoundError(f"Model file not found: {filepath}")

        with open(filepath, 'rb') as f:
            model_data = pickle.load(f)

        self.model = model_data['model']
        self.scaler = model_data['scaler']
        self.n_clusters = model_data['n_clusters']
        self.feature_names = model_data['feature_names']
        self.group_names = model_data['group_names']
        self.group_descriptions = model_data['group_descriptions']
        self.training_date = model_data['training_date']
        self.is_trained = True

        print(f"Model loaded from: {filepath}")
        return True

    def get_group_info(self) -> List[Dict]:
        """
        Get information about all groups.

        Returns:
            List of dictionaries with group information
        """
        if not self.is_trained:
            raise ValueError("Model not trained!")

        groups = []
        for i in range(self.n_clusters):
            groups.append({
                'group_id': i,
                'group_name': self.group_names.get(i, f"Group {i}"),
                'description': self.group_descriptions.get(i, "No description")
            })

        return groups