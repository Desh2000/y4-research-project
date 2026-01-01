# train_gmm.py
# Script to train the GMM model

"""
GMM Training Script
===================
This script trains the GMM clustering model on our data.
"""

import pandas as pd
from app.core.clustering import ClusteringService

print("="*60)
print("GMM MODEL TRAINING")
print("="*60)

# Step 1: Load training data
print("\nLoading training data...")
train_data = pd.read_csv('data/gmm_training_data.csv')
print(f"Loaded {len(train_data)} records")

# Step 2: Create clustering service
print("\nInitializing clustering service...")
clustering = ClusteringService(n_clusters=5)

# Step 3: Train the model
print("\nTraining model...")
results = clustering.train(train_data)

# Step 4: Save the model
print("\nSaving model...")
model_path = clustering.save_model()

# Step 5: Print summary
print("\n" + "="*60)
print("TRAINING SUMMARY")
print("="*60)
print(f"  Samples used: {results['n_samples']}")
print(f"  Features used: {results['n_features']}")
print(f"  Clusters created: {results['n_clusters']}")
print(f"  Model score: {results['model_score']:.4f}")
print(f"\nCluster distribution:")
for cluster_id, count in results['cluster_counts'].items():
    name = results['group_names'].get(cluster_id, f"Group {cluster_id}")
    print(f"  - {name}: {count} members")
print(f"\nModel saved to: {model_path}")
print("="*60)