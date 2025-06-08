package com.example.mediaplayer;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.ui.PlayerView;

import java.util.HashMap;
import java.util.Map;

public class VideoPlayerActivity extends AppCompatActivity {
    private static final String TAG = "VideoPlayerActivity";
    private PlayerView playerView;
    private ProgressBar progressBar;
    private TextView tvError;
    private ImageButton btnRetry;
    private ExoPlayer player;
    private String mediaPath;
    private String mediaTitle;
    private boolean isStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // Get media details from intent
        mediaPath = getIntent().getStringExtra("media_path");
        mediaTitle = getIntent().getStringExtra("media_title");
        isStream = getIntent().getBooleanExtra("is_stream", false);

        if (mediaPath == null) {
            Log.e(TAG, "No media path provided");
            finish();
            return;
        }

        initializeViews();
        setupPlayer();
    }

    private void initializeViews() {
        playerView = findViewById(R.id.playerView);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        btnRetry = findViewById(R.id.btnRetry);

        // Set title
        if (mediaTitle != null) {
            setTitle(mediaTitle);
        }

        // Setup retry button
        btnRetry.setOnClickListener(v -> {
            hideError();
            setupPlayer();
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupPlayer() {
        if (mediaPath == null || mediaPath.isEmpty()) {
            showError("Invalid media URL");
            return;
        }

        try {
            Log.d(TAG, "Setting up player with path: " + mediaPath);

            // Create player
            player = new ExoPlayer.Builder(this)
                    .setHandleAudioBecomingNoisy(true)
                    .build();
            playerView.setPlayer(player);

            // Create media source factory with headers for streams
            DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory()
                    .setAllowCrossProtocolRedirects(true);

            if (isStream) {
                // Add headers for IPTV streams
                dataSourceFactory.setDefaultRequestProperties(new java.util.HashMap<String, String>() {
                    {
                        put("User-Agent", "ExoPlayer");
                        put("Accept", "*/*");
                        put("Connection", "keep-alive");
                    }
                });
            }

            // Determine MIME type based on file extension
            String mimeType = null;
            if (mediaPath.toLowerCase().endsWith(".m3u8")) {
                mimeType = "application/x-mpegURL";
            } else if (mediaPath.toLowerCase().endsWith(".mp4")) {
                mimeType = "video/mp4";
            } else if (mediaPath.toLowerCase().endsWith(".mkv")) {
                mimeType = "video/x-matroska";
            }

            // Create media item
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(Uri.parse(mediaPath))
                    .setMimeType(mimeType)
                    .build();

            // Set media item to player
            player.setMediaItem(mediaItem);

            // Add listeners
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    switch (state) {
                        case Player.STATE_BUFFERING:
                            showLoading(true);
                            break;
                        case Player.STATE_READY:
                            showLoading(false);
                            hideError();
                            break;
                        case Player.STATE_ENDED:
                            showLoading(false);
                            break;
                        case Player.STATE_IDLE:
                            showLoading(false);
                            break;
                    }
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    Log.e(TAG, "Player error: " + error.getMessage(), error);
                    showError("Error playing media: " + error.getMessage());
                }
            });

            // Prepare and play
            player.prepare();
            player.play();

        } catch (Exception e) {
            Log.e(TAG, "Error setting up player", e);
            showError("Error loading media: " + e.getMessage());
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.VISIBLE);
        playerView.setVisibility(View.GONE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
        playerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.play();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
