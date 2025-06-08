package com.example.mediaplayer.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mediaplayer.R;
import com.example.mediaplayer.VideoPlayerActivity;
import com.example.mediaplayer.api.IPTVService;
import com.example.mediaplayer.models.Movie;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private final List<Movie> movies;
    private final Context context;
    private final IPTVService iptvService;

    public MovieAdapter(Context context, List<Movie> movies, IPTVService iptvService) {
        this.context = context;
        this.movies = movies;
        this.iptvService = iptvService;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);

        // Load movie poster
        if (movie.getStreamIcon() != null && !movie.getStreamIcon().isEmpty()) {
            Glide.with(context)
                    .load(movie.getStreamIcon())
                    .placeholder(R.drawable.ic_movie_placeholder)
                    .error(R.drawable.ic_movie_placeholder)
                    .into(holder.posterView);
        } else {
            holder.posterView.setImageResource(R.drawable.ic_movie_placeholder);
        }

        // Set movie details
        holder.titleView.setText(movie.getName());
        holder.genreView.setText(movie.getGenre());
        holder.yearView.setText(movie.getReleaseDate());

        // Set rating if available
        if (movie.getRating5based() != null && !movie.getRating5based().isEmpty()) {
            holder.ratingView.setText(movie.getRating5based() + " ★");
            holder.ratingView.setVisibility(View.VISIBLE);
        } else {
            holder.ratingView.setVisibility(View.GONE);
        }

        // Set plot if available
        if (movie.getPlot() != null && !movie.getPlot().isEmpty()) {
            holder.plotView.setText(movie.getPlot());
            holder.plotView.setVisibility(View.VISIBLE);
        } else {
            holder.plotView.setVisibility(View.GONE);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            String streamUrl = iptvService.getMovieUrl(movie.getStreamId(),
                    movie.getContainerExtension() != null ? movie.getContainerExtension() : "mp4");

            Intent intent = new Intent(context, VideoPlayerActivity.class);
            intent.putExtra("media_path", streamUrl);
            intent.putExtra("media_title", movie.getName());
            intent.putExtra("is_stream", true);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        final ImageView posterView;
        final TextView titleView;
        final TextView genreView;
        final TextView yearView;
        final TextView ratingView;
        final TextView plotView;

        MovieViewHolder(View itemView) {
            super(itemView);
            posterView = itemView.findViewById(R.id.moviePoster);
            titleView = itemView.findViewById(R.id.movieTitle);
            genreView = itemView.findViewById(R.id.movieGenre);
            yearView = itemView.findViewById(R.id.movieYear);
            ratingView = itemView.findViewById(R.id.movieRating);
            plotView = itemView.findViewById(R.id.moviePlot);
        }
    }
}