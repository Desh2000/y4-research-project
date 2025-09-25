"""
Mental Health Clustering API Service
RESTful API to integrate Python GMM model with Java backend
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import numpy as np
import sys
import os
import traceback
from datetime import datetime

# Add project root to path for absolute imports
project_root = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
sys.path.insert(0, project_root)

from src.models.mental_health_clustering_system import MentalHealthClusteringSystem
from src.data.mental_health_generator import MentalHealthDataGenerator

app = Flask(__name__)
CORS(app)  # Enable CORS for Java backend calls

# Global model instance
clustering_model = None


def load_or_train_model():
    """Load existing model or train a new one"""
    global clustering_model

    # Note: The model path is now relative to the project root
    model_path = os.path.join(project_root, 'models/mental_health_gmm_production.pkl')
    clustering_model = MentalHealthClusteringSystem(n_clusters=9, random_state=42)

    try:
        # Try to load existing model
        clustering_model.load_model(model_path)
        print(f"✅ Loaded existing model from {model_path}")
    except (FileNotFoundError, Exception):
        print("⚠️  No existing model found. Training new model...")

        # Generate training data and train new model
        generator = MentalHealthDataGenerator(random_state=42)
        data, true_clusters, df = generator.generate_full_dataset(samples_per_cluster=150)

        clustering_model.fit(data, true_clusters)

        # Save the trained model
        os.makedirs(os.path.dirname(model_path), exist_ok=True)
        clustering_model.save_model(model_path)
        print(f"✅ New model trained and saved to {model_path}")


@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint for monitoring"""
    model_is_ready = clustering_model is not None and clustering_model.is_fitted
    return jsonify({
        'status': 'healthy',
        'service': 'Mental Health Clustering API',
        'version': '1.0.0',
        'timestamp': datetime.now().isoformat(),
        'model_loaded': model_is_ready
    })


@app.route('/predict', methods=['POST'])
def predict_cluster():
    """
    Predict cluster for a user based on mental health scores
    """
    try:
        if clustering_model is None or not clustering_model.is_fitted:
            return jsonify({'error': 'Model is not available or not trained yet.'}), 503

        if not request.is_json:
            return jsonify({'error': 'Content-Type must be application/json'}), 400

        data = request.get_json()
        required_fields = ['stress_score', 'depression_score', 'anxiety_score']
        if not all(field in data for field in required_fields):
            return jsonify({'error': 'Missing one or more required fields'}), 400

        stress = float(data['stress_score'])
        depression = float(data['depression_score'])
        anxiety = float(data['anxiety_score'])

        result = clustering_model.predict_user_cluster(stress, depression, anxiety)
        result['api_info'] = {
            'prediction_timestamp': datetime.now().isoformat(),
            'model_version': clustering_model.model_version,
            'service_version': '1.0.0'
        }
        return jsonify(result)

    except ValueError as e:
        return jsonify({'error': f'Invalid input: {str(e)}'}), 400
    except Exception:
        print(f"Error in prediction: {traceback.format_exc()}")
        return jsonify({'error': 'Internal server error'}), 500


@app.route('/predict/batch', methods=['POST'])
def predict_cluster_batch():
    """
    Predict clusters for multiple users
    """
    try:
        if clustering_model is None or not clustering_model.is_fitted:
            return jsonify({'error': 'Model is not available or not trained yet.'}), 503

        if not request.is_json:
            return jsonify({'error': 'Content-Type must be application/json'}), 400

        data = request.get_json()
        if 'users' not in data or not isinstance(data['users'], list):
            return jsonify({'error': "Request must contain a 'users' list."}), 400

        results = []
        for i, user in enumerate(data['users']):
            try:
                stress = float(user['stress_score'])
                depression = float(user['depression_score'])
                anxiety = float(user['anxiety_score'])
                result = clustering_model.predict_user_cluster(stress, depression, anxiety)
                result['user_index'] = i
                results.append(result)
            except (ValueError, KeyError) as e:
                results.append({'user_index': i, 'error': f'Invalid data for user: {str(e)}'})

        return jsonify({
            'predictions': results,
            'total_users': len(data['users']),
            'api_info': {
                'prediction_timestamp': datetime.now().isoformat(),
                'model_version': clustering_model.model_version,
                'service_version': '1.0.0'
            }
        })

    except Exception:
        print(f"Error in batch prediction: {traceback.format_exc()}")
        return jsonify({'error': 'Internal server error'}), 500


@app.route('/clusters/info', methods=['GET'])
def get_cluster_info():
    """Get information about all clusters"""
    try:
        if clustering_model is None or not clustering_model.is_fitted:
            return jsonify({'error': 'Model is not available or not trained yet.'}), 503
        stats = clustering_model.get_cluster_statistics()
        return jsonify(stats)
    except Exception as e:
        return jsonify({'error': str(e)}), 500


@app.route('/clusters/update', methods=['POST'])
def update_clusters():
    """
    Update cluster centroids with new training data
    """
    try:
        if clustering_model is None or not clustering_model.is_fitted:
            return jsonify({'error': 'Model is not available or not trained yet.'}), 503

        data = request.get_json()
        training_data = np.array(data['training_data'])
        learning_rate = data.get('learning_rate', 0.1)

        clustering_model.update_centroids(training_data, learning_rate)
        clustering_model.save_model(os.path.join(project_root, 'models/mental_health_gmm_production.pkl'))

        return jsonify({'status': 'success', 'message': 'Centroids updated'})

    except Exception as e:
        print(f"Error updating clusters: {traceback.format_exc()}")
        return jsonify({'error': str(e)}), 500


@app.route('/model/retrain', methods=['POST'])
def retrain_model():
    """
    Retrain the entire model
    """
    try:
        print("🔄 Retraining model...")
        generator = MentalHealthDataGenerator(random_state=42)
        data, true_clusters, _ = generator.generate_full_dataset(samples_per_cluster=200)

        global clustering_model
        clustering_model = MentalHealthClusteringSystem(n_clusters=9, random_state=42)
        results = clustering_model.fit(data, true_clusters)

        model_path = os.path.join(project_root, 'models/mental_health_gmm_production.pkl')
        clustering_model.save_model(model_path)

        return jsonify({
            'status': 'success',
            'message': 'Model retrained successfully',
            'metrics': results['metrics']
        })

    except Exception as e:
        print(f"Error retraining model: {traceback.format_exc()}")
        return jsonify({'error': str(e)}), 500


if __name__ == '__main__':
    print("🚀 Starting Mental Health Clustering API...")
    load_or_train_model()
    print("✅ Model ready!")
    print("🌐 Starting Flask server...")
    app.run(host='0.0.0.0', port=5000, debug=True)
