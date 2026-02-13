"""
Backend API for Empathetic Conversational Support System
FastAPI-based REST API for chat interactions
"""

from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, List, Dict, Any
import sys
import os
import json

# Add parent directory to path
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from personas.base_persona import FriendPersona
from personas.counselor_persona import CounselorPersona
from personas.doctor_persona import DoctorPersona
from privacy.privacy_manager import SessionManager, DifferentialPrivacy, DataAnonymizer
from utils.text_preprocessor import TextPreprocessor
import pickle
from datetime import datetime


# Initialize FastAPI app
app = FastAPI(
    title="Empathetic Conversational Support System API",
    description="Privacy-preserving mental health support chatbot",
    version="1.0.0"
)

# Chat history storage
CHAT_HISTORY_DIR = "chat_history"
os.makedirs(CHAT_HISTORY_DIR, exist_ok=True)

# Questionnaire results storage
QUESTIONNAIRE_RESULTS_DIR = "questionnaire_results"
os.makedirs(QUESTIONNAIRE_RESULTS_DIR, exist_ok=True)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global instances
personas = {
    'friend': FriendPersona(),
    'counselor': CounselorPersona(),
    'medical_officer': DoctorPersona()
}

session_manager = SessionManager()
dp_mechanism = DifferentialPrivacy(epsilon=1.0)
anonymizer = DataAnonymizer()
preprocessor = TextPreprocessor()

# Try to load intent classifier (using fine-tuned model)
try:
    from models.intent_classifier import IntentClassificationEngine
    intent_classifier = IntentClassificationEngine()
    # Try improved fine-tuned model first, fallback to original
    try:
        intent_classifier.load_model("models/finetuned_intent_classifier_v2")
        print("✓ Loaded improved fine-tuned model (v2)")
    except:
        intent_classifier.load_model("models/trained_intent_classifier")
        print("✓ Loaded original trained model")
    CLASSIFIER_LOADED = True
except Exception as e:
    print(f"Warning: Could not load intent classifier: {e}")
    print("Using rule-based intent detection as fallback")
    CLASSIFIER_LOADED = False


# Load response database
try:
    with open("models/response_database.json", 'r', encoding='utf-8') as f:
        response_database = json.load(f)
except:
    response_database = {}


# Pydantic models
class SessionCreate(BaseModel):
    user_id: Optional[str] = None


class ChatMessage(BaseModel):
    session_id: str
    message: str
    persona: str = "friend"  # friend, counselor, or medical_officer


class ChatResponse(BaseModel):
    session_id: str
    user_message: str
    bot_response: str
    persona: str
    intent: Optional[str] = None
    confidence: Optional[float] = None
    crisis_detected: bool = False


class SessionInfo(BaseModel):
    session_id: str
    created_at: str
    message_count: int
    persona_used: str


class QuestionnaireAnswers(BaseModel):
    session_id: str
    answers: Dict[str, Any]


class QuestionnaireResult(BaseModel):
    session_id: str
    total_score: float
    category: str
    interpretation: str
    individual_scores: Dict[str, float]
    file_path: str
    timestamp: str


# Fallback intent detection
def detect_intent_fallback(text: str) -> tuple:
    """Simple rule-based intent detection"""
    text_lower = text.lower()
    
    # Define keyword mappings
    intent_keywords = {
        'greeting': ['hello', 'hi', 'hey', 'good morning', 'good afternoon', 'good evening'],
        'goodbye': ['bye', 'goodbye', 'see you', 'gotta go'],
        'thanks': ['thank', 'thanks', 'appreciate'],
        'sad': ['sad', 'down', 'unhappy', 'depressed feeling'],
        'depressed': ['depressed', 'depression', 'hopeless'],
        'anxious': ['anxious', 'anxiety', 'worried', 'nervous', 'panic'],
        'stressed': ['stressed', 'stress', 'overwhelmed', 'pressure'],
        'happy': ['happy', 'great', 'good', 'wonderful', 'excellent'],
        'help': ['help', 'support', 'assist'],
        'suicide': ['suicide', 'kill myself', 'end my life']
    }
    
    for intent, keywords in intent_keywords.items():
        if any(keyword in text_lower for keyword in keywords):
            return intent, 0.75
    
    return 'casual', 0.5


# API Endpoints
@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "message": "Empathetic Conversational Support System API",
        "version": "1.0.0",
        "status": "running",
        "classifier_loaded": CLASSIFIER_LOADED
    }


@app.post("/session/create", response_model=Dict)
async def create_session(session_create: SessionCreate):
    """Create a new chat session"""
    try:
        session_id = session_manager.create_session(session_create.user_id)
        return {
            "session_id": session_id,
            "message": "Session created successfully"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/chat", response_model=ChatResponse)
async def chat(chat_message: ChatMessage):
    """Process a chat message"""
    
    # Validate session
    session = session_manager.get_session(chat_message.session_id)
    if not session:
        raise HTTPException(status_code=404, detail="Session not found")
    
    # Validate persona
    if chat_message.persona not in personas:
        raise HTTPException(status_code=400, detail="Invalid persona")
    
    try:
        # Get persona
        persona = personas[chat_message.persona]
        
        # Detect PII
        pii_detected = anonymizer.detect_pii(chat_message.message)
        if pii_detected:
            print(f"Warning: PII detected in message: {pii_detected}")
        
        # Preprocess message
        processed_message = preprocessor.clean_text(chat_message.message)
        
        # Detect intent
        if CLASSIFIER_LOADED:
            intent, confidence = intent_classifier.predict(
                processed_message, return_confidence=True
            )
        else:
            intent, confidence = detect_intent_fallback(processed_message)
        
        # Check for crisis
        crisis_detected = persona.detect_crisis(chat_message.message)
        
        # Detect emotions
        emotions = preprocessor.detect_emotion_keywords(chat_message.message)
        
        # Generate response
        context = {
            'emotions': emotions,
            'session_history': session['conversation_history'][-5:]  # Last 5 messages
        }
        
        bot_response = persona.generate_response(
            chat_message.message,
            intent,
            confidence,
            context
        )
        
        # Add to session
        session_manager.add_message(
            chat_message.session_id,
            chat_message.message,
            bot_response,
            metadata={
                'persona': chat_message.persona,
                'intent': intent,
                'confidence': confidence,
                'crisis_detected': crisis_detected
            }
        )
        
        # Auto-save chat history
        try:
            session_data = session_manager.get_session(chat_message.session_id)
            history_file = os.path.join(CHAT_HISTORY_DIR, f"{chat_message.session_id}.json")
            with open(history_file, 'w', encoding='utf-8') as f:
                json.dump({
                    "session_id": chat_message.session_id,
                    "messages": session_data.get('conversation_history', []),
                    "created_at": session_data.get('created_at', datetime.now().isoformat()),
                    "last_updated": datetime.now().isoformat()
                }, f, indent=2)
        except Exception as save_error:
            print(f"Warning: Failed to auto-save chat history: {save_error}")
        
        return ChatResponse(
            session_id=chat_message.session_id,
            user_message=chat_message.message,
            bot_response=bot_response,
            persona=chat_message.persona,
            intent=intent,
            confidence=confidence,
            crisis_detected=crisis_detected
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/session/{session_id}", response_model=SessionInfo)
async def get_session_info(session_id: str):
    """Get session information"""
    session = session_manager.get_session(session_id)
    
    if not session:
        raise HTTPException(status_code=404, detail="Session not found")
    
    # Get most used persona
    personas_used = [msg['metadata'].get('persona', 'unknown') 
                    for msg in session['conversation_history']]
    most_used_persona = max(set(personas_used), key=personas_used.count) if personas_used else 'unknown'
    
    return SessionInfo(
        session_id=session_id,
        created_at=session['created_at'],
        message_count=len(session['conversation_history']),
        persona_used=most_used_persona
    )


@app.get("/session/{session_id}/history")
async def get_session_history(session_id: str, limit: int = 50):
    """Get conversation history"""
    session = session_manager.get_session(session_id)
    
    if not session:
        raise HTTPException(status_code=404, detail="Session not found")
    
    history = session['conversation_history'][-limit:]
    return {"history": history}


@app.delete("/session/{session_id}")
async def delete_session(session_id: str):
    """Delete a session"""
    try:
        session_manager.delete_session(session_id)
        return {"message": "Session deleted successfully"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/personas")
async def list_personas():
    """List available personas"""
    return {
        "personas": [
            {
                "name": "friend",
                "description": personas['friend'].description,
                "style": personas['friend'].get_persona_style()
            },
            {
                "name": "counselor",
                "description": personas['counselor'].description,
                "style": personas['counselor'].get_persona_style()
            },
            {
                "name": "medical_officer",
                "description": personas['medical_officer'].description,
                "style": personas['medical_officer'].get_persona_style()
            }
        ]
    }


@app.get("/history/{session_id}")
async def get_chat_history(session_id: str):
    """Get chat history for a session"""
    try:
        history_file = os.path.join(CHAT_HISTORY_DIR, f"{session_id}.json")
        
        if os.path.exists(history_file):
            with open(history_file, 'r', encoding='utf-8') as f:
                history = json.load(f)
            return {"session_id": session_id, "history": history}
        else:
            raise HTTPException(status_code=404, detail="Chat history not found")
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/history/{session_id}/save")
async def save_chat_history(session_id: str):
    """Manually save chat history for a session"""
    try:
        if session_id not in session_manager.sessions:
            raise HTTPException(status_code=404, detail="Session not found")
        
        session_data = session_manager.sessions[session_id]
        history_file = os.path.join(CHAT_HISTORY_DIR, f"{session_id}.json")
        
        with open(history_file, 'w', encoding='utf-8') as f:
            json.dump({
                "session_id": session_id,
                "messages": session_data.get('conversation_history', []),
                "created_at": session_data.get('created_at', datetime.now().isoformat()),
                "last_updated": datetime.now().isoformat()
            }, f, indent=2)
        
        return {"message": "Chat history saved successfully", "file": history_file}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/history")
async def list_all_histories():
    """List all saved chat histories"""
    try:
        history_files = [f for f in os.listdir(CHAT_HISTORY_DIR) if f.endswith('.json')]
        histories = []
        
        for history_file in history_files:
            with open(os.path.join(CHAT_HISTORY_DIR, history_file), 'r', encoding='utf-8') as f:
                data = json.load(f)
                histories.append({
                    "session_id": data.get("session_id"),
                    "message_count": len(data.get("messages", [])),
                    "created_at": data.get("created_at"),
                    "last_updated": data.get("last_updated")
                })
        
        return {"histories": histories, "count": len(histories)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# Model Endpoints
class PredictRequest(BaseModel):
    text: str
    return_confidence: bool = True


class PredictResponse(BaseModel):
    text: str
    intent: str
    confidence: float


@app.post("/model/predict", response_model=PredictResponse)
async def predict_intent(request: PredictRequest):
    """Predict intent for a given text using the trained model"""
    if not CLASSIFIER_LOADED:
        raise HTTPException(status_code=503, detail="Model not loaded")
    
    try:
        intent, confidence = intent_classifier.predict(request.text, return_confidence=True)
        
        return {
            "text": request.text,
            "intent": intent,
            "confidence": confidence
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


class BatchPredictRequest(BaseModel):
    texts: List[str]
    return_confidence: bool = True


@app.post("/model/predict/batch")
async def batch_predict_intent(request: BatchPredictRequest):
    """Predict intents for multiple texts"""
    if not CLASSIFIER_LOADED:
        raise HTTPException(status_code=503, detail="Model not loaded")
    
    try:
        predictions = []
        for text in request.texts:
            intent, confidence = intent_classifier.predict(text, return_confidence=True)
            predictions.append({
                "text": text,
                "intent": intent,
                "confidence": confidence
            })
        
        return {"predictions": predictions, "count": len(predictions)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/model/info")
async def get_model_info():
    """Get information about the loaded model"""
    if not CLASSIFIER_LOADED:
        raise HTTPException(status_code=503, detail="Model not loaded")
    
    return {
        "model_loaded": CLASSIFIER_LOADED,
        "model_name": intent_classifier.model_name,
        "max_length": intent_classifier.max_length,
        "device": str(intent_classifier.device),
        "num_classes": len(intent_classifier.label_encoder.classes_) if intent_classifier.label_encoder else 0,
        "intents": list(intent_classifier.label_encoder.classes_) if intent_classifier.label_encoder else []
    }


# Questionnaire Endpoint
@app.post("/questionnaire/submit", response_model=QuestionnaireResult)
async def submit_questionnaire(questionnaire: QuestionnaireAnswers):
    """Submit behavioral assessment questionnaire and calculate scores"""
    try:
        answers = questionnaire.answers
        
        # Calculate individual scores based on the scoring rules
        scores = {}
        
        # Q1: Work Environment (1-4 points)
        work_env_scores = {
            "High-pressure deadlines": 1,
            "Collaborative team": 3,
            "Independent focus": 2,
            "Balanced routine": 4
        }
        scores['work_environment'] = work_env_scores.get(answers.get('work_environment'), 2)
        
        # Q2: Stress Management (1-10 direct score)
        scores['stress_management'] = float(answers.get('stress_management', 5))
        
        # Q3: Self-care Frequency (1-4 points)
        selfcare_scores = {
            "Daily": 4,
            "A few times a week": 3,
            "Rarely": 2,
            "Never": 1
        }
        scores['selfcare_frequency'] = selfcare_scores.get(answers.get('selfcare_frequency'), 2)
        
        # Q4: Support Interest (1-3 points)
        support_scores = {
            "Quick tips": 2,
            "Long-term strategies": 3,
            "Professional advice": 3,
            "None right now": 1
        }
        scores['support_interest'] = support_scores.get(answers.get('support_interest'), 2)
        
        # Q5: Energy Level (1-10 direct score)
        scores['energy_level'] = float(answers.get('energy_level', 5))
        
        # Calculate total score (max: 4+10+4+3+10 = 31, but we'll normalize to 24 as implied)
        # Normalized: work(4) + stress(10) + selfcare(4) + support(3) + energy(10) = 31
        # Let's keep it as is and adjust interpretation
        total_score = sum(scores.values())
        
        # Determine category and interpretation
        if total_score >= 25:
            category = "Excellent Well-being"
            interpretation = "You demonstrate strong resilience and healthy stress management practices. Continue maintaining these positive behaviors."
        elif total_score >= 20:
            category = "Good Well-being"
            interpretation = "You're managing well overall. Consider strengthening areas with lower scores for optimal well-being."
        elif total_score >= 15:
            category = "Moderate Concern"
            interpretation = "Some areas may benefit from attention. Consider exploring the support options and self-care strategies available."
        else:
            category = "Needs Attention"
            interpretation = "Multiple areas indicate stress or burnout risk. We strongly recommend speaking with a counselor or medical professional."
        
        # Save results to file
        timestamp = datetime.now().isoformat()
        result_data = {
            "session_id": questionnaire.session_id,
            "timestamp": timestamp,
            "answers": answers,
            "individual_scores": scores,
            "total_score": total_score,
            "category": category,
            "interpretation": interpretation
        }
        
        # Save as JSON
        filename = f"{questionnaire.session_id}_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
        filepath = os.path.join(QUESTIONNAIRE_RESULTS_DIR, filename)
        
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(result_data, f, indent=2)
        
        # Also save as CSV for easier analysis
        csv_filename = f"{questionnaire.session_id}_{datetime.now().strftime('%Y%m%d_%H%M%S')}.csv"
        csv_filepath = os.path.join(QUESTIONNAIRE_RESULTS_DIR, csv_filename)
        
        import csv
        with open(csv_filepath, 'w', newline='', encoding='utf-8') as f:
            writer = csv.writer(f)
            writer.writerow(['Metric', 'Value/Score'])
            writer.writerow(['Session ID', questionnaire.session_id])
            writer.writerow(['Timestamp', timestamp])
            writer.writerow(['Work Environment', answers.get('work_environment')])
            writer.writerow(['Work Environment Score', scores['work_environment']])
            writer.writerow(['Stress Management (1-10)', answers.get('stress_management')])
            writer.writerow(['Stress Management Score', scores['stress_management']])
            writer.writerow(['Self-care Frequency', answers.get('selfcare_frequency')])
            writer.writerow(['Self-care Score', scores['selfcare_frequency']])
            writer.writerow(['Support Interest', answers.get('support_interest')])
            writer.writerow(['Support Score', scores['support_interest']])
            writer.writerow(['Energy Level (1-10)', answers.get('energy_level')])
            writer.writerow(['Energy Level Score', scores['energy_level']])
            writer.writerow(['Total Score', total_score])
            writer.writerow(['Category', category])
            writer.writerow(['Interpretation', interpretation])
        
        return QuestionnaireResult(
            session_id=questionnaire.session_id,
            total_score=total_score,
            category=category,
            interpretation=interpretation,
            individual_scores=scores,
            file_path=filepath,
            timestamp=timestamp
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/statistics")
async def get_statistics():
    """Get aggregated statistics (with differential privacy)"""
    try:
        stats = session_manager.export_aggregated_data(dp_mechanism)
        return {"statistics": stats, "privacy": "differential privacy applied"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "classifier_loaded": CLASSIFIER_LOADED,
        "personas_available": len(personas),
        "active_sessions": len(session_manager.sessions)
    }


if __name__ == "__main__":
    import uvicorn
    
    print("\n" + "="*70)
    print(" EMPATHETIC CONVERSATIONAL SUPPORT SYSTEM - BACKEND API ")
    print("="*70)
    print("\nStarting server...")
    print("API Documentation: http://localhost:8000/docs")
    print("="*70 + "\n")
    
    uvicorn.run(app, host="0.0.0.0", port=8000, log_level="info")
