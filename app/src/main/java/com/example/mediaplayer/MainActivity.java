package com.example.mediaplayer;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "MediaPlayerPrefs";
    private static final String DISCLAIMER_ACCEPTED = "disclaimer_accepted";
    private static final String IPTV_PREFS = "iptv_prefs";

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private boolean disclaimerAccepted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupToolbar();

        if (!isDisclaimerAccepted()) {
            showComplianceDisclaimer();
        } else {
            disclaimerAccepted = true;
            proceedWithSetup();
        }
    }

    private boolean isDisclaimerAccepted() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(DISCLAIMER_ACCEPTED, false);
    }

    private void saveDisclaimerAcceptance() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(DISCLAIMER_ACCEPTED, true)
                .apply();
    }

    private void showComplianceDisclaimer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Legal Compliance Notice")
                .setMessage("IMPORTANT LEGAL DISCLAIMER:\n\n" +
                        "• This media player is a neutral tool for playing your own content\n" +
                        "• You are solely responsible for ensuring all content you play is legally obtained\n" +
                        "• Do not use this app to access pirated, copyrighted, or unauthorized content\n" +
                        "• IPTV streams must be from legitimate, authorized sources only\n" +
                        "• We do not provide, host, or facilitate access to any content\n" +
                        "• Use of this app for illegal activities is strictly prohibited\n" +
                        "• Content streaming may consume significant data - check your plan\n\n" +
                        "By continuing, you acknowledge your responsibility for legal compliance.")
                .setPositiveButton("I Understand & Accept", (dialog, which) -> {
                    disclaimerAccepted = true;
                    saveDisclaimerAcceptance();
                    proceedWithSetup();
                    dialog.dismiss();
                })
                .setNegativeButton("Exit App", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void proceedWithSetup() {
        setupTabs();
        checkPermissions();
    }

    private void initializeViews() {
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Media Player");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        } else if (id == R.id.action_legal) {
            showComplianceDisclaimer();
            return true;
        } else if (id == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("No", null)
                .show();
    }

    private void performLogout() {
        // Clear all SharedPreferences data
        getSharedPreferences(IPTV_PREFS, MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();

        // Clear any cached data in memory
        if (viewPager != null && viewPager.getAdapter() != null) {
            ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
            if (adapter != null) {
                for (Fragment fragment : adapter.fragmentList) {
                    if (fragment instanceof IPTVFragment) {
                        ((IPTVFragment) fragment).clearData();
                    }
                }
            }
        }

        // Switch to IPTV tab to show login form
        if (viewPager != null) {
            viewPager.setCurrentItem(1); // Assuming IPTV tab is at index 1
        }
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About Media Player")
                .setMessage("Version 1.0\n\n" +
                        "A legal media player for your personal content.\n\n" +
                        "Features:\n" +
                        "• Local file playback\n" +
                        "• IPTV streaming support\n" +
                        "• Multiple format support\n" +
                        "• User-friendly interface\n\n" +
                        "Remember to only use legal content sources!")
                .setPositiveButton("OK", null)
                .show();
    }

    private void setupTabs() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new LocalFilesFragment(), "Local Files");
        adapter.addFragment(new IPTVFragment(), "IPTV");
        adapter.addFragment(new PlayerFragment(), "Player");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkModernPermissions();
        } else {
            checkLegacyPermissions();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkModernPermissions() {
        String[] permissions = {
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
        };

        boolean needsPermission = false;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                needsPermission = true;
                break;
            }
        }

        if (needsPermission) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private void checkLegacyPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Some permissions denied. Features may be limited.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final java.util.List<Fragment> fragmentList = new java.util.ArrayList<>();
        private final java.util.List<String> fragmentTitleList = new java.util.ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }
}