"""
MANO Component 1: TimeGAN Main Execution Pipeline

This is the entry point. It parses your commands and runs the specific 
part of the pipeline you requested.
"""
import sys
import os
import argparse
import numpy as np
from pathlib import Path

# Add the source directory to Python path so we can import our modules
sys.path.insert(0, str(Path(__file__).parent))
config_path = os.path.join(os.getcwd(), 'ml-services', 'privacy-preserving-gan', 'config')
sys.path.append(config_path)

from timegan_config import config
from timegan_model import TimeGAN
from timegan_trainer import TimeGANTrainer
from timegan_generator import SyntheticDataGenerator
from timegan_evaluator import SyntheticDataEvaluator

def main():
    # Setup Argument Parser
    parser = argparse.ArgumentParser()
    parser.add_argument('--mode', type=str, default='full', 
                        choices=['train', 'generate', 'evaluate', 'full'],
                        help="Choose execution mode.")
    args = parser.parse_args()

    print(f"\n{'█'*80}\n█ MANO PHASE 3: TimeGAN PIPELINE (PyTorch)\n{'█'*80}")
    
    # --- 1. LOAD DATA ---
    if not Path(config.data.REAL_DATA_FILE).exists():
        print(f"❌ Error: Real data not found at {config.data.REAL_DATA_FILE}")
        print("Did you run Phase 2 (main_data_prep.py)?")
        return
    
    data_npz = np.load(config.data.REAL_DATA_FILE)
    real_data = data_npz['sequences']
    print(f"✅ Loaded Real Data: {real_data.shape}")
    
    # --- 2. INITIALIZE MODEL ---
    # Create the Brain (Architecture)
    model = TimeGAN(config)
    
    # --- 3. TRAINING MODE ---
    if args.mode in ['train', 'full']:
        trainer = TimeGANTrainer(model, config)
        # Starts the 3-phase training loop
        trainer.train(real_data)
        
    # --- 4. GENERATION MODE ---
    if args.mode in ['generate', 'full']:
        generator = SyntheticDataGenerator(model, config)
        # Creates 10,000 synthetic users
        synthetic_data = generator.generate()
        
    # --- 5. EVALUATION MODE ---
    if args.mode in ['evaluate', 'full']:
        # Reload synthetic data if we skipped generation step
        if 'synthetic_data' not in locals():
            if not Path(config.data.SYNTHETIC_DATA_FILE).exists():
                print("❌ No synthetic data found to evaluate.")
                return
            synthetic_data = np.load(config.data.SYNTHETIC_DATA_FILE)['sequences']
            
        evaluator = SyntheticDataEvaluator(config)
        # Compare Real vs Synthetic
        evaluator.evaluate(real_data, synthetic_data)
    
    print("\n" + "█"*80 + "\n█ PHASE 3 COMPLETE\n" + "█"*80)

if __name__ == "__main__":
    main()