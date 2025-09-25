"""
9-Cluster GMM Clustering System for Mental Health Scores
"""

import numpy as np
import pandas as pd
from sklearn.mixture import GaussianMixture
from typing import Tuple, List
import matplotlib.pyplot as plt
import seaborn as sns


class NineClusterGMM:
    """
    Implements the 9-cluster mental health system using GMM
    Clusters users based on stress, depression, and anxiety scores
    9 clusters = 3 categories × 3 severity levels
    """

    def __init__(self, n_components: int = 9, random_state: int = 42):
        self.n_components = n_components
        self.random_state = random_state
        self.gmm = GaussianMixture(n_components=n_components, random_state=random_state)

    def fit(self, data: np.ndarray):
        """
        Fit the GMM model to data

        Args:
            data: np.ndarray shape (n_samples, 3) - stress, depression, anxiety
        """
        print("🔄 Fitting GMM model on data...")
        self.gmm.fit(data)
        print("✅ Model training complete!")

    def predict(self, data: np.ndarray) -> np.ndarray:
        """
        Predict cluster labels for users

        Args:
            data: np.ndarray shape (n_samples, 3)

        Returns:
            cluster labels: np.ndarray shape (n_samples,)
        """
        return self.gmm.predict(data)

    def predict_proba(self, data: np.ndarray) -> np.ndarray:
        """
        Predict soft cluster memberships (probabilities)

        Args:
            data: np.ndarray shape (n_samples, 3)

        Returns:
            cluster probabilities: np.ndarray shape (n_samples, n_components)
        """
        return self.gmm.predict_proba(data)

    def get_parameters(self) -> Tuple[np.ndarray, np.ndarray, np.ndarray]:
        """
        Get trained model parameters

        Returns:
            means: Cluster centroids (n_components, 3)
            covariances: Cluster covariance matrices (n_components, 3, 3)
            weights: Mixing weights (n_components,)
        """
        return self.gmm.means_, self.gmm.covariances_, self.gmm.weights_

    def visualize_clusters(self, data: np.ndarray, labels: np.ndarray):
        """
        Visualize the GMM clusters in 2D

        Args:
            data: np.ndarray (n_samples, 3)
            labels: np.ndarray (n_samples,)
        """
        features = ['Stress', 'Depression', 'Anxiety']
        df = pd.DataFrame(data, columns=features)
        df['Cluster'] = labels

        sns.set(style='whitegrid')
        sns.pairplot(df, hue='Cluster', palette='tab10', diag_kind='kde', height=2.5)
        plt.suptitle('GMM Cluster Assignments', y=1.02, fontsize=16, fontweight='bold')
        plt.show()


# Example usage
if __name__ == "__main__":
    from src.data.mental_health_generator import MentalHealthDataGenerator

    # Generate data
    generator = MentalHealthDataGenerator()
    data, true_clusters, df = generator.generate_full_dataset(samples_per_cluster=100)

    # Create GMM model
    gmm_system = NineClusterGMM(n_components=9)

    # Train model
    gmm_system.fit(data)

    # Predict cluster labels
    predicted_labels = gmm_system.predict(data)

    # Visualize predicted clusters
    gmm_system.visualize_clusters(data, predicted_labels)

    # Show cluster centroids
    means, covariances, weights = gmm_system.get_parameters()
    print("\n🎯 Cluster Centroids (means):\n", means)
    print("\n⚖️ Cluster Weights:\n", weights)
