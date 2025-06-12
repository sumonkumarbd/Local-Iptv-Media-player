package com.feed.sphere.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.feed.sphere.Interface.OnLoginStatusChangedListener;
import com.feed.sphere.R;
import com.feed.sphere.activities.MainActivity;
import com.feed.sphere.adapters.IPTVPagerAdapter;
import com.feed.sphere.api.IPTVService;
import com.feed.sphere.utils.NetworkUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

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

    private static final String SUPABASE_URL = "https://vsggpasapvrjjkjfomup.supabase.co/rest/v1/cast_sphere_db";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZzZ2dwYXNhcHZyampramZvbXVwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDk2NzI4NTEsImV4cCI6MjA2NTI0ODg1MX0.g2TFNsvMxC9gBTIhyFA1vjNT15ITAAWrKgVEO80D5Rc";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_iptv, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);

        // ✅ Safe to call after views are initialized
        fetchDB((baseUrl, isActive) -> {
            if (!isActive) {
                etBaseUrl.setVisibility(View.VISIBLE);
            } else {
                etBaseUrl.setVisibility(View.GONE);
            }
        });

        checkInternet();  // Called only after views are ready
        checkLoginStatus();
    }

    // Public method to check internet and show Toast
    public void checkInternet() {
        if (NetworkUtil.isInternetAvailable(getContext())) {
            setupTabLayout();
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
        viewPager.setSaveEnabled(false);  // ✅ This line prevents restoration crash
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




    private void fetchDB(SupabaseCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                SUPABASE_URL,
                null,
                response -> {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String getBaseUrl = obj.getString("base_url");
                            boolean isActive = obj.getBoolean("is_active");

                            // ✅ Callback here
                            callback.onDataFetched(getBaseUrl, isActive);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    Log.e(TAG, "Error: " + error.getMessage());
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("apikey", SUPABASE_ANON_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_ANON_KEY);
                return headers;
            }
        };

        queue.add(jsonArrayRequest);
    }



    public interface SupabaseCallback {
        void onDataFetched(String server_baseUrl, boolean isActive);
    }


    private void checkLoginStatus() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        String url = prefs.getString("base_url", "");
        String username = prefs.getString("username", "");
        String password = prefs.getString("password", "");
        boolean isXUI = prefs.getBoolean("is_xui", false);

        boolean isLoggedIn = !url.isEmpty() && !username.isEmpty() && !password.isEmpty();

        if (isLoggedIn) {
            // User is logged in
            iptvService = new IPTVService(url, username, password, isXUI);
            showContent();
            if (loginStatusListener != null) {
                ((MainActivity) requireActivity()).onLoginStatusChanged(true);
            }
        } else {
            // User needs to login
            showLoginForm();
            ((MainActivity) requireActivity()).onLoginStatusChanged(false);
        }
    }

    private void attemptLogin() {
        // Reset errors
        tilBaseUrl.setError(null);
        tilUsername.setError(null);
        tilPassword.setError(null);

        fetchDB((server_baseUrl, isActive) -> {
            if (!server_baseUrl.isEmpty() && isActive) {
                String baseUrl = server_baseUrl;
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
            }else {
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
        });


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


    private OnLoginStatusChangedListener loginStatusListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginStatusChangedListener) {
            loginStatusListener = (OnLoginStatusChangedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnLoginStatusChangedListener");
        }
    }


}//main