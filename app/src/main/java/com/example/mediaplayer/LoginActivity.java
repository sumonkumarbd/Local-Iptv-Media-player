package com.example.mediaplayer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mediaplayer.api.IPTVService;
import com.example.mediaplayer.models.UserInfo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

import org.json.JSONException;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private TextInputLayout tilBaseUrl;
    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etBaseUrl;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvDisclaimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        tilBaseUrl = findViewById(R.id.tilBaseUrl);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etBaseUrl = findViewById(R.id.etBaseUrl);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvDisclaimer = findViewById(R.id.tvDisclaimer);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        // Reset errors
        tilBaseUrl.setError(null);
        tilUsername.setError(null);
        tilPassword.setError(null);

        // Get values
        String baseUrl = etBaseUrl.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(baseUrl, username, password)) {
            return;
        }

        // Show loading
        showLoading(true);

        // Create service instance
        IPTVService service = new IPTVService(baseUrl, username, password, false);

        // Perform login in background
        new Thread(() -> {
            try {
                UserInfo userInfo = service.getUserInfo();
                runOnUiThread(() -> handleLoginSuccess(userInfo));
            } catch (IOException e) {
                Log.e(TAG, "Network error during login", e);
                runOnUiThread(() -> handleLoginError("Network error: " + e.getMessage()));
            } catch (JSONException e) {
                Log.e(TAG, "Invalid response format", e);
                runOnUiThread(() -> handleLoginError("Invalid server response"));
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error during login", e);
                runOnUiThread(() -> handleLoginError("Login failed: " + e.getMessage()));
            } finally {
                runOnUiThread(() -> showLoading(false));
            }
        }).start();
    }

    private boolean validateInputs(String baseUrl, String username, String password) {
        boolean isValid = true;

        if (baseUrl.isEmpty()) {
            tilBaseUrl.setError("Base URL is required");
            isValid = false;
        } else if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            tilBaseUrl.setError("URL must start with http:// or https://");
            isValid = false;
        }

        if (username.isEmpty()) {
            tilUsername.setError("Username is required");
            isValid = false;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            isValid = false;
        }

        return isValid;
    }

    private void handleLoginSuccess(UserInfo userInfo) {
        // Save credentials to SharedPreferences
        saveCredentials(userInfo);

        // Show success message
        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

        // Start MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleLoginError(String errorMessage) {
        tvDisclaimer.setText(errorMessage);
        tvDisclaimer.setVisibility(View.VISIBLE);
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        etBaseUrl.setEnabled(!show);
        etUsername.setEnabled(!show);
        etPassword.setEnabled(!show);
    }

    private void saveCredentials(UserInfo userInfo) {
        getSharedPreferences("iptv_prefs", MODE_PRIVATE)
                .edit()
                .putString("base_url", etBaseUrl.getText().toString().trim())
                .putString("username", etUsername.getText().toString().trim())
                .putString("password", etPassword.getText().toString().trim())
                .putString("exp_date", userInfo.getExpDate())
                .putInt("max_connections", userInfo.getMaxConnections())
                .putString("allowed_formats", userInfo.getAllowedOutputFormats())
                .apply();
    }
} 