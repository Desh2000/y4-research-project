"""
REST API for Component 4

Provides endpoints for clustering and community management.

Author: [Your Name]
Date: [Current Date]
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import numpy as np
import pandas as pd
import sys
import os

# Add src to path
sys.path.append(os.path.dirname(__file__))

# Get project root directory (Component4_GMM)
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, ".."))

from clustering.gmm_model import GMMClusterer
from community.community_manager import CommunityManager
from utils.config_loader import ConfigLoader
from utils.logger import ManoCommunityLogger

# Initialize Flask app
app = Flask(__name__)
CORS(app)

# Initialize logger
logger = ManoCommunityLogger(name="API")

# Load configuration
config = ConfigLoader(os.path.join(PROJECT_ROOT, "config", "config.yaml"))

# Global variables for models (loaded on startup)
clusterer = None
community_manager = None


def load_models():
    """Load trained models"""
    global clusterer, community_manager

    logger.info("Loading models...")

    try:
        # Load GMM model
        clusterer = GMMClusterer.load_model(os.path.join(PROJECT_ROOT, "models", "gmm_model_v1.pkl"))
        logger.info("✓ GMM model loaded")

        # Load community data
        import json
        with open(os.path.join(PROJECT_ROOT, "results", "communities", "communities.json"), 'r') as f:
            communities_data = json.load(f)

        logger.info(f"✓ Loaded {len(communities_data)} communities")

        return True

    except Exception as e:
        logger.error(f"Error loading models: {str(e)}", exc_info=True)
        return False


# Load models on startup
load_models()


@app.route('/', methods=['GET'])
def index():
    """API root endpoint"""
    return jsonify({
        'name': 'Manō Component 4 API',
        'version': '1.0.0',
        'status': 'running',
        'endpoints': {
            'predict_cluster': '/api/predict',
            'get_community': '/api/community/<user_id>',
            'get_all_communities': '/api/communities',
            'health_check': '/api/health'
        }
    })


@app.route('/api/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    models_loaded = clusterer is not None

    return jsonify({
        'status': 'healthy' if models_loaded else 'degraded',
        'models_loaded': models_loaded,
        'timestamp': pd.Timestamp.now().isoformat()
    })


@app.route('/api/predict', methods=['POST'])
def predict_cluster():
    """
    Predict cluster for new user

    Request body:
    {
        "emotional_regulation_score": 0.75,
        "social_connectivity_score": 0.68,
        "behavioral_stability_score": 0.72,
        "cognitive_flexibility_score": 0.70,
        "stress_coping_mechanism": 0.65
    }
    """
    try:
        # Get data from request
        data = request.json

        # Validate required fields
        feature_cols = config.get('data.feature_columns')

        for col in feature_cols:
            if col not in data:
                return jsonify({
                    'error': f'Missing required field: {col}'
                }), 400

        # Create feature vector
        X = np.array([[data[col] for col in feature_cols]])

        # Predict
        cluster = int(clusterer.predict(X)[0])
        probas = clusterer.predict_proba(X)[0].tolist()

        # Log prediction
        logger.log_user_assignment(
            data.get('user_id', 'unknown'),
            cluster,
            max(probas)
        )

        # Return prediction
        return jsonify({
            'predicted_cluster': cluster,
            'cluster_probabilities': probas,
            'primary_probability': max(probas),
            'timestamp': pd.Timestamp.now().isoformat()
        })

    except Exception as e:
        logger.error(f"Prediction error: {str(e)}", exc_info=True)
        return jsonify({'error': str(e)}), 500


@app.route('/api/communities', methods=['GET'])
def get_all_communities():
    """Get list of all communities"""
    try:
        # Load community list
        community_list = pd.read_csv(os.path.join(PROJECT_ROOT, "results", "communities", "community_list.csv"))

        return jsonify({
            'communities': community_list.to_dict('records'),
            'count': len(community_list)
        })

    except Exception as e:
        logger.error(f"Error getting communities: {str(e)}", exc_info=True)
        return jsonify({'error': str(e)}), 500


@app.route('/api/community/<user_id>', methods=['GET'])
def get_user_community(user_id):
    """Get community for specific user"""
    try:
        # Load assignments
        assignments = pd.read_csv(os.path.join(PROJECT_ROOT, "results", "clusters", "cluster_assignments.csv"))

        # Find user
        user_row = assignments[assignments['user_id'] == user_id]

        if len(user_row) == 0:
            return jsonify({'error': 'User not found'}), 404

        cluster = int(user_row.iloc[0]['cluster'])

        # Load community info
        import json
        with open(os.path.join(PROJECT_ROOT, "results", "communities", "communities.json"), 'r') as f:
            communities_data = json.load(f)

        # Find user's community
        user_community = None
        for comm_id, comm_data in communities_data.items():
            if user_id in comm_data['members']:
                user_community = comm_data
                break

        if user_community:
            return jsonify({
                'user_id': user_id,
                'community': user_community
            })
        else:
            return jsonify({'error': 'Community not found'}), 404

    except Exception as e:
        logger.error(f"Error getting user community: {str(e)}", exc_info=True)
        return jsonify({'error': str(e)}), 500


@app.route('/api/stats', methods=['GET'])
def get_statistics():
    """Get system statistics"""
    try:
        # Load data
        assignments = pd.read_csv(os.path.join(PROJECT_ROOT, "results", "clusters", "cluster_assignments.csv"))

        import json
        with open(os.path.join(PROJECT_ROOT, "results", "communities", "communities.json"), 'r') as f:
            communities_data = json.load(f)

        # Calculate stats
        stats = {
            'total_users': len(assignments),
            'total_clusters': len(assignments['cluster'].unique()),
            'total_communities': len(communities_data),
            'avg_community_size': np.mean([c['size'] for c in communities_data.values()]),
            'cluster_distribution': assignments['cluster'].value_counts().to_dict()
        }

        return jsonify(stats)

    except Exception as e:
        logger.error(f"Error getting stats: {str(e)}", exc_info=True)
        return jsonify({'error': str(e)}), 500


def run_api():
    """Run the API server"""
    api_config = config.get_section('api')

    app.run(
        host=api_config.get('host', '0.0.0.0'),
        port=api_config.get('port', 5000),
        debug=api_config.get('debug', False)
    )


if __name__ == "__main__":
    run_api()