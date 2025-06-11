package com.feed.sphere.fragments;

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

import com.feed.sphere.R;
import com.feed.sphere.activities.VideoPlayerActivity;
import com.feed.sphere.adapters.CategoryAdapter;
import com.feed.sphere.adapters.MovieAdapter;
import com.feed.sphere.api.IPTVService;
import com.feed.sphere.models.Category;
import com.feed.sphere.models.Movie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

public class MoviesFragment extends Fragment {
    private static final String TAG = "MoviesFragment";
    private static final String ARG_IPTV_SERVICE = "iptv_service";

    private IPTVService iptvService;
    private RecyclerView categoriesRecyclerView;
    private RecyclerView moviesRecyclerView;
    private ProgressBar progressBar;
    private CategoryAdapter categoryAdapter;
    private MovieAdapter movieAdapter;
    private List<Category> categories = new ArrayList<>();
    private List<Movie> movies = new ArrayList<>();

    public static MoviesFragment newInstance(IPTVService service) {
        MoviesFragment fragment = new MoviesFragment();
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
        View view = inflater.inflate(R.layout.fragment_movies, container, false);
        initializeViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerViews
        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView);
        moviesRecyclerView = view.findViewById(R.id.moviesRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup categories RecyclerView
        categoriesRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(categories, this::onCategorySelected);
        categoriesRecyclerView.setAdapter(categoryAdapter);

        // Setup movies RecyclerView
        moviesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        movieAdapter = new MovieAdapter(requireContext(), movies, iptvService);
        moviesRecyclerView.setAdapter(movieAdapter);

        // Load categories
        loadCategories();
    }

    private void initializeViews(View view) {
        // Initialize other views if needed
    }

    private void loadCategories() {
        Log.d(TAG, "Loading movie categories...");
        new Thread(() -> {
            try {
                List<Category> loadedCategories = iptvService.getVodCategories();
                requireActivity().runOnUiThread(() -> {
                    categories.clear();
                    categories.addAll(loadedCategories);
                    categoryAdapter.notifyDataSetChanged();

                    // Select first category by default
                    if (!categories.isEmpty()) {
                        onCategorySelected(categories.get(0));
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading categories", e);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(),
                        "Error loading categories: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void onCategorySelected(Category category) {
        showLoading(true);
        new Thread(() -> {
            try {
                Log.d(TAG, "Loading movies for category: " + category.getCategoryName());
                List<Movie> categoryMovies = iptvService.getVodStreams(category.getCategoryId());
                Log.d(TAG, "Loaded " + categoryMovies.size() + " movies");

                requireActivity().runOnUiThread(() -> {
                    movies.clear();
                    movies.addAll(categoryMovies);
                    movieAdapter.notifyDataSetChanged();
                    showLoading(false);

                    if (categoryMovies.isEmpty()) {
                        Toast.makeText(getContext(), "No movies found in this category", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error loading movies", e);
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Error loading movies: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                });
            }
        }).start();
    }

    private void onMovieSelected(Movie movie) {
        String streamUrl = iptvService.getMovieUrl(movie.getStreamId(),
                movie.getContainerExtension() != null ? movie.getContainerExtension() : "mp4");
        Log.d(TAG, "Selected movie: " + movie.getName() + ", Stream URL: " + streamUrl);

        Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
        intent.putExtra("media_path", streamUrl);
        intent.putExtra("media_title", movie.getName());
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
        movies.clear();

        // Notify adapters
        if (categoryAdapter != null) {
            categoryAdapter.notifyDataSetChanged();
        }
        if (movieAdapter != null) {
            movieAdapter.notifyDataSetChanged();
        }

        // Hide loading indicator
        showLoading(false);
    }
}