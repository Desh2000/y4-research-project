"""
MANO Component : LSTM Data Loading Module
Handles Dataset creation, Stratified Splitting, and DataLoader generation.

WHY SEPARATE THIS?
Decoupling data logic from training logic allows us to:
1. Reuse this loader for Inference/API endpoints later.
2. Ensure consistent splitting logic across different experiments.
3. Keep the Main script clean and readable.
"""
import numpy as np
import torch
from torch.utils.data import Dataset, DataLoader
from sklearn.model_selection import train_test_split
from pathlib import Path
import sys
import os

# Ensure we can find the config
sys.path.insert(0, str(Path(__file__).parent))
config_path = os.path.join(os.getcwd(), 'ml-services',
                           'privacy-preserving-lstm', 'config')
sys.path.append(config_path)

try:
    from lstm_config import config
except ImportError:
    # Fallback for direct execution
    sys.path.append(str(Path(__file__).parent.parent / 'config'))
    from lstm_config import config


class HybridDataset(Dataset):
    """
    PyTorch Dataset wrapper for our Multi-Modal Data.
    Stores:
    1. X_dynamic: Time-series wearable data [Batch, 7, 4]
    2. X_static:  Demographic data [Batch, 20]
    3. y:         Risk Labels [Batch]
    """

    def __init__(self, X_dynamic, X_static, y):
        # Convert to FloatTensor for inputs (required for matmul)
        self.X_dynamic = torch.tensor(X_dynamic, dtype=torch.float32)
        self.X_static = torch.tensor(X_static, dtype=torch.float32)
        # Convert to LongTensor for labels (required for CrossEntropy)
        self.y = torch.tensor(y, dtype=torch.long)

        # Sanity Check
        assert len(self.X_dynamic) == len(self.X_static) == len(self.y), \
            "Input arrays must have the same length"

    def __len__(self):
        return len(self.y)

    def __getitem__(self, idx):
        return self.X_dynamic[idx], self.X_static[idx], self.y[idx]


def load_and_split_data():
    """
    Main Data Pipeline function.
    1. Loads NPZ artifact.
    2. Performs Stratified Split (Train/Val/Test).
    3. Computes Class Weights.
    4. Returns DataLoaders.
    """
    print("\n" + "="*70)
    print("DATA LOADING & SPLITTING")
    print("="*70)

    # 1. Load Data
    path = Path(config.data.DATASET_PATH)
    if not path.exists():
        print(f"❌ Error: Dataset not found at {path}")
        print("   Did you run Phase 4 (Data Fusion)?")
        sys.exit(1)

    print(f"📂 Loading: {path}")
    data = np.load(path)
    X_dyn = data['X_dynamic']
    X_stat = data['X_static']
    y = data['y']

    print(f"✅ Loaded {len(y)} samples")

    # Auto-update config with actual static feature count
    # This prevents crashes if LabelEncoder produced unexpected columns
    config.model.STATIC_INPUT_DIM = X_stat.shape[1]

    # 2. Stratified Splitting
    # Step A: Split Train vs (Val + Test)
    # Stratify=y ensures we keep the same % of High Risk cases in all sets
    X_d_train, X_d_temp, X_s_train, X_s_temp, y_train, y_temp = train_test_split(
        X_dyn, X_stat, y,
        test_size=(1 - config.data.TRAIN_RATIO),
        stratify=y,
        random_state=42
    )

    # Step B: Split Val vs Test (from the remaining temp data)
    X_d_val, X_d_test, X_s_val, X_s_test, y_val, y_test = train_test_split(
        X_d_temp, X_s_temp, y_temp,
        test_size=0.5,  # Split remaining 30% equally -> 15% Val / 15% Test
        stratify=y_temp,
        random_state=42
    )

    print(f"✅ Data Splits:")
    print(f"   Train: {len(y_train)} samples ({len(y_train)/len(y):.0%})")
    print(f"   Val:   {len(y_val)} samples ({len(y_val)/len(y):.0%})")
    print(f"   Test:  {len(y_test)} samples ({len(y_test)/len(y):.0%})")

    # 3. Compute Class Weights (Balanced)
    # Formula: n_samples / (n_classes * count_per_class)
    counts = np.bincount(y_train)
    total = len(y_train)
    n_classes = len(counts)
    weights = total / (n_classes * counts)

    print(f"\n⚖️  Class Weights (for Loss Balancing):")
    for i, w in enumerate(weights):
        print(f"   Class {i}: {w:.4f}")

    # 4. Create DataLoaders
    # Pin Memory speeds up CPU->GPU transfer
    train_loader = DataLoader(
        HybridDataset(X_d_train, X_s_train, y_train),
        batch_size=config.data.BATCH_SIZE,
        shuffle=True,  # Shuffle ONLY training data
        pin_memory=config.data.PIN_MEMORY,
        num_workers=config.data.NUM_WORKERS
    )

    val_loader = DataLoader(
        HybridDataset(X_d_val, X_s_val, y_val),
        batch_size=config.data.BATCH_SIZE,
        shuffle=False,
        pin_memory=config.data.PIN_MEMORY,
        num_workers=config.data.NUM_WORKERS
    )

    test_loader = DataLoader(
        HybridDataset(X_d_test, X_s_test, y_test),
        batch_size=config.data.BATCH_SIZE,
        shuffle=False,
        pin_memory=config.data.PIN_MEMORY,
        num_workers=config.data.NUM_WORKERS
    )

    # Return weights as a list (Main script will convert to Tensor)
    return train_loader, val_loader, test_loader, list(weights)


if __name__ == "__main__":
    # Self-test block
    try:
        t, v, te, w = load_and_split_data()
        x_d, x_s, y = next(iter(t))
        print(
            f"\n✅ Test Batch Shape: Dyn={x_d.shape}, Stat={x_s.shape}, y={y.shape}")
    except Exception as e:
        print(f"\n❌ Test Failed: {e}")
