"""
GMM Clustering Module

Implements Gaussian Mixture Model clustering for resilience-based
community formation.

Author: [Your Name]
Date: [Current Date]
"""

import numpy as np
import pandas as pd
from sklearn.mixture import GaussianMixture
from sklearn.metrics import silhouette_score, davies_bouldin_score
import pickle
import os
from typing import Tuple, Dict, Optional, List
import warnings

warnings.filterwarnings('ignore')


class GMMClusterer:
    """
    Gaussian Mixture Model clustering for resilience profiles

    Attributes:
        n_components (int): Number of clusters
        covariance_type (str): Type of covariance parameters
        random_state (int): Random seed for reproducibility
        model (GaussianMixture): The trained GMM model
        feature_names (list): Names of features used for clustering
        is_fitted (bool): Whether model has been trained
    """

    def __init__(self,
                 n_components: int = 5,
                 covariance_type: str = 'full',
                 random_state: int = 42,
                 max_iter: int = 100,
                 n_init: int = 10):
        """
        Initialize the GMM clusterer

        Parameters:
            n_components (int): Number of clusters (default: 5)
            covariance_type (str): Covariance type - 'full', 'tied', 'diag', 'spherical'
            random_state (int): Random seed for reproducibility
            max_iter (int): Maximum number of EM iterations
            n_init (int): Number of random initializations
        """
        self.n_components = n_components
        self.covariance_type = covariance_type
        self.random_state = random_state
        self.max_iter = max_iter
        self.n_init = n_init

        # Initialize the GMM model
        self.model = GaussianMixture(
            n_components=self.n_components,
            covariance_type=self.covariance_type,
            random_state=self.random_state,
            max_iter=self.max_iter,
            n_init=self.n_init,
            verbose=0
        )

        self.feature_names = None
        self.is_fitted = False
        self.cluster_centers_ = None
        self.labels_ = None

        print(f"✓ GMMClusterer initialized")
        print(f"  Number of clusters: {self.n_components}")
        print(f"  Covariance type: {self.covariance_type}")
        print(f"  Random state: {self.random_state}")

    def fit(self, X: np.ndarray, feature_names: Optional[List[str]] = None):
        """
        Train the GMM model on the data

        Parameters:
            X (np.ndarray): Training data, shape (n_samples, n_features)
            feature_names (list): Names of features (optional)

        Returns:
            self: The fitted model
        """
        print(f"\n--- Training GMM Model ---")
        print(f"Data shape: {X.shape}")
        print(f"Training with EM algorithm...")

        # Store feature names
        if feature_names is not None:
            self.feature_names = feature_names
        else:
            self.feature_names = [f"feature_{i}" for i in range(X.shape[1])]

        # Fit the model
        self.model.fit(X)

        # Store results
        self.cluster_centers_ = self.model.means_
        self.labels_ = self.model.predict(X)
        self.is_fitted = True

        # Calculate model metrics
        log_likelihood = self.model.score(X)
        bic = self.model.bic(X)
        aic = self.model.aic(X)

        print(f"✓ Model trained successfully")
        print(f"  Converged: {self.model.converged_}")
        print(f"  Iterations: {self.model.n_iter_}")
        print(f"  Log-likelihood: {log_likelihood:.2f}")
        print(f"  BIC: {bic:.2f}")
        print(f"  AIC: {aic:.2f}")

        return self

    def predict(self, X: np.ndarray) -> np.ndarray:
        """
        Predict cluster assignments for new data

        Parameters:
            X (np.ndarray): Data to cluster, shape (n_samples, n_features)

        Returns:
            np.ndarray: Cluster assignments (hard labels)

        Raises:
            ValueError: If model hasn't been fitted
        """
        if not self.is_fitted:
            raise ValueError("Model not fitted. Call fit() first.")

        labels = self.model.predict(X)
        return labels

    def predict_proba(self, X: np.ndarray) -> np.ndarray:
        """
        Predict cluster membership probabilities

        Parameters:
            X (np.ndarray): Data to cluster

        Returns:
            np.ndarray: Probability distributions, shape (n_samples, n_components)
                       Each row sums to 1.0

        Example:
            User_001 → [0.70, 0.25, 0.05]  # 70% cluster 0, 25% cluster 1, 5% cluster 2
        """
        if not self.is_fitted:
            raise ValueError("Model not fitted. Call fit() first.")

        probas = self.model.predict_proba(X)
        return probas

    def fit_predict(self, X: np.ndarray, feature_names: Optional[List[str]] = None) -> np.ndarray:
        """
        Fit model and predict cluster assignments in one step

        Parameters:
            X (np.ndarray): Training data
            feature_names (list): Feature names (optional)

        Returns:
            np.ndarray: Cluster assignments
        """
        self.fit(X, feature_names)
        return self.labels_

    def get_cluster_centers(self) -> np.ndarray:
        """
        Get the cluster centers (means)

        Returns:
            np.ndarray: Cluster centers, shape (n_components, n_features)
        """
        if not self.is_fitted:
            raise ValueError("Model not fitted. Call fit() first.")

        return self.cluster_centers_

    def get_cluster_info(self) -> pd.DataFrame:
        """
        Get detailed information about each cluster

        Returns:
            pd.DataFrame: Cluster information with columns for each feature
        """
        if not self.is_fitted:
            raise ValueError("Model not fitted. Call fit() first.")

        # Create DataFrame with cluster centers
        cluster_info = pd.DataFrame(
            self.cluster_centers_,
            columns=self.feature_names
        )

        # Add cluster IDs
        cluster_info.insert(0, 'cluster_id', range(self.n_components))

        # Add mixing weights (how common each cluster is)
        cluster_info['weight'] = self.model.weights_

        return cluster_info

    def evaluate_clustering(self, X: np.ndarray) -> Dict[str, float]:
        """
        Evaluate clustering quality using multiple metrics

        Parameters:
            X (np.ndarray): The data that was clustered

        Returns:
            dict: Dictionary of evaluation metrics
        """
        if not self.is_fitted:
            raise ValueError("Model not fitted. Call fit() first.")

        print(f"\n--- Evaluating Clustering Quality ---")

        labels = self.labels_

        # Silhouette Score (-1 to 1, higher is better)
        # Measures how similar points are to their own cluster vs other clusters
        silhouette = silhouette_score(X, labels)

        # Davies-Bouldin Index (>=0, lower is better)
        # Measures average similarity between each cluster and its most similar cluster
        davies_bouldin = davies_bouldin_score(X, labels)

        # BIC and AIC (lower is better)
        # Balance model complexity with fit quality
        bic = self.model.bic(X)
        aic = self.model.aic(X)

        # Log-likelihood (higher is better)
        # How well the model explains the data
        log_likelihood = self.model.score(X)

        metrics = {
            'silhouette_score': silhouette,
            'davies_bouldin_index': davies_bouldin,
            'bic': bic,
            'aic': aic,
            'log_likelihood': log_likelihood,
            'n_components': self.n_components
        }

        print(f"Silhouette Score: {silhouette:.3f} (range: -1 to 1, higher better)")
        print(f"Davies-Bouldin Index: {davies_bouldin:.3f} (lower better)")
        print(f"BIC: {bic:.2f} (lower better)")
        print(f"AIC: {aic:.2f} (lower better)")
        print(f"Log-Likelihood: {log_likelihood:.2f} (higher better)")

        return metrics

    def get_cluster_assignments(self, X: np.ndarray, user_ids: List[str]) -> pd.DataFrame:
        """
        Get cluster assignments for users with probabilities

        Parameters:
            X (np.ndarray): Feature data
            user_ids (list): List of user IDs

        Returns:
            pd.DataFrame: User assignments with probabilities
        """
        if not self.is_fitted:
            raise ValueError("Model not fitted. Call fit() first.")

        # Get hard labels and probabilities
        labels = self.predict(X)
        probas = self.predict_proba(X)

        # Create DataFrame
        assignments = pd.DataFrame({
            'user_id': user_ids,
            'cluster': labels
        })

        # Add probability columns
        for i in range(self.n_components):
            assignments[f'prob_cluster_{i}'] = probas[:, i]

        # Add primary and secondary clusters
        assignments['primary_cluster'] = labels
        assignments['primary_probability'] = probas.max(axis=1)

        # Get secondary cluster (second highest probability)
        secondary_cluster_idx = np.argsort(probas, axis=1)[:, -2]
        assignments['secondary_cluster'] = secondary_cluster_idx
        assignments['secondary_probability'] = probas[np.arange(len(probas)), secondary_cluster_idx]

        return assignments

    def save_model(self, filepath: str):
        """
        Save the trained model to disk

        Parameters:
            filepath (str): Path where to save the model
        """
        if not self.is_fitted:
            raise ValueError("Model not fitted. Nothing to save.")

        print(f"\n--- Saving Model ---")

        # Create directory if it doesn't exist
        os.makedirs(os.path.dirname(filepath), exist_ok=True)

        # Save model and metadata
        model_data = {
            'model': self.model,
            'n_components': self.n_components,
            'covariance_type': self.covariance_type,
            'feature_names': self.feature_names,
            'cluster_centers': self.cluster_centers_,
            'random_state': self.random_state
        }

        with open(filepath, 'wb') as f:
            pickle.dump(model_data, f)

        print(f"✓ Model saved to: {filepath}")

    @classmethod
    def load_model(cls, filepath: str):
        """
        Load a trained model from disk

        Parameters:
            filepath (str): Path to the saved model

        Returns:
            GMMClusterer: Loaded model instance
        """
        print(f"\n--- Loading Model ---")

        if not os.path.exists(filepath):
            raise FileNotFoundError(f"Model file not found: {filepath}")

        with open(filepath, 'rb') as f:
            model_data = pickle.load(f)

        # Create instance
        instance = cls(
            n_components=model_data['n_components'],
            covariance_type=model_data['covariance_type'],
            random_state=model_data['random_state']
        )

        # Restore model state
        instance.model = model_data['model']
        instance.feature_names = model_data['feature_names']
        instance.cluster_centers_ = model_data['cluster_centers']
        instance.is_fitted = True

        print(f"✓ Model loaded from: {filepath}")
        print(f"  Clusters: {instance.n_components}")
        print(f"  Features: {len(instance.feature_names)}")

        return instance


def find_optimal_k(X: np.ndarray,
                   k_range: range = range(2, 11),
                   covariance_type: str = 'full',
                   random_state: int = 42) -> Tuple[int, pd.DataFrame]:
    """
    Find optimal number of clusters using BIC

    Parameters:
        X (np.ndarray): Data to cluster
        k_range (range): Range of k values to test
        covariance_type (str): Covariance type
        random_state (int): Random seed

    Returns:
        Tuple[int, pd.DataFrame]: (optimal_k, results_dataframe)
    """
    print(f"\n{'=' * 60}")
    print(f"FINDING OPTIMAL NUMBER OF CLUSTERS")
    print(f"{'=' * 60}")
    print(f"Testing k from {k_range.start} to {k_range.stop - 1}")

    results = []

    for k in k_range:
        print(f"\nTesting k={k}...")

        # Train GMM
        gmm = GaussianMixture(
            n_components=k,
            covariance_type=covariance_type,
            random_state=random_state,
            n_init=10
        )

        gmm.fit(X)
        labels = gmm.predict(X)

        # Calculate metrics
        bic = gmm.bic(X)
        aic = gmm.aic(X)
        silhouette = silhouette_score(X, labels)

        results.append({
            'k': k,
            'bic': bic,
            'aic': aic,
            'silhouette': silhouette,
            'converged': gmm.converged_
        })

        print(f"  BIC: {bic:.2f}, AIC: {aic:.2f}, Silhouette: {silhouette:.3f}")

    # Convert to DataFrame
    results_df = pd.DataFrame(results)

    # Find optimal k (lowest BIC)
    optimal_k = results_df.loc[results_df['bic'].idxmin(), 'k']

    print(f"\n{'=' * 60}")
    print(f"✓ Optimal k: {optimal_k} (lowest BIC)")
    print(f"{'=' * 60}")

    return optimal_k, results_df


# Testing code
if __name__ == "__main__":
    """
    Test the GMM clustering module
    """
    print("Testing GMM Clustering Module")
    print("=" * 60)

    # Get the project root directory (Component4_GMM)
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(os.path.dirname(script_dir))
    os.chdir(project_root)

    # Load preprocessed data
    print("\n1. Loading preprocessed data...")
    df = pd.read_csv("data/processed/resilience_indicators_normalized.csv")

    # Get feature columns
    feature_cols = [
        'emotional_regulation_score',
        'social_connectivity_score',
        'behavioral_stability_score',
        'cognitive_flexibility_score',
        'stress_coping_mechanism'
    ]

    X = df[feature_cols].values
    user_ids = df['user_id'].tolist()

    print(f"✓ Loaded {len(df)} users with {len(feature_cols)} features")

    # Find optimal k (optional - takes time)
    print("\n2. Finding optimal number of clusters...")
    optimal_k, results = find_optimal_k(X, k_range=range(3, 8))
    print("\nResults:")
    print(results)

    # Train GMM with optimal k
    print(f"\n3. Training GMM with k={optimal_k}...")
    clusterer = GMMClusterer(
        n_components=optimal_k,
        covariance_type='full',
        random_state=42
    )

    clusterer.fit(X, feature_names=feature_cols)

    # Evaluate clustering
    print("\n4. Evaluating clustering quality...")
    metrics = clusterer.evaluate_clustering(X)

    # Get cluster information
    print("\n5. Cluster centers:")
    cluster_info = clusterer.get_cluster_info()
    print(cluster_info)

    # Get assignments
    print("\n6. Getting user assignments...")
    assignments = clusterer.get_cluster_assignments(X, user_ids)
    print("\nFirst 10 assignments:")
    print(assignments.head(10))

    # Show cluster distribution
    print("\n7. Cluster distribution:")
    cluster_counts = assignments['cluster'].value_counts().sort_index()
    for cluster_id, count in cluster_counts.items():
        percentage = (count / len(assignments)) * 100
        print(f"  Cluster {cluster_id}: {count} users ({percentage:.1f}%)")

    # Save model
    print("\n8. Saving model...")
    clusterer.save_model("models/gmm_model_v1.pkl")

    # Save assignments
    print("\n9. Saving cluster assignments...")
    os.makedirs("results/clusters", exist_ok=True)
    assignments.to_csv("results/clusters/cluster_assignments.csv", index=False)

    print("\n" + "=" * 60)
    print("✓ ALL TESTS PASSED!")
    print("=" * 60)