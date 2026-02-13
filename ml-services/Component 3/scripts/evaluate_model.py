"""
Model Accuracy Evaluation
Calculates accuracy metrics for the trained model
"""

import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from models.intent_classifier import IntentClassificationEngine
from utils.data_loader import DataLoader
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix
import numpy as np


def evaluate_model(model_path: str = "models/finetuned_intent_classifier_v2"):
    """Evaluate model accuracy on validation data"""
    
    print("="*80)
    print(" MODEL ACCURACY EVALUATION")
    print("="*80)
    
    # Load data
    print("\n[1/4] Loading dataset...")
    data_loader = DataLoader(data_dir="data/")
    data_loader.load_intents_data()
    patterns, tags, responses = data_loader.prepare_intent_dataset()
    
    # Create train/val split (same as training)
    X_train, X_val, y_train, y_val = data_loader.create_train_val_split(
        patterns, tags, test_size=0.15, random_state=42
    )
    
    print(f"✓ Training samples: {len(X_train)}")
    print(f"✓ Validation samples: {len(X_val)}")
    print(f"✓ Unique intents: {len(set(tags))}")
    
    # Load model
    print(f"\n[2/4] Loading model from: {model_path}")
    classifier = IntentClassificationEngine(model_name='bert-base-uncased', max_length=256)
    
    try:
        classifier.load_model(model_path)
        print("✓ Model loaded successfully")
    except Exception as e:
        print(f"❌ Error loading model: {e}")
        return
    
    # Make predictions
    print("\n[3/4] Making predictions on validation set...")
    predictions = []
    confidences = []
    
    for text in X_val:
        pred, conf = classifier.predict(text, return_confidence=True)
        predictions.append(pred)
        confidences.append(conf)
    
    print("✓ Predictions complete")
    
    # Calculate metrics
    print("\n[4/4] Calculating accuracy metrics...")
    accuracy = accuracy_score(y_val, predictions)
    avg_confidence = np.mean(confidences)
    
    print("\n" + "="*80)
    print(" RESULTS")
    print("="*80)
    print(f"\n📊 Overall Accuracy: {accuracy*100:.2f}%")
    print(f"📈 Average Confidence: {avg_confidence*100:.2f}%")
    print(f"📉 Standard Deviation: {np.std(confidences)*100:.2f}%")
    print(f"🎯 Min Confidence: {np.min(confidences)*100:.2f}%")
    print(f"🎯 Max Confidence: {np.max(confidences)*100:.2f}%")
    
    # Confidence distribution
    high_conf = sum(1 for c in confidences if c >= 0.7)
    med_conf = sum(1 for c in confidences if 0.4 <= c < 0.7)
    low_conf = sum(1 for c in confidences if c < 0.4)
    
    print(f"\n📦 Confidence Distribution:")
    print(f"   High (≥70%): {high_conf} samples ({high_conf/len(confidences)*100:.1f}%)")
    print(f"   Medium (40-70%): {med_conf} samples ({med_conf/len(confidences)*100:.1f}%)")
    print(f"   Low (<40%): {low_conf} samples ({low_conf/len(confidences)*100:.1f}%)")
    
    # Per-class accuracy
    print(f"\n📋 Classification Report:")
    print("-"*80)
    report = classification_report(y_val, predictions, zero_division=0)
    print(report)
    
    # Confusion matrix analysis
    cm = confusion_matrix(y_val, predictions, labels=list(set(tags)))
    correct_per_class = cm.diagonal()
    total_per_class = cm.sum(axis=1)
    
    print(f"\n📌 Per-Intent Accuracy:")
    print("-"*80)
    unique_intents = sorted(set(tags))
    intent_accuracies = []
    
    for i, intent in enumerate(unique_intents):
        if intent in y_val:
            idx = unique_intents.index(intent)
            if total_per_class[idx] > 0:
                acc = correct_per_class[idx] / total_per_class[idx]
                intent_accuracies.append((intent, acc, int(total_per_class[idx])))
    
    # Sort by accuracy
    intent_accuracies.sort(key=lambda x: x[1], reverse=True)
    
    for intent, acc, count in intent_accuracies[:10]:  # Top 10
        print(f"   {intent:20s}: {acc*100:5.1f}% ({count} samples)")
    
    if len(intent_accuracies) > 10:
        print(f"   ... and {len(intent_accuracies)-10} more intents")
    
    print("\n" + "="*80)
    
    return {
        'accuracy': accuracy,
        'avg_confidence': avg_confidence,
        'validation_size': len(X_val),
        'training_size': len(X_train)
    }


def compare_models():
    """Compare original vs fine-tuned model"""
    
    print("\n" + "="*80)
    print(" MODEL COMPARISON")
    print("="*80)
    
    print("\n[Evaluating Original Model]")
    original_metrics = evaluate_model("models/trained_intent_classifier")
    
    print("\n[Evaluating Fine-Tuned Model]")
    finetuned_metrics = evaluate_model("models/finetuned_intent_classifier_v2")
    
    if original_metrics and finetuned_metrics:
        print("\n" + "="*80)
        print(" COMPARISON SUMMARY")
        print("="*80)
        
        acc_diff = (finetuned_metrics['accuracy'] - original_metrics['accuracy']) * 100
        conf_diff = (finetuned_metrics['avg_confidence'] - original_metrics['avg_confidence']) * 100
        
        print(f"\n📊 Accuracy Change: {acc_diff:+.2f}%")
        print(f"📈 Confidence Change: {conf_diff:+.2f}%")
        
        if acc_diff > 0:
            print(f"✅ Fine-tuning improved accuracy by {acc_diff:.2f}%")
        else:
            print(f"⚠️  Fine-tuning decreased accuracy by {abs(acc_diff):.2f}%")


if __name__ == "__main__":
    # Evaluate fine-tuned model
    metrics = evaluate_model()
    
    # Uncomment to compare models
    # compare_models()
