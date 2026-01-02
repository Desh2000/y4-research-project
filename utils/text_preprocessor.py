"""
Text Preprocessing Module
Advanced text preprocessing for mental health conversations
"""

import re
import nltk
from typing import List, Dict
import numpy as np

# Download required NLTK data
try:
    nltk.data.find('tokenizers/punkt')
except LookupError:
    nltk.download('punkt')

try:
    nltk.data.find('corpora/stopwords')
except LookupError:
    nltk.download('stopwords')

try:
    nltk.data.find('corpora/wordnet')
except LookupError:
    nltk.download('wordnet')

from nltk.tokenize import word_tokenize, sent_tokenize
from nltk.corpus import stopwords
from nltk.stem import WordNetLemmatizer


class TextPreprocessor:
    """Advanced text preprocessing for conversational AI"""
    
    def __init__(self, remove_stopwords: bool = False):
        self.lemmatizer = WordNetLemmatizer()
        self.stop_words = set(stopwords.words('english'))
        self.remove_stopwords = remove_stopwords
        
        # Mental health specific terms to preserve
        self.preserve_words = {
            'not', 'no', 'never', 'nothing', 'nobody', 'nowhere',
            'neither', 'none', 'nor', 'cannot', 'couldn\'t', 'didn\'t',
            'doesn\'t', 'hadn\'t', 'hasn\'t', 'haven\'t', 'isn\'t',
            'mightn\'t', 'mustn\'t', 'needn\'t', 'shan\'t', 'shouldn\'t',
            'wasn\'t', 'weren\'t', 'won\'t', 'wouldn\'t', 'don\'t',
            'very', 'too', 'more', 'most', 'much', 'always', 'often'
        }
    
    def clean_text(self, text: str) -> str:
        """Basic text cleaning"""
        if not text:
            return ""
        
        # Convert to lowercase
        text = text.lower()
        
        # Remove URLs
        text = re.sub(r'http\S+|www\S+|https\S+', '', text)
        
        # Remove email addresses
        text = re.sub(r'\S+@\S+', '', text)
        
        # Remove extra whitespace
        text = ' '.join(text.split())
        
        return text
    
    def tokenize(self, text: str) -> List[str]:
        """Tokenize text into words"""
        return word_tokenize(text)
    
    def remove_stop_words(self, tokens: List[str]) -> List[str]:
        """Remove stopwords while preserving important mental health terms"""
        if not self.remove_stopwords:
            return tokens
        
        return [
            token for token in tokens 
            if token not in self.stop_words or token in self.preserve_words
        ]
    
    def lemmatize(self, tokens: List[str]) -> List[str]:
        """Lemmatize tokens"""
        return [self.lemmatizer.lemmatize(token) for token in tokens]
    
    def preprocess(self, text: str, return_tokens: bool = False) -> str or List[str]:
        """
        Complete preprocessing pipeline
        
        Args:
            text: Input text
            return_tokens: If True, return list of tokens, else return string
        
        Returns:
            Preprocessed text or tokens
        """
        # Clean text
        text = self.clean_text(text)
        
        # Tokenize
        tokens = self.tokenize(text)
        
        # Remove stopwords
        tokens = self.remove_stop_words(tokens)
        
        # Lemmatize
        tokens = self.lemmatize(tokens)
        
        if return_tokens:
            return tokens
        else:
            return ' '.join(tokens)
    
    def extract_sentences(self, text: str) -> List[str]:
        """Extract sentences from text"""
        return sent_tokenize(text)
    
    def detect_emotion_keywords(self, text: str) -> Dict[str, List[str]]:
        """
        Detect emotion-related keywords in text
        Useful for understanding user emotional state
        """
        text = text.lower()
        
        emotion_keywords = {
            'sadness': ['sad', 'depressed', 'down', 'unhappy', 'miserable', 'lonely', 
                       'empty', 'hopeless', 'crying', 'tears'],
            'anxiety': ['anxious', 'worried', 'nervous', 'scared', 'afraid', 'panic',
                       'stress', 'tense', 'overwhelmed', 'fear'],
            'anger': ['angry', 'mad', 'furious', 'frustrated', 'irritated', 'annoyed',
                     'rage', 'hate'],
            'joy': ['happy', 'joyful', 'glad', 'cheerful', 'excited', 'pleased',
                   'delighted', 'content'],
            'worthlessness': ['worthless', 'useless', 'failure', 'inadequate', 'inferior',
                            'pointless', 'meaningless'],
            'suicidal': ['suicide', 'kill myself', 'end it', 'die', 'death wish',
                        'not worth living']
        }
        
        detected = {}
        for emotion, keywords in emotion_keywords.items():
            found = [kw for kw in keywords if kw in text]
            if found:
                detected[emotion] = found
        
        return detected
    
    def calculate_sentiment_score(self, text: str) -> float:
        """
        Simple sentiment score based on keyword presence
        Returns: -1 (negative) to 1 (positive)
        """
        positive_words = ['good', 'great', 'better', 'happy', 'glad', 'thanks',
                         'helpful', 'appreciate', 'wonderful', 'excellent']
        negative_words = ['bad', 'worse', 'sad', 'terrible', 'awful', 'hate',
                         'worthless', 'depressed', 'anxious', 'stressed']
        
        text = text.lower()
        tokens = self.tokenize(text)
        
        pos_count = sum(1 for token in tokens if token in positive_words)
        neg_count = sum(1 for token in tokens if token in negative_words)
        
        total = pos_count + neg_count
        if total == 0:
            return 0.0
        
        return (pos_count - neg_count) / total


if __name__ == "__main__":
    # Test the preprocessor
    preprocessor = TextPreprocessor()
    
    test_texts = [
        "I'm feeling really depressed and worthless today.",
        "Thank you so much! You've been very helpful.",
        "I can't sleep and I'm constantly worried about everything."
    ]
    
    for text in test_texts:
        print(f"\nOriginal: {text}")
        print(f"Preprocessed: {preprocessor.preprocess(text)}")
        print(f"Emotions: {preprocessor.detect_emotion_keywords(text)}")
        print(f"Sentiment: {preprocessor.calculate_sentiment_score(text):.2f}")
