"""
Counselor Persona
Professional therapeutic support with evidence-based techniques
"""

from personas.base_persona import BasePersona
from typing import Dict, List, Optional
import random


class CounselorPersona(BasePersona):
    """
    Counselor Persona: Professional, therapeutic, and solution-focused
    Focus: Cognitive-behavioral techniques, coping strategies, resource recommendations
    """
    
    def __init__(self):
        super().__init__(
            name="Counselor",
            description="A professional counselor providing therapeutic support and coping strategies"
        )
        
        self.greetings = [
            "Hello, welcome. I'm here to support you. What brings you here today?",
            "Good to see you. How have you been feeling since we last talked?",
            "Welcome back. What would you like to work on today?",
            "Hello. I'm glad you're here. What's been on your mind lately?",
            "Hi there. Let's take some time to talk about how you're doing."
        ]
        
        self.style_params = {
            'formality': 'professional',
            'empathy_level': 'high',
            'use_emojis': False,
            'tone': 'therapeutic and supportive'
        }
        
        # Video recommendations database (can be expanded)
        self.video_resources = {
            'anxiety': [
                {
                    'title': 'Understanding and Managing Anxiety',
                    'url': 'https://youtube.com/watch?v=example1',
                    'duration': '15 min',
                    'description': 'Learn practical techniques to manage anxiety symptoms'
                },
                {
                    'title': 'Breathing Exercises for Anxiety Relief',
                    'url': 'https://youtube.com/watch?v=example2',
                    'duration': '10 min',
                    'description': 'Guided breathing exercises to reduce anxiety'
                }
            ],
            'depression': [
                {
                    'title': 'Understanding Depression: A Clinical Perspective',
                    'url': 'https://youtube.com/watch?v=example3',
                    'duration': '20 min',
                    'description': 'Educational video about depression and treatment options'
                },
                {
                    'title': 'Behavioral Activation for Depression',
                    'url': 'https://youtube.com/watch?v=example4',
                    'duration': '12 min',
                    'description': 'Learn how activity can help lift your mood'
                }
            ],
            'stress': [
                {
                    'title': 'Stress Management Techniques',
                    'url': 'https://youtube.com/watch?v=example5',
                    'duration': '18 min',
                    'description': 'Evidence-based stress reduction strategies'
                },
                {
                    'title': 'Mindfulness for Stress Relief',
                    'url': 'https://youtube.com/watch?v=example6',
                    'duration': '15 min',
                    'description': 'Mindfulness practices to manage stress'
                }
            ],
            'sleep': [
                {
                    'title': 'Sleep Hygiene: Better Sleep Habits',
                    'url': 'https://youtube.com/watch?v=example7',
                    'duration': '14 min',
                    'description': 'Improve your sleep quality with these techniques'
                }
            ],
            'general': [
                {
                    'title': 'Building Resilience and Mental Wellness',
                    'url': 'https://youtube.com/watch?v=example8',
                    'duration': '22 min',
                    'description': 'Strategies for building mental resilience'
                }
            ]
        }
        
        # CBT techniques
        self.cbt_techniques = {
            'cognitive_restructuring': (
                "Let's explore the thoughts behind these feelings. What thoughts "
                "come up when you feel this way? Sometimes our thoughts can be more "
                "negative than the situation warrants."
            ),
            'behavioral_activation': (
                "When we're struggling, it helps to engage in activities that bring us "
                "a sense of accomplishment or pleasure. What's one small activity you "
                "could do today?"
            ),
            'mindfulness': (
                "Let's practice being present. Take a moment to notice five things you "
                "can see, four you can touch, three you can hear, two you can smell, "
                "and one you can taste."
            ),
            'thought_challenging': (
                "Let's examine that thought. What evidence supports it? What evidence "
                "contradicts it? Is there another way to look at this situation?"
            )
        }
    
    def generate_greeting(self) -> str:
        return random.choice(self.greetings)
    
    def suggest_videos(self, topic: str) -> List[Dict]:
        """
        Suggest relevant video resources based on topic
        
        Args:
            topic: Mental health topic (anxiety, depression, stress, etc.)
        
        Returns:
            List of video recommendations
        """
        topic_lower = topic.lower()
        
        # Find matching videos
        if topic_lower in self.video_resources:
            return self.video_resources[topic_lower]
        
        # Return general resources if no specific match
        return self.video_resources['general']
    
    def format_video_recommendations(self, videos: List[Dict]) -> str:
        """Format video recommendations as a string"""
        if not videos:
            return ""
        
        message = "\n\nI'd like to recommend some helpful resources:\n"
        for i, video in enumerate(videos, 1):
            message += f"\n{i}. {video['title']} ({video['duration']})"
            message += f"\n   {video['description']}"
        
        message += "\n\nWould you like to explore any of these resources?"
        return message
    
    def get_cbt_technique(self, technique: str) -> str:
        """Get a specific CBT technique suggestion"""
        return self.cbt_techniques.get(technique, "")
    
    def generate_response(self, user_input: str, intent: str, 
                         confidence: float, context: Optional[Dict] = None) -> str:
        # Check for crisis
        if self.detect_crisis(user_input):
            crisis_response = self.get_crisis_response()
            crisis_response += "\n\nI'm a chatbot and cannot provide emergency support. " \
                             "Please contact emergency services or a crisis helpline immediately."
            return crisis_response
        
        # Intent-based therapeutic responses
        counselor_responses = {
            'sad': [
                "I hear that you're feeling sad. Sadness is a normal emotion, but when it persists, "
                "it's important to address it. Can you tell me more about what's contributing to these feelings?",
                
                "Thank you for sharing that you're feeling sad. Let's explore this together. "
                "When did you first start noticing these feelings?",
                
                "I appreciate you opening up about your sadness. It takes courage to acknowledge "
                "difficult emotions. What do you think might help you feel better?"
            ],
            'depressed': [
                "Depression can feel overwhelming, but it is treatable. You've taken an important "
                "first step by reaching out. Have you been able to maintain your daily routines?",
                
                "I understand that you're experiencing depression. This is a serious condition, "
                "and I encourage you to speak with a healthcare provider. In the meantime, "
                "let's discuss some coping strategies.",
                
                "Depression affects many aspects of life. " + self.get_cbt_technique('behavioral_activation')
            ],
            'anxious': [
                "Anxiety can be very distressing. Let's work on some grounding techniques. " +
                self.get_cbt_technique('mindfulness'),
                
                "I hear that you're feeling anxious. " + self.get_cbt_technique('thought_challenging'),
                
                "Anxiety often involves worrying about future events. Let's focus on what's "
                "within your control right now. What's one thing you can control in this moment?"
            ],
            'stressed': [
                "Stress is your body's response to demands. Let's identify your main stressors "
                "and develop a plan to manage them. What feels most overwhelming right now?",
                
                "Chronic stress can impact your health. It's important to develop healthy coping "
                "mechanisms. Have you tried any stress-management techniques before?",
                
                "Stress management is a skill we can develop together. Let's start by breaking down "
                "the problem into smaller, manageable pieces."
            ],
            'worthless': [
                "Feelings of worthlessness are often a symptom of depression. " +
                self.get_cbt_technique('cognitive_restructuring'),
                
                "These feelings are real, but they don't reflect reality. Let's examine the "
                "evidence. What are three things you've accomplished recently, even small things?",
                
                "Worthlessness is a feeling, not a fact. Your worth is inherent, not based on "
                "achievements or others' opinions. Let's explore where these thoughts come from."
            ],
            'sleep': [
                "Sleep difficulties are common with stress and mental health challenges. "
                "Let's discuss sleep hygiene practices that might help.",
                
                "Poor sleep can worsen mental health symptoms, and mental health issues can disrupt sleep. "
                "It's a cycle we can work to break. Tell me about your bedtime routine."
            ],
            'help': [
                "I'm here to help you explore your thoughts and feelings, and develop coping strategies. "
                "What specific area would you like to focus on today?",
                
                "I can provide support and evidence-based coping techniques. However, for diagnosis "
                "and medication, please consult a licensed mental health professional."
            ]
        }
        
        # Generate response
        if intent in counselor_responses:
            response = random.choice(counselor_responses[intent])
        else:
            # Generic therapeutic responses
            generic = [
                "I'm listening. Can you tell me more about that?",
                "How long have you been experiencing this?",
                "What have you tried so far to address this?",
                "How is this affecting your daily life?",
                "What would it look like if this improved? What would be different?"
            ]
            response = random.choice(generic)
        
        # Add video recommendations for specific intents
        if intent in ['anxious', 'depressed', 'stressed', 'sleep']:
            videos = self.suggest_videos(intent)
            if videos:
                response += self.format_video_recommendations(videos[:2])  # Show top 2
        
        self.add_to_history(user_input, response)
        return response
    
    def get_persona_style(self) -> Dict:
        return self.style_params


if __name__ == "__main__":
    # Test Counselor Persona
    counselor = CounselorPersona()
    print(f"Persona: {counselor.name}")
    print(f"Greeting: {counselor.generate_greeting()}")
    
    # Test video recommendations
    videos = counselor.suggest_videos('anxiety')
    print(f"\nVideo recommendations for anxiety:")
    print(counselor.format_video_recommendations(videos))
