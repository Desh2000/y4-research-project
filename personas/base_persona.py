"""
Base Persona Class
Defines the base class for all personas (Friend, Counselor, Doctor)
"""

from abc import ABC, abstractmethod
from typing import Dict, List, Optional
import random


class BasePersona(ABC):
    """Abstract base class for all personas"""
    
    def __init__(self, name: str, description: str):
        self.name = name
        self.description = description
        self.conversation_history = []
        self.emotional_state_tracker = {}
    
    @abstractmethod
    def generate_greeting(self) -> str:
        """Generate a greeting message"""
        pass
    
    @abstractmethod
    def generate_response(self, user_input: str, intent: str, 
                         confidence: float, context: Optional[Dict] = None) -> str:
        """Generate a response based on user input and intent"""
        pass
    
    @abstractmethod
    def get_persona_style(self) -> Dict:
        """Get the persona's communication style parameters"""
        pass
    
    def update_emotional_state(self, emotion: str, intensity: float):
        """Track user's emotional state"""
        self.emotional_state_tracker[emotion] = intensity
    
    def get_emotional_state(self) -> Dict:
        """Get current emotional state tracking"""
        return self.emotional_state_tracker
    
    def add_to_history(self, user_input: str, bot_response: str):
        """Add interaction to conversation history"""
        self.conversation_history.append({
            'user': user_input,
            'bot': bot_response,
            'persona': self.name
        })
    
    def get_history(self) -> List[Dict]:
        """Get conversation history"""
        return self.conversation_history
    
    def clear_history(self):
        """Clear conversation history"""
        self.conversation_history = []
        self.emotional_state_tracker = {}
    
    def detect_crisis(self, user_input: str) -> bool:
        """Detect if user is in crisis (suicidal thoughts, severe distress)"""
        crisis_keywords = [
            'suicide', 'kill myself', 'end my life', 'die', 'death wish',
            'not worth living', 'end it all', 'harm myself'
        ]
        user_input_lower = user_input.lower()
        return any(keyword in user_input_lower for keyword in crisis_keywords)
    
    def get_crisis_response(self) -> str:
        """Get immediate crisis response"""
        return ("I'm very concerned about what you're sharing. Please reach out to a crisis "
                "helpline immediately. National Suicide Prevention Lifeline: 988 (US) or "
                "1-800-273-8255. You can also text 'HELLO' to 741741. Your life matters, "
                "and there are people who want to help you right now.")


class FriendPersona(BasePersona):
    """
    Friend Persona: Casual, supportive, and emotionally warm
    Focus: Emotional support, active listening, encouragement
    """
    
    def __init__(self):
        super().__init__(
            name="Friend",
            description="A supportive friend who listens and provides emotional comfort"
        )
        
        self.greetings = [
            "Hey there! How are you doing today? 😊",
            "Hi friend! What's on your mind?",
            "Hello! I'm here to listen. How are you feeling?",
            "Hey! Good to see you. How has your day been?",
            "Hi there! Want to talk about what's going on?"
        ]
        
        self.style_params = {
            'formality': 'casual',
            'empathy_level': 'high',
            'use_emojis': True,
            'tone': 'warm and friendly'
        }
    
    def generate_greeting(self) -> str:
        return random.choice(self.greetings)
    
    def generate_response(self, user_input: str, intent: str, 
                         confidence: float, context: Optional[Dict] = None) -> str:
        # Check for crisis
        if self.detect_crisis(user_input):
            return self.get_crisis_response()
        
        user_lower = user_input.lower()
        
        # Check for availability/time questions
        if any(word in user_lower for word in ['free', 'available', 'time to talk', 'talk now', 'can we talk']):
            return random.choice([
                "Of course! I'm here for you right now. What's been going on? Take your time. 😊",
                "Yes, I'm totally free! I'm all ears. Tell me what's on your mind.",
                "Absolutely! I'm here and ready to listen. What would you like to talk about?",
                "I'm here for you whenever you need me! Let's talk - what's happening?"
            ])
        
        # Check for affirmation/wanting to talk
        if any(word in user_lower for word in ['yes', 'yeah', 'okay', 'sure', 'want to talk', 'need to talk']):
            if len(self.conversation_history) > 0:  # If there's conversation context
                return random.choice([
                    "I'm listening. Take your time and share whatever feels comfortable. What's going on?",
                    "I'm here with you. What would you like to talk about?",
                    "Okay, I'm ready to listen. Tell me more - what's been happening?",
                    "I appreciate you opening up. What's been on your mind lately?",
                    "Thank you for trusting me. Let's talk through this together. What's bothering you?"
                ])
        
        # Check for stress-related keywords
        if any(word in user_lower for word in ['stress', 'overwhelm', 'pressure', 'too much', 'burden', 'exhausted']):
            return random.choice([
                "That sounds really overwhelming. I'm here for you. Can you tell me more about what's stressing you out? 💙",
                "Stress can be so draining. What specifically has been weighing on you? Let's talk through it.",
                "I hear you - that sounds like a lot to carry. What's been the biggest stressor for you?",
                "Feeling stressed is tough. I'm here to listen. What's causing the most pressure right now?",
                "That must feel exhausting. Want to tell me more about what's making you feel this way?"
            ])
        
        # Check for uncertainty/confusion
        if any(word in user_lower for word in ["don't know", 'not sure', 'confused', 'uncertain', 'lost']):
            return random.choice([
                "It's okay not to have all the answers right now. Sometimes just talking helps clarify things. What's going through your mind?",
                "That's completely normal - sometimes we need to talk things out to figure them out. I'm here to listen.",
                "You don't need to have it all figured out. Let's just chat and see where it goes. What's been happening?",
                "It's alright to feel uncertain. I'm here to help you work through it. Tell me what you're feeling."
            ])
        
        # Check for emotional distress
        if any(word in user_lower for word in ['not well', 'feel bad', 'feel terrible', 'awful', 'horrible']):
            return random.choice([
                "I'm really sorry you're feeling this way. You deserve support. What's been making you feel not well?",
                "That sounds really hard. I'm here for you. Can you tell me more about what's going on?",
                "I hear you, and I'm concerned. What's been happening that's making you feel this way?",
                "Thank you for sharing that with me. You're not alone. What's been troubling you?"
            ])
        
        # Intent-based responses
        friend_responses = {
            'sad': [
                "I'm really sorry you're feeling this way. Want to talk about it? I'm here for you. 💙",
                "That sounds really tough. I'm here to listen, no judgment. What's been going on?",
                "I can hear that you're going through a hard time. You don't have to face this alone."
            ],
            'stressed': [
                "Wow, that sounds overwhelming. Take a deep breath with me. Want to talk through it?",
                "Stress is so tough. What's been weighing on you the most?",
                "I hear you. Sometimes everything feels like too much. Let's break it down together."
            ],
            'anxious': [
                "Anxiety can feel so scary. I'm here with you. What's making you feel anxious?",
                "Those anxious feelings are real, and they're valid. Want to share what's on your mind?",
                "I understand how unsettling anxiety can be. You're not alone in this."
            ],
            'happy': [
                "That's awesome! I'm so glad you're feeling good! 😊",
                "Yay! I love hearing that! What's making you happy?",
                "That's wonderful! Tell me more about what's going well!"
            ],
            'thanks': [
                "Of course! That's what friends are for! 💙",
                "Anytime! I'm always here when you need someone to talk to.",
                "You're so welcome! I'm glad I could help."
            ],
            'goodbye': [
                "Take care of yourself! I'm here whenever you need me. 💙",
                "See you soon! Remember, I'm just a message away.",
                "Bye for now! Hope things get better. Talk soon!"
            ],
            'greeting': [
                "Hey! Good to hear from you! How are you doing today? 😊",
                "Hi there! What's been going on with you?",
                "Hello! I'm here for you. What's on your mind?"
            ]
        }
        
        if intent in friend_responses:
            response = random.choice(friend_responses[intent])
        else:
            # Contextual generic responses
            if len(self.conversation_history) > 2:
                # After several exchanges, show deeper engagement
                generic = [
                    "I appreciate you sharing this with me. How has this been affecting you?",
                    "You've been through a lot. How are you holding up with everything?",
                    "I want to make sure I understand - what's the hardest part about this for you?",
                    "That's a lot to process. How are you taking care of yourself through this?"
                ]
            else:
                # Early conversation - open-ended and supportive
                generic = [
                    "I hear you. Tell me more - what else has been on your mind?",
                    "Thanks for opening up. What would help you feel better right now?",
                    "I'm here to listen. Can you tell me more about what's going on?",
                    "That sounds important to you. Help me understand - what's the situation?"
                ]
            response = random.choice(generic)
        
        self.add_to_history(user_input, response)
        return response
    
    def get_persona_style(self) -> Dict:
        return self.style_params


if __name__ == "__main__":
    # Test Friend Persona
    friend = FriendPersona()
    print(f"Persona: {friend.name}")
    print(f"Greeting: {friend.generate_greeting()}")
    print(f"Style: {friend.get_persona_style()}")
