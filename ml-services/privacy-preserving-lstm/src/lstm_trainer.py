"""
MANO Component 2: LSTM Training Logic
Includes Custom Dataset Loader, Training Loop, Early Stopping, and LR Scheduling.

WHAT THIS DOES:
1. HybridDataset: Wraps our 3-part data (Dynamic, Static, Labels) into PyTorch format.
2. LSTMTrainer: Manages the training loop, validation, optimization, and checkpointing.
"""
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset
import numpy as np
import copy
import time
from pathlib import Path


class HybridDataset(Dataset):
    """
    Custom Data Loader that handles the 3-part data structure:
    1. X_dyn: Dynamic Time Series [Batch, 7, 4]
    2. X_stat: Static Demographics [Batch, 20]
    3. y: Risk Labels [Batch] (0, 1, 2)
    """

    def __init__(self, X_dyn, X_stat, y):
        # Convert numpy arrays to PyTorch tensors
        self.X_dyn = torch.tensor(X_dyn, dtype=torch.float32)
        self.X_stat = torch.tensor(X_stat, dtype=torch.float32)
        # Labels must be Long (Integers) for CrossEntropyLoss
        self.y = torch.tensor(y, dtype=torch.long)

    def __len__(self):
        return len(self.y)

    def __getitem__(self, idx):
        return self.X_dyn[idx], self.X_stat[idx], self.y[idx]


class LSTMTrainer:
    def __init__(self, model, config, class_counts=None):
        """
        Initialize the Trainer.

        Args:
            model: The initialized PyTorch model
            config: Configuration object
            class_counts (list): Count of samples per class [Low, Med, High] for balancing
        """
        self.model = model
        self.config = config
        self.device = config.training.DEVICE
        self.model.to(self.device)

        # --- Optimizer ---
        # AdamW is generally superior to Adam for generalization (better weight decay handling)
        self.optimizer = optim.AdamW(
            model.parameters(),
            lr=config.training.LEARNING_RATE,
            weight_decay=config.training.WEIGHT_DECAY
        )

        # --- Learning Rate Scheduler (Premium Feature) ---
        # Reduces LR if validation loss stops improving. Squeezes out extra accuracy.
        self.scheduler = optim.lr_scheduler.ReduceLROnPlateau(
            self.optimizer, mode='min', factor=0.5, patience=5, verbose=True
        )

        # --- Loss Function with Class Balancing ---
        # Critical for Medical AI where "High Risk" cases might be rarer but more important.
        class_weights = None
        if config.training.USE_CLASS_WEIGHTS and class_counts is not None:
            # Calculate inverse frequency weights
            total = sum(class_counts)
            weights = [total / (len(class_counts) * c) for c in class_counts]
            class_weights = torch.tensor(
                weights, dtype=torch.float32).to(self.device)
            print(f"⚖️  Applied Class Weights: {weights}")

        # CrossEntropyLoss expects raw logits (no Softmax) and Integer targets (0,1,2)
        self.criterion = nn.CrossEntropyLoss(weight=class_weights)

        # History Tracking
        self.history = {
            'train_loss': [], 'train_acc': [],
            'val_loss': [], 'val_acc': []
        }

    def train_epoch(self, loader):
        """Train for one pass over the data"""
        self.model.train()  # Enable Dropout and BatchNorm updates
        total_loss = 0
        correct = 0
        total = 0

        for x_dyn, x_stat, y in loader:
            # Move batch to GPU/CPU
            x_dyn, x_stat, y = x_dyn.to(self.device), x_stat.to(
                self.device), y.to(self.device)

            self.optimizer.zero_grad()  # Clear old gradients

            # Forward Pass
            outputs = self.model(x_dyn, x_stat)
            loss = self.criterion(outputs, y)

            # Backward Pass
            loss.backward()

            # --- Gradient Clipping (Industrial Standard) ---
            # LSTMs can suffer from "exploding gradients". This caps them at 1.0.
            torch.nn.utils.clip_grad_norm_(
                self.model.parameters(), max_norm=1.0)

            self.optimizer.step()

            # Metrics
            total_loss += loss.item()
            # Get class with highest score
            _, predicted = torch.max(outputs.data, 1)
            total += y.size(0)
            correct += (predicted == y).sum().item()

        return total_loss / len(loader), 100 * correct / total

    def evaluate(self, loader):
        """Check performance on validation data (No weight updates)"""
        self.model.eval()  # Disable Dropout/BatchNorm
        total_loss = 0
        correct = 0
        total = 0

        with torch.no_grad():  # Save memory, don't calculate gradients
            for x_dyn, x_stat, y in loader:
                x_dyn, x_stat, y = x_dyn.to(self.device), x_stat.to(
                    self.device), y.to(self.device)

                outputs = self.model(x_dyn, x_stat)
                loss = self.criterion(outputs, y)

                total_loss += loss.item()
                _, predicted = torch.max(outputs.data, 1)
                total += y.size(0)
                correct += (predicted == y).sum().item()

        return total_loss / len(loader), 100 * correct / total

    def fit(self, train_loader, val_loader):
        """Full training loop with Early Stopping"""
        print(f"\n🚀 Starting Training on {self.device}...")
        start_time = time.time()

        best_val_loss = float('inf')
        patience_counter = 0
        best_model_wts = copy.deepcopy(self.model.state_dict())

        for epoch in range(self.config.training.EPOCHS):
            # 1. Train & Validate
            train_loss, train_acc = self.train_epoch(train_loader)
            val_loss, val_acc = self.evaluate(val_loader)

            # 2. Update Scheduler
            # If loss plateaus, this will lower the learning rate automatically
            self.scheduler.step(val_loss)

            # 3. Store History
            self.history['train_loss'].append(train_loss)
            self.history['train_acc'].append(train_acc)
            self.history['val_loss'].append(val_loss)
            self.history['val_acc'].append(val_acc)

            # 4. Print Progress
            print(f"Epoch {epoch+1}/{self.config.training.EPOCHS} | "
                  f"Train Loss: {train_loss:.4f} Acc: {train_acc:.2f}% | "
                  f"Val Loss: {val_loss:.4f} Acc: {val_acc:.2f}%")

            # 5. Early Stopping Logic
            if val_loss < best_val_loss:
                best_val_loss = val_loss
                best_model_wts = copy.deepcopy(self.model.state_dict())
                patience_counter = 0
                # Checkpointing
                torch.save(self.model.state_dict(),
                           self.config.data.BEST_MODEL_PATH)
            else:
                patience_counter += 1
                if patience_counter >= self.config.training.PATIENCE:
                    print(f"\n⏹️  Early Stopping triggered at Epoch {epoch+1}")
                    break

        elapsed = time.time() - start_time
        print(
            f"\n✅ Training Complete in {elapsed:.1f}s. Best Val Loss: {best_val_loss:.4f}")

        # Load best weights so the model is ready for testing
        self.model.load_state_dict(best_model_wts)
        return self.model
