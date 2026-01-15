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
            'greeting': [
                "Hello. I'm here to support you. What brings you here today?",
                "Good to see you. How have you been feeling?",
                "Welcome. What would you like to discuss today?"
            ],
            'sad': [
                "I hear that you're feeling sad. Sadness is a normal emotion, but when it persists, "
                "it's important to address it. Can you tell me more about what's contributing to these feelings?",
                
                "Thank you for sharing that you're feeling sad. Let's explore this together. "
                "When did you first start noticing these feelings?",
                
                "I appreciate you opening up about your sadness. It takes courage to acknowledge "
                "difficult emotions. What do you think might help you feel better?"
            ],
            'depression': [
                "Depression can feel overwhelming, but it is treatable. You've taken an important "
                "first step by reaching out. Have you been able to maintain your daily routines?",
                
                "I understand that you're experiencing depression. This is a serious condition, "
                "and I encourage you to speak with a healthcare provider. In the meantime, "
                "let's discuss some coping strategies.",
                
                "Depression affects many aspects of life. " + self.get_cbt_technique('behavioral_activation')
            ],
            'anxiety': [
                "Anxiety can be very distressing. Let's work on some grounding techniques. " +
                self.get_cbt_technique('mindfulness'),
                
                "I hear that you're feeling anxious. " + self.get_cbt_technique('thought_challenging'),
                
                "Anxiety often involves worrying about future events. Let's focus on what's "
                "within your control right now. What's one thing you can control in this moment?"
            ],
            'stress': [
                "It's understandable to feel stressed in a demanding environment. Based on privacy-protected simulations, quick daily practices like mindfulness can help. What parts of your day feel most overwhelming?",
                
                "Stress is your body's response to demands. Based on privacy-protected data patterns, breaking tasks into smaller steps often reduces overwhelm. Let's identify your main stressors and develop a plan. What feels most overwhelming right now?",
                
                "I understand stress can be intense. Privacy-protected case studies suggest that structured breaks and boundary-setting can significantly reduce stress levels. What strategies have you tried so far?",
                
                "Chronic stress can impact your health. It's important to develop healthy coping mechanisms. Have you tried any stress-management techniques before?",
                
                "Stress management is a skill we can develop together. Let's start by identifying specific stressors and creating an action plan."
            ],
            'work_stress': [
                "It's understandable to feel stressed in a demanding work environment. Based on privacy-protected simulations, quick daily practices like mindfulness can help. What parts of your day feel most overwhelming?",
                
                "Work-related stress is very common, especially in demanding professions. Privacy-protected research suggests that setting boundaries and taking micro-breaks can help. What does your typical workday look like?",
                
                "I hear the work pressure is really affecting you. Based on privacy-protected case studies, time management and stress-reduction techniques can make a significant difference. Would you like to explore some specific strategies?",
                
                "Work stress can accumulate over time. Let's break this down - what specific aspects of work are most stressful? We can develop targeted coping strategies for each.",
                
                "Many professionals experience this. Privacy-protected data suggests that work-life balance and self-care practices are crucial. What does relaxation look like for you?"
            ],
            'burnout': [
                "Burnout is a serious concern. Based on privacy-protected simulations, establishing boundaries and incorporating small relaxation practices can gradually help. What's making it difficult for you to relax?",
                
                "I hear that you're experiencing burnout. Privacy-protected research shows that structured self-care and professional support are key. Have you been able to identify what activities used to help you unwind?",
                
                "Burnout requires intentional recovery. Let's explore what sustainable changes you can make to your routine. What does a typical day look like for you right now?"
            ],
            'coping_strategies': [
                "There are many evidence-based coping strategies we can explore. Some effective ones include mindfulness, progressive muscle relaxation, and cognitive restructuring. Which of these interests you?",
                
                "Let's develop a personalized toolkit of coping strategies. What has worked for you in the past? What would you like to try?",
                
                "For relaxation, I often recommend: deep breathing exercises, progressive muscle relaxation, mindfulness meditation, gentle exercise like walking, or creative activities. What appeals to you?"
            ],
            'worthless': [
                "Feelings of worthlessness are often a symptom of depression. " +
                self.get_cbt_technique('cognitive_restructuring'),
                
                "These feelings are real, but they don't reflect reality. Let's examine the "
                "evidence. What are three things you've accomplished recently, even small things?",
                
                "Worthlessness is a feeling, not a fact. Your worth is inherent, not based on "
                "achievements or others' opinions. Let's explore where these thoughts come from."
            ],
            'sleep_problems': [
                "Sleep difficulties are common with stress and mental health challenges. "
                "Let's discuss sleep hygiene practices that might help, such as maintaining a consistent schedule and creating a relaxing bedtime routine.",
                
                "Poor sleep can worsen mental health symptoms, and mental health issues can disrupt sleep. "
                "It's a cycle we can work to break. Tell me about your current bedtime routine and sleep environment."
            ],
            'help': [
                "I'm here to help you explore your thoughts and feelings, and develop coping strategies. "
                "What specific area would you like to focus on today?",
                
                "I can provide support and evidence-based coping techniques. However, for diagnosis "
                "and medication, please consult a licensed mental health professional. What brings you here?"
            ],
            'therapy': [
                "Seeking professional therapy is an excellent step. I can offer support and coping strategies, but a licensed therapist can provide comprehensive treatment. What specific concerns would you like to address?",
                
                "Professional therapy can be very beneficial. In the meantime, let's work on some coping strategies you can use. What's your main concern right now?"
            ]
        }
        
        # Generate response
        if intent in counselor_responses:
            response = random.choice(counselor_responses[intent])
        else:
            # Improved generic therapeutic responses based on common patterns
            if any(word in user_input.lower() for word in ['work', 'job', 'workplace', 'office', 'career']):
                response = random.choice([
                    "Work-related challenges can significantly impact our well-being. What specific aspects of work are most difficult for you right now?",
                    "I understand workplace stress can be overwhelming. Let's explore what's happening and develop some coping strategies. What's your biggest concern?",
                    "Many people struggle with work-related stress. Based on privacy-protected patterns, targeted strategies can help. Tell me more about what's going on."
                ])
            elif any(word in user_input.lower() for word in ['relax', 'calm', 'unwind', 'de-stress']):
                response = random.choice([
                    "Finding ways to relax is important. Some evidence-based techniques include: deep breathing, progressive muscle relaxation, mindfulness meditation, and engaging in enjoyable activities. What sounds appealing to you?",
                    "Relaxation is a skill we can develop. I can guide you through several techniques - breathing exercises, body scanning, or guided imagery. Which would you like to try?",
                    "There are many relaxation strategies we can explore. Based on privacy-protected research, regular practice of even 5-10 minutes daily can make a difference. What have you tried before?"
                ])
            elif any(word in user_input.lower() for word in ['stress', 'stressed', 'overwhelmed', 'pressure']):
                response = random.choice([
                    "Stress can feel very overwhelming. Let's break this down into manageable pieces. What's causing the most stress right now?",
                    "I hear that you're feeling stressed. Based on privacy-protected case studies, identifying specific stressors and addressing them one at a time can help. Where shall we start?",
                    "Stress affects us all differently. What symptoms are you noticing? Understanding your stress response helps us develop targeted strategies."
                ])
            else:
                response = random.choice([
                    "I'm listening. Can you tell me more about what you're experiencing?",
                    "Thank you for sharing. How long have you been dealing with this?",
                    "What have you tried so far to address this situation?",
                    "How is this affecting your daily life and well-being?",
                    "I'd like to understand this better. Can you describe what a typical day looks like for you?"
                ])
        
        # Add video recommendations for specific intents
        if intent in ['anxiety', 'depression', 'stress', 'work_stress', 'sleep_problems']:
            topic_map = {
                'anxiety': 'anxiety',
                'depression': 'depression',
                'stress': 'stress',
                'work_stress': 'stress',
                'sleep_problems': 'sleep'
            }
            videos = self.suggest_videos(topic_map.get(intent, 'general'))
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
