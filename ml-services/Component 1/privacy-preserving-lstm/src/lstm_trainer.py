"""
MANO Component 2: LSTM Training Logic
Includes Training Loop, Early Stopping, and LR Scheduling.

Note: Data loading logic is imported from lstm_data_loader.py, 
so we do not redefine the Dataset class here.
"""
import torch
import torch.nn as nn
import torch.optim as optim
import numpy as np
import copy
import time
from pathlib import Path

class LSTMTrainer:
    """
    The 'Teacher' class that manages the model's learning process.
    It handles Optimization, Regularization, and Validation logic.
    """
    def __init__(self, model, config, class_counts=None):
        """
        Initialize the Trainer.
        
        Args:
            model: The initialized PyTorch model (RiskPredictionModel)
            config: Configuration object (from lstm_config.py)
            class_counts (list): Count of samples per class [Low, Med, High] for balancing
        """
        self.model = model
        self.config = config
        self.device = config.training.DEVICE
        self.model.to(self.device)
        
        # --- 1. OPTIMIZER: AdamW ---
        # WHAT: A variant of Adam (Adaptive Moment Estimation).
        # WHY: AdamW fixes a bug in how Adam handles L2 Regularization (Weight Decay).
        # It is currently the industry standard for training Transformers and LSTMs
        # because it generalizes better to unseen data.
        self.optimizer = optim.AdamW(
            model.parameters(), 
            lr=config.training.LEARNING_RATE,
            weight_decay=config.training.WEIGHT_DECAY
        )
        
        # --- 2. SCHEDULER: ReduceLROnPlateau ---
        # WHAT: Automatically lowers the Learning Rate when the model gets stuck.
        # WHY: Like parking a car—you drive fast at first, but slow down when you get close
        # to the spot. This squeezes out the last few % of accuracy.
        self.scheduler = optim.lr_scheduler.ReduceLROnPlateau(
            self.optimizer, mode='min', factor=0.5, patience=5
        )
        
        # --- 3. LOSS FUNCTION: CrossEntropy with Class Weights ---
        # WHAT: Calculates how "wrong" the model's guess is.
        # WHY: We use Class Weights because our data is imbalanced (Few Medium/High risk).
        # Without weights, the model might just guess "Low Risk" for everyone to get 55% accuracy.
        # Weights force it to pay attention to the rare, dangerous cases.
        class_weights = None
        if config.training.USE_CLASS_WEIGHTS and class_counts is not None:
            # Calculate inverse frequency weights
            total = sum(class_counts)
            weights = [total / (len(class_counts) * c) for c in class_counts]
            class_weights = torch.tensor(weights, dtype=torch.float32).to(self.device)
            print(f"⚖️  Applied Class Weights: {weights}")
            
        # CrossEntropyLoss expects raw logits (no Softmax) and Integer targets (0,1,2)
        self.criterion = nn.CrossEntropyLoss(weight=class_weights)
        
        # History Tracking for graphs later
        self.history = {
            'train_loss': [], 'train_acc': [],
            'val_loss': [], 'val_acc': []
        }

    def train_epoch(self, loader):
        """Train for one pass over the data"""
        self.model.train() # Enable Dropout and BatchNorm updates
        total_loss = 0
        correct = 0
        total = 0
        
        for x_dyn, x_stat, y in loader:
            # Move batch to GPU/CPU
            x_dyn, x_stat, y = x_dyn.to(self.device), x_stat.to(self.device), y.to(self.device)
            
            self.optimizer.zero_grad() # Clear old gradients
            
            # Forward Pass
            outputs = self.model(x_dyn, x_stat)
            loss = self.criterion(outputs, y)
            
            # Backward Pass
            loss.backward()
            
            # --- 4. GRADIENT CLIPPING ---
            # WHAT: Caps the gradient vector norm at 1.0.
            # WHY: LSTMs suffer from "Exploding Gradients" where weights update too much,
            # causing the Loss to become NaN. Clipping stabilizes training.
            torch.nn.utils.clip_grad_norm_(self.model.parameters(), max_norm=1.0)
            
            self.optimizer.step()
            
            # Metrics
            total_loss += loss.item()
            _, predicted = torch.max(outputs.data, 1) # Get class with highest score
            total += y.size(0)
            correct += (predicted == y).sum().item()
            
        return total_loss / len(loader), 100 * correct / total

    def evaluate(self, loader):
        """Check performance on validation data (No weight updates)"""
        self.model.eval() # Disable Dropout/BatchNorm
        total_loss = 0
        correct = 0
        total = 0
        
        with torch.no_grad(): # Save memory, don't calculate gradients
            for x_dyn, x_stat, y in loader:
                x_dyn, x_stat, y = x_dyn.to(self.device), x_stat.to(self.device), y.to(self.device)
                
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
            last_lr = self.optimizer.param_groups[0]['lr']
            print(f"Epoch {epoch+1}/{self.config.training.EPOCHS} | LR: {last_lr:.6f} | "
                  f"Train Loss: {train_loss:.4f} Acc: {train_acc:.2f}% | "
                  f"Val Loss: {val_loss:.4f} Acc: {val_acc:.2f}%")
            
            # 5. Early Stopping Logic
            # WHAT: Stop if the model gets worse on validation data.
            # WHY: Prevents "Overfitting" (Memorizing the training data instead of learning patterns).
            if val_loss < best_val_loss:
                best_val_loss = val_loss
                best_model_wts = copy.deepcopy(self.model.state_dict())
                patience_counter = 0 
                # Checkpointing
                torch.save(self.model.state_dict(), self.config.data.BEST_MODEL_PATH)
            else:
                patience_counter += 1
                if patience_counter >= self.config.training.PATIENCE:
                    print(f"\n⏹️  Early Stopping triggered at Epoch {epoch+1}")
                    break
        
        elapsed = time.time() - start_time
        print(f"\n✅ Training Complete in {elapsed:.1f}s. Best Val Loss: {best_val_loss:.4f}")
        
        # Load best weights before returning
        self.model.load_state_dict(best_model_wts)
        return self.model