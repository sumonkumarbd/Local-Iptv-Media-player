package com.example.mediaplayer;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class VideoPlayerActivity extends AppCompatActivity {
    private VideoView videoView;
    private MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // Hide status bar for full-screen experience
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        videoView = findViewById(R.id.videoView);

        String mediaPath = getIntent().getStringExtra("media_path");
        String mediaTitle = getIntent().getStringExtra("media_title");
        boolean isStream = getIntent().getBooleanExtra("is_stream", false);

        if (mediaPath != null) {
            setupVideoPlayer(mediaPath, mediaTitle, isStream);
        } else {
            Toast.makeText(this, "Error: No media file selected", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupVideoPlayer(String path, String title, boolean isStream) {
        try {
            Uri uri = Uri.parse(path);
            videoView.setVideoURI(uri);

            mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);

            videoView.setOnPreparedListener(mp -> {
                if (title != null) {
                    setTitle(title);
                }
                videoView.start();
            });

            videoView.setOnErrorListener((mp, what, extra) -> {
                String errorMsg = isStream ?
                        "Error playing stream. Check your internet connection and stream URL." :
                        "Error playing media file. Format may not be supported.";
                Toast.makeText(VideoPlayerActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                return false;
            });

            videoView.setOnCompletionListener(mp -> {
                Toast.makeText(this, "Playback completed", Toast.LENGTH_SHORT).show();
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error loading media: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null) {
            videoView.resume();
        }
    }
}
