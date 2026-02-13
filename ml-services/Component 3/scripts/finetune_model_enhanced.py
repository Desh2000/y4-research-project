"""
Enhanced Fine-tuning Script
Improved parameters and data augmentation for better accuracy
"""

import sys
import os
import pandas as pd
import numpy as np
import random
from typing import List, Tuple
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from utils.data_loader import DataLoader
from models.intent_classifier import IntentClassificationEngine


def generate_comprehensive_training_samples(csv_path: str) -> List[Tuple[str, str]]:
    """
    Generate comprehensive training samples with high-quality augmentation
    """
    print("\n[Enhanced Data Augmentation]")
    df = pd.read_csv(csv_path)
    print(f"Processing {len(df)} survey responses")
    
    training_samples = []
    
    # Comprehensive template library
    templates = {
        'work_stress': {
            'Often': [
                "Work is constantly interfering with my mental health",
                "My job is severely affecting my mental wellbeing",
                "I can't cope with work stress anymore",
                "Work pressure is destroying my mental health",
                "The demands at work are overwhelming my mental state",
                "I'm drowning in work-related stress",
                "Work is taking a serious toll on my mental health"
            ],
            'Sometimes': [
                "Work sometimes affects my mental health",
                "My job occasionally stresses me out mentally",
                "Work pressure gets to me from time to time",
                "Sometimes the job impacts my wellbeing"
            ]
        },
        'stress': {
            'general': [
                "I'm feeling really stressed lately",
                "The stress is getting too much to handle",
                "I'm overwhelmed with stress right now",
                "Everything feels so stressful",
                "I can't deal with all this stress"
            ]
        },
        'anxiety': {
            'work': [
                "I'm anxious about work constantly",
                "Work gives me so much anxiety",
                "I feel anxious every time I think about my job",
                "My workplace triggers my anxiety"
            ],
            'general': [
                "I've been feeling very anxious",
                "My anxiety is out of control",
                "I'm dealing with constant anxiety",
                "Anxiety is affecting my daily life"
            ]
        },
        'depression': {
            'general': [
                "I'm feeling depressed all the time",
                "Depression is taking over my life",
                "I can't shake this feeling of depression",
                "Everything feels hopeless and I'm depressed"
            ]
        },
        'therapy': {
            'seeking': [
                "I'm currently in therapy",
                "I'm seeing a therapist for help",
                "I've started therapy sessions",
                "I'm getting professional mental health treatment",
                "I'm working with a counselor"
            ],
            'considering': [
                "Should I see a therapist?",
                "I'm thinking about getting therapy",
                "Do I need professional help?",
                "I haven't tried therapy yet but I'm considering it",
                "Maybe I should talk to a mental health professional"
            ]
        },
        'help': {
            'general': [
                "I need help with my mental health",
                "Can someone help me?",
                "I don't know where to get help",
                "I need support right now",
                "Please help me cope with this"
            ]
        },
        'burnout': {
            'work': [
                "I'm completely burned out from work",
                "Work burnout is real and I'm experiencing it",
                "I'm exhausted and burned out professionally",
                "The burnout from my job is severe"
            ]
        }
    }
    
    # Process survey data with sampling strategy
    for idx, row in df.iterrows():
        # Work interference patterns (high priority)
        work_int = row.get('work_interfere', 'Never')
        if work_int in ['Often', 'Sometimes']:
            if random.random() < 0.8:  # 80% sampling for work stress
                category = 'work_stress'
                template_key = work_int
                if category in templates and template_key in templates[category]:
                    text = random.choice(templates[category][template_key])
                    training_samples.append((text, 'work_stress'))
        
        # Treatment/therapy patterns
        treatment = row.get('treatment', 'No')
        if random.random() < 0.3:
            if treatment == 'Yes':
                text = random.choice(templates['therapy']['seeking'])
                training_samples.append((text, 'therapy'))
            else:
                text = random.choice(templates['therapy']['considering'])
                training_samples.append((text, 'help'))
        
        # Remote work isolation
        if row.get('remote_work') == 'Yes' and random.random() < 0.15:
            training_samples.append((
                random.choice([
                    "Remote work is isolating me",
                    "Working from home affects my mental health",
                    "I feel lonely working remotely",
                    "WFH is impacting my wellbeing"
                ]),
                'loneliness'
            ))
        
        # Workplace support/stigma
        if row.get('mental_health_consequence') == 'Yes' and random.random() < 0.2:
            training_samples.append((
                random.choice([
                    "I'm afraid to discuss mental health at work",
                    "There's stigma around mental health at my workplace",
                    "I worry about consequences of seeking help",
                    "My employer doesn't support mental health"
                ]),
                'anxiety'
            ))
        
        # Family history awareness
        if row.get('family_history') == 'Yes' and random.random() < 0.1:
            training_samples.append((
                "Mental health issues run in my family",
                'family_problems'
            ))
        
        # Benefits and support
        if row.get('benefits') == 'No' and random.random() < 0.15:
            training_samples.append((
                random.choice([
                    "My company doesn't offer mental health benefits",
                    "There's no mental health support at work",
                    "I have no resources for mental health at my job"
                ]),
                'work_stress'
            ))
        
        # Add general mental health samples every N rows
        if idx % 20 == 0:
            training_samples.append((random.choice(templates['stress']['general']), 'stress'))
        if idx % 25 == 0:
            training_samples.append((random.choice(templates['anxiety']['general']), 'anxiety'))
        if idx % 30 == 0:
            training_samples.append((random.choice(templates['depression']['general']), 'depression'))
        if idx % 40 == 0:
            training_samples.append((
                random.choice([
                    "I'm completely burned out",
                    "Burnout is affecting everything",
                    "I need help with burnout"
                ]),
                'burnout'
            ))
    
    # Remove duplicates
    training_samples = list(set(training_samples))
    print(f"✓ Generated {len(training_samples)} unique high-quality training samples")
    
    return training_samples


def enhanced_finetune():
    """Enhanced fine-tuning with optimized parameters"""
    
    print("="*80)
    print(" ENHANCED FINE-TUNING FOR IMPROVED ACCURACY")
    print("="*80)
    
    # Load base data
    print("\n[1/5] Loading base datasets...")
    data_loader = DataLoader(data_dir="data/")
    data_loader.load_intents_data()
    patterns, tags, responses = data_loader.prepare_intent_dataset()
    print(f"✓ Base patterns: {len(patterns)}")
    
    # Generate enhanced samples
    print("\n[2/5] Generating enhanced training data...")
    survey_samples = generate_comprehensive_training_samples("data/synthetic_mental_health_data_v1.csv")
    
    for text, intent in survey_samples:
        patterns.append(text)
        tags.append(intent)
    
    print(f"✓ Total patterns: {len(patterns)}")
    print(f"✓ Unique intents: {len(set(tags))}")
    
    # Intent distribution
    from collections import Counter
    intent_counts = Counter(tags)
    print(f"\n📊 Top 10 Intent Distribution:")
    for intent, count in intent_counts.most_common(10):
        print(f"   {intent:20s}: {count:3d} samples")
    
    # Prepare data with better split
    print("\n[3/5] Preparing training data...")
    X_train, X_val, y_train, y_val = data_loader.create_train_val_split(
        patterns, tags, test_size=0.2, random_state=42
    )
    print(f"✓ Training: {len(X_train)}, Validation: {len(X_val)}")
    
    # Load model
    print("\n[4/5] Loading pre-trained model...")
    classifier = IntentClassificationEngine(model_name='bert-base-uncased', max_length=256)
    classifier.load_model("models/trained_intent_classifier")
    print("✓ Model loaded")
    
    # Fine-tune with OPTIMIZED parameters
    print("\n[5/5] Fine-tuning with optimized parameters...")
    print("Configuration:")
    print("  - Epochs: 10 (increased for better convergence)")
    print("  - Learning Rate: 2e-5 (balanced for fine-tuning)")
    print("  - Batch Size: 8")
    print("  - Scheduler: ReduceLROnPlateau")
    print("  - Early stopping: Best model saved")
    print("\nThis will take approximately 25-30 minutes...")
    print("-" * 80)
    
    classifier.train(
        train_texts=X_train,
        train_labels=y_train,
        val_texts=X_val,
        val_labels=y_val,
        epochs=10,  # More epochs for convergence
        batch_size=8,
        learning_rate=2e-5  # Optimal for fine-tuning
    )
    
    print("-" * 80)
    
    # Save
    print("\nSaving enhanced model...")
    classifier.save_model("models/finetuned_intent_classifier_v3")
    print("✓ Saved to: models/finetuned_intent_classifier_v3")
    
    print("\n" + "="*80)
    print("✅ ENHANCED FINE-TUNING COMPLETE!")
    print("="*80)
    print("\nNext steps:")
    print("1. Run: python evaluate_model.py to check accuracy")
    print("2. Compare with original model")
    print("3. Update backend if accuracy is improved")


if __name__ == "__main__":
    enhanced_finetune()
