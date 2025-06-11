package com.feed.mediaplayer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.feed.mediaplayer.R;
import com.feed.mediaplayer.models.Series;

import java.util.List;

public class SeriesAdapter extends RecyclerView.Adapter<SeriesAdapter.SeriesViewHolder> {
    private final List<Series> seriesList;
    private final OnSeriesClickListener listener;

    public interface OnSeriesClickListener {
        void onSeriesClick(Series series);
    }

    public SeriesAdapter(List<Series> seriesList, OnSeriesClickListener listener) {
        this.seriesList = seriesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_series, parent, false);
        return new SeriesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeriesViewHolder holder, int position) {
        Series series = seriesList.get(position);
        holder.bind(series, listener);
    }

    @Override
    public int getItemCount() {
        return seriesList.size();
    }

    static class SeriesViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPoster;
        private final TextView tvTitle;
        private final TextView tvYear;
        private final TextView tvRating;

        public SeriesViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvYear = itemView.findViewById(R.id.tvYear);
            tvRating = itemView.findViewById(R.id.tvRating);
        }

        public void bind(Series series, OnSeriesClickListener listener) {
            tvTitle.setText(series.getName());
            tvYear.setText(series.getReleaseDate());

            // Handle rating display
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

            // Load series poster using Glide
            Glide.with(itemView.getContext())
                    .load(series.getCover())
                    .placeholder(R.drawable.ic_series_placeholder)
                    .error(R.drawable.ic_series_placeholder)
                    .into(ivPoster);

            itemView.setOnClickListener(v -> listener.onSeriesClick(series));
        }
    }
}