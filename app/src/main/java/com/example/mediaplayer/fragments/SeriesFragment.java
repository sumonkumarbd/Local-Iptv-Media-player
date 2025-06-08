package com.example.mediaplayer.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mediaplayer.R;
import com.example.mediaplayer.activities.VideoPlayerActivity;
import com.example.mediaplayer.adapters.CategoryAdapter;
import com.example.mediaplayer.adapters.EpisodeAdapter;
import com.example.mediaplayer.adapters.SeriesAdapter;
import com.example.mediaplayer.api.IPTVService;
import com.example.mediaplayer.models.Category;
import com.example.mediaplayer.models.Episode;
import com.example.mediaplayer.models.Series;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;

public class SeriesFragment extends Fragment {
    private static final String TAG = "SeriesFragment";
    private static final String ARG_IPTV_SERVICE = "iptv_service";

    private RecyclerView rvCategories;
    private RecyclerView rvSeries;
    private ProgressBar progressBar;
    private CategoryAdapter categoryAdapter;
    private SeriesAdapter seriesAdapter;
    private IPTVService iptvService;
    private List<Category> categories = new ArrayList<>();
    private List<Series> seriesList = new ArrayList<>();

    public static SeriesFragment newInstance(IPTVService service) {
        SeriesFragment fragment = new SeriesFragment();
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
        View view = inflater.inflate(R.layout.fragment_series, container, false);
        initializeViews(view);
        setupRecyclerViews();
        loadCategories();
        return view;
    }

    private void initializeViews(View view) {
        rvCategories = view.findViewById(R.id.rvCategories);
        rvSeries = view.findViewById(R.id.rvSeries);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerViews() {
        // Setup Categories RecyclerView
        categoryAdapter = new CategoryAdapter(categories, this::onCategorySelected);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // Setup Series RecyclerView
        seriesAdapter = new SeriesAdapter(seriesList, this::onSeriesSelected);
        rvSeries.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSeries.setAdapter(seriesAdapter);
    }

    private void loadCategories() {
        showLoading(true);
        new Thread(() -> {
            try {
                Log.d(TAG, "Loading series categories...");
                List<Category> seriesCategories = iptvService.getSeriesCategories();
                Log.d(TAG, "Loaded " + seriesCategories.size() + " categories");

                requireActivity().runOnUiThread(() -> {
                    categories.clear();
                    categories.addAll(seriesCategories);
                    categoryAdapter.notifyDataSetChanged();
                    showLoading(false);

                    // Load series for the first category if available
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
                Log.d(TAG, "Loading series for category: " + category.getCategoryName());
                List<Series> categorySeries = iptvService.getSeries(category.getCategoryId());
                Log.d(TAG, "Loaded " + categorySeries.size() + " series");

                requireActivity().runOnUiThread(() -> {
                    seriesList.clear();
                    seriesList.addAll(categorySeries);
                    seriesAdapter.notifyDataSetChanged();
                    showLoading(false);

                    if (categorySeries.isEmpty()) {
                        Toast.makeText(getContext(), "No series found in this category", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error loading series", e);
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Error loading series: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                });
            }
        }).start();
    }

    private void onSeriesSelected(Series series) {
        showLoading(true);
        new Thread(() -> {
            try {
                Log.d(TAG, "Loading series info for: " + series.getName());
                Series seriesInfo = iptvService.getSeriesInfo(series.getSeriesId());
                Log.d(TAG, "Loaded series info with " + seriesInfo.getSeasons().size() + " seasons");

                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    showSeriesDetailsDialog(seriesInfo);
                });
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error loading series info", e);
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Error loading series info: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                });
            }
        }).start();
    }

    private void showSeriesDetailsDialog(Series series) {
        // Create dialog
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_series_details);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Initialize views
        ImageView ivPoster = dialog.findViewById(R.id.ivPoster);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvYear = dialog.findViewById(R.id.tvYear);
        TextView tvRating = dialog.findViewById(R.id.tvRating);
        TextView tvGenre = dialog.findViewById(R.id.tvGenre);
        TextView tvDirector = dialog.findViewById(R.id.tvDirector);
        TextView tvCast = dialog.findViewById(R.id.tvCast);
        TextView tvPlot = dialog.findViewById(R.id.tvPlot);
        RecyclerView rvEpisodes = dialog.findViewById(R.id.rvEpisodes);

        // Set series info
        tvTitle.setText(series.getName());
        tvYear.setText(series.getReleaseDate());
        tvGenre.setText(series.getGenre());
        tvDirector.setText(series.getDirector());
        tvCast.setText(series.getCast());
        tvPlot.setText(series.getPlot());

        // Set rating if available
        String rating = series.getRating5based();
        if (rating != null && !rating.isEmpty()) {
            try {
                float ratingValue = Float.parseFloat(rating);
                tvRating.setText(String.format("%.1f ★", ratingValue));
                tvRating.setVisibility(View.VISIBLE);
            } catch (NumberFormatException e) {
                tvRating.setVisibility(View.GONE);
            }
        } else {
            tvRating.setVisibility(View.GONE);
        }

        // Load series poster
        Glide.with(requireContext())
                .load(series.getCover())
                .placeholder(R.drawable.ic_series_placeholder)
                .error(R.drawable.ic_series_placeholder)
                .into(ivPoster);

        // Setup episodes RecyclerView
        rvEpisodes.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Get all episodes from all seasons
        List<Episode> allEpisodes = new ArrayList<>();
        for (String season : series.getSeasons()) {
            allEpisodes.addAll(series.getEpisodes(season));
        }

        // Sort episodes by season and episode number
        Collections.sort(allEpisodes, (e1, e2) -> {
            int seasonCompare = e1.getSeason().compareTo(e2.getSeason());
            if (seasonCompare != 0) {
                return seasonCompare;
            }
            return e1.getEpisodeNum().compareTo(e2.getEpisodeNum());
        });

        EpisodeAdapter episodeAdapter = new EpisodeAdapter(requireContext(), allEpisodes, iptvService,
                series.getName());
        rvEpisodes.setAdapter(episodeAdapter);

        // Show dialog
        dialog.show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void clearData() {
        // Clear the IPTV service instance
        iptvService = null;

        // Clear the data lists
        categories.clear();
        seriesList.clear();

        // Notify adapters
        if (categoryAdapter != null) {
            categoryAdapter.notifyDataSetChanged();
        }
        if (seriesAdapter != null) {
            seriesAdapter.notifyDataSetChanged();
        }

        // Hide loading indicator
        showLoading(false);
    }
}