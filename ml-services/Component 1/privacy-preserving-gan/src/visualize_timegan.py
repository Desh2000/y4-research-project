"""
MANO Component 1: TimeGAN Visualization Suite
Generates PCA, t-SNE, and Distribution plots to validate synthetic time-series data.
"""
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.manifold import TSNE
from sklearn.decomposition import PCA
import sys
import os
from pathlib import Path

# --- SETUP PATHS ---
sys.path.insert(0, str(Path(__file__).parent))
config_path = os.path.join(os.getcwd(), 'ml-services', 'privacy-preserving-gan', 'config')
sys.path.append(config_path)

try:
    from timegan_config import config
except ImportError:
    sys.exit("❌ Error: Config not found.")

def visualize_timegan():
    print("\n" + "="*60)
    print("GENERATING TIMEGAN VISUALIZATIONS")
    print("="*60)
    
    # 1. Load Data
    real_path = config.data.REAL_DATA_FILE
    synth_path = config.data.SYNTHETIC_DATA_FILE
    
    real_data = np.load(real_path)['sequences']
    synth_data = np.load(synth_path)['sequences']
    
    # Flatten: [N, Seq, Feat] -> [N, Seq*Feat]
    real_flat = real_data.reshape(real_data.shape[0], -1)
    synth_flat = synth_data.reshape(synth_data.shape[0], -1)
    
    # Subsample for speed (TSNE is slow)
    n_sub = min(1000, len(real_flat))
    idx = np.random.permutation(len(real_flat))[:n_sub]
    real_sub = real_flat[idx]
    synth_sub = synth_flat[:n_sub]
    
    save_dir = Path("ml-services/privacy-preserving-gan/gan_logs/plots/timegan")
    save_dir.mkdir(parents=True, exist_ok=True)
    
    # --- PLOT 1: PCA ---
    print("1. Generating PCA Plot...")
    pca = PCA(n_components=2)
    pca.fit(real_sub)
    pca_real = pca.transform(real_sub)
    pca_synth = pca.transform(synth_sub)
    
    plt.figure(figsize=(10, 6))
    plt.scatter(pca_real[:,0], pca_real[:,1], c='blue', alpha=0.2, label='Real')
    plt.scatter(pca_synth[:,0], pca_synth[:,1], c='red', alpha=0.2, label='Synthetic')
    plt.legend()
    plt.title('PCA: Real vs Synthetic Sequences')
    plt.savefig(save_dir / "pca_comparison.png", dpi=300)
    plt.close()
    
    # --- PLOT 2: t-SNE ---
    print("2. Generating t-SNE Plot (This takes a moment)...")
    tsne = TSNE(n_components=2, random_state=42)
    # Combine to fit TSNE space together
    combined = np.concatenate([real_sub, synth_sub], axis=0)
    tsne_results = tsne.fit_transform(combined)
    
    tsne_real = tsne_results[:n_sub]
    tsne_synth = tsne_results[n_sub:]
    
    plt.figure(figsize=(10, 6))
    plt.scatter(tsne_real[:,0], tsne_real[:,1], c='blue', alpha=0.2, label='Real')
    plt.scatter(tsne_synth[:,0], tsne_synth[:,1], c='red', alpha=0.2, label='Synthetic')
    plt.legend()
    plt.title('t-SNE: Real vs Synthetic Sequences')
    plt.savefig(save_dir / "tsne_comparison.png", dpi=300)
    plt.close()
    
    # --- PLOT 3: Signal Distributions ---
    print("3. Generating Signal Histograms...")
    signal_names = ['Sleep Duration', 'Quality of Sleep', 'Heart Rate', 'Stress Level']
    
    fig, axes = plt.subplots(2, 2, figsize=(14, 10))
    axes = axes.flatten()
    
    for i, ax in enumerate(axes):
        # Mean value per sequence
        r_mean = real_data[:, :, i].mean(axis=1)
        s_mean = synth_data[:, :, i].mean(axis=1)
        
        sns.kdeplot(r_mean, ax=ax, color='blue', label='Real', fill=True, alpha=0.3)
        sns.kdeplot(s_mean, ax=ax, color='red', label='Synthetic', fill=True, alpha=0.3)
        ax.set_title(f'{signal_names[i]} Distribution')
        ax.legend()
        
    plt.tight_layout()
    plt.savefig(save_dir / "signal_distributions.png", dpi=300)
    plt.close()
    
    print(f"✅ TimeGAN Visualizations saved to: {save_dir}")

if __name__ == "__main__":
    visualize_timegan()