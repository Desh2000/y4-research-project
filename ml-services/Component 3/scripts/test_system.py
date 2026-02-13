"""
System Test Script
Quick verification that all components are working
"""

import sys
from pathlib import Path

def test_imports():
    """Test if all major imports work"""
    print("Testing imports...")
    try:
        from utils.data_loader import DataLoader
        from utils.text_preprocessor import TextPreprocessor
        from personas.base_persona import FriendPersona
        from personas.counselor_persona import CounselorPersona
        from personas.doctor_persona import DoctorPersona
        from privacy.privacy_manager import DifferentialPrivacy, DataAnonymizer
        print("✓ All imports successful")
        return True
    except ImportError as e:
        print(f"❌ Import error: {e}")
        return False


def test_data_loader():
    """Test data loading"""
    print("\nTesting data loader...")
    try:
        from utils.data_loader import DataLoader
        loader = DataLoader(data_dir="data/")
        
        # Check if data files exist
        data_dir = Path("data")
        if not (data_dir / "intents.json").exists():
            print("⚠️  intents.json not found in data/ directory")
            return False
        
        loader.load_intents_data()
        stats = loader.get_intent_statistics()
        print(f"✓ Loaded {stats['total_patterns']} patterns, {stats['unique_intents']} intents")
        return True
    except Exception as e:
        print(f"❌ Data loader error: {e}")
        return False


def test_personas():
    """Test persona creation"""
    print("\nTesting personas...")
    try:
        from personas.base_persona import FriendPersona
        from personas.counselor_persona import CounselorPersona
        from personas.doctor_persona import DoctorPersona
        
        friend = FriendPersona()
        counselor = CounselorPersona()
        doctor = DoctorPersona()
        
        print(f"✓ Friend: {friend.name}")
        print(f"✓ Counselor: {counselor.name}")
        print(f"✓ Doctor: {doctor.name}")
        
        # Test response generation
        test_message = "I'm feeling stressed"
        response = friend.generate_response(test_message, "stressed", 0.9)
        print(f"✓ Friend response generated: {response[:50]}...")
        
        return True
    except Exception as e:
        print(f"❌ Persona error: {e}")
        return False


def test_privacy():
    """Test privacy mechanisms"""
    print("\nTesting privacy mechanisms...")
    try:
        from privacy.privacy_manager import DifferentialPrivacy, DataAnonymizer
        
        # Test differential privacy
        dp = DifferentialPrivacy(epsilon=1.0)
        value = 100.0
        noisy = dp.add_laplace_noise(value, 1.0)
        print(f"✓ Differential privacy: {value} → {noisy:.2f}")
        
        # Test anonymization
        anonymizer = DataAnonymizer()
        text = "Email me at john@example.com or call 555-1234"
        anonymized = anonymizer.anonymize_text(text)
        print(f"✓ Anonymization: '{text}' → '{anonymized}'")
        
        return True
    except Exception as e:
        print(f"❌ Privacy error: {e}")
        return False


def test_text_processing():
    """Test text preprocessing"""
    print("\nTesting text processing...")
    try:
        from utils.text_preprocessor import TextPreprocessor
        
        preprocessor = TextPreprocessor()
        
        test_text = "I'm feeling really ANXIOUS and stressed out!!!"
        processed = preprocessor.preprocess(test_text)
        print(f"✓ Preprocessed: '{test_text}' → '{processed}'")
        
        emotions = preprocessor.detect_emotion_keywords(test_text)
        print(f"✓ Detected emotions: {list(emotions.keys())}")
        
        sentiment = preprocessor.calculate_sentiment_score(test_text)
        print(f"✓ Sentiment score: {sentiment:.2f}")
        
        return True
    except Exception as e:
        print(f"❌ Text processing error: {e}")
        return False


def test_video_recommendations():
    """Test video recommendation system"""
    print("\nTesting video recommendations...")
    try:
        from personas.counselor_persona import CounselorPersona
        
        counselor = CounselorPersona()
        videos = counselor.suggest_videos('anxiety')
        print(f"✓ Found {len(videos)} video recommendations for anxiety")
        
        if videos:
            print(f"  Example: {videos[0]['title']}")
        
        return True
    except Exception as e:
        print(f"❌ Video recommendation error: {e}")
        return False


def test_crisis_detection():
    """Test crisis detection"""
    print("\nTesting crisis detection...")
    try:
        from personas.base_persona import FriendPersona
        
        friend = FriendPersona()
        
        # Test positive case
        crisis_text = "I want to end my life"
        is_crisis = friend.detect_crisis(crisis_text)
        print(f"✓ Crisis detected in: '{crisis_text}' → {is_crisis}")
        
        # Test negative case
        normal_text = "I'm feeling sad today"
        is_crisis = friend.detect_crisis(normal_text)
        print(f"✓ No crisis in: '{normal_text}' → {is_crisis}")
        
        return True
    except Exception as e:
        print(f"❌ Crisis detection error: {e}")
        return False


def run_all_tests():
    """Run all tests"""
    print("="*70)
    print(" EMPATHETIC CONVERSATIONAL SUPPORT SYSTEM - SYSTEM TEST")
    print("="*70)
    
    results = []
    
    results.append(("Imports", test_imports()))
    results.append(("Data Loader", test_data_loader()))
    results.append(("Personas", test_personas()))
    results.append(("Privacy", test_privacy()))
    results.append(("Text Processing", test_text_processing()))
    results.append(("Video Recommendations", test_video_recommendations()))
    results.append(("Crisis Detection", test_crisis_detection()))
    
    # Summary
    print("\n" + "="*70)
    print(" TEST SUMMARY")
    print("="*70)
    
    passed = sum(1 for _, result in results if result)
    total = len(results)
    
    for test_name, result in results:
        status = "✓ PASS" if result else "❌ FAIL"
        print(f"{test_name:.<40} {status}")
    
    print("-"*70)
    print(f"Total: {passed}/{total} tests passed ({passed/total*100:.1f}%)")
    print("="*70)
    
    if passed == total:
        print("\n🎉 All tests passed! System is ready to use.")
        print("\nNext steps:")
        print("  1. Train the model: python train_model.py")
        print("  2. Start backend: python backend/api.py")
        print("  3. Start frontend: streamlit run frontend/app.py")
    else:
        print("\n⚠️  Some tests failed. Please check the errors above.")
        print("Refer to SETUP_GUIDE.md for troubleshooting.")
    
    return passed == total


if __name__ == "__main__":
    try:
        success = run_all_tests()
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        print("\n\nTests interrupted by user")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
