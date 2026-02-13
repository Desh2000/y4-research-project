"""
Test Script for Fine-Tuned Model
Evaluates the fine-tuned intent classification model with various test cases
"""

import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from models.intent_classifier import IntentClassificationEngine
from personas.base_persona import FriendPersona
from personas.counselor_persona import CounselorPersona
from personas.doctor_persona import DoctorPersona


def test_intent_classification(model_path: str = "models/finetuned_intent_classifier"):
    """Test the fine-tuned model with various inputs"""
    
    print("="*80)
    print(" TESTING FINE-TUNED INTENT CLASSIFICATION MODEL")
    print("="*80)
    
    # Load model
    print(f"\n[1/3] Loading fine-tuned model from: {model_path}")
    classifier = IntentClassificationEngine(
        model_name='bert-base-uncased',
        max_length=256
    )
    
    try:
        classifier.load_model(model_path)
        print("✓ Model loaded successfully")
    except Exception as e:
        print(f"❌ Error loading model: {e}")
        return
    
    # Test cases covering various scenarios
    test_cases = [
        # Work-related stress (from training examples)
        "Work is making me so tired all the time. Help?",
        "Hey, I'm super stressed from work. Any ideas?",
        "I'm burned out and don't know how to relax.",
        
        # Survey-derived scenarios
        "Work is constantly interfering with my mental health",
        "Working remotely is affecting my mental wellbeing",
        "My workplace doesn't provide mental health support",
        "I'm afraid to seek help at work",
        "I'm worried about consequences of discussing mental health at work",
        "I can't talk to coworkers about mental health",
        
        # General mental health
        "I'm feeling really anxious lately",
        "I think I need therapy",
        "I'm depressed and nothing helps",
        "Can't sleep at night",
        "Feeling lonely and isolated",
        
        # Crisis scenarios
        "I don't want to live anymore",
        
        # Positive scenarios
        "I'm feeling much better now",
        "Thank you for your help",
        
        # General queries
        "Hello, how are you?",
        "What is depression?",
    ]
    
    print("\n[2/3] Running intent classification tests...")
    print("-" * 80)
    
    results = []
    for i, text in enumerate(test_cases, 1):
        intent, confidence = classifier.predict(text, return_confidence=True)
        results.append((text, intent, confidence))
        
        print(f"\n{i}. Input: \"{text}\"")
        print(f"   → Intent: {intent}")
        print(f"   → Confidence: {confidence:.2%}")
    
    print("\n" + "-" * 80)
    
    # Test with personas
    print("\n[3/3] Testing persona responses with fine-tuned model intents...")
    print("-" * 80)
    
    friend = FriendPersona()
    counselor = CounselorPersona()
    doctor = DoctorPersona()
    
    # Test specific work-related scenarios
    work_tests = [
        ("Work is making me so tired all the time. Help?", friend),
        ("Hey, I'm super stressed from work. Any ideas?", counselor),
        ("I'm burned out and don't know how to relax.", doctor),
    ]
    
    for message, persona in work_tests:
        intent, confidence = classifier.predict(message, return_confidence=True)
        response = persona.generate_response(message, intent, confidence)
        
        print(f"\n📨 User: \"{message}\"")
        print(f"🤖 {persona.name}: \"{response}\"")
        print(f"   (Intent: {intent}, Confidence: {confidence:.2%})")
    
    print("\n" + "="*80)
    print(" TEST COMPLETE")
    print("="*80)
    
    # Summary statistics
    print("\n📊 Summary Statistics:")
    print(f"   Total test cases: {len(results)}")
    high_confidence = sum(1 for _, _, conf in results if conf >= 0.7)
    medium_confidence = sum(1 for _, _, conf in results if 0.4 <= conf < 0.7)
    low_confidence = sum(1 for _, _, conf in results if conf < 0.4)
    
    print(f"   High confidence (≥70%): {high_confidence}")
    print(f"   Medium confidence (40-70%): {medium_confidence}")
    print(f"   Low confidence (<40%): {low_confidence}")
    
    avg_confidence = sum(conf for _, _, conf in results) / len(results)
    print(f"   Average confidence: {avg_confidence:.2%}")
    
    # Intent distribution
    intent_counts = {}
    for _, intent, _ in results:
        intent_counts[intent] = intent_counts.get(intent, 0) + 1
    
    print(f"\n📋 Intent Distribution:")
    for intent, count in sorted(intent_counts.items(), key=lambda x: x[1], reverse=True):
        print(f"   {intent}: {count} cases")
    
    return results


def compare_models():
    """Compare original and fine-tuned models"""
    
    print("\n" + "="*80)
    print(" COMPARING ORIGINAL VS FINE-TUNED MODELS")
    print("="*80)
    
    test_inputs = [
        "Work is constantly interfering with my mental health",
        "Working remotely is affecting my mental wellbeing",
        "My workplace doesn't provide mental health support",
        "I'm afraid to seek help at work",
    ]
    
    print("\n[Loading Original Model]")
    original = IntentClassificationEngine(model_name='bert-base-uncased', max_length=256)
    try:
        original.load_model("models/trained_intent_classifier")
        print("✓ Original model loaded")
    except Exception as e:
        print(f"❌ Could not load original model: {e}")
        return
    
    print("\n[Loading Fine-Tuned Model]")
    finetuned = IntentClassificationEngine(model_name='bert-base-uncased', max_length=256)
    try:
        finetuned.load_model("models/finetuned_intent_classifier")
        print("✓ Fine-tuned model loaded")
    except Exception as e:
        print(f"❌ Could not load fine-tuned model: {e}")
        return
    
    print("\n[Comparison Results]")
    print("-" * 80)
    
    for text in test_inputs:
        orig_intent, orig_conf = original.predict(text, return_confidence=True)
        fine_intent, fine_conf = finetuned.predict(text, return_confidence=True)
        
        print(f"\nInput: \"{text}\"")
        print(f"  Original Model:")
        print(f"    Intent: {orig_intent} | Confidence: {orig_conf:.2%}")
        print(f"  Fine-Tuned Model:")
        print(f"    Intent: {fine_intent} | Confidence: {fine_conf:.2%}")
        
        if orig_intent != fine_intent:
            print(f"  ⚠️  Intent changed: {orig_intent} → {fine_intent}")
        if fine_conf > orig_conf:
            print(f"  ✓ Confidence improved by {(fine_conf - orig_conf)*100:.1f}%")
        elif fine_conf < orig_conf:
            print(f"  ⚠️  Confidence decreased by {(orig_conf - fine_conf)*100:.1f}%")
    
    print("\n" + "="*80)


if __name__ == "__main__":
    # Test fine-tuned model
    results = test_intent_classification()
    
    # Compare models
    print("\n")
    compare_models()
    
    print("\n✅ All tests completed!")
