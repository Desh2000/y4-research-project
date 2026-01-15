"""
Training Script for Empathetic Conversational Support System
Trains the intent classification model on the provided datasets
"""

import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from utils.data_loader import DataLoader
from models.intent_classifier import IntentClassificationEngine
import json


def train_intent_classifier():
    """Train the intent classification model"""
    
    print("="*70)
    print("EMPATHETIC CONVERSATIONAL SUPPORT SYSTEM - MODEL TRAINING")
    print("="*70)
    
    # Initialize data loader
    print("\n[1/5] Loading datasets...")
    data_loader = DataLoader(data_dir="data/")
    
    try:
        # Load intents data
        data_loader.load_intents_data()
        patterns, tags, responses = data_loader.prepare_intent_dataset()
        
        # Get statistics
        stats = data_loader.get_intent_statistics()
        print(f"✓ Loaded {stats['total_patterns']} patterns")
        print(f"✓ Found {stats['unique_intents']} unique intents")
        
        # Show persona distribution
        persona_intents = data_loader.categorize_for_personas()
        print(f"\nIntent distribution across personas:")
        for persona, intents in persona_intents.items():
            print(f"  - {persona.capitalize()}: {len(intents)} intents")
        
    except FileNotFoundError as e:
        print(f"\n❌ Error: Could not find dataset files.")
        print(f"Please ensure the following files are in the 'data/' directory:")
        print(f"  - Mental_Health_FAQ.csv")
        print(f"  - intents.json")
        print(f"  - train.csv")
        print(f"\nYou can copy them from: {e}")
        return
    
    # Prepare training data
    print("\n[2/5] Preparing training data...")
    X_train, X_val, y_train, y_val = data_loader.create_train_val_split(
        patterns, tags, test_size=0.2, random_state=42
    )
    print(f"✓ Training samples: {len(X_train)}")
    print(f"✓ Validation samples: {len(X_val)}")
    
    # Initialize intent classifier
    print("\n[3/5] Initializing intent classification model...")
    print("Note: This may take a few minutes to download the pre-trained BERT model...")
    
    try:
        classifier = IntentClassificationEngine(
            model_name='bert-base-uncased',
            max_length=256
        )
        print("✓ Model initialized successfully")
    except Exception as e:
        print(f"\n❌ Error initializing model: {e}")
        print("Please ensure you have internet connection to download BERT model")
        print("Or install the model manually: transformers-cli download bert-base-uncased")
        return
    
    # Train model
    print("\n[4/5] Training intent classifier...")
    print("Training with optimized parameters (15 epochs, batch_size=8, lr=3e-5)")
    print("This may take 30-45 minutes depending on your hardware...")
    print("-" * 70)
    
    try:
        classifier.train(
            train_texts=X_train,
            train_labels=y_train,
            val_texts=X_val,
            val_labels=y_val,
            epochs=15,
            batch_size=8,
            learning_rate=3e-5
        )
        print("-" * 70)
        print("✓ Training completed successfully!")
    except Exception as e:
        print(f"\n❌ Error during training: {e}")
        return
    
    # Save model
    print("\n[5/5] Saving trained model...")
    save_dir = "models/trained_intent_classifier"
    
    try:
        classifier.save_model(save_dir)
        print(f"✓ Model saved to: {save_dir}")
    except Exception as e:
        print(f"\n❌ Error saving model: {e}")
        return
    
    # Test predictions
    print("\n" + "="*70)
    print("TESTING MODEL PREDICTIONS")
    print("="*70)
    
    test_inputs = [
        "I'm feeling really depressed and worthless",
        "Thank you so much for your help!",
        "Can you tell me about anxiety?",
        "Good morning! How are you?",
        "I'm stressed out from work"
    ]
    
    print("\nSample predictions:")
    for test_input in test_inputs:
        try:
            intent, confidence = classifier.predict(test_input, return_confidence=True)
            print(f"\nInput: '{test_input}'")
            print(f"Predicted Intent: {intent} (confidence: {confidence:.2%})")
            
            # Show top 3 predictions
            top_k = classifier.predict_top_k(test_input, k=3)
            print("Top 3 predictions:")
            for i, (label, prob) in enumerate(top_k, 1):
                print(f"  {i}. {label}: {prob:.2%}")
        except Exception as e:
            print(f"Error predicting: {e}")
    
    print("\n" + "="*70)
    print("TRAINING COMPLETE!")
    print("="*70)
    print("\nNext steps:")
    print("1. Run the backend API: python backend/api.py")
    print("2. Run the frontend: streamlit run frontend/app.py")
    print("3. Start chatting with the personas!")


def create_response_database():
    """Create a response database from intents.json for the personas"""
    
    print("\n" + "="*70)
    print("CREATING RESPONSE DATABASE")
    print("="*70)
    
    data_loader = DataLoader(data_dir="data/")
    
    try:
        intents_data = data_loader.load_intents_data()
        
        # Create response mapping
        response_db = {}
        for intent in intents_data['intents']:
            tag = intent['tag']
            responses = intent.get('responses', [])
            response_db[tag] = responses
        
        # Save to file
        output_path = "models/response_database.json"
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(response_db, f, indent=2, ensure_ascii=False)
        
        print(f"✓ Response database created: {output_path}")
        print(f"✓ Total intents: {len(response_db)}")
        
    except Exception as e:
        print(f"❌ Error creating response database: {e}")


if __name__ == "__main__":
    print("\n" + "="*70)
    print(" EMPATHETIC CONVERSATIONAL SUPPORT SYSTEM ")
    print(" Component 3: Training Module ")
    print("="*70)
    
    # Train intent classifier
    train_intent_classifier()
    
    # Create response database
    create_response_database()
    
    print("\n✓ All training steps completed!")
    print("="*70 + "\n")
