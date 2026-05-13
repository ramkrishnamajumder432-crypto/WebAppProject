from flask import Flask, render_template, request, jsonify
import pickle
import numpy as np

app = Flask(__name__)

# Load the trained model
try:
    with open('model.pkl', 'rb') as f:
        model = pickle.load(f)
except FileNotFoundError:
    model = None
    print("Warning: model.pkl not found. Run train_model.py first.")


@app.route('/')
def index():
    """Home page with property search"""
    return render_template('index.html')


@app.route('/seller')
def seller():
    """Seller page to list a new property"""
    return render_template('seller.html')


@app.route('/predict', methods=['POST'])
def predict():
    """Predict property price based on features"""
    try:
        # Get features from form or JSON
        if request.is_json:
            data = request.get_json()
        else:
            data = request.form
        
        # Extract features (adjust based on your model's requirements)
        bedrooms = float(data.get('bedrooms', 0))
        bathrooms = float(data.get('bathrooms', 0))
        sqft = float(data.get('sqft', 0))
        location_score = float(data.get('location_score', 5))
        age = float(data.get('age', 0))
        
        # Create feature array
        features = np.array([[bedrooms, bathrooms, sqft, location_score, age]])
        
        if model is None:
            return jsonify({'error': 'Model not loaded. Please train the model first.'}), 500
        
        # Make prediction
        prediction = model.predict(features)[0]
        
        # Return result
        if request.is_json:
            return jsonify({
                'predicted_price': round(prediction, 2),
                'features': {
                    'bedrooms': bedrooms,
                    'bathrooms': bathrooms,
                    'sqft': sqft,
                    'location_score': location_score,
                    'age': age
                }
            })
        else:
            return render_template('results.html', 
                                   predicted_price=round(prediction, 2),
                                   bedrooms=bedrooms,
                                   bathrooms=bathrooms,
                                   sqft=sqft)
    
    except Exception as e:
        return jsonify({'error': str(e)}), 400


@app.route('/api/predict', methods=['POST'])
def api_predict():
    """API endpoint for Android app"""
    return predict()


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
