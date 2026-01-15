"""
Fine-tuning Script for Empathetic Conversational Support System
Fine-tunes the existing model with synthetic mental health survey data
"""

import sys
import os
import pandas as pd
import numpy as np
from typing import List, Tuple
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from utils.data_loader import DataLoader
from models.intent_classifier import IntentClassificationEngine


def generate_training_samples_from_survey(csv_path: str) -> List[Tuple[str, str]]:
    """
    Generate training samples from the mental health survey data.
    Creates realistic user inputs based on survey responses.
    """
    print("\n[Processing Survey Data]")
    df = pd.read_csv(csv_path)
    print(f"Loaded {len(df)} survey responses")
    
    training_samples = []
    
    for idx, row in df.iterrows():
        # Generate samples based on work interference
        if row.get('work_interfere') == 'Often':
            training_samples.append((
                "Work is constantly interfering with my mental health",
                "work_stress"
            ))
        elif row.get('work_interfere') == 'Sometimes':
            training_samples.append((
                "Sometimes work affects my mental wellbeing",
                "stress"
            ))
        
        # Generate samples based on treatment status
        if row.get('treatment') == 'Yes':
            training_samples.append((
                "I'm currently seeking treatment for mental health",
                "therapy"
            ))
        elif row.get('treatment') == 'No':
            training_samples.append((
                "I haven't sought professional help yet",
                "help"
            ))
        
        # Generate samples based on family history
        if row.get('family_history') == 'Yes':
            training_samples.append((
                "Mental health issues run in my family",
                "mental_health_info"
            ))
        
        # Generate samples based on remote work
        if row.get('remote_work') == 'Yes':
            training_samples.append((
                "Working remotely is affecting my mental health",
                "work_stress"
            ))
        
        # Generate samples about workplace support
        if row.get('benefits') == 'Yes':
            training_samples.append((
                "My workplace offers mental health benefits",
                "mental_health_info"
            ))
        elif row.get('benefits') == 'No':
            training_samples.append((
                "My workplace doesn't provide mental health support",
                "work_stress"
            ))
        
        # Generate samples about seeking help
        if row.get('seek_help') == 'Yes':
            training_samples.append((
                "I'm comfortable seeking help from my employer",
                "help"
            ))
        elif row.get('seek_help') == 'No':
            training_samples.append((
                "I'm afraid to seek help at work",
                "anxiety"
            ))
        
        # Generate samples about mental health consequences
        if row.get('mental_health_consequence') == 'Yes':
            training_samples.append((
                "I'm worried about consequences of discussing mental health at work",
                "anxiety"
            ))
        
        # Generate samples about coworker support
        if row.get('coworkers') == 'Yes':
            training_samples.append((
                "I would discuss mental health with coworkers",
                "mental_health_info"
            ))
        elif row.get('coworkers') == 'No':
            training_samples.append((
                "I can't talk to coworkers about mental health",
                "loneliness"
            ))
        
        # Limit to avoid too many duplicates - use every 10th row
        if idx % 10 == 0:
            # Add age-specific samples
            age = row.get('Age', 0)
            if age < 25:
                training_samples.append((
                    "I'm young and struggling with career stress",
                    "stress"
                ))
            elif age > 50:
                training_samples.append((
                    "I'm older and dealing with workplace challenges",
                    "work_stress"
                ))
    
    # Remove duplicates
    training_samples = list(set(training_samples))
    print(f"Generated {len(training_samples)} unique training samples from survey data")
    
    return training_samples


def finetune_intent_classifier():
    """Fine-tune the existing intent classification model"""
    
    print("="*70)
    print("EMPATHETIC CONVERSATIONAL SUPPORT SYSTEM - MODEL FINE-TUNING")
    print("="*70)
    
    # Step 1: Load existing training data
    print("\n[1/6] Loading existing datasets...")
    data_loader = DataLoader(data_dir="data/")
    
    try:
        data_loader.load_intents_data()
        patterns, tags, responses = data_loader.prepare_intent_dataset()
        print(f"✓ Loaded {len(patterns)} existing patterns")
        print(f"✓ Found {len(set(tags))} existing intent classes")
    except Exception as e:
        print(f"❌ Error loading existing data: {e}")
        return
    
    # Step 2: Load and process survey data
    print("\n[2/6] Processing synthetic mental health survey data...")
    survey_path = "data/synthetic_mental_health_data_v1.csv"
    
    try:
        survey_samples = generate_training_samples_from_survey(survey_path)
        
        # Add survey samples to training data
        for text, intent in survey_samples:
            patterns.append(text)
            tags.append(intent)
        
        print(f"✓ Total patterns after augmentation: {len(patterns)}")
        print(f"✓ Total unique intents: {len(set(tags))}")
        
    except Exception as e:
        print(f"❌ Error processing survey data: {e}")
        return
    
    # Step 3: Prepare training data
    print("\n[3/6] Preparing fine-tuning dataset...")
    X_train, X_val, y_train, y_val = data_loader.create_train_val_split(
        patterns, tags, test_size=0.2, random_state=42
    )
    print(f"✓ Training samples: {len(X_train)}")
    print(f"✓ Validation samples: {len(X_val)}")
    
    # Step 4: Load pre-trained model
    print("\n[4/6] Loading pre-trained model...")
    model_path = "models/trained_intent_classifier"
    
    if not os.path.exists(model_path):
        print(f"❌ Pre-trained model not found at {model_path}")
        print("Please train the base model first using train_model.py")
        return
    
    try:
        classifier = IntentClassificationEngine(
            model_name='bert-base-uncased',
            max_length=256
        )
        classifier.load_model(model_path)
        print("✓ Pre-trained model loaded successfully")
    except Exception as e:
        print(f"❌ Error loading model: {e}")
        return
    
    # Step 5: Fine-tune model
    print("\n[5/6] Fine-tuning model on expanded dataset...")
    print("Fine-tuning with parameters (epochs=5, batch_size=8, lr=2e-5)")
    print("This will take approximately 15-20 minutes...")
    print("-" * 70)
    
    try:
        classifier.train(
            train_texts=X_train,
            train_labels=y_train,
            val_texts=X_val,
            val_labels=y_val,
            epochs=5,  # Fewer epochs for fine-tuning
            batch_size=8,
            learning_rate=2e-5  # Lower learning rate for fine-tuning
        )
        print("-" * 70)
        print("✓ Fine-tuning completed successfully!")
    except Exception as e:
        print(f"❌ Error during fine-tuning: {e}")
        return
    
    # Step 6: Save fine-tuned model
    print("\n[6/6] Saving fine-tuned model...")
    save_dir = "models/finetuned_intent_classifier"
    
    try:
        classifier.save_model(save_dir)
        print(f"✓ Fine-tuned model saved to: {save_dir}")
        print("\nModel is ready to use!")
        print("Update your application to use the fine-tuned model path:")
        print(f"  model_path = '{save_dir}'")
    except Exception as e:
        print(f"❌ Error saving model: {e}")
        return
    
    print("\n" + "="*70)
    print("FINE-TUNING COMPLETE!")
    print("="*70)
    print("\nNext steps:")
    print("1. Test the fine-tuned model")
    print("2. Update backend/frontend to use the new model")
    print("3. Run the system with improved performance")


if __name__ == "__main__":
    finetune_intent_classifier()
