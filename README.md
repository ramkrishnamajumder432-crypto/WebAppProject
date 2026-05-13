# Real Estate Price Predictor

A full-stack real estate price prediction application with a Flask backend, web interface, and Android app.

## Project Structure

```
real_estate_project/
├── app.py                 # Flask web server
├── train_model.py         # ML model training script
├── property.csv           # Sample property dataset
├── model.pkl              # Trained model (generated)
├── requirements.txt       # Python dependencies
│
├── templates/
│   ├── index.html         # Home page - price estimator
│   ├── seller.html        # Seller listing page
│   └── results.html       # Price prediction results
│
└── android_app/
    ├── MainActivity.java  # Android main activity
    ├── activity_main.xml  # Android layout
    └── res/drawable/
        └── edit_text_background.xml
```

## Setup Instructions

### 1. Flask Backend

```bash
# Navigate to project directory
cd real_estate_project

# Create virtual environment (recommended)
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Train the ML model (generates model.pkl)
python train_model.py

# Run the Flask server
python app.py
```

The server will start at `http://localhost:5000`

### 2. Web Interface

Open your browser and navigate to:
- **Home Page**: http://localhost:5000/
- **Seller Page**: http://localhost:5000/seller

### 3. Android App

1. Open Android Studio
2. Create a new project or add files to existing project
3. Copy `MainActivity.java` to your `app/src/main/java/com/example/realestate/` directory
4. Copy `activity_main.xml` to your `app/src/main/res/layout/` directory
5. Copy `edit_text_background.xml` to `app/src/main/res/drawable/`
6. Add internet permission to `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

7. Update the API_URL in MainActivity.java:
   - For emulator: `http://10.0.2.2:5000/api/predict`
   - For physical device: Use your computer's local IP (e.g., `http://192.168.1.x:5000/api/predict`)

## API Endpoints

### POST /predict
Web form submission for price prediction.

### POST /api/predict
JSON API endpoint for Android app.

**Request:**
```json
{
  "bedrooms": 3,
  "bathrooms": 2,
  "sqft": 1500,
  "location_score": 7.5,
  "age": 10
}
```

**Response:**
```json
{
  "predicted_price": 325000.00,
  "features": {
    "bedrooms": 3,
    "bathrooms": 2,
    "sqft": 1500,
    "location_score": 7.5,
    "age": 10
  }
}
```

## Model Features

The ML model uses these features to predict property prices:
- **bedrooms**: Number of bedrooms
- **bathrooms**: Number of bathrooms  
- **sqft**: Square footage of the property
- **location_score**: Location desirability (1-10)
- **age**: Property age in years

## Technologies Used

- **Backend**: Python, Flask, scikit-learn
- **ML Model**: Random Forest Regressor
- **Frontend**: HTML, CSS (custom styling)
- **Mobile**: Android (Java)
