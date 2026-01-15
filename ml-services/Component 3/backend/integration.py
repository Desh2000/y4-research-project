"""
Integration Interface
API for communication with other Manō platform components
"""

from typing import Dict, List, Optional, Any
import json
import requests
from datetime import datetime


class Component1Interface:
    """
    Interface for Component 1: Privacy-Preserving Mental Health Simulation System
    Receives synthetic mental health profiles and scenarios for training
    """
    
    def __init__(self, component1_url: str = "http://localhost:8001"):
        self.base_url = component1_url
    
    def fetch_synthetic_profiles(self, count: int = 100) -> List[Dict]:
        """
        Fetch synthetic mental health profiles for training
        
        Args:
            count: Number of profiles to fetch
        
        Returns:
            List of synthetic profiles
        """
        try:
            response = requests.get(
                f"{self.base_url}/synthetic/profiles",
                params={'count': count}
            )
            if response.status_code == 200:
                return response.json()['profiles']
        except Exception as e:
            print(f"Error fetching synthetic profiles: {e}")
        return []
    
    def fetch_conversation_scenarios(self, scenario_type: str = "all") -> List[Dict]:
        """
        Fetch synthetic conversation scenarios
        
        Args:
            scenario_type: Type of scenarios (stress, anxiety, depression, all)
        
        Returns:
            List of conversation scenarios
        """
        try:
            response = requests.get(
                f"{self.base_url}/synthetic/scenarios",
                params={'type': scenario_type}
            )
            if response.status_code == 200:
                return response.json()['scenarios']
        except Exception as e:
            print(f"Error fetching scenarios: {e}")
        return []
    
    def request_intervention_simulations(self, user_profile: Dict) -> Dict:
        """
        Request intervention simulations based on user profile
        
        Args:
            user_profile: User's mental health profile
        
        Returns:
            Intervention recommendations
        """
        try:
            response = requests.post(
                f"{self.base_url}/interventions/simulate",
                json={'profile': user_profile}
            )
            if response.status_code == 200:
                return response.json()
        except Exception as e:
            print(f"Error requesting interventions: {e}")
        return {}


class Component2Interface:
    """
    Interface for Component 2: Stress and Cognitive Risk Prediction System
    Shares interaction patterns and receives risk predictions
    """
    
    def __init__(self, component2_url: str = "http://localhost:8002"):
        self.base_url = component2_url
    
    def send_interaction_patterns(self, session_data: Dict) -> bool:
        """
        Send anonymized interaction patterns for model refinement
        
        Args:
            session_data: Anonymized session interaction data
        
        Returns:
            Success status
        """
        try:
            response = requests.post(
                f"{self.base_url}/patterns/upload",
                json={'data': session_data}
            )
            return response.status_code == 200
        except Exception as e:
            print(f"Error sending patterns: {e}")
        return False
    
    def get_risk_prediction(self, conversation_excerpt: str) -> Dict:
        """
        Get stress/cognitive risk prediction for conversation
        
        Args:
            conversation_excerpt: Recent conversation text
        
        Returns:
            Risk prediction results
        """
        try:
            response = requests.post(
                f"{self.base_url}/predict/risk",
                json={'text': conversation_excerpt}
            )
            if response.status_code == 200:
                return response.json()
        except Exception as e:
            print(f"Error getting risk prediction: {e}")
        return {}
    
    def subscribe_to_high_risk_alerts(self, callback_url: str) -> bool:
        """
        Subscribe to receive alerts for high-risk scenarios
        
        Args:
            callback_url: URL to receive alerts
        
        Returns:
            Success status
        """
        try:
            response = requests.post(
                f"{self.base_url}/alerts/subscribe",
                json={'callback_url': callback_url}
            )
            return response.status_code == 200
        except Exception as e:
            print(f"Error subscribing to alerts: {e}")
        return False


class Component4Interface:
    """
    Interface for Component 4: Resilience Clustering System
    Shares resilience insights and receives peer matching recommendations
    """
    
    def __init__(self, component4_url: str = "http://localhost:8004"):
        self.base_url = component4_url
    
    def send_resilience_insights(self, insights: Dict) -> bool:
        """
        Send anonymized resilience insights from conversations
        
        Args:
            insights: Resilience-related insights (coping strategies, etc.)
        
        Returns:
            Success status
        """
        try:
            response = requests.post(
                f"{self.base_url}/resilience/insights",
                json={'insights': insights}
            )
            return response.status_code == 200
        except Exception as e:
            print(f"Error sending insights: {e}")
        return False
    
    def get_peer_recommendations(self, user_profile: Dict) -> List[Dict]:
        """
        Get peer matching recommendations based on resilience profile
        
        Args:
            user_profile: Anonymized user resilience profile
        
        Returns:
            List of peer group recommendations
        """
        try:
            response = requests.post(
                f"{self.base_url}/peers/match",
                json={'profile': user_profile}
            )
            if response.status_code == 200:
                return response.json()['peers']
        except Exception as e:
            print(f"Error getting peer recommendations: {e}")
        return []
    
    def fetch_community_resources(self, topic: str) -> List[Dict]:
        """
        Fetch community-driven support resources
        
        Args:
            topic: Resource topic (stress, anxiety, etc.)
        
        Returns:
            List of community resources
        """
        try:
            response = requests.get(
                f"{self.base_url}/community/resources",
                params={'topic': topic}
            )
            if response.status_code == 200:
                return response.json()['resources']
        except Exception as e:
            print(f"Error fetching resources: {e}")
        return []


class IntegrationManager:
    """
    Manages integration with all platform components
    """
    
    def __init__(self):
        self.component1 = Component1Interface()
        self.component2 = Component2Interface()
        self.component4 = Component4Interface()
        self.integration_log = []
    
    def log_integration_event(self, event_type: str, component: str, 
                             details: Dict[str, Any]):
        """Log integration event"""
        self.integration_log.append({
            'timestamp': datetime.now().isoformat(),
            'event_type': event_type,
            'component': component,
            'details': details
        })
    
    def enhance_conversation_with_integrations(self, 
                                              session_data: Dict,
                                              conversation_text: str) -> Dict:
        """
        Enhance conversation using integrated components
        
        Args:
            session_data: Current session data
            conversation_text: Recent conversation
        
        Returns:
            Enhancement data (risk levels, peer suggestions, etc.)
        """
        enhancements = {}
        
        # Get risk prediction from Component 2
        try:
            risk_prediction = self.component2.get_risk_prediction(conversation_text)
            enhancements['risk_level'] = risk_prediction.get('risk_level', 'unknown')
            enhancements['stress_indicators'] = risk_prediction.get('indicators', [])
            
            self.log_integration_event(
                'risk_prediction',
                'component2',
                {'risk_level': enhancements['risk_level']}
            )
        except Exception as e:
            print(f"Error in Component 2 integration: {e}")
        
        # Get peer recommendations from Component 4
        try:
            peer_recs = self.component4.get_peer_recommendations(
                {'interests': [], 'challenges': []}  # Simplified
            )
            enhancements['peer_suggestions'] = peer_recs[:3]  # Top 3
            
            self.log_integration_event(
                'peer_matching',
                'component4',
                {'suggestions_count': len(peer_recs)}
            )
        except Exception as e:
            print(f"Error in Component 4 integration: {e}")
        
        return enhancements
    
    def sync_data_periodically(self, session_manager):
        """
        Periodically sync data with other components
        
        Args:
            session_manager: Session manager instance
        """
        # Export aggregated data for Component 2
        try:
            aggregated = session_manager.export_aggregated_data(
                None  # DP mechanism should be passed
            )
            self.component2.send_interaction_patterns(aggregated)
            
            self.log_integration_event(
                'data_sync',
                'component2',
                {'status': 'success'}
            )
        except Exception as e:
            print(f"Error syncing with Component 2: {e}")
        
        # Send resilience insights to Component 4
        # (Would extract from conversation analysis)
        
    def get_integration_status(self) -> Dict:
        """Get status of all component integrations"""
        status = {
            'component1': self._check_component_health(self.component1.base_url),
            'component2': self._check_component_health(self.component2.base_url),
            'component4': self._check_component_health(self.component4.base_url),
            'last_sync': self.integration_log[-1] if self.integration_log else None
        }
        return status
    
    def _check_component_health(self, url: str) -> str:
        """Check if component is reachable"""
        try:
            response = requests.get(f"{url}/health", timeout=2)
            return 'online' if response.status_code == 200 else 'error'
        except:
            return 'offline'


if __name__ == "__main__":
    print("Integration Interface Module\n")
    print("This module provides APIs for communication with other Manō components:")
    print("- Component 1: Privacy-Preserving Mental Health Simulation")
    print("- Component 2: Stress and Cognitive Risk Prediction")
    print("- Component 4: Resilience Clustering System")
    print("\nTo use: Initialize IntegrationManager and call appropriate methods")
