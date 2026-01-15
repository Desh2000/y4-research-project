"""
Improved Fine-tuning Script with Enhanced Data Augmentation
"""

import sys
import os
import pandas as pd
import numpy as np
from typing import List, Tuple, Dict
import random
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from utils.data_loader import DataLoader
from models.intent_classifier import IntentClassificationEngine


def generate_enhanced_training_samples(csv_path: str) -> List[Tuple[str, str]]:
    """Generate diverse training samples with better templates"""
    
    print("\n[Enhanced Data Augmentation]")
    df = pd.read_csv(csv_path)
    print(f"Processing {len(df)} survey responses")
    
    training_samples = []
    
    # Template-based generation with variation
    work_interfere_templates = {
        'Often': [
            "Work constantly affects my mental health",
            "My job is really taking a toll on my mental wellbeing",
            "Work stress is overwhelming me",
            "I can't handle the work pressure anymore",
            "Work is destroying my mental health"
        ],
        'Sometimes': [
            "Sometimes work gets to me mentally",
            "Work occasionally affects my mood",
            "My job sometimes stresses me out",
            "Work pressure bothers me from time to time"
        ],
        'Rarely': [
            "Work rarely affects my mental state",
            "My job doesn't usually stress me",
            "Work isn't a major source of stress"
        ],
        'Never': [
            "Work doesn't impact my mental health",
            "My job is fine for my mental wellbeing"
        ]
    }
    
    treatment_templates = {
        'Yes': [
            "I'm in therapy right now",
            "I'm seeing a therapist",
            "I'm getting professional help",
            "Currently in treatment for mental health",
            "Working with a mental health professional"
        ],
        'No': [
            "I haven't seen a therapist",
            "Not currently in treatment",
            "Should I seek professional help?",
            "Haven't gotten therapy yet",
            "Need to find a therapist"
        ]
    }
    
    anxiety_templates = [
        "I'm feeling anxious",
        "Having anxiety issues",
        "Anxious all the time",
        "Can't stop worrying",
        "Anxiety is getting worse"
    ]
    
    stress_templates = [
        "I'm stressed out",
        "So much stress lately",
        "Feeling overwhelmed by stress",
        "Stress is getting to me",
        "Can't handle this stress"
    ]
    
    depression_templates = [
        "I'm feeling depressed",
        "Struggling with depression",
        "Everything feels hopeless",
        "Lost interest in everything",
        "Can't shake this sadness"
    ]
    
    sleep_templates = [
        "Can't sleep well",
        "Having trouble sleeping",
        "Insomnia problems",
        "Sleep is terrible",
        "Not getting enough rest"
    ]
    
    # Generate samples
    sample_count = 0
    for idx, row in df.iterrows():
        # Work interference samples
        work_int = row.get('work_interfere', 'Never')
        if work_int in work_interfere_templates:
            template = random.choice(work_interfere_templates[work_int])
            training_samples.append((template, 'work_stress' if work_int in ['Often', 'Sometimes'] else 'stress'))
            sample_count += 1
        
        # Treatment samples
        treatment = row.get('treatment', 'No')
        if treatment in treatment_templates and random.random() < 0.3:  # 30% sampling
            template = random.choice(treatment_templates[treatment])
            training_samples.append((template, 'therapy' if treatment == 'Yes' else 'help'))
            sample_count += 1
        
        # Family history samples
        if row.get('family_history') == 'Yes' and random.random() < 0.1:
            training_samples.append((
                random.choice([
                    "Mental health issues run in my family",
                    "Family history of mental illness",
                    "My family has mental health problems"
                ]),
                'mental_health_info'
            ))
            sample_count += 1
        
        # Remote work samples
        if row.get('remote_work') == 'Yes' and random.random() < 0.2:
            training_samples.append((
                random.choice([
                    "Remote work is affecting my mental health",
                    "Working from home is isolating",
                    "WFH is taking a toll on me",
                    "Struggling with remote work"
                ]),
                'work_stress'
            ))
            sample_count += 1
        
        # Benefits samples
        benefits = row.get('benefits', 'No')
        if random.random() < 0.15:
            if benefits == 'Yes':
                training_samples.append((
                    random.choice([
                        "My company has mental health benefits",
                        "Workplace offers mental health support",
                        "My employer provides wellness programs"
                    ]),
                    'mental_health_info'
                ))
            else:
                training_samples.append((
                    random.choice([
                        "No mental health support at work",
                        "My workplace doesn't care about mental health",
                        "Company offers no mental health benefits"
                    ]),
                    'work_stress'
                ))
            sample_count += 1
        
        # Stigma samples
        if row.get('mental_health_consequence') == 'Yes' and random.random() < 0.2:
            training_samples.append((
                random.choice([
                    "Afraid of consequences at work",
                    "Worried about stigma at my job",
                    "Can't discuss mental health at work",
                    "Fear of being judged at work"
                ]),
                'anxiety'
            ))
            sample_count += 1
        
        # Add general mental health samples periodically
        if idx % 50 == 0:
            training_samples.append((random.choice(anxiety_templates), 'anxiety'))
            training_samples.append((random.choice(stress_templates), 'stress'))
            training_samples.append((random.choice(depression_templates), 'depression'))
            training_samples.append((random.choice(sleep_templates), 'sleep_problems'))
    
    # Remove duplicates
    training_samples = list(set(training_samples))
    print(f"✓ Generated {len(training_samples)} unique augmented samples")
    
    return training_samples


def improved_finetune():
    """Improved fine-tuning with better data and parameters"""
    
    print("="*70)
    print("IMPROVED MODEL FINE-TUNING")
    print("="*70)
    
    # Load existing data
    print("\n[1/5] Loading base datasets...")
    data_loader = DataLoader(data_dir="data/")
    data_loader.load_intents_data()
    patterns, tags, responses = data_loader.prepare_intent_dataset()
    print(f"✓ Base patterns: {len(patterns)}")
    
    # Generate enhanced samples
    print("\n[2/5] Generating enhanced training data...")
    survey_samples = generate_enhanced_training_samples("data/synthetic_mental_health_data_v1.csv")
    
    for text, intent in survey_samples:
        patterns.append(text)
        tags.append(intent)
    
    print(f"✓ Total patterns: {len(patterns)}")
    print(f"✓ Unique intents: {len(set(tags))}")
    
    # Prepare data
    print("\n[3/5] Preparing training data...")
    X_train, X_val, y_train, y_val = data_loader.create_train_val_split(
        patterns, tags, test_size=0.15, random_state=42
    )
    print(f"✓ Training: {len(X_train)}, Validation: {len(X_val)}")
    
    # Load model
    print("\n[4/5] Loading pre-trained model...")
    classifier = IntentClassificationEngine(model_name='bert-base-uncased', max_length=256)
    classifier.load_model("models/trained_intent_classifier")
    print("✓ Model loaded")
    
    # Fine-tune with optimized parameters
    print("\n[5/5] Fine-tuning (3 epochs, lr=1e-5, batch=8)...")
    print("Using lower learning rate to preserve base model performance")
    print("-" * 70)
    
    classifier.train(
        train_texts=X_train,
        train_labels=y_train,
        val_texts=X_val,
        val_labels=y_val,
        epochs=3,  # Fewer epochs
        batch_size=8,
        learning_rate=1e-5  # Much lower learning rate
    )
    
    print("-" * 70)
    
    # Save
    print("\nSaving improved model...")
    classifier.save_model("models/finetuned_intent_classifier_v2")
    print("✓ Saved to: models/finetuned_intent_classifier_v2")
    
    print("\n" + "="*70)
    print("✅ IMPROVED FINE-TUNING COMPLETE!")
    print("="*70)


if __name__ == "__main__":
    improved_finetune()
