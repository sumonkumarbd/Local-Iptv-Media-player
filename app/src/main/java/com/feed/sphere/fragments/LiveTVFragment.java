package com.feed.sphere.fragments;

import android.content.Intent;
import android.os.Build;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.feed.sphere.R;
import com.feed.sphere.activities.VideoPlayerActivity;
import com.feed.sphere.adapters.CategoryAdapter;
import com.feed.sphere.adapters.ChannelAdapter;
import com.feed.sphere.api.IPTVService;
import com.feed.sphere.models.Category;
import com.feed.sphere.models.Channel;
import com.feed.sphere.utils.FavoriteManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONException;

public class LiveTVFragment extends Fragment {
    private static final String TAG = "LiveTVFragment";
    private static final String ARG_IPTV_SERVICE = "iptv_service";

    private RecyclerView rvCategories;
    private RecyclerView rvChannels;
    private RecyclerView rvFavorites;
    private ProgressBar progressBar;
    private TextView tvFavoritesTitle;
    private CategoryAdapter categoryAdapter;
    private ChannelAdapter channelAdapter;
    private ChannelAdapter favoritesAdapter;
    private IPTVService iptvService;
    private List<Category> categories = new ArrayList<>();
    private List<Channel> channels = new ArrayList<>();
    private List<Channel> favoriteChannels = new ArrayList<>();
    private FavoriteManager favoriteManager;

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
        favoriteManager = FavoriteManager.getInstance(requireContext());
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
        rvFavorites = view.findViewById(R.id.rvFavorites);
        progressBar = view.findViewById(R.id.progressBar);
        tvFavoritesTitle = view.findViewById(R.id.tvFavoritesTitle);
    }

    private void setupRecyclerViews() {
        // Setup Categories RecyclerView
        categoryAdapter = new CategoryAdapter(categories, this::onCategorySelected);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // Setup Channels RecyclerView
        channelAdapter = new ChannelAdapter(channels, this::onChannelSelected, favoriteManager);
        rvChannels.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvChannels.setAdapter(channelAdapter);

        // Setup Favorites RecyclerView
        favoritesAdapter = new ChannelAdapter(favoriteChannels, this::onChannelSelected, favoriteManager);
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFavorites.setAdapter(favoritesAdapter);
    }

    private void updateFavoritesList() {
        favoriteChannels.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            favoriteChannels.addAll(channels.stream()
                    .filter(Channel::isFavorite)
                    .collect(Collectors.toList()));
        }

        // Update visibility of favorites section
        boolean hasFavorites = !favoriteChannels.isEmpty();
        tvFavoritesTitle.setVisibility(hasFavorites ? View.VISIBLE : View.GONE);
        rvFavorites.setVisibility(hasFavorites ? View.VISIBLE : View.GONE);

        favoritesAdapter.notifyDataSetChanged();
    }

    private void loadCategories() {
        showLoading(true);
        new Thread(() -> {
            try {
                Log.d(TAG, "Loading categories...");
                List<Category> liveCategories = iptvService.getLiveCategories();
                Log.d(TAG, "Loaded " + liveCategories.size() + " categories");

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        categories.clear();
                        categories.addAll(liveCategories);
                        categoryAdapter.notifyDataSetChanged();
                        showLoading(false);

                        if (!categories.isEmpty()) {
                            onCategorySelected(categories.get(0));
                        } else {
                            Toast.makeText(getContext(), "No categories found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error loading categories", e);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(getContext(), "Error loading categories: " + e.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    });
                }
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

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        channels.clear();
                        channels.addAll(categoryChannels);

                        // Load favorites for the channels
                        favoriteManager.loadFavorites(channels);

                        channelAdapter.notifyDataSetChanged();
                        updateFavoritesList();
                        showLoading(false);

                        if (categoryChannels.isEmpty()) {
                            Toast.makeText(getContext(), "No channels found in this category", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                }
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error loading channels", e);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(getContext(), "Error loading channels: " + e.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    });
                }
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
        intent.putExtra("iptv_service", iptvService);

        // Pass the channel list and current channel index
        ArrayList<Channel> channelArrayList = new ArrayList<>(channels);
        intent.putParcelableArrayListExtra("channel_list", channelArrayList);
        intent.putExtra("current_channel_index", channels.indexOf(channel));

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
        favoriteChannels.clear();

        // Notify adapters
        if (categoryAdapter != null) {
            categoryAdapter.notifyDataSetChanged();
        }
        if (channelAdapter != null) {
            channelAdapter.notifyDataSetChanged();
        }
        if (favoritesAdapter != null) {
            favoritesAdapter.notifyDataSetChanged();
        }

        // Hide loading indicator
        showLoading(false);
    }
}