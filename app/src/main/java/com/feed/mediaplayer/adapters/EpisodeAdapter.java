package com.feed.mediaplayer.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.feed.mediaplayer.R;
import com.feed.mediaplayer.activities.VideoPlayerActivity;
import com.feed.mediaplayer.api.IPTVService;
import com.feed.mediaplayer.models.Episode;

import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder> {
    private final List<Episode> episodes;
    private final Context context;
    private final IPTVService iptvService;
    private final String seriesName;

    public EpisodeAdapter(Context context, List<Episode> episodes, IPTVService iptvService, String seriesName) {
        this.context = context;
        this.episodes = episodes;
        this.iptvService = iptvService;
        this.seriesName = seriesName;
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_episode, parent, false);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        Episode episode = episodes.get(position);
        holder.bind(episode);
    }

    @Override
    public int getItemCount() {
        return episodes.size();
    }

    class EpisodeViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvPlot;

        EpisodeViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPlot = itemView.findViewById(R.id.tvPlot);
        }

        void bind(Episode episode) {
            // Set episode title (e.g., "S01E01 - Episode Title")
            String title = String.format("S%sE%s - %s",
                    episode.getSeason(),
                    episode.getEpisodeNum(),
                    episode.getTitle());
            tvTitle.setText(title);

            // Set episode plot if available
            if (episode.getInfo() != null && !episode.getInfo().isEmpty()) {
                tvPlot.setText(episode.getInfo());
                tvPlot.setVisibility(View.VISIBLE);
            } else {
                tvPlot.setVisibility(View.GONE);
            }

            // Set click listener to play episode
            itemView.setOnClickListener(v -> {
                String streamUrl = iptvService.getSeriesUrl(
                    episode.getId(),
                    episode.getContainerExtension() != null ? episode.getContainerExtension() : "mp4"
                );

                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("media_path", streamUrl);
                intent.putExtra("media_title", seriesName + " - " + title);
                intent.putExtra("is_stream", true);
                context.startActivity(intent);
            });
        }
    }
}