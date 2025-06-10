package com.example.mediaplayer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mediaplayer.R;
import com.example.mediaplayer.activities.VideoPlayerActivity;
import com.example.mediaplayer.adapters.CategoryAdapter;
import com.example.mediaplayer.adapters.ChannelAdapter;
import com.example.mediaplayer.api.IPTVService;
import com.example.mediaplayer.models.Category;
import com.example.mediaplayer.models.Channel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

public class LiveTVFragment extends Fragment {
    private static final String TAG = "LiveTVFragment";
    private static final String ARG_IPTV_SERVICE = "iptv_service";

    private RecyclerView rvCategories;
    private RecyclerView rvChannels;
    private ProgressBar progressBar;
    private CategoryAdapter categoryAdapter;
    private ChannelAdapter channelAdapter;
    private IPTVService iptvService;
    private List<Category> categories = new ArrayList<>();
    private List<Channel> channels = new ArrayList<>();

    public static LiveTVFragment newInstance(IPTVService service) {
        LiveTVFragment fragment = new LiveTVFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_IPTV_SERVICE, service);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            iptvService = (IPTVService) getArguments().getSerializable(ARG_IPTV_SERVICE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_tv, container, false);
        initializeViews(view);
        setupRecyclerViews();
        loadCategories();
        return view;
    }

    private void initializeViews(View view) {
        rvCategories = view.findViewById(R.id.rvCategories);
        rvChannels = view.findViewById(R.id.rvChannels);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerViews() {
        // Setup Categories RecyclerView
        categoryAdapter = new CategoryAdapter(categories, this::onCategorySelected);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // Setup Channels RecyclerView
        channelAdapter = new ChannelAdapter(channels, this::onChannelSelected);
        rvChannels.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChannels.setAdapter(channelAdapter);
    }

    private void loadCategories() {
        showLoading(true);
        new Thread(() -> {
            try {
                Log.d(TAG, "Loading categories...");
                List<Category> liveCategories = iptvService.getLiveCategories();
                Log.d(TAG, "Loaded " + liveCategories.size() + " categories");

                requireActivity().runOnUiThread(() -> {
                    categories.clear();
                    categories.addAll(liveCategories);
                    categoryAdapter.notifyDataSetChanged();
                    showLoading(false);

                    // Load channels for the first category if available
                    if (!categories.isEmpty()) {
                        onCategorySelected(categories.get(0));
                    } else {
                        Toast.makeText(getContext(), "No categories found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error loading categories", e);
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Error loading categories: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                });
            }
        }).start();
    }

    private void onCategorySelected(Category category) {
        showLoading(true);
        new Thread(() -> {
            try {
                Log.d(TAG, "Loading channels for category: " + category.getCategoryName());
                List<Channel> categoryChannels = iptvService.getLiveStreams(category.getCategoryId());
                Log.d(TAG, "Loaded " + categoryChannels.size() + " channels");

                requireActivity().runOnUiThread(() -> {
                    channels.clear();
                    channels.addAll(categoryChannels);
                    channelAdapter.notifyDataSetChanged();
                    showLoading(false);

                    if (categoryChannels.isEmpty()) {
                        Toast.makeText(getContext(), "No channels found in this category", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error loading channels", e);
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Error loading channels: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                });
            }
        }).start();
    }

    private void onChannelSelected(Channel channel) {
        String streamUrl = iptvService.getLiveStreamUrl(channel.getStreamId(), "m3u8");
        Log.d(TAG, "Selected channel: " + channel.getName() + ", Stream URL: " + streamUrl);

        Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
        intent.putExtra("media_path", streamUrl);
        intent.putExtra("media_title", channel.getName());
        intent.putExtra("is_stream", true);
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void clearData() {
        // Clear the IPTV service instance
        iptvService = null;

        // Clear the data lists
        categories.clear();
        channels.clear();

        // Notify adapters
        if (categoryAdapter != null) {
            categoryAdapter.notifyDataSetChanged();
        }
        if (channelAdapter != null) {
            channelAdapter.notifyDataSetChanged();
        }

        // Hide loading indicator
        showLoading(false);
    }
}