package com.feed.sphere.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.feed.sphere.R;
import com.feed.sphere.models.Channel;
import java.util.List;

public class ChannelListAdapter extends RecyclerView.Adapter<ChannelListAdapter.ChannelViewHolder> {
    private List<Channel> channels;
    private OnChannelClickListener listener;
    private int selectedPosition = -1;

    public interface OnChannelClickListener {
        void onChannelClick(Channel channel, int position);
    }

    public ChannelListAdapter(List<Channel> channels, OnChannelClickListener listener) {
        this.channels = channels;
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        int previousSelected = selectedPosition;
        selectedPosition = position;

        // Notify changes for both old and new selected items
        if (previousSelected != -1) {
            notifyItemChanged(previousSelected);
        }
        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition);
        }
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
        holder.bind(channel, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return channels != null ? channels.size() : 0;
    }

    public class ChannelViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivChannelLogo;
        private TextView tvChannelName;
        private TextView tvChannelDescription;
        private View itemView;

        public ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            ivChannelLogo = itemView.findViewById(R.id.ivChannelIcon);
            tvChannelName = itemView.findViewById(R.id.tvChannelName);
            tvChannelDescription = itemView.findViewById(R.id.tvChannelDescription);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onChannelClick(channels.get(position), position);
                }
            });
        }

        public void bind(Channel channel, boolean isSelected) {
            if (channel == null)
                return;

            // Set channel name
            tvChannelName.setText(channel.getName() != null ? channel.getName() : "");

            // Hide description since Channel class doesn't have description
            tvChannelDescription.setVisibility(View.GONE);

            // Load channel logo
            String streamIcon = channel.getStreamIcon();
            if (streamIcon != null && !streamIcon.isEmpty()) {
                try {
                    Glide.with(itemView.getContext())
                            .load(streamIcon)
                            .placeholder(R.drawable.ic_tv_placeholder)
                            .error(R.drawable.ic_tv_placeholder)
                            .into(ivChannelLogo);
                } catch (Exception e) {
                    // If there's any error loading the image, show placeholder
                    ivChannelLogo.setImageResource(R.drawable.ic_tv_placeholder);
                }
            } else {
                ivChannelLogo.setImageResource(R.drawable.ic_tv_placeholder);
            }

            // Highlight selected channel
            if (isSelected) {
                itemView.setBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.selected_channel_background));
                tvChannelName
                        .setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.selected_channel_text));
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
                tvChannelName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.default_channel_text));
            }
        }
    }
}