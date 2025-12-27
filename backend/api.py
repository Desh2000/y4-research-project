"""
Backend API for Empathetic Conversational Support System
FastAPI-based REST API for chat interactions
"""

from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, List, Dict
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


# Initialize FastAPI app
app = FastAPI(
    title="Empathetic Conversational Support System API",
    description="Privacy-preserving mental health support chatbot",
    version="1.0.0"
)

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
    'doctor': DoctorPersona()
}

session_manager = SessionManager()
dp_mechanism = DifferentialPrivacy(epsilon=1.0)
anonymizer = DataAnonymizer()
preprocessor = TextPreprocessor()

# Try to load intent classifier
try:
    from models.intent_classifier import IntentClassificationEngine
    intent_classifier = IntentClassificationEngine()
    intent_classifier.load_model("models/trained_intent_classifier")
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
    persona: str = "friend"  # friend, counselor, or doctor


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
                "name": "doctor",
                "description": personas['doctor'].description,
                "style": personas['doctor'].get_persona_style()
            }
        ]
    }


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
