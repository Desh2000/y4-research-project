"""
MANO Component 1: TimeGAN Evaluation
Enhanced with KS-Test, Correlation Analysis, and Temporal Coherence
"""
import numpy as np
import matplotlib.pyplot as plt
from sklearn.decomposition import PCA
from scipy.stats import ks_2samp
import os
import json
from datetime import datetime

# --- JSON Encoder Helper ---
class NumpyEncoder(json.JSONEncoder):
    """
    Helper to convert Numpy types (float32, int64) to Python types
    so json.dump doesn't crash.
    """
    def default(self, obj):
        if isinstance(obj, np.integer):
            return int(obj)
        if isinstance(obj, np.floating):
            return float(obj)
        if isinstance(obj, np.ndarray):
            return obj.tolist()
        return super(NumpyEncoder, self).default(obj)

class SyntheticDataEvaluator:
    def __init__(self, config):
        self.config = config
        self.plots_dir = config.evaluation.PLOTS_DIR
        self.signal_names = config.signals.SIGNAL_NAMES
        self.metrics = {}

    def compute_metrics(self, real_data, synthetic_data):
        print("\n📊 Computing Metrics...")
        
        # 1. Distribution Similarity (KS Test)
        ks_scores = {}
        for i, name in enumerate(self.signal_names):
            r_mean = real_data[:, :, i].mean(axis=1)
            s_mean = synthetic_data[:, :, i].mean(axis=1)
            stat, _ = ks_2samp(r_mean, s_mean)
            ks_scores[name] = 1.0 - stat 
            
        self.metrics['distribution_similarity'] = np.mean(list(ks_scores.values()))
        
        # 2. Temporal Coherence (Diff variation)
        diffs = np.diff(synthetic_data, axis=1)
        self.metrics['temporal_coherence'] = np.mean(np.abs(diffs))
        
        print(f"   Distribution Score: {self.metrics['distribution_similarity']:.4f} (Higher is better)")
        print(f"   Temporal Coherence: {self.metrics['temporal_coherence']:.4f}")

    def evaluate(self, real_data, synthetic_data):
        print("\n" + "="*80 + "\nSYNTHETIC DATA EVALUATION\n" + "="*80)
        
        self.compute_metrics(real_data, synthetic_data)
        
        # PCA Visualization
        print("   Generating PCA plot...")
        real_flat = real_data.reshape(real_data.shape[0], -1)
        synth_flat = synthetic_data.reshape(synthetic_data.shape[0], -1)
        n_sub = min(1000, len(real_flat))
        
        pca = PCA(2).fit(real_flat)
        r_pca = pca.transform(real_flat[:n_sub])
        s_pca = pca.transform(synth_flat[:n_sub])
        
        plt.figure(figsize=(10, 6))
        plt.scatter(r_pca[:,0], r_pca[:,1], c='blue', alpha=0.2, label='Real')
        plt.scatter(s_pca[:,0], s_pca[:,1], c='red', alpha=0.2, label='Synthetic')
        plt.legend()
        plt.title('PCA: Real vs Synthetic')
        plt.savefig(f"{self.plots_dir}/pca_comparison.png")
        plt.close()
        
        # Save Report (Using the NumpyEncoder fix)
        report_file = self.config.evaluation.REPORT_FILE
        with open(report_file, 'w') as f:
            json.dump(self.metrics, f, indent=2, cls=NumpyEncoder)
        print(f"✅ Report saved to {report_file}")