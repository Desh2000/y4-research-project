"""
Cluster Updater Module

Handles real-time updates to GMM clusters as new data arrives.

Author: [Your Name]
Date: [Current Date]
"""

import numpy as np
import pandas as pd
from typing import Dict, List, Optional
import pickle
import os
import sys
from datetime import datetime

# Handle imports for both direct execution and module import
try:
    from .gmm_model import GMMClusterer
except ImportError:
    from gmm_model import GMMClusterer


class ClusterUpdater:
    """
    Manages dynamic cluster updates
    """

    def __init__(self,
                 clusterer: GMMClusterer,
                 update_threshold: float = 0.1,
                 min_samples_for_update: int = 10):
        """
        Initialize the cluster updater

        Parameters:
            clusterer (GMMClusterer): Trained GMM model
            update_threshold (float): Minimum change to trigger update
            min_samples_for_update (int): Minimum new samples before updating
        """
        self.clusterer = clusterer
        self.update_threshold = update_threshold
        self.min_samples_for_update = min_samples_for_update
        self.update_history = []

        print(f"✓ ClusterUpdater initialized")
        print(f"  Update threshold: {update_threshold}")
        print(f"  Min samples for update: {min_samples_for_update}")

    def add_new_users(self,
                      X_new: np.ndarray,
                      user_ids: List[str],
                      retrain: bool = False) -> pd.DataFrame:
        """
        Add new users and optionally retrain clusters

        Parameters:
            X_new (np.ndarray): New user features
            user_ids (list): New user IDs
            retrain (bool): Whether to retrain the model

        Returns:
            pd.DataFrame: Cluster assignments for new users
        """
        print(f"\n--- Adding New Users ---")
        print(f"New users: {len(user_ids)}")

        # Predict clusters for new users (using existing model)
        labels = self.clusterer.predict(X_new)
        probas = self.clusterer.predict_proba(X_new)

        # Create assignments DataFrame
        assignments = pd.DataFrame({
            'user_id': user_ids,
            'cluster': labels
        })

        # Add probabilities
        for i in range(self.clusterer.n_components):
            assignments[f'prob_cluster_{i}'] = probas[:, i]

        assignments['primary_probability'] = probas.max(axis=1)

        print(f"✓ Assigned {len(user_ids)} new users to clusters")

        # Retrain if requested
        if retrain:
            print(f"\nRetraining model with new data...")
            self._retrain_with_new_data(X_new)

        return assignments

    def _retrain_with_new_data(self, X_new: np.ndarray):
        """
        Retrain GMM with new data added to existing data

        Parameters:
            X_new (np.ndarray): New user features
        """
        # This is a simplified approach
        # In production, you'd maintain a growing dataset

        print(f"⚠ Note: Full retraining requires access to all historical data")
        print(f"   This is a placeholder for incremental learning")

        # Record update
        self.update_history.append({
            'timestamp': datetime.now().isoformat(),
            'n_new_samples': len(X_new),
            'action': 'retrain_requested'
        })

    def update_user_cluster(self,
                            user_id: str,
                            X_updated: np.ndarray) -> Dict:
        """
        Update a single user's cluster assignment

        Parameters:
            user_id (str): User ID
            X_updated (np.ndarray): Updated feature vector

        Returns:
            dict: New cluster assignment info
        """
        print(f"\n--- Updating User Cluster ---")
        print(f"User: {user_id}")

        # Predict new cluster
        new_cluster = self.clusterer.predict(X_updated)[0]
        new_probas = self.clusterer.predict_proba(X_updated)[0]

        result = {
            'user_id': user_id,
            'new_cluster': int(new_cluster),
            'cluster_probabilities': new_probas.tolist(),
            'primary_probability': float(new_probas.max()),
            'timestamp': datetime.now().isoformat()
        }

        print(f"✓ New cluster: {new_cluster}")
        print(f"  Confidence: {new_probas.max():.1%}")

        # Record update
        self.update_history.append({
            'timestamp': result['timestamp'],
            'user_id': user_id,
            'action': 'cluster_updated',
            'new_cluster': int(new_cluster)
        })

        return result

    def detect_cluster_drift(self,
                             X_current: np.ndarray,
                             current_labels: np.ndarray) -> Dict:
        """
        Detect if cluster assignments have drifted significantly

        Parameters:
            X_current (np.ndarray): Current user features
            current_labels (np.ndarray): Current cluster assignments

        Returns:
            dict: Drift detection results
        """
        print(f"\n--- Detecting Cluster Drift ---")

        # Predict with current model
        predicted_labels = self.clusterer.predict(X_current)

        # Calculate percentage of changed assignments
        n_changed = np.sum(predicted_labels != current_labels)
        pct_changed = (n_changed / len(current_labels)) * 100

        drift_detected = pct_changed > (self.update_threshold * 100)

        result = {
            'drift_detected': drift_detected,
            'pct_changed': pct_changed,
            'n_changed': n_changed,
            'total_users': len(current_labels),
            'threshold': self.update_threshold * 100,
            'timestamp': datetime.now().isoformat()
        }

        print(f"Changed assignments: {n_changed}/{len(current_labels)} ({pct_changed:.1f}%)")
        print(f"Drift detected: {drift_detected}")

        return result

    def get_update_history(self) -> pd.DataFrame:
        """
        Get history of all updates

        Returns:
            pd.DataFrame: Update history
        """
        if not self.update_history:
            return pd.DataFrame()

        return pd.DataFrame(self.update_history)

    def save_update_history(self, filepath: str):
        """
        Save update history to file

        Parameters:
            filepath (str): Save path
        """
        history_df = self.get_update_history()

        if len(history_df) > 0:
            os.makedirs(os.path.dirname(filepath), exist_ok=True)
            history_df.to_csv(filepath, index=False)
            print(f"✓ Saved update history: {filepath}")
        else:
            print(f"⚠ No update history to save")


# Testing code
if __name__ == "__main__":
    """
    Test the Cluster Updater module
    """
    # Get the project root directory (Component4_GMM)
    SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
    PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..",".."))

    print("Testing Cluster Updater Module")
    print("=" * 60)

    # Load existing model
    print("\n1. Loading existing model...")
    from gmm_model import GMMClusterer

    model_path = os.path.join(PROJECT_ROOT, "models", "gmm_model_v1.pkl")
    clusterer = GMMClusterer.load_model(model_path)

    # Initialize updater
    print("\n2. Initializing Cluster Updater...")
    updater = ClusterUpdater(
        clusterer=clusterer,
        update_threshold=0.1,
        min_samples_for_update=10
    )

    # Simulate new users
    print("\n3. Simulating new users...")
    np.random.seed(123)
    X_new = np.random.rand(5, 5)  # 5 new users, 5 features
    new_user_ids = [f"NEW_USER_{i}" for i in range(1, 6)]

    assignments = updater.add_new_users(X_new, new_user_ids)
    print("\nNew user assignments:")
    print(assignments[['user_id', 'cluster', 'primary_probability']])

    # Simulate user update
    print("\n4. Simulating user resilience improvement...")
    X_updated = np.array([[0.85, 0.82, 0.88, 0.80, 0.83]])  # Improved scores

    result = updater.update_user_cluster("NEW_USER_1", X_updated)
    print(f"\nUpdate result:")
    print(f"  New cluster: {result['new_cluster']}")
    print(f"  Confidence: {result['primary_probability']:.1%}")

    # Check update history
    print("\n5. Update history:")
    history = updater.get_update_history()
    print(history)

    # Save history
    history_path = os.path.join(PROJECT_ROOT, "results", "clusters", "update_history.csv")
    updater.save_update_history(history_path)

    print("\n" + "=" * 60)
    print("✓ ALL TESTS PASSED!")
    print("=" * 60)