"""
Data Loader Module
Loads and preprocesses datasets for the Empathetic Conversational Support System
"""

import pandas as pd
import json
import re
import os
from typing import Dict, List, Tuple
import numpy as np
from sklearn.model_selection import train_test_split


class DataLoader:
    """Handles loading and preprocessing of mental health datasets"""
    
    def __init__(self, data_dir: str = "data/"):
        self.data_dir = data_dir
        self.faq_data = None
        self.intents_data = None
        self.train_data = None
        
    def load_faq_data(self, filename: str = "Mental_Health_FAQ.csv") -> pd.DataFrame:
        """Load Mental Health FAQ dataset"""
        filepath = f"{self.data_dir}{filename}"
        self.faq_data = pd.read_csv(filepath)
        print(f"Loaded {len(self.faq_data)} FAQ entries")
        return self.faq_data
    
    def load_intents_data(self, filename: str = "intents.json") -> Dict:
        """Load intents dataset for intent classification"""
        filepath = f"{self.data_dir}{filename}"
        with open(filepath, 'r', encoding='utf-8') as f:
            self.intents_data = json.load(f)
        
        # Load additional intents if available
        additional_filepath = f"{self.data_dir}additional_intents.json"
        if os.path.exists(additional_filepath):
            try:
                with open(additional_filepath, 'r', encoding='utf-8') as f:
                    additional_data = json.load(f)
                    # Merge additional intents
                    self.intents_data['intents'].extend(additional_data['intents'])
                    print(f"Loaded additional intents")
            except Exception as e:
                print(f"Warning: Could not load additional intents: {e}")
        
        print(f"Loaded {len(self.intents_data['intents'])} intent categories")
        return self.intents_data
    
    def load_train_data(self, filename: str = "train.csv") -> pd.DataFrame:
        """Load training conversation dataset"""
        filepath = f"{self.data_dir}{filename}"
        self.train_data = pd.read_csv(filepath)
        print(f"Loaded {len(self.train_data)} training conversations")
        return self.train_data
    
    def clean_text(self, text: str) -> str:
        """Clean and normalize text data"""
        if pd.isna(text):
            return ""
        
        # Remove special characters but keep punctuation
        text = re.sub(r'[^\w\s.,!?\'-]', '', str(text))
        
        # Remove extra whitespace
        text = ' '.join(text.split())
        
        return text.strip()
    
    def prepare_intent_dataset(self) -> Tuple[List[str], List[str], List[str]]:
        """
        Prepare dataset for intent classification
        Returns: (patterns, tags, responses)
        """
        if self.intents_data is None:
            self.load_intents_data()
        
        patterns = []
        tags = []
        responses = []
        
        for intent in self.intents_data['intents']:
            tag = intent['tag']
            
            # Add patterns
            if 'patterns' in intent:
                for pattern in intent['patterns']:
                    patterns.append(self.clean_text(pattern))
                    tags.append(tag)
            
            # Store responses
            if 'responses' in intent:
                for response in intent['responses']:
                    responses.append(self.clean_text(response))
        
        print(f"Prepared {len(patterns)} patterns across {len(set(tags))} intent classes")
        return patterns, tags, responses
    
    def prepare_conversation_dataset(self) -> Tuple[List[str], List[str]]:
        """
        Prepare dataset for conversation modeling
        Returns: (contexts, responses)
        """
        if self.train_data is None:
            self.load_train_data()
        
        contexts = []
        responses = []
        
        for _, row in self.train_data.iterrows():
            context = self.clean_text(row['Context'])
            response = self.clean_text(row['Response'])
            
            if context and response:
                contexts.append(context)
                responses.append(response)
        
        print(f"Prepared {len(contexts)} conversation pairs")
        return contexts, responses
    
    def prepare_faq_dataset(self) -> Tuple[List[str], List[str]]:
        """
        Prepare FAQ dataset for question answering
        Returns: (questions, answers)
        """
        if self.faq_data is None:
            self.load_faq_data()
        
        questions = []
        answers = []
        
        for _, row in self.faq_data.iterrows():
            question = self.clean_text(row['Questions'])
            answer = self.clean_text(row['Answers'])
            
            if question and answer:
                questions.append(question)
                answers.append(answer)
        
        print(f"Prepared {len(questions)} FAQ pairs")
        return questions, answers
    
    def create_train_val_split(self, X, y, test_size=0.2, random_state=42):
        """Create training and validation splits"""
        # Try stratified split first, fall back to regular split if classes are too small
        try:
            X_train, X_val, y_train, y_val = train_test_split(
                X, y, test_size=test_size, random_state=random_state, stratify=y
            )
        except ValueError:
            print("Warning: Some classes have too few samples for stratified split. Using regular split.")
            X_train, X_val, y_train, y_val = train_test_split(
                X, y, test_size=test_size, random_state=random_state
            )
        print(f"Train size: {len(X_train)}, Validation size: {len(X_val)}")
        return X_train, X_val, y_train, y_val
    
    def get_intent_statistics(self) -> Dict:
        """Get statistics about intent distribution"""
        patterns, tags, _ = self.prepare_intent_dataset()
        
        tag_counts = {}
        for tag in tags:
            tag_counts[tag] = tag_counts.get(tag, 0) + 1
        
        return {
            'total_patterns': len(patterns),
            'unique_intents': len(set(tags)),
            'distribution': tag_counts
        }
    
    def categorize_for_personas(self) -> Dict[str, List]:
        """
        Categorize intents for different personas
        Friend: casual, emotional support
        Counselor: therapeutic, advice-giving
        Doctor: clinical, informational
        """
        if self.intents_data is None:
            self.load_intents_data()
        
        friend_intents = ['greeting', 'goodbye', 'thanks', 'casual', 'happy', 
                         'friends', 'jokes', 'morning', 'afternoon', 'evening', 'night']
        
        counselor_intents = ['sad', 'stressed', 'anxious', 'depressed', 'worthless',
                            'scared', 'not-talking', 'sleep', 'problem', 'help',
                            'meditation', 'user-advice']
        
        doctor_intents = ['about', 'skill', 'fact-1', 'fact-2', 'fact-3', 'fact-4',
                         'fact-5', 'fact-6', 'fact-7', 'fact-8', 'fact-9', 'fact-10',
                         'fact-11', 'fact-12', 'fact-13', 'fact-14', 'suicide', 'death']
        
        return {
            'friend': friend_intents,
            'counselor': counselor_intents,
            'doctor': doctor_intents
        }


if __name__ == "__main__":
    # Test the data loader
    loader = DataLoader(data_dir="../data/")
    
    # Load all datasets
    loader.load_faq_data()
    loader.load_intents_data()
    loader.load_train_data()
    
    # Get statistics
    stats = loader.get_intent_statistics()
    print("\nIntent Statistics:")
    print(f"Total patterns: {stats['total_patterns']}")
    print(f"Unique intents: {stats['unique_intents']}")
    
    # Show persona categorization
    personas = loader.categorize_for_personas()
    print("\nPersona Intent Distribution:")
    for persona, intents in personas.items():
        print(f"{persona.capitalize()}: {len(intents)} intents")
