package com.feed.sphere.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.feed.sphere.fragments.IPTVFragment;
import com.feed.sphere.fragments.LocalFilesFragment;
import com.feed.sphere.fragments.PlayerFragment;
import com.feed.sphere.R;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "MediaPlayerPrefs";
    private static final String DISCLAIMER_ACCEPTED = "disclaimer_accepted";
    private static final String IPTV_PREFS = "iptv_prefs";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private boolean disclaimerAccepted = false;

    // Fragment instances
    private LocalFilesFragment localFilesFragment;
    private IPTVFragment iptvFragment;
    private PlayerFragment playerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeStatusBarTransparent();
        setContentView(R.layout.activity_main);

        initializeViews();
        setupToolbar();
        setupNavigationDrawer();

        if (!isDisclaimerAccepted()) {
            showComplianceDisclaimer();
        } else {
            disclaimerAccepted = true;
            proceedWithSetup();
        }
    }

    private void makeStatusBarTransparent() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        window.setStatusBarColor(getResources().getColor(R.color.primary));
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
        String bulletPoints = "• This media player is a neutral tool for playing your own content\n" +
                "• You are solely responsible for ensuring all content you play is legally obtained\n" +
                "• Do not use this app to access pirated, copyrighted, or unauthorized content\n" +
                "• IPTV streams must be from legitimate, authorized sources only\n" +
                "• We do not provide, host, or facilitate access to any content\n" +
                "• Use of this app for illegal activities is strictly prohibited\n" +
                "• Content streaming may consume significant data - check your plan";

        String disclaimer = String.format(getString(R.string.disclaimer_text_format), bulletPoints);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Legal Compliance Notice")
                .setMessage(disclaimer)
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
        checkPermissions();
        // Load Local Files fragment by default after permissions
        loadLocalFilesFragment();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        // Tint the drawer icon here
        toggle.setDrawerArrowDrawable(new DrawerArrowDrawable(this) {{
            setColor(ContextCompat.getColor(MainActivity.this, R.color.white));
        }});
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_local_files) {
            loadLocalFilesFragment();
        } else if (id == R.id.nav_iptv) {
            loadIPTVFragment();
        } else if (id == R.id.nav_player) {
            loadPlayerFragment();
        }else if (id == R.id.action_about) {
            showAboutDialog();
        } else if (id == R.id.privacy_policy) {
            showPrivacyPolicy();
        } else if (id == R.id.action_logout) {
            showLogoutConfirmation();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showPrivacyPolicy() {
        String url = "https://sites.google.com/view/usrprivacypolicy/";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        // Optionally check if there is a browser to handle the intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "No web browser found to open the link.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLocalFilesFragment() {
        if (localFilesFragment == null) {
            localFilesFragment = new LocalFilesFragment();
        }
        loadFragment(localFilesFragment, "Local Files");
    }

    private void loadIPTVFragment() {
        if (iptvFragment == null) {
            iptvFragment = new IPTVFragment();
        }
        loadFragment(iptvFragment, "IPTV");
    }

    private void loadPlayerFragment() {
        if (playerFragment == null) {
            playerFragment = new PlayerFragment();
        }
        loadFragment(playerFragment, "Player");
    }

    private void loadFragment(Fragment fragment, String title) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();

        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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

        // Clear cached data in fragments
        if (iptvFragment != null) {
            iptvFragment.clearData();
        }

        // Reset fragments
        localFilesFragment = null;
        iptvFragment = null;
        playerFragment = null;

        // Load local files fragment again
        loadLocalFilesFragment();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
        builder.setTitle(R.string.app_name)
                .setMessage(version+"\n\n" +
                        "A legal media player for your personal content.\n\n" +
                        "Features:\n" +
                        "• Local file playback\n" +
                        "• Network streaming support\n" +
                        "• Multiple format support\n" +
                        "• User-friendly interface\n\n" +
                        "Remember to only use legal content sources!")
                .setPositiveButton("OK", null)
                .show();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
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
                // Load Local Files fragment after permissions are granted
                loadLocalFilesFragment();
            } else {
                Toast.makeText(this, "Some permissions denied. Features may be limited.", Toast.LENGTH_LONG).show();
                // Still load Local Files fragment even if some permissions are denied
                loadLocalFilesFragment();
            }
        }
    }
}