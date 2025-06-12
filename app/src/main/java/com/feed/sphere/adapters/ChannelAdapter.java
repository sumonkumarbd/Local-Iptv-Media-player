package com.feed.sphere.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.feed.sphere.R;
import com.feed.sphere.models.Channel;
import com.feed.sphere.utils.FavoriteManager;

import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder> {
    private final List<Channel> channels;
    private final OnChannelClickListener listener;
    private final FavoriteManager favoriteManager;

    public interface OnChannelClickListener {
        void onChannelClick(Channel channel);
    }

    public ChannelAdapter(List<Channel> channels, OnChannelClickListener listener, FavoriteManager favoriteManager) {
        this.channels = channels;
        this.listener = listener;
        this.favoriteManager = favoriteManager;
    }

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_channel, parent, false);
        return new ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
        Channel channel = channels.get(position);
        holder.bind(channel);
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    class ChannelViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivChannelIcon;
        private final TextView tvChannelName;
        private final ImageButton btnFavorite;

        ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivChannelIcon = itemView.findViewById(R.id.ivChannelIcon);
            tvChannelName = itemView.findViewById(R.id.tvChannelName);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }

        void bind(Channel channel) {
            tvChannelName.setText(channel.getName());

            // Load channel icon using Glide
            if (channel.getStreamIcon() != null && !channel.getStreamIcon().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(channel.getStreamIcon())
                        .placeholder(R.drawable.ic_channel_placeholder)
                        .error(R.drawable.ic_channel_placeholder)
                        .into(ivChannelIcon);
            } else {
                ivChannelIcon.setImageResource(R.drawable.ic_channel_placeholder);
            }

            // Set favorite button state
            updateFavoriteButton(channel);

            // Set click listeners
            itemView.setOnClickListener(v -> listener.onChannelClick(channel));
            btnFavorite.setOnClickListener(v -> {
                favoriteManager.toggleFavorite(channel);
                updateFavoriteButton(channel);
            });
        }

        private void updateFavoriteButton(Channel channel) {
            btnFavorite.setImageResource(channel.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        }
    }
}