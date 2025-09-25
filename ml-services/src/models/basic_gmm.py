"""
Basic GMM Example for Mental Health Clustering
This is our first step to understand how GMM works
"""

import numpy as np
import matplotlib.pyplot as plt
from sklearn.mixture import GaussianMixture
from sklearn.datasets import make_blobs


def create_sample_data():
    """
    Create sample mental health data
    3 features: stress, depression, anxiety (0.0 to 1.0)
    """
    # Generate sample data with 3 natural clusters
    np.random.seed(42)

    # Create 3 groups of users with different mental health profiles
    data, _ = make_blobs(
        n_samples=300,  # 300 users
        centers=3,  # 3 natural groups
        n_features=3,  # stress, depression, anxiety
        cluster_std=0.15,  # how spread out each group is
        random_state=42
    )

    # Scale to 0-1 range (mental health scores)
    data = np.clip(data, 0, 1)

    # Add a small amount of noise to prevent duplicate points
    noise = np.random.normal(0, 1e-5, data.shape)
    data = data + noise

    return data


def fit_basic_gmm(data, n_components=3):
    """
    Fit a basic GMM model
    """
    print("🤖 Training GMM Model...")

    # Create GMM model
    gmm = GaussianMixture(
        n_components=n_components,  # Number of clusters
        random_state=42
    )

    # Fit the model
    gmm.fit(data)

    # Get cluster assignments
    cluster_labels = gmm.predict(data)

    # Get probabilities for each cluster
    cluster_probabilities = gmm.predict_proba(data)

    print(f"✅ Model trained! Found {n_components} clusters")

    return gmm, cluster_labels, cluster_probabilities


def analyze_clusters(gmm, data, labels):
    """
    Analyze what each cluster represents
    """
    print("\n📊 Cluster Analysis:")
    print("-" * 50)

    for i in range(gmm.n_components):
        # Find users in this cluster
        cluster_mask = labels == i
        
        if not np.any(cluster_mask):
            print(f"Cluster {i+1} is empty.")
            continue
            
        cluster_data = data[cluster_mask]

        # Calculate average scores for this cluster
        avg_stress = np.mean(cluster_data[:, 0])
        avg_depression = np.mean(cluster_data[:, 1])
        avg_anxiety = np.mean(cluster_data[:, 2])

        # Determine cluster characteristics
        dominant_issue = "Mixed"
        if avg_stress > avg_depression and avg_stress > avg_anxiety:
            dominant_issue = "Stress-Dominant"
        elif avg_depression > avg_stress and avg_depression > avg_anxiety:
            dominant_issue = "Depression-Dominant"
        elif avg_anxiety > avg_stress and avg_anxiety > avg_depression:
            dominant_issue = "Anxiety-Dominant"

        print(f"Cluster {i + 1}: {dominant_issue}")
        print(f"  👥 Users: {len(cluster_data)}")
        print(f"  📈 Avg Stress: {avg_stress:.3f}")
        print(f"  😔 Avg Depression: {avg_depression:.3f}")
        print(f"  😰 Avg Anxiety: {avg_anxiety:.3f}")
        print()


if __name__ == "__main__":
    print("🧠 Mental Health GMM Clustering - Basic Example")
    print("=" * 60)

    # Step 1: Create sample data
    print("📊 Creating sample mental health data...")
    data = create_sample_data()
    print(f"✅ Generated data for {len(data)} users")

    # Step 2: Fit GMM model
    gmm_model, cluster_labels, cluster_probs = fit_basic_gmm(data)

    # Step 3: Analyze results
    analyze_clusters(gmm_model, data, cluster_labels)

    print("🎉 Basic GMM example complete!")
