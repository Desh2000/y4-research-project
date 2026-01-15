"""
Model Output Generator
Creates comprehensive output file with model predictions and analysis
"""

import sys
import os
import json
import pandas as pd
import pickle
from datetime import datetime
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from models.intent_classifier import IntentClassificationEngine
from personas.base_persona import FriendPersona
from personas.counselor_persona import CounselorPersona
from personas.doctor_persona import DoctorPersona


def generate_model_outputs(model_path: str = "models/finetuned_intent_classifier_v2"):
    """Generate comprehensive output file from the fine-tuned model"""
    
    print("="*80)
    print(" GENERATING MODEL OUTPUT FILE")
    print("="*80)
    
    # Load model
    print(f"\n[1/4] Loading model from: {model_path}")
    classifier = IntentClassificationEngine(model_name='bert-base-uncased', max_length=256)
    
    try:
        classifier.load_model(model_path)
        print("✓ Model loaded successfully")
    except Exception as e:
        print(f"❌ Error loading model: {e}")
        return
    
    # Initialize personas
    print("\n[2/4] Initializing personas...")
    personas = {
        'Friend': FriendPersona(),
        'Counselor': CounselorPersona(),
        'Doctor': DoctorPersona()
    }
    print("✓ Personas initialized")
    
    # Test cases
    print("\n[3/4] Processing test cases...")
    test_cases = [
        # Work stress (from training examples)
        {"input": "Work is making me so tired all the time. Help?", "category": "Work Stress"},
        {"input": "Hey, I'm super stressed from work. Any ideas?", "category": "Work Stress"},
        {"input": "I'm burned out and don't know how to relax.", "category": "Burnout"},
        
        # Survey-derived scenarios
        {"input": "Work is constantly interfering with my mental health", "category": "Work-Mental Health"},
        {"input": "Working remotely is affecting my mental wellbeing", "category": "Remote Work"},
        {"input": "My workplace doesn't provide mental health support", "category": "Workplace Support"},
        {"input": "I'm afraid to seek help at work", "category": "Workplace Stigma"},
        {"input": "I'm worried about consequences of discussing mental health at work", "category": "Workplace Stigma"},
        {"input": "I can't talk to coworkers about mental health", "category": "Social Support"},
        
        # General mental health
        {"input": "I'm feeling really anxious lately", "category": "Anxiety"},
        {"input": "I think I need therapy", "category": "Treatment Seeking"},
        {"input": "I'm depressed and nothing helps", "category": "Depression"},
        {"input": "Can't sleep at night", "category": "Sleep Problems"},
        {"input": "Feeling lonely and isolated", "category": "Loneliness"},
        {"input": "I'm stressed about my career", "category": "Career Stress"},
        
        # Crisis
        {"input": "I don't want to live anymore", "category": "Crisis"},
        
        # Positive
        {"input": "I'm feeling much better now", "category": "Positive"},
        {"input": "Thank you for your help", "category": "Gratitude"},
        
        # General
        {"input": "Hello, how are you?", "category": "Greeting"},
        {"input": "What is depression?", "category": "Information Seeking"},
    ]
    
    results = []
    
    for idx, test_case in enumerate(test_cases, 1):
        user_input = test_case["input"]
        category = test_case["category"]
        
        # Get intent prediction
        intent, confidence = classifier.predict(user_input, return_confidence=True)
        
        # Generate responses from each persona
        responses = {}
        for persona_name, persona_obj in personas.items():
            response = persona_obj.generate_response(user_input, intent, confidence)
            responses[persona_name] = response
        
        result = {
            "id": idx,
            "category": category,
            "user_input": user_input,
            "predicted_intent": intent,
            "confidence": round(confidence * 100, 2),
            "responses": responses
        }
        
        results.append(result)
        print(f"  Processed {idx}/{len(test_cases)}: {category}")
    
    print("✓ All test cases processed")
    
    # Generate outputs
    print("\n[4/4] Generating output files...")
    
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    
    # JSON output
    json_output = {
        "metadata": {
            "model_path": model_path,
            "generation_time": datetime.now().isoformat(),
            "total_test_cases": len(test_cases),
            "model_type": "BERT-based Intent Classifier (Fine-tuned)",
            "personas": list(personas.keys())
        },
        "results": results,
        "summary": {
            "average_confidence": round(sum(r["confidence"] for r in results) / len(results), 2),
            "intent_distribution": {}
        }
    }
    
    # Calculate intent distribution
    for result in results:
        intent = result["predicted_intent"]
        json_output["summary"]["intent_distribution"][intent] = \
            json_output["summary"]["intent_distribution"].get(intent, 0) + 1
    
    json_filename = f"output/model_output_{timestamp}.json"
    os.makedirs("output", exist_ok=True)
    
    with open(json_filename, 'w', encoding='utf-8') as f:
        json.dump(json_output, f, indent=2, ensure_ascii=False)
    print(f"✓ JSON output saved: {json_filename}")
    
    # CSV output (flattened)
    csv_data = []
    for result in results:
        row = {
            "ID": result["id"],
            "Category": result["category"],
            "User Input": result["user_input"],
            "Predicted Intent": result["predicted_intent"],
            "Confidence (%)": result["confidence"],
            "Friend Response": result["responses"]["Friend"],
            "Counselor Response": result["responses"]["Counselor"],
            "Doctor Response": result["responses"]["Doctor"]
        }
        csv_data.append(row)
    
    df = pd.DataFrame(csv_data)
    csv_filename = f"output/model_output_{timestamp}.csv"
    df.to_csv(csv_filename, index=False, encoding='utf-8-sig')
    print(f"✓ CSV output saved: {csv_filename}")
    
    # Pickle output (for Python serialization)
    pickle_filename = f"output/model_output_{timestamp}.pkl"
    with open(pickle_filename, 'wb') as f:
        pickle.dump(json_output, f)
    print(f"✓ Pickle output saved: {pickle_filename}")
    
    # Human-readable text output
    txt_filename = f"output/model_output_{timestamp}.txt"
    with open(txt_filename, 'w', encoding='utf-8') as f:
        f.write("="*80 + "\n")
        f.write(" FINE-TUNED MODEL OUTPUT REPORT\n")
        f.write("="*80 + "\n\n")
        
        f.write(f"Model: {model_path}\n")
        f.write(f"Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write(f"Test Cases: {len(test_cases)}\n")
        f.write(f"Average Confidence: {json_output['summary']['average_confidence']}%\n\n")
        
        f.write("-"*80 + "\n")
        f.write(" DETAILED RESULTS\n")
        f.write("-"*80 + "\n\n")
        
        for result in results:
            f.write(f"[{result['id']}] {result['category']}\n")
            f.write(f"User Input: \"{result['user_input']}\"\n")
            f.write(f"Predicted Intent: {result['predicted_intent']}\n")
            f.write(f"Confidence: {result['confidence']}%\n\n")
            
            for persona_name, response in result['responses'].items():
                f.write(f"  {persona_name} Response:\n")
                f.write(f"  \"{response}\"\n\n")
            
            f.write("-"*80 + "\n\n")
        
        f.write("\n" + "="*80 + "\n")
        f.write(" SUMMARY STATISTICS\n")
        f.write("="*80 + "\n\n")
        
        f.write("Intent Distribution:\n")
        for intent, count in sorted(json_output['summary']['intent_distribution'].items(), 
                                    key=lambda x: x[1], reverse=True):
            f.write(f"  {intent}: {count} cases\n")
        
        f.write(f"\nAverage Confidence: {json_output['summary']['average_confidence']}%\n")
        
        # Confidence levels
        high_conf = sum(1 for r in results if r['confidence'] >= 70)
        med_conf = sum(1 for r in results if 40 <= r['confidence'] < 70)
        low_conf = sum(1 for r in results if r['confidence'] < 40)
        
        f.write(f"\nConfidence Distribution:\n")
        f.write(f"  High (≥70%): {high_conf} cases\n")
        f.write(f"  Medium (40-70%): {med_conf} cases\n")
        f.write(f"  Low (<40%): {low_conf} cases\n")
    
    print(f"✓ Text output saved: {txt_filename}")
    
    print("\n" + "="*80)
    print(" OUTPUT GENERATION COMPLETE")
    print("="*80)
    print(f"\n📁 Output files created in 'output/' directory:")
    print(f"   - {json_filename}")
    print(f"   - {csv_filename}")
    print(f"   - {pickle_filename}")
    print(f"   - {txt_filename}")
    
    return json_output


if __name__ == "__main__":
    generate_model_outputs()
