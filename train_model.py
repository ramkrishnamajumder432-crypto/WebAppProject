import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_squared_error, r2_score
import pickle

def load_and_prepare_data(filepath='property.csv'):
    """Load and prepare the property dataset"""
    try:
        df = pd.read_csv(filepath)
        print(f"Loaded {len(df)} records from {filepath}")
        return df
    except FileNotFoundError:
        print(f"File {filepath} not found. Creating sample data...")
        return create_sample_data()


def create_sample_data():
    """Create sample property data for demonstration"""
    np.random.seed(42)
    n_samples = 500
    
    data = {
        'bedrooms': np.random.randint(1, 6, n_samples),
        'bathrooms': np.random.randint(1, 4, n_samples),
        'sqft': np.random.randint(500, 5000, n_samples),
        'location_score': np.random.uniform(1, 10, n_samples),
        'age': np.random.randint(0, 50, n_samples),
    }
    
    # Generate price based on features with some noise
    data['price'] = (
        data['bedrooms'] * 25000 +
        data['bathrooms'] * 15000 +
        data['sqft'] * 150 +
        data['location_score'] * 20000 -
        data['age'] * 2000 +
        np.random.normal(0, 50000, n_samples)
    )
    
    df = pd.DataFrame(data)
    df['price'] = df['price'].clip(lower=50000)  # Minimum price
    
    # Save sample data
    df.to_csv('property.csv', index=False)
    print(f"Created sample dataset with {n_samples} records")
    
    return df


def train_model(df):
    """Train the property price prediction model"""
    # Features and target
    feature_columns = ['bedrooms', 'bathrooms', 'sqft', 'location_score', 'age']
    X = df[feature_columns]
    y = df['price']
    
    # Split data
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42
    )
    
    # Train Random Forest model
    model = RandomForestRegressor(
        n_estimators=100,
        max_depth=10,
        random_state=42
    )
    model.fit(X_train, y_train)
    
    # Evaluate
    y_pred = model.predict(X_test)
    mse = mean_squared_error(y_test, y_pred)
    r2 = r2_score(y_test, y_pred)
    
    print(f"\nModel Performance:")
    print(f"  Mean Squared Error: ${mse:,.2f}")
    print(f"  Root MSE: ${np.sqrt(mse):,.2f}")
    print(f"  R² Score: {r2:.4f}")
    
    # Feature importance
    print(f"\nFeature Importance:")
    for feat, imp in zip(feature_columns, model.feature_importances_):
        print(f"  {feat}: {imp:.4f}")
    
    return model


def save_model(model, filepath='model.pkl'):
    """Save the trained model to a file"""
    with open(filepath, 'wb') as f:
        pickle.dump(model, f)
    print(f"\nModel saved to {filepath}")


def main():
    print("=" * 50)
    print("Real Estate Price Prediction - Model Training")
    print("=" * 50)
    
    # Load data
    df = load_and_prepare_data()
    
    # Display data info
    print(f"\nDataset shape: {df.shape}")
    print(f"\nPrice statistics:")
    print(df['price'].describe())
    
    # Train model
    model = train_model(df)
    
    # Save model
    save_model(model)
    
    # Test prediction
    print("\n" + "=" * 50)
    print("Sample Prediction:")
    print("=" * 50)
    sample = [[3, 2, 1500, 7.5, 10]]  # 3 bed, 2 bath, 1500 sqft, location 7.5, 10 years old
    pred = model.predict(sample)[0]
    print(f"Input: 3 bed, 2 bath, 1500 sqft, location score 7.5, 10 years old")
    print(f"Predicted Price: ${pred:,.2f}")


if __name__ == '__main__':
    main()
