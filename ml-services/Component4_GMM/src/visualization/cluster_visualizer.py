"""
Cluster Visualization Module

Creates visualizations for GMM clustering results.

Author: [Your Name]
Date: [Current Date]
"""

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.decomposition import PCA
from typing import Optional, List
import os

# Set style
sns.set_style("whitegrid")
plt.rcParams['figure.figsize'] = (12, 8)


class ClusterVisualizer:
    """
    Visualizes clustering results and patterns
    """

    def __init__(self, output_dir: str = "results/visualizations"):
        """
        Initialize the visualizer

        Parameters:
            output_dir (str): Directory to save plots
        """
        self.output_dir = output_dir
        os.makedirs(self.output_dir, exist_ok=True)

        print(f"✓ ClusterVisualizer initialized")
        print(f"  Output directory: {self.output_dir}")

    def plot_clusters_2d(self,
                         X: np.ndarray,
                         labels: np.ndarray,
                         title: str = "GMM Clusters",
                         save_path: Optional[str] = None):
        """
        Plot clusters in 2D using PCA for dimensionality reduction

        Parameters:
            X (np.ndarray): Original high-dimensional data
            labels (np.ndarray): Cluster assignments
            title (str): Plot title
            save_path (str): Path to save plot (optional)
        """
        print(f"\n--- Creating 2D Cluster Plot ---")

        # Reduce to 2D using PCA
        pca = PCA(n_components=2, random_state=42)
        X_2d = pca.fit_transform(X)

        # Explained variance
        explained_var = pca.explained_variance_ratio_
        print(f"PCA explained variance: {explained_var[0]:.2%}, {explained_var[1]:.2%}")
        print(f"Total: {sum(explained_var):.2%}")

        # Create plot
        fig, ax = plt.subplots(figsize=(12, 8))

        # Plot each cluster
        n_clusters = len(np.unique(labels))
        colors = sns.color_palette("husl", n_clusters)

        for cluster_id in range(n_clusters):
            mask = labels == cluster_id
            ax.scatter(
                X_2d[mask, 0],
                X_2d[mask, 1],
                c=[colors[cluster_id]],
                label=f'Cluster {cluster_id}',
                alpha=0.6,
                s=100,
                edgecolors='black',
                linewidth=0.5
            )

        ax.set_xlabel(f'PC1 ({explained_var[0]:.1%} variance)', fontsize=12)
        ax.set_ylabel(f'PC2 ({explained_var[1]:.1%} variance)', fontsize=12)
        ax.set_title(title, fontsize=14, fontweight='bold')
        ax.legend(loc='best', fontsize=10)
        ax.grid(True, alpha=0.3)

        plt.tight_layout()

        # Save if path provided
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
            print(f"✓ Saved to: {save_path}")
        else:
            default_path = os.path.join(self.output_dir, "clusters_2d.png")
            plt.savefig(default_path, dpi=300, bbox_inches='tight')
            print(f"✓ Saved to: {default_path}")

        plt.show()

    def plot_cluster_heatmap(self,
                             cluster_centers: pd.DataFrame,
                             title: str = "Cluster Centers Heatmap",
                             save_path: Optional[str] = None):
        """
        Plot heatmap of cluster centers

        Parameters:
            cluster_centers (pd.DataFrame): Cluster centers data
            title (str): Plot title
            save_path (str): Path to save plot
        """
        print(f"\n--- Creating Cluster Heatmap ---")

        # Prepare data (exclude cluster_id and weight columns)
        feature_cols = [col for col in cluster_centers.columns
                        if col not in ['cluster_id', 'weight']]

        data = cluster_centers[feature_cols].values

        # Create plot
        fig, ax = plt.subplots(figsize=(10, 6))

        sns.heatmap(
            data,
            annot=True,
            fmt='.2f',
            cmap='RdYlGn',
            vmin=0,
            vmax=1,
            center=0.5,
            yticklabels=[f'Cluster {i}' for i in range(len(data))],
            xticklabels=[col.replace('_', ' ').title() for col in feature_cols],
            cbar_kws={'label': 'Score'},
            linewidths=0.5,
            ax=ax
        )

        ax.set_title(title, fontsize=14, fontweight='bold', pad=20)
        plt.xticks(rotation=45, ha='right')
        plt.yticks(rotation=0)
        plt.tight_layout()

        # Save
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
            print(f"✓ Saved to: {save_path}")
        else:
            default_path = os.path.join(self.output_dir, "cluster_heatmap.png")
            plt.savefig(default_path, dpi=300, bbox_inches='tight')
            print(f"✓ Saved to: {default_path}")

        plt.show()

    def plot_cluster_distribution(self,
                                  labels: np.ndarray,
                                  title: str = "Cluster Distribution",
                                  save_path: Optional[str] = None):
        """
        Plot distribution of users across clusters

        Parameters:
            labels (np.ndarray): Cluster assignments
            title (str): Plot title
            save_path (str): Path to save plot
        """
        print(f"\n--- Creating Cluster Distribution Plot ---")

        # Count users per cluster
        unique, counts = np.unique(labels, return_counts=True)
        percentages = (counts / len(labels)) * 100

        # Create plot
        fig, ax = plt.subplots(figsize=(10, 6))

        colors = sns.color_palette("husl", len(unique))
        bars = ax.bar(unique, counts, color=colors, edgecolor='black', linewidth=1.5)

        # Add percentage labels on bars
        for bar, pct in zip(bars, percentages):
            height = bar.get_height()
            ax.text(
                bar.get_x() + bar.get_width() / 2.,
                height,
                f'{int(height)}\n({pct:.1f}%)',
                ha='center',
                va='bottom',
                fontsize=10,
                fontweight='bold'
            )

        ax.set_xlabel('Cluster ID', fontsize=12)
        ax.set_ylabel('Number of Users', fontsize=12)
        ax.set_title(title, fontsize=14, fontweight='bold')
        ax.set_xticks(unique)
        ax.grid(True, alpha=0.3, axis='y')

        plt.tight_layout()

        # Save
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
            print(f"✓ Saved to: {save_path}")
        else:
            default_path = os.path.join(self.output_dir, "cluster_distribution.png")
            plt.savefig(default_path, dpi=300, bbox_inches='tight')
            print(f"✓ Saved to: {default_path}")

        plt.show()

    def plot_bic_scores(self,
                        results_df: pd.DataFrame,
                        title: str = "Model Selection (BIC Scores)",
                        save_path: Optional[str] = None):
        """
        Plot BIC scores for different k values

        Parameters:
            results_df (pd.DataFrame): Results from find_optimal_k()
            title (str): Plot title
            save_path (str): Path to save plot
        """
        print(f"\n--- Creating BIC Score Plot ---")

        fig, ax = plt.subplots(figsize=(10, 6))

        ax.plot(results_df['k'], results_df['bic'],
                marker='o', linewidth=2, markersize=8,
                color='#2E86AB', label='BIC')

        ax.plot(results_df['k'], results_df['aic'],
                marker='s', linewidth=2, markersize=8,
                color='#A23B72', label='AIC', alpha=0.7)

        # Mark optimal k
        optimal_idx = results_df['bic'].idxmin()
        optimal_k = results_df.loc[optimal_idx, 'k']
        optimal_bic = results_df.loc[optimal_idx, 'bic']

        ax.plot(optimal_k, optimal_bic, 'r*', markersize=20,
                label=f'Optimal k={optimal_k}')

        ax.set_xlabel('Number of Clusters (k)', fontsize=12)
        ax.set_ylabel('Score (lower is better)', fontsize=12)
        ax.set_title(title, fontsize=14, fontweight='bold')
        ax.legend(loc='best', fontsize=10)
        ax.grid(True, alpha=0.3)
        ax.set_xticks(results_df['k'])

        plt.tight_layout()

        # Save
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
            print(f"✓ Saved to: {save_path}")
        else:
            default_path = os.path.join(self.output_dir, "bic_scores.png")
            plt.savefig(default_path, dpi=300, bbox_inches='tight')
            print(f"✓ Saved to: {default_path}")

        plt.show()


# Testing code
if __name__ == "__main__":
    """
    Test the visualization module
    """
    import sys

    print("Testing Cluster Visualizer")
    print("=" * 60)

    # Get the project root directory (Component4_GMM)
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(os.path.dirname(script_dir))
    os.chdir(project_root)

    # Add src directory to path for imports
    sys.path.insert(0, os.path.join(project_root, 'src'))

    # Load data
    print("\n1. Loading data...")
    df = pd.read_csv("data/processed/resilience_indicators_normalized.csv")
    assignments = pd.read_csv("results/clusters/cluster_assignments.csv")

    feature_cols = [
        'emotional_regulation_score',
        'social_connectivity_score',
        'behavioral_stability_score',
        'cognitive_flexibility_score',
        'stress_coping_mechanism'
    ]

    X = df[feature_cols].values
    labels = assignments['cluster'].values

    # Load cluster centers
    from clustering.gmm_model import GMMClusterer

    clusterer = GMMClusterer.load_model("models/gmm_model_v1.pkl")
    cluster_info = clusterer.get_cluster_info()

    # Initialize visualizer
    visualizer = ClusterVisualizer()

    # Create visualizations
    print("\n2. Creating visualizations...")

    visualizer.plot_clusters_2d(X, labels)
    visualizer.plot_cluster_heatmap(cluster_info)
    visualizer.plot_cluster_distribution(labels)

    # If you have BIC results
    try:
        results = pd.read_csv("results/clusters/optimal_k_results.csv")
        visualizer.plot_bic_scores(results)
    except:
        print("\nSkipping BIC plot (no results file found)")

    print("\n✓ All visualizations created!")