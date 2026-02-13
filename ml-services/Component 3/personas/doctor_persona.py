"""
Doctor Persona
Clinical, informational, and medically-oriented support
"""

from personas.base_persona import BasePersona
from typing import Dict, List, Optional
import random


class DoctorPersona(BasePersona):
    """
    Medical Officer Persona: Clinical, informational, medically-oriented
    Focus: Mental health education, symptoms, treatment options, medical guidance
    """
    
    def __init__(self):
        super().__init__(
            name="Medical Officer",
            description="A medical officer providing clinical information and guidance"
        )
        
        self.greetings = [
            "Hello, I'm here to provide information about mental health. How can I assist you today?",
            "Good day. What mental health questions do you have for me?",
            "Hello. I can provide clinical information about mental health conditions. What would you like to know?",
            "Welcome. I'm here to help you understand mental health from a clinical perspective. What brings you here?",
            "Hello. Let's discuss your mental health concerns from a medical standpoint."
        ]
        
        self.style_params = {
            'formality': 'very_professional',
            'empathy_level': 'moderate',
            'use_emojis': False,
            'tone': 'clinical and informative'
        }
        
        # Clinical knowledge base
        self.mental_health_info = {
            'depression': {
                'definition': "Depression is a mood disorder characterized by persistent feelings of sadness, "
                             "hopelessness, and loss of interest in activities. It affects how you feel, think, "
                             "and handle daily activities.",
                'symptoms': [
                    "Persistent sad, anxious, or empty mood",
                    "Loss of interest in activities once enjoyed",
                    "Changes in appetite and weight",
                    "Sleep disturbances (insomnia or oversleeping)",
                    "Fatigue and decreased energy",
                    "Feelings of worthlessness or guilt",
                    "Difficulty concentrating or making decisions",
                    "Thoughts of death or suicide"
                ],
                'treatments': [
                    "Psychotherapy (CBT, interpersonal therapy)",
                    "Antidepressant medications (SSRIs, SNRIs)",
                    "Combination of therapy and medication",
                    "Lifestyle modifications (exercise, sleep hygiene)",
                    "In severe cases: ECT or TMS"
                ],
                'when_to_seek_help': "Seek immediate help if symptoms persist for more than two weeks, "
                                    "interfere with daily functioning, or if you have thoughts of self-harm."
            },
            'anxiety': {
                'definition': "Anxiety disorders involve excessive fear or worry that interferes with daily "
                             "activities. Types include generalized anxiety disorder, panic disorder, and "
                             "specific phobias.",
                'symptoms': [
                    "Excessive worrying",
                    "Restlessness or feeling on edge",
                    "Difficulty concentrating",
                    "Muscle tension",
                    "Sleep disturbances",
                    "Panic attacks (in panic disorder)",
                    "Avoidance behaviors"
                ],
                'treatments': [
                    "Cognitive-behavioral therapy (CBT)",
                    "Exposure therapy",
                    "Anti-anxiety medications (SSRIs, benzodiazepines)",
                    "Relaxation techniques and mindfulness",
                    "Lifestyle changes (reducing caffeine, regular exercise)"
                ],
                'when_to_seek_help': "Consult a healthcare provider if anxiety interferes with work, "
                                    "relationships, or daily activities, or if you experience panic attacks."
            },
            'stress': {
                'definition': "Stress is the body's response to challenges or demands. While acute stress is "
                             "normal, chronic stress can lead to physical and mental health problems.",
                'symptoms': [
                    "Headaches",
                    "Muscle tension or pain",
                    "Fatigue",
                    "Changes in sex drive",
                    "Upset stomach",
                    "Sleep problems",
                    "Anxiety and restlessness",
                    "Irritability or anger"
                ],
                'treatments': [
                    "Stress management techniques",
                    "Time management and organization",
                    "Regular physical activity",
                    "Adequate sleep",
                    "Social support",
                    "Professional counseling if needed",
                    "Relaxation techniques (meditation, deep breathing)"
                ],
                'when_to_seek_help': "Seek help if stress is chronic, overwhelming, or leading to unhealthy "
                                    "coping mechanisms like substance abuse."
            },
            'burnout': {
                'definition': "Burnout is a state of emotional, physical, and mental exhaustion caused by "
                             "prolonged stress, often work-related. It's particularly common in STEM and "
                             "high-pressure professions.",
                'symptoms': [
                    "Chronic fatigue and exhaustion",
                    "Cynicism and detachment",
                    "Reduced professional efficacy",
                    "Physical symptoms (headaches, GI problems)",
                    "Emotional symptoms (irritability, depression)",
                    "Cognitive impairment (poor concentration)"
                ],
                'treatments': [
                    "Workload management and boundary setting",
                    "Professional counseling",
                    "Stress management techniques",
                    "Career counseling or job change if necessary",
                    "Self-care and work-life balance",
                    "Medical treatment if physical symptoms present"
                ],
                'when_to_seek_help': "Seek help when burnout symptoms persist despite self-care efforts or "
                                    "when it affects your health, relationships, or job performance."
            }
        }
        
        # Treatment options
        self.treatment_info = {
            'therapy_types': {
                'CBT': "Cognitive-Behavioral Therapy focuses on identifying and changing negative thought "
                       "patterns and behaviors.",
                'DBT': "Dialectical Behavior Therapy combines CBT with mindfulness, useful for emotional "
                       "regulation.",
                'Psychodynamic': "Explores how unconscious thoughts from past experiences affect current behavior.",
                'Interpersonal': "Focuses on improving relationship patterns that may contribute to mental health issues."
            },
            'medication_types': {
                'SSRIs': "Selective Serotonin Reuptake Inhibitors (e.g., Prozac, Zoloft) are commonly used "
                        "for depression and anxiety.",
                'SNRIs': "Serotonin-Norepinephrine Reuptake Inhibitors (e.g., Effexor, Cymbalta) treat "
                        "depression and anxiety.",
                'Benzodiazepines': "Used for short-term anxiety relief but can be habit-forming.",
                'Antipsychotics': "Used for severe mental health conditions like schizophrenia or bipolar disorder."
            }
        }
    
    def generate_greeting(self) -> str:
        return random.choice(self.greetings)
    
    def provide_clinical_info(self, condition: str) -> str:
        """
        Provide clinical information about a mental health condition
        
        Args:
            condition: Mental health condition name
        
        Returns:
            Formatted clinical information
        """
        condition_lower = condition.lower()
        
        if condition_lower not in self.mental_health_info:
            return "I don't have specific information about that condition. Please consult a healthcare provider."
        
        info = self.mental_health_info[condition_lower]
        
        response = f"\n**{condition.upper()}**\n\n"
        response += f"**Definition:** {info['definition']}\n\n"
        
        response += "**Common Symptoms:**\n"
        for symptom in info['symptoms']:
            response += f"• {symptom}\n"
        
        response += "\n**Treatment Options:**\n"
        for treatment in info['treatments']:
            response += f"• {treatment}\n"
        
        response += f"\n**When to Seek Help:** {info['when_to_seek_help']}\n"
        
        return response
    
    def explain_treatment(self, treatment_type: str, category: str = 'therapy_types') -> str:
        """Explain a specific treatment option"""
        if category in self.treatment_info:
            treatments = self.treatment_info[category]
            return treatments.get(treatment_type, "I don't have information about that treatment.")
        return "Invalid category."
    
    def assess_severity(self, symptoms: List[str]) -> Dict:
        """
        Assess symptom severity (simplified)
        NOTE: This is for educational purposes only, not a clinical diagnosis
        """
        severity_indicators = {
            'severe': ['suicide', 'self-harm', 'psychosis', 'hallucinations', 'severe impairment'],
            'moderate': ['persistent symptoms', 'functional impairment', 'chronic', 'daily impact'],
            'mild': ['occasional', 'manageable', 'mild discomfort']
        }
        
        # This is a simplified assessment
        assessment = {
            'severity': 'unknown',
            'recommendation': '',
            'urgency': 'routine'
        }
        
        symptoms_lower = [s.lower() for s in symptoms]
        
        for symptom in symptoms_lower:
            if any(indicator in symptom for indicator in severity_indicators['severe']):
                assessment['severity'] = 'severe'
                assessment['urgency'] = 'immediate'
                assessment['recommendation'] = "Seek immediate professional help. Contact emergency services or a crisis hotline."
                return assessment
        
        assessment['severity'] = 'moderate'
        assessment['recommendation'] = "Schedule an appointment with a mental health professional."
        assessment['urgency'] = 'soon'
        
        return assessment
    
    def generate_response(self, user_input: str, intent: str, 
                         confidence: float, context: Optional[Dict] = None) -> str:
        # Check for crisis
        if self.detect_crisis(user_input):
            crisis_response = self.get_crisis_response()
            crisis_response += "\n\n**This is a medical emergency.** I am a chatbot and cannot provide " \
                             "emergency care. Please contact emergency services (911 in US) or go to " \
                             "the nearest emergency room immediately."
            return crisis_response
        
        # Intent-based clinical responses
        doctor_responses = {
            'fact-1': [
                "Mental health refers to cognitive, behavioral, and emotional well-being. It encompasses "
                "how we think, feel, and act. Good mental health enables people to realize their potential, "
                "cope with normal life stresses, work productively, and contribute to their community."
            ],
            'fact-2': [
                "Mental health is crucial for overall health and quality of life. It affects how we handle "
                "stress, relate to others, and make decisions. Poor mental health increases risk for chronic "
                "physical conditions like cardiovascular disease. Maintaining good mental health involves "
                "emotional, psychological, and social well-being."
            ],
            'fact-3': [
                self.provide_clinical_info('depression')
            ],
            'fact-5': [
                "For a clinical diagnosis of Major Depressive Disorder, symptoms must persist for at least "
                "two weeks and represent a change from previous functioning. Five or more of the following "
                "must be present: depressed mood, diminished interest/pleasure, weight changes, sleep "
                "disturbances, psychomotor changes, fatigue, feelings of worthlessness, concentration "
                "difficulties, or recurrent thoughts of death. A thorough evaluation by a qualified mental "
                "health professional is necessary for accurate diagnosis."
            ],
            'fact-8': [
                "Mental illnesses are medical conditions involving changes in emotion, thinking, or behavior "
                "(or a combination). They are associated with distress and impaired functioning in social, "
                "work, or family activities. Mental illnesses are brain disorders with biological, genetic, "
                "and environmental factors. They are NOT character flaws or signs of personal weakness. "
                "With proper treatment, 70-90% of individuals experience significant symptom reduction."
            ],
            'sad': [
                "Persistent sadness lasting more than two weeks may indicate depression, which requires "
                "clinical evaluation. I recommend documenting your symptoms (duration, intensity, impact on "
                "functioning) and scheduling an appointment with a mental health professional. Early "
                "intervention improves outcomes."
            ],
            'depressed': [
                "If you're experiencing depressive symptoms, clinical evaluation is important. " +
                self.provide_clinical_info('depression') +
                "\n\nI strongly recommend consulting with a psychiatrist or psychologist for proper assessment "
                "and treatment planning."
            ],
            'anxious': [
                "Anxiety symptoms warrant clinical attention, especially if they interfere with daily "
                "functioning. " + self.provide_clinical_info('anxiety') +
                "\n\nA mental health professional can provide accurate diagnosis and evidence-based treatment."
            ],
            'stressed': [
                "Chronic stress can have serious health consequences. Based on privacy-preserving simulations, implementing structured breaks and boundary-setting strategies may help. " + self.provide_clinical_info('stress') +
                "\n\nConsider consulting a healthcare provider to rule out underlying medical conditions and "
                "develop a comprehensive stress management plan."
            ],
            'work_tired': [
                "Chronic work-related fatigue warrants clinical evaluation. Privacy-preserving simulations from similar professional profiles suggest that persistent tiredness may indicate underlying conditions requiring assessment. Document your sleep patterns, energy levels, and work hours for clinical review. How long has this fatigue been ongoing?"
            ],
            'burnout': [
                "Burnout symptoms align with exhaustion from prolonged demands, often seen in STEM fields. Our privacy-preserving simulations indicate weekly commitments, such as exercise routines, may improve resilience scores by 20-30% in similar profiles. This isn't medical advice – consider professional evaluation if fatigue persists. How long has this been ongoing?",
                
                "Clinical research shows burnout is characterized by emotional exhaustion, depersonalization, and reduced personal accomplishment. Privacy-preserving analysis of comparable professional cohorts suggests structured interventions including cognitive-behavioral approaches and lifestyle modifications show 25-35% improvement in resilience metrics over 8-12 weeks. Professional evaluation is recommended for persistent symptoms exceeding 3 months. What is your primary occupation?",
                
                "Burnout in high-demand professions demonstrates measurable impacts on cognitive function and physical health. Privacy-preserving data from similar demographic profiles indicates multimodal interventions may improve recovery trajectories. However, this is informational only – consult with a healthcare provider for personalized assessment. When did symptoms first appear?"
            ],
            'sleep': [
                "Sleep disturbances are often comorbid with mental health conditions. Poor sleep can exacerbate "
                "depression and anxiety, while these conditions can disrupt sleep. Medical evaluation is "
                "recommended to rule out sleep disorders (sleep apnea, restless leg syndrome) and address "
                "any underlying mental health conditions."
            ],
            'medication': [
                "Psychiatric medications work by altering brain chemistry. Common classes include:\n\n" +
                self.explain_treatment('SSRIs', 'medication_types') + "\n" +
                self.explain_treatment('SNRIs', 'medication_types') + "\n\n" +
                "Medications should only be prescribed by a qualified physician after thorough evaluation. "
                "They often work best in combination with psychotherapy."
            ],
            'help': [
                "I can provide clinical information about mental health conditions, symptoms, and treatment "
                "options. However, I cannot diagnose conditions or prescribe treatment. For personalized care, "
                "please consult with a licensed mental health professional. How can I assist you with mental "
                "health information today?"
            ]
        }
        
        # Generate response
        if intent in doctor_responses:
            response = random.choice(doctor_responses[intent])
        else:
            # Generic clinical responses
            generic = [
                "Could you describe your symptoms in more detail? How long have you been experiencing them?",
                "Have you consulted with a healthcare provider about these symptoms?",
                "What specific mental health information are you seeking?",
                "I can provide clinical information. What aspect of mental health would you like to understand better?",
                "For accurate diagnosis and treatment, I recommend consulting a licensed mental health professional. "
                "What questions can I answer about mental health conditions?"
            ]
            response = random.choice(generic)
        
        # Add disclaimer
        disclaimer = "\n\n**Disclaimer:** This information is for educational purposes only and not a " \
                    "substitute for professional medical advice, diagnosis, or treatment."
        response += disclaimer
        
        self.add_to_history(user_input, response)
        return response
    
    def get_persona_style(self) -> Dict:
        return self.style_params


if __name__ == "__main__":
    # Test Doctor Persona
    doctor = DoctorPersona()
    print(f"Persona: {doctor.name}")
    print(f"Greeting: {doctor.generate_greeting()}")
    
    # Test clinical info
    print("\n" + "="*50)
    print(doctor.provide_clinical_info('depression'))
