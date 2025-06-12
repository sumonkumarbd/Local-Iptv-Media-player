package com.feed.sphere.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.feed.sphere.R;
import com.feed.sphere.api.IPTVService;
import com.feed.sphere.models.UserInfo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;

public class UserInfoActivity extends AppCompatActivity {
    private static final String TAG = "UserInfoActivity";

    private ProgressBar progressBar;
    private TextView tvUsername;
    private TextView tvStatus;
    private TextView tvExpDate;
    private TextView tvTrialStatus;
    private TextView tvActiveConnections;
    private TextView tvMaxConnections;
    private TextView tvCreatedAt;
    private TextView tvOutputFormats;

    private IPTVService iptvService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        iptvService = (IPTVService) getIntent().getSerializableExtra("iptv_service");
        if (iptvService == null) {
            Toast.makeText(this, "IPTV service not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        loadUserInfo();
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progressBar);
        tvUsername = findViewById(R.id.tv_username);
        tvStatus = findViewById(R.id.tv_status);
        tvExpDate = findViewById(R.id.tv_exp_date);
        tvTrialStatus = findViewById(R.id.tv_trial_status);
        tvActiveConnections = findViewById(R.id.tv_active_connections);
        tvMaxConnections = findViewById(R.id.tv_max_connections);
        tvCreatedAt = findViewById(R.id.tv_created_at);
        tvOutputFormats = findViewById(R.id.tv_output_formats);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("User Information");
        }
    }

    private void loadUserInfo() {
        showLoading(true);
        new Thread(() -> {
            try {
                UserInfo userInfo = iptvService.getUserInfo();
                runOnUiThread(() -> {
                    displayUserInfo(userInfo);
                    showLoading(false);
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(UserInfoActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                });
            } catch (JSONException e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(UserInfoActivity.this, "Invalid response format", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void displayUserInfo(UserInfo userInfo) {
        tvUsername.setText(userInfo.getUsername());
        tvStatus.setText(userInfo.getStatus());
        tvExpDate.setText(formatDate(userInfo.getExpDate()));
        tvTrialStatus.setText(userInfo.isTrial() ? "Trial Account" : "Full Account");
        tvActiveConnections.setText(userInfo.isActiveCons() ? "Active" : "Inactive");
        tvMaxConnections.setText(String.valueOf(userInfo.getMaxConnections()));
        tvCreatedAt.setText(formatDate(userInfo.getCreatedAt()));
        tvOutputFormats.setText(userInfo.getAllowedOutputFormats());
    }


    private String formatDate(int timestamp) {
        try {
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a - MMM dd, yyyy", Locale.getDefault());
            Date date = new Date(timestamp * 1000L); // Convert Unix timestamp to milliseconds
            return outputFormat.format(date);
        } catch (Exception e) {
            return String.valueOf(timestamp);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}