package com.example.mediaplayer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mediaplayer.R;
import com.example.mediaplayer.models.Channel;

import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder> {
    private final List<Channel> channels;
    private final OnChannelClickListener listener;

    public interface OnChannelClickListener {
        void onChannelClick(Channel channel);
    }

    public ChannelAdapter(List<Channel> channels, OnChannelClickListener listener) {
        this.channels = channels;
        this.listener = listener;
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

        ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivChannelIcon = itemView.findViewById(R.id.ivChannelIcon);
            tvChannelName = itemView.findViewById(R.id.tvChannelName);
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

            itemView.setOnClickListener(v -> listener.onChannelClick(channel));
        }
    }
}