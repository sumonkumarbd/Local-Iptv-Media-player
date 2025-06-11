package com.example.mediaplayer.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mediaplayer.R;
import com.example.mediaplayer.adapters.IPTVPagerAdapter;
import com.example.mediaplayer.api.IPTVService;
import com.example.mediaplayer.utils.NetworkUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.Objects;

import org.json.JSONException;

public class IPTVFragment extends Fragment {
    private static final String TAG = "IPTVFragment";
    private static final String PREFS_NAME = "iptv_prefs";

    // Login form views
    private TextInputLayout tilBaseUrl;
    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etBaseUrl;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private SwitchMaterial switchXUI;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private TextView tvError,tvStatus;
    private View loginForm;
    private View contentLayout;

    // Content views
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private IPTVPagerAdapter pagerAdapter;

    private IPTVService iptvService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_iptv, container, false);
        initializeViews(view);
        checkInternetAndToast();
        return view;
    }

    // Public method to check internet and show Toast
    public void checkInternetAndToast() {
        if (NetworkUtil.internetCheck(getContext())) {
            setupTabLayout();
            checkLoginStatus();
        } else {
            tvStatus.setText("No internet connection");

        }
    }

    private void initializeViews(View view) {
        // Login form views
        tilBaseUrl = view.findViewById(R.id.tilBaseUrl);
        tilUsername = view.findViewById(R.id.tilUsername);
        tilPassword = view.findViewById(R.id.tilPassword);
        etBaseUrl = view.findViewById(R.id.etBaseUrl);
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        switchXUI = view.findViewById(R.id.switchXUI);
        btnLogin = view.findViewById(R.id.btnLogin);
        progressBar = view.findViewById(R.id.progressBar);
        tvError = view.findViewById(R.id.tvError);
        loginForm = view.findViewById(R.id.loginForm);
        contentLayout = view.findViewById(R.id.contentLayout);
        tvStatus = view.findViewById(R.id.tvStatus);

        // Content views
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void setupTabLayout() {
        pagerAdapter = new IPTVPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Live TV");
                    break;
                case 1:
                    tab.setText("Movies");
                    break;
                case 2:
                    tab.setText("Series");
                    break;
            }
        }).attach();
    }

    private void checkLoginStatus() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        String url = prefs.getString("base_url", "");
        String username = prefs.getString("username", "");
        String password = prefs.getString("password", "");
        boolean isXUI = prefs.getBoolean("is_xui", false);

        if (!url.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
            // User is logged in
            iptvService = new IPTVService(url, username, password, isXUI);
            showContent();
        } else {
            // User needs to login
            showLoginForm();
        }
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
        boolean isXUI = switchXUI.isChecked();

        // Validate inputs
        if (!validateInputs(baseUrl, username, password)) {
            return;
        }

        showLoading(true);
        hideError();

        // Create service instance
        iptvService = new IPTVService(baseUrl, username, password, isXUI);

        // Perform login in background
        new Thread(() -> {
            try {
                iptvService.getUserInfo();
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    saveCredentials(baseUrl, username, password, isXUI);
                    showContent();
                });
            } catch (IOException e) {
                Log.e(TAG, "Network error during login", e);
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    showError("Network error: " + e.getMessage());
                });
            } catch (JSONException e) {
                Log.e(TAG, "Invalid response format", e);
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    showError("Invalid server response");
                });
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error during login", e);
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    showError("Login failed: " + e.getMessage());
                });
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

    private void saveCredentials(String baseUrl, String username, String password, boolean isXUI) {
        requireContext().getSharedPreferences(PREFS_NAME, 0)
                .edit()
                .putString("base_url", baseUrl)
                .putString("username", username)
                .putString("password", password)
                .putBoolean("is_xui", isXUI)
                .apply();
    }

    private void showLoginForm() {
        if (getView() == null)
            return; // Safety check for fragment view

        // Show login form
        View loginForm = getView().findViewById(R.id.loginForm);
        if (loginForm != null) {
            loginForm.setVisibility(View.VISIBLE);
        }

        // Hide content
        View contentLayout = getView().findViewById(R.id.contentLayout);
        if (contentLayout != null) {
            contentLayout.setVisibility(View.GONE);
        }

        // Clear any existing data
        if (pagerAdapter != null) {
            pagerAdapter.clearData();
        }
    }

    private void showContent() {
        loginForm.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }

    public IPTVService getIPTVService() {
        return iptvService;
    }

    public void clearData() {
        // Clear IPTV service
        iptvService = null;

        // Clear adapter data
        if (pagerAdapter != null) {
            pagerAdapter.clearData();
        }

        // Clear login form fields
        if (etBaseUrl != null)
            etBaseUrl.setText("");
        if (etUsername != null)
            etUsername.setText("");
        if (etPassword != null)
            etPassword.setText("");
        if (switchXUI != null)
            switchXUI.setChecked(false);
        if (tvError != null)
            tvError.setVisibility(View.GONE);

        // Show login form if view is available
        if (isAdded() && getView() != null) {
            showLoginForm();
        }
    }
}