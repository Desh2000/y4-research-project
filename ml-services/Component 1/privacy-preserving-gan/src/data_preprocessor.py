"""
MANO Component 1: Wearable Data Preprocessor
Converts 374 static records into 374×7×4 temporal sequences
Date: December 18, 2025
"""

import pandas as pd
import numpy as np
from pathlib import Path
import sys
import os

# Import configuration
# We assume the script is run from Project Root
config_path = os.path.join(os.getcwd(), 'ml-services', 'privacy-preserving-gan', 'config')
sys.path.append(config_path)

try:
    import config
except ImportError:
    # Fallback if running from src directory directly
    config_path = os.path.join(os.getcwd(), '..', 'config')
    sys.path.append(config_path)
    import config

from normalization_config import NORMALIZATION_CONFIG, SEQUENCE_CONFIG, VALIDATION_THRESHOLDS

class WearableSequenceGenerator:
    """
    Converts static wearable measurements into 7-day temporal sequences.
    
    Example transformation:
    INPUT:  Person_1: Sleep=7.5, HeartRate=75, Quality=7, Stress=5
    OUTPUT: 7-day sequence where each day has slight variation:
            Day1: [0.52, 0.48, 0.60, 0.50]
            Day2: [0.51, 0.50, 0.62, 0.49]
            ...
            Day7: [0.53, 0.47, 0.58, 0.51]
    """
    
    def __init__(self, config=NORMALIZATION_CONFIG, seq_config=SEQUENCE_CONFIG):
        """Initialize with normalization configuration"""
        self.config = config
        self.seq_config = seq_config
        self.signals = list(config.keys())
        self.seq_len = seq_config['sequence_length']
        self.noise_std = seq_config['noise_std']
        
        print(f"\n✅ Initialized WearableSequenceGenerator")
        print(f"   Signals: {self.signals}")
        print(f"   Sequence length: {self.seq_len} days")
        print(f"   Daily noise: ±{self.noise_std*100:.1f}%")
        
    def normalize_signal(self, signal_name: str, value: float) -> float:
        """
        Convert a signal value from original range to [0, 1]
        
        Formula: (value - min) / (max - min)
        Example: Sleep Duration 7.5 hours
        Normalized = (7.5 - 5.8) / (8.5 - 5.8) = 0.63
        """
        cfg = self.config[signal_name]
        min_val = cfg['min']
        max_val = cfg['max']
        
        # Apply MinMax scaling
        normalized = (value - min_val) / (max_val - min_val)
        
        # Clip to [0, 1] in case of edge values
        return np.clip(normalized, 0.0, 1.0)
        
    def denormalize_signal(self, signal_name: str, normalized_value: float) -> float:
        """
        Convert a normalized value [0,1] back to original range
        
        Formula: normalized * (max - min) + min
        Example: Sleep Duration normalized = 0.63
        Original = 0.63 * (8.5 - 5.8) + 5.8 = 7.5 hours
        """
        cfg = self.config[signal_name]
        min_val = cfg['min']
        max_val = cfg['max']
        
        return normalized_value * (max_val - min_val) + min_val
        
    def generate_sequence_for_person(self, person_dict: dict, add_noise: bool = True) -> np.ndarray:
        """
        Generate a 7-day sequence for one person
        
        Args:
            person_dict: Dictionary with signal_name -> value pairs
                        {'Sleep Duration': 7.5, 'Heart Rate': 75, ...}
            add_noise: Whether to add realistic daily variation
        
        Returns:
            np.ndarray of shape (7, 4) - 7 days × 4 signals
        """
        sequence = []
        
        for day in range(self.seq_len):
            daily_measurements = []
            
            for signal in self.signals:
                # Get baseline value for this person
                base_value = person_dict[signal]
                
                # Normalize to 0-1 range
                normalized = self.normalize_signal(signal, base_value)
                
                # Add realistic daily variation
                if add_noise:
                    # Add random noise ~N(0, noise_std)
                    variation = np.random.normal(0, self.noise_std)
                    noisy_value = np.clip(normalized + variation, 0.0, 1.0)
                else:
                    noisy_value = normalized
                
                daily_measurements.append(noisy_value)
            
            sequence.append(daily_measurements)
            
        # Shape: (7, 4) for this person
        return np.array(sequence)
        
    def process_dataset(self, input_csv: str, output_npz: str) -> np.ndarray:
        """
        Main pipeline: Load CSV → Generate sequences → Save NPZ
        
        Args:
            input_csv: Path to Sleep_health_and_lifestyle_dataset.csv
            output_npz: Path to save wearable_sequences.npz
        
        Returns:
            np.ndarray of shape (374, 7, 4)
        """
        print(f"\n" + "="*80)
        print("SEQUENCE GENERATION PIPELINE")
        print("="*80)
        
        # Step 1: Load CSV
        print(f"\n📖 Loading data from: {input_csv}")
        try:
            df = pd.read_csv(input_csv)
            print(f"   ✅ Loaded {len(df)} individuals")
        except FileNotFoundError:
            print(f"   ❌ Error: File not found at {input_csv}")
            return None
        
        # Step 2: Extract signals
        print(f"\n🔍 Extracting {len(self.signals)} signals...")
        
        sequences = []
        
        for idx, row in df.iterrows():
            # Create dictionary with signal values for this person
            person_dict = {}
            for signal in self.signals:
                person_dict[signal] = row[signal]
            
            # Generate 7-day sequence for this person
            seq = self.generate_sequence_for_person(person_dict, add_noise=True)
            sequences.append(seq)
            
            # Progress indicator every 50 people
            if (idx + 1) % 50 == 0:
                print(f"   Processed {idx + 1}/{len(df)} individuals")
        
        # Step 3: Stack all sequences
        sequences_array = np.array(sequences)
        print(f"\n✅ Generated {len(sequences)} sequences")
        print(f"   Shape: {sequences_array.shape}")
        print(f"   - Individuals: {sequences_array.shape[0]}")
        print(f"   - Days per sequence: {sequences_array.shape[1]}")
        print(f"   - Signals per day: {sequences_array.shape[2]}")
        
        # Step 4: Save as compressed NPZ
        print(f"\n💾 Saving to: {output_npz}")
        np.savez_compressed(output_npz, sequences=sequences_array)
        
        if os.path.exists(output_npz):
            file_size_mb = os.path.getsize(output_npz) / (1024 * 1024)
            print(f"   ✅ File size: {file_size_mb:.2f} MB")
            print(f"   ✅ Compression ratio: {(sequences_array.nbytes / (1024*1024)) / file_size_mb:.1f}x")
        
        return sequences_array