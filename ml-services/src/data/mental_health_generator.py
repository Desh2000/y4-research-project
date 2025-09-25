"""
Realistic Mental Health Data Generator
Creates synthetic data that mimics real mental health patterns
"""

import numpy as np
import pandas as pd
from typing import Tuple, List
import matplotlib.pyplot as plt
import seaborn as sns


class MentalHealthDataGenerator:
    """
    Generate realistic mental health data for clustering
    """

    def __init__(self, random_state: int = 42):
        """
        Initialize the data generator

        Args:
            random_state: For reproducible results
        """
        self.random_state = random_state
        np.random.seed(random_state)

        # Define the 9 cluster templates (3x3 matrix)
        self.cluster_templates = {
            # STRESS clusters
            'STRESS_LOW': {'stress': (0.1, 0.3), 'depression': (0.1, 0.4), 'anxiety': (0.1, 0.4)},
            'STRESS_MEDIUM': {'stress': (0.4, 0.7), 'depression': (0.1, 0.5), 'anxiety': (0.1, 0.5)},
            'STRESS_HIGH': {'stress': (0.7, 0.9), 'depression': (0.2, 0.6), 'anxiety': (0.3, 0.7)},

            # DEPRESSION clusters
            'DEPRESSION_LOW': {'stress': (0.1, 0.4), 'depression': (0.1, 0.3), 'anxiety': (0.1, 0.4)},
            'DEPRESSION_MEDIUM': {'stress': (0.1, 0.5), 'depression': (0.4, 0.7), 'anxiety': (0.1, 0.5)},
            'DEPRESSION_HIGH': {'stress': (0.2, 0.6), 'depression': (0.7, 0.9), 'anxiety': (0.2, 0.6)},

            # ANXIETY clusters
            'ANXIETY_LOW': {'stress': (0.1, 0.4), 'depression': (0.1, 0.4), 'anxiety': (0.1, 0.3)},
            'ANXIETY_MEDIUM': {'stress': (0.1, 0.5), 'depression': (0.1, 0.5), 'anxiety': (0.4, 0.7)},
            'ANXIETY_HIGH': {'stress': (0.3, 0.7), 'depression': (0.2, 0.6), 'anxiety': (0.7, 0.9)},
        }

        # Professional support recommendations for each cluster
        self.support_levels = {
            'STRESS_LOW': 'MINIMAL',
            'STRESS_MEDIUM': 'MODERATE',
            'STRESS_HIGH': 'INTENSIVE',
            'DEPRESSION_LOW': 'MINIMAL',
            'DEPRESSION_MEDIUM': 'MODERATE',
            'DEPRESSION_HIGH': 'CRITICAL',
            'ANXIETY_LOW': 'MINIMAL',
            'ANXIETY_MEDIUM': 'MODERATE',
            'ANXIETY_HIGH': 'INTENSIVE'
        }

    def generate_cluster_data(self, cluster_name: str, n_samples: int) -> np.ndarray:
        """
        Generate data for a specific cluster

        Args:
            cluster_name: Name of cluster (e.g., 'STRESS_HIGH')
            n_samples: Number of samples to generate

        Returns:
            Array of shape (n_samples, 3) with [stress, depression, anxiety] scores
        """
        template = self.cluster_templates[cluster_name]
        data = []

        for _ in range(n_samples):
            # Generate correlated scores (mental health issues often co-occur)
            base_stress = np.random.uniform(*template['stress'])
            base_depression = np.random.uniform(*template['depression'])
            base_anxiety = np.random.uniform(*template['anxiety'])

            # Add some correlation between scores (realistic pattern)
            if 'STRESS_HIGH' in cluster_name:
                # High stress often increases anxiety
                base_anxiety = min(1.0, base_anxiety + 0.1 * np.random.random())

            if 'DEPRESSION_HIGH' in cluster_name:
                # High depression often increases stress
                base_stress = min(1.0, base_stress + 0.1 * np.random.random())

            if 'ANXIETY_HIGH' in cluster_name:
                # High anxiety often increases stress
                base_stress = min(1.0, base_stress + 0.1 * np.random.random())

            # Add some random noise (real data isn't perfect)
            noise = np.random.normal(0, 0.05, 3)
            scores = np.array([base_stress, base_depression, base_anxiety]) + noise

            # Ensure scores stay in valid range [0, 1]
            scores = np.clip(scores, 0.0, 1.0)
            data.append(scores)

        return np.array(data)

    def generate_full_dataset(self, samples_per_cluster: int = 100) -> Tuple[np.ndarray, List[str], pd.DataFrame]:
        """
        Generate complete dataset with all 9 clusters

        Args:
            samples_per_cluster: Number of users per cluster

        Returns:
            data: Mental health scores array
            cluster_names: List of true cluster names
            df: Pandas DataFrame with additional info
        """
        print("🧠 Generating realistic mental health dataset...")
        print(f"📊 Creating {samples_per_cluster} users per cluster (9 clusters total)")

        all_data = []
        all_labels = []
        all_cluster_names = []

        for cluster_name in self.cluster_templates.keys():
            print(f"  🔄 Generating {cluster_name}...")

            # Generate data for this cluster
            cluster_data = self.generate_cluster_data(cluster_name, samples_per_cluster)

            # Store data and labels
            all_data.append(cluster_data)
            all_labels.extend([cluster_name] * samples_per_cluster)
            all_cluster_names.extend([cluster_name] * samples_per_cluster)

        # Combine all data
        data = np.vstack(all_data)

        # Create DataFrame with additional information
        df = pd.DataFrame({
            'stress_score': data[:, 0],
            'depression_score': data[:, 1],
            'anxiety_score': data[:, 2],
            'true_cluster': all_cluster_names,
            'dominant_issue': [name.split('_')[0] for name in all_cluster_names],
            'severity_level': [name.split('_')[1] for name in all_cluster_names],
            'support_level': [self.support_levels[name] for name in all_cluster_names]
        })

        # Add overall risk score
        df['overall_risk'] = (df['stress_score'] * 0.4 +
                              df['depression_score'] * 0.35 +
                              df['anxiety_score'] * 0.25)

        # Add user IDs
        df['user_id'] = [f'USER_{i + 1:04d}' for i in range(len(df))]

        print(f"✅ Generated {len(df)} total users")
        print(f"📈 Dataset shape: {data.shape}")

        return data, all_cluster_names, df

    def visualize_clusters(self, df: pd.DataFrame, save_path: str = None):
        """
        Visualize the generated clusters
        """
        print("📊 Creating cluster visualizations...")

        # Set up the plot style
        plt.style.use('default')
        fig, axes = plt.subplots(2, 2, figsize=(15, 12))
        fig.suptitle('Mental Health Clusters - Ground Truth', fontsize=16, fontweight='bold')

        # 1. 3D scatter plot (simplified to 2D pairs)
        ax1 = axes[0, 0]
        scatter = ax1.scatter(df['stress_score'], df['depression_score'],
                              c=df['true_cluster'].astype('category').cat.codes,
                              cmap='tab10', alpha=0.7, s=50)
        ax1.set_xlabel('Stress Score')
        ax1.set_ylabel('Depression Score')
        ax1.set_title('Stress vs Depression by Cluster')
        ax1.grid(True, alpha=0.3)

        # 2. Distribution by dominant issue
        ax2 = axes[0, 1]
        issue_counts = df['dominant_issue'].value_counts()
        ax2.pie(issue_counts.values, labels=issue_counts.index, autopct='%1.1f%%')
        ax2.set_title('Distribution by Dominant Issue')

        # 3. Support level distribution
        ax3 = axes[1, 0]
        support_counts = df['support_level'].value_counts()
        bars = ax3.bar(support_counts.index, support_counts.values)
        ax3.set_title('Users by Support Level Required')
        ax3.set_ylabel('Number of Users')

        # Color bars by severity
        colors = {'MINIMAL': 'green', 'MODERATE': 'yellow', 'INTENSIVE': 'orange', 'CRITICAL': 'red'}
        for bar, level in zip(bars, support_counts.index):
            bar.set_color(colors.get(level, 'blue'))

        # 4. Overall risk distribution
        ax4 = axes[1, 1]
        ax4.hist(df['overall_risk'], bins=20, alpha=0.7, color='purple')
        ax4.axvline(df['overall_risk'].mean(), color='red', linestyle='--',
                    label=f'Mean: {df["overall_risk"].mean():.3f}')
        ax4.set_xlabel('Overall Risk Score')
        ax4.set_ylabel('Number of Users')
        ax4.set_title('Overall Risk Score Distribution')
        ax4.legend()
        ax4.grid(True, alpha=0.3)

        plt.tight_layout()

        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
            print(f"💾 Visualization saved to: {save_path}")

        plt.show()

        # Print cluster statistics
        self._print_cluster_stats(df)

    def _print_cluster_stats(self, df: pd.DataFrame):
        """Print detailed cluster statistics"""
        print("\n📊 CLUSTER STATISTICS")
        print("=" * 60)

        for cluster in sorted(df['true_cluster'].unique()):
            cluster_data = df[df['true_cluster'] == cluster]
            print(f"\n🎯 {cluster}")
            print(f"   👥 Users: {len(cluster_data)}")
            print(f"   📈 Avg Scores: Stress={cluster_data['stress_score'].mean():.3f}, "
                  f"Depression={cluster_data['depression_score'].mean():.3f}, "
                  f"Anxiety={cluster_data['anxiety_score'].mean():.3f}")
            print(f"   🏥 Support Level: {cluster_data['support_level'].iloc[0]}")
            print(
                f"   ⚠️  Risk Range: {cluster_data['overall_risk'].min():.3f} - {cluster_data['overall_risk'].max():.3f}")


# Example usage
if __name__ == "__main__":
    print("🧠 Mental Health Data Generator - Testing")
    print("=" * 50)

    # Create generator
    generator = MentalHealthDataGenerator(random_state=42)

    # Generate dataset
    data, cluster_names, df = generator.generate_full_dataset(samples_per_cluster=50)

    # Show basic info
    print(f"\n📊 Dataset Overview:")
    print(f"   Total users: {len(df)}")
    print(f"   Clusters: {df['true_cluster'].nunique()}")
    print(f"   Features: {data.shape[1]} (stress, depression, anxiety)")

    # Show sample data
    print(f"\n📋 Sample Data:")
    print(df.head(10).to_string(index=False))

    # Create visualizations
    generator.visualize_clusters(df)

    print("\n🎉 Data generation complete!")
