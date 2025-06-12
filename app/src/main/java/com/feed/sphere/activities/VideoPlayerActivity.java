package com.feed.sphere.activities;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.feed.sphere.R;
import com.feed.sphere.adapters.ChannelListAdapter;
import com.feed.sphere.models.Channel;
import com.feed.sphere.api.IPTVService;
import com.feed.sphere.utils.FavoriteManager;

import java.util.ArrayList;
import java.util.List;

import androidx.media3.exoplayer.DefaultLoadControl;

public class VideoPlayerActivity extends AppCompatActivity implements ChannelListAdapter.OnChannelClickListener {
    private static final String TAG = "VideoPlayerActivity";
    private PlayerView playerView;
    private ProgressBar progressBar;
    private TextView tvError;
    private ImageButton btnRetry;
    private ImageButton btnFullscreen;
    private ImageButton btnToggleList;
    private LinearLayout channelListContainer;
    private RecyclerView recyclerViewChannels;
    private ExoPlayer player;
    private String mediaPath;
    private String mediaTitle;
    private boolean isStream;
    private boolean isFullscreen = false;
    private List<Channel> channelList;
    private ChannelListAdapter channelAdapter;
    private int currentChannelIndex = 0;
    private IPTVService iptvService;
    private boolean isChannelListVisible = true;
    private boolean isControllerVisible = false;
    private FavoriteManager favoriteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // Initialize FavoriteManager
        favoriteManager = FavoriteManager.getInstance(this);

        // Get IPTVService from intent
        iptvService = (IPTVService) getIntent().getSerializableExtra("iptv_service");

        // Get media details from intent
        mediaPath = getIntent().getStringExtra("media_path");
        mediaTitle = getIntent().getStringExtra("media_title");
        isStream = getIntent().getBooleanExtra("is_stream", false);

        // Get channel list from intent (you'll need to pass this from the calling
        // activity)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            channelList = getIntent().getParcelableArrayListExtra("channel_list", Channel.class);
        } else {
            ArrayList<Parcelable> parcelableList = getIntent().getParcelableArrayListExtra("channel_list");
            if (parcelableList != null) {
                channelList = new ArrayList<>();
                for (Parcelable parcelable : parcelableList) {
                    channelList.add((Channel) parcelable);
                }
            }
        }
        currentChannelIndex = getIntent().getIntExtra("current_channel_index", 0);

        if (mediaPath == null) {
            Log.e(TAG, "No media path provided");
            finish();
            return;
        }

        // Initialize empty channel list if none provided
        if (channelList == null) {
            channelList = new ArrayList<>();
        }

        initializeViews();
        setupChannelList();
        setupPlayer();
    }

    private void initializeViews() {
        playerView = findViewById(R.id.playerView);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        btnRetry = findViewById(R.id.btnRetry);
        btnFullscreen = findViewById(R.id.btnFullscreen);
        btnToggleList = findViewById(R.id.btnToggleList);
        channelListContainer = findViewById(R.id.channelListContainer);
        recyclerViewChannels = findViewById(R.id.recyclerViewChannels);

        // Set title
        if (mediaTitle != null) {
            setTitle(mediaTitle);
        }

        // Setup retry button
        btnRetry.setOnClickListener(v -> {
            hideError();
            setupPlayer();
        });

        // Setup channel list toggle buttons
        btnFullscreen.setImageResource(R.drawable.ic_back);
        btnFullscreen.setOnClickListener(v -> toggleChannelList());
        btnToggleList.setOnClickListener(v -> toggleChannelList());
    }

    private void setupChannelList() {
        if (channelList != null && !channelList.isEmpty()) {
            channelAdapter = new ChannelListAdapter(channelList, this, favoriteManager);
            recyclerViewChannels.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewChannels.setAdapter(channelAdapter);

            // Load favorites for the channels
            favoriteManager.loadFavorites(channelList);

            // Highlight current channel
            channelAdapter.setSelectedPosition(currentChannelIndex);

            // Show channel list container
            channelListContainer.setVisibility(View.VISIBLE);
        } else {
            // Hide channel list if no channels available
            channelListContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onChannelClick(Channel channel, int position) {
        // Update current channel
        currentChannelIndex = position;
        String streamUrl = iptvService.getLiveStreamUrl(channel.getStreamId(), "m3u8");
        mediaPath = streamUrl;
        mediaTitle = channel.getName();

        // Update title
        setTitle(mediaTitle);

        // Highlight selected channel
        channelAdapter.setSelectedPosition(position);

        // Stop current player and setup new one
        if (player != null) {
            player.stop();
            player.clearMediaItems();
        }

        // Setup player with new channel
        setupPlayer();
    }

    private void toggleChannelList() {
        if (isChannelListVisible) {
            // Hide channel list
            channelListContainer.animate()
                    .translationX(-channelListContainer.getWidth())
                    .setDuration(300)
                    .withEndAction(() -> channelListContainer.setVisibility(View.GONE))
                    .start();
            // Show toggle button with same visibility as player controls
            btnToggleList.setVisibility(isControllerVisible ? View.VISIBLE : View.GONE);
            btnToggleList.setAlpha(0f);
            btnToggleList.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
            // Rotate back button
            btnFullscreen.animate()
                    .rotation(180)
                    .setDuration(300)
                    .start();
        } else {
            // Hide toggle button
            btnToggleList.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> btnToggleList.setVisibility(View.GONE))
                    .start();
            // Show channel list
            channelListContainer.setVisibility(View.VISIBLE);
            channelListContainer.setTranslationX(-channelListContainer.getWidth());
            channelListContainer.animate()
                    .translationX(0)
                    .setDuration(300)
                    .start();
            // Rotate back button
            btnFullscreen.animate()
                    .rotation(0)
                    .setDuration(300)
                    .start();
        }
        isChannelListVisible = !isChannelListVisible;
    }

    private void enterFullscreen() {
        isFullscreen = true;

        // Hide system UI
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Set landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Hide channel list if visible
        if (isChannelListVisible) {
            toggleChannelList();
        }

        // Hide system navigation
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void exitFullscreen() {
        isFullscreen = false;

        // Show system UI
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Show action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }

        // Set portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Show channel list if it was visible before
        if (!isChannelListVisible) {
            toggleChannelList();
        }

        // Show system navigation
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupPlayer() {
        if (mediaPath == null || mediaPath.isEmpty()) {
            showError("Invalid media URL");
            return;
        }

        try {
            Log.d(TAG, "Setting up player with path: " + mediaPath);

            // Create player if it doesn't exist
            if (player == null) {
                player = new ExoPlayer.Builder(this)
                        .setHandleAudioBecomingNoisy(true)
                        .setLoadControl(new DefaultLoadControl.Builder()
                                .setBufferDurationsMs(
                                        DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                                        DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS)
                                .setPrioritizeTimeOverSizeThresholds(true)
                                .build())
                        .build();
                playerView.setPlayer(player);

                // Set player view properties
                playerView.setUseController(true);
                playerView.setControllerVisibilityListener(new PlayerView.ControllerVisibilityListener() {
                    @Override
                    public void onVisibilityChanged(int visibility) {
                        isControllerVisible = visibility == View.VISIBLE;
                        // Sync list toggle button visibility with player controls
                        if (!isChannelListVisible) {
                            btnToggleList.setVisibility(visibility);
                        }
                    }
                });
            }

            // Create media source factory with headers for streams
            DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory()
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(30000)
                    .setReadTimeoutMs(30000)
                    .setDefaultRequestProperties(new java.util.HashMap<String, String>() {
                        {
                            put("User-Agent", "ExoPlayer");
                            put("Accept", "*/*");
                            put("Connection", "keep-alive");
                        }
                    });

            // Create media source based on content type
            MediaSource mediaSource;
            if (mediaPath.toLowerCase().endsWith(".m3u8")) {
                HlsMediaSource.Factory hlsFactory = new HlsMediaSource.Factory(dataSourceFactory)
                        .setAllowChunklessPreparation(true);
                mediaSource = hlsFactory.createMediaSource(MediaItem.fromUri(Uri.parse(mediaPath)));
            } else {
                DefaultMediaSourceFactory defaultFactory = new DefaultMediaSourceFactory(dataSourceFactory);
                mediaSource = defaultFactory.createMediaSource(MediaItem.fromUri(Uri.parse(mediaPath)));
            }

            // Set media source to player
            player.setMediaSource(mediaSource);

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
                            playerView.setVisibility(View.VISIBLE);
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
    public void onBackPressed() {
        if (isFullscreen) {
            exitFullscreen();
        } else if (!isChannelListVisible) {
            toggleChannelList();
        } else {
            super.onBackPressed();
        }
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