"""
MANO Component 3: Seq2Seq Simulator Trainer
Phase 5.1: Trains the Neural Network "World Model".

FEATURES:
- Uses the advanced Attention-based LSTM model.
- Mixed Precision (AMP) for 2x speedup on NVIDIA GPUs.
- Gradient Clipping to prevent LSTM instability.
- Auto-Checkpointing of best models.
"""
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader, random_split
from torch.cuda.amp import autocast, GradScaler
import numpy as np
import time
import sys
import os
import logging
from pathlib import Path

# --- SETUP PATHS ---
sys.path.insert(0, str(Path(__file__).parent))
config_path = os.path.join(os.getcwd(), 'ml-services', 'intervention-simulation', 'config')
sys.path.append(config_path)

try:
    from intervention_config import config
    from seq2seq_model import InterventionSimulator
except ImportError:
    print("❌ Import Error: Check paths.")
    sys.exit(1)

# Setup Logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')
logger = logging.getLogger(__name__)

class SimulationDataset(Dataset):
    def __init__(self, path):
        if not os.path.exists(path):
            raise FileNotFoundError(f"Missing data: {path}")
        data = np.load(path)
        self.sources = torch.FloatTensor(data['sources'])
        self.conditions = torch.FloatTensor(data['conditions'])
        self.targets = torch.FloatTensor(data['targets'])
        
    def __len__(self): return len(self.sources)
    def __getitem__(self, idx): return self.sources[idx], self.conditions[idx], self.targets[idx]

class SimulatorTrainer:
    def __init__(self):
        self.config = config
        self.device = config.training.DEVICE
        
        # Initialize Model
        self.model = InterventionSimulator(config).to(self.device)
        
        # Optimizer (AdamW is best for LSTMs)
        self.optimizer = optim.AdamW(
            self.model.parameters(), 
            lr=config.training.LEARNING_RATE,
            weight_decay=1e-5
        )
        self.criterion = nn.MSELoss() 
        
        # Scheduler (Squeeze out accuracy)
        self.scheduler = optim.lr_scheduler.ReduceLROnPlateau(
            self.optimizer, mode='min', factor=0.5, patience=3
        )
        
        # Mixed Precision Scaler
        # Fix: Disable GradScaler if no CUDA to avoid warnings
        use_scaler = (self.device == 'cuda')
        self.scaler = GradScaler(enabled=use_scaler)
        
        logger.info(f"✅ Trainer Initialized on {self.device}")
        logger.info(f"   Model Parameters: {sum(p.numel() for p in self.model.parameters()):,}")

        # Check for Data Availability immediately
        if not os.path.exists(config.data.SIMULATION_DATA_PATH):
            logger.error(f"❌ MISSING DATA: {config.data.SIMULATION_DATA_PATH}")
            logger.error("   Please run 'intervention_data_prep.py' first to generate training data.")
            logger.error("   Command: python ml-services/intervention-simulation/src/intervention_data_prep.py")
            sys.exit(1)

    def get_loaders(self):
        dataset = SimulationDataset(self.config.data.SIMULATION_DATA_PATH)
        
        # 90/10 Split (Need lots of training data for physics learning)
        train_len = int(0.9 * len(dataset))
        val_len = len(dataset) - train_len
        train_set, val_set = random_split(dataset, [train_len, val_len])
        
        train_loader = DataLoader(train_set, batch_size=config.training.BATCH_SIZE, shuffle=True, pin_memory=True)
        val_loader = DataLoader(val_set, batch_size=config.training.BATCH_SIZE, shuffle=False, pin_memory=True)
        
        return train_loader, val_loader

    def train_epoch(self, loader):
        self.model.train()
        total_loss = 0
        
        for src, cond, tgt in loader:
            src, cond, tgt = src.to(self.device), cond.to(self.device), tgt.to(self.device)
            
            self.optimizer.zero_grad()
            
            # Mixed Precision Forward Pass
            with autocast():
                preds = self.model(src, cond, target=tgt, teacher_forcing_ratio=0.5)
                loss = self.criterion(preds, tgt)
            
            # Scaled Backward Pass
            self.scaler.scale(loss).backward()
            
            # Gradient Clipping (Unscale first)
            self.scaler.unscale_(self.optimizer)
            torch.nn.utils.clip_grad_norm_(self.model.parameters(), 1.0)
            
            self.scaler.step(self.optimizer)
            self.scaler.update()
            
            total_loss += loss.item()
            
        return total_loss / len(loader)

    def validate(self, loader):
        self.model.eval()
        total_loss = 0
        with torch.no_grad():
            for src, cond, tgt in loader:
                src, cond, tgt = src.to(self.device), cond.to(self.device), tgt.to(self.device)
                # No Teacher Forcing in Validation
                preds = self.model(src, cond, target=None, teacher_forcing_ratio=0.0)
                loss = self.criterion(preds, tgt)
                total_loss += loss.item()
        return total_loss / len(loader)

    def run(self):
        train_loader, val_loader = self.get_loaders()
        best_loss = float('inf')
        patience = 0
        
        logger.info(f"\n{'='*40}\nSTARTING TRAINING ({self.config.training.EPOCHS} Epochs)\n{'='*40}")
        
        for epoch in range(1, self.config.training.EPOCHS + 1):
            t0 = time.time()
            train_loss = self.train_epoch(train_loader)
            val_loss = self.validate(val_loader)
            dt = time.time() - t0
            
            self.scheduler.step(val_loss)
            
            print(f"Epoch {epoch:03d} | Train: {train_loss:.6f} | Val: {val_loss:.6f} | Time: {dt:.1f}s")
            
            # Checkpointing
            if val_loss < best_loss:
                best_loss = val_loss
                patience = 0
                torch.save(self.model.state_dict(), self.config.data.SIMULATOR_PATH)
                print(f"   💾 Saved Best Model (Loss: {best_loss:.6f})")
            else:
                patience += 1
                if patience >= self.config.training.PATIENCE:
                    logger.info(f"\n🛑 Early stopping at Epoch {epoch}")
                    break
        
        logger.info(f"\n✅ TRAINING COMPLETE. Best Loss: {best_loss:.6f}")

if __name__ == "__main__":
    trainer = SimulatorTrainer()
    trainer.run()