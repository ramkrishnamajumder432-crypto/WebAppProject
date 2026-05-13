package com.example.realestate;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI Elements
    private EditText etBedrooms, etBathrooms, etSqft, etLocationScore, etAge;
    private Button btnPredict;
    private TextView tvResult, tvResultLabel;
    private ProgressBar progressBar;

    // API Configuration - Update this to your Flask server URL
    private static final String API_URL = "http://10.0.2.2:5000/api/predict"; // For Android emulator
    // Use your actual IP for physical device: "http://192.168.1.x:5000/api/predict"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etBedrooms = findViewById(R.id.etBedrooms);
        etBathrooms = findViewById(R.id.etBathrooms);
        etSqft = findViewById(R.id.etSqft);
        etLocationScore = findViewById(R.id.etLocationScore);
        etAge = findViewById(R.id.etAge);
        btnPredict = findViewById(R.id.btnPredict);
        tvResult = findViewById(R.id.tvResult);
        tvResultLabel = findViewById(R.id.tvResultLabel);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    predictPrice();
                }
            }
        });
    }

    private boolean validateInputs() {
        if (etBedrooms.getText().toString().isEmpty()) {
            etBedrooms.setError("Required");
            return false;
        }
        if (etBathrooms.getText().toString().isEmpty()) {
            etBathrooms.setError("Required");
            return false;
        }
        if (etSqft.getText().toString().isEmpty()) {
            etSqft.setError("Required");
            return false;
        }
        if (etLocationScore.getText().toString().isEmpty()) {
            etLocationScore.setError("Required");
            return false;
        }
        if (etAge.getText().toString().isEmpty()) {
            etAge.setError("Required");
            return false;
        }

        // Validate location score range
        float locationScore = Float.parseFloat(etLocationScore.getText().toString());
        if (locationScore < 1 || locationScore > 10) {
            etLocationScore.setError("Must be between 1 and 10");
            return false;
        }

        return true;
    }

    private void predictPrice() {
        String bedrooms = etBedrooms.getText().toString();
        String bathrooms = etBathrooms.getText().toString();
        String sqft = etSqft.getText().toString();
        String locationScore = etLocationScore.getText().toString();
        String age = etAge.getText().toString();

        new PredictPriceTask().execute(bedrooms, bathrooms, sqft, locationScore, age);
    }

    private class PredictPriceTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            btnPredict.setEnabled(false);
            tvResultLabel.setVisibility(View.GONE);
            tvResult.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                // Create JSON payload
                JSONObject jsonPayload = new JSONObject();
                jsonPayload.put("bedrooms", params[0]);
                jsonPayload.put("bathrooms", params[1]);
                jsonPayload.put("sqft", params[2]);
                jsonPayload.put("location_score", params[3]);
                jsonPayload.put("age", params[4]);

                // Send request
                OutputStream os = connection.getOutputStream();
                os.write(jsonPayload.toString().getBytes("UTF-8"));
                os.close();

                // Read response
                int responseCode = connection.getResponseCode();
                BufferedReader reader;
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else {
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                connection.disconnect();

                return response.toString();

            } catch (Exception e) {
                return "{\"error\": \"" + e.getMessage() + "\"}";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
            btnPredict.setEnabled(true);

            try {
                JSONObject jsonResponse = new JSONObject(result);
                
                if (jsonResponse.has("error")) {
                    Toast.makeText(MainActivity.this, 
                        "Error: " + jsonResponse.getString("error"), 
                        Toast.LENGTH_LONG).show();
                } else {
                    double predictedPrice = jsonResponse.getDouble("predicted_price");
                    
                    // Format as currency
                    NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
                    String formattedPrice = formatter.format(predictedPrice);
                    
                    tvResultLabel.setVisibility(View.VISIBLE);
                    tvResult.setVisibility(View.VISIBLE);
                    tvResult.setText(formattedPrice);
                }
            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, 
                    "Failed to parse response", 
                    Toast.LENGTH_SHORT).show();
            }
        }
    }
}
