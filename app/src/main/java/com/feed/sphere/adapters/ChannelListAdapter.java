package com.feed.sphere.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.feed.sphere.R;
import com.feed.sphere.models.Channel;
import com.feed.sphere.utils.FavoriteManager;
import java.util.List;

public class ChannelListAdapter extends RecyclerView.Adapter<ChannelListAdapter.ChannelViewHolder> {
    private List<Channel> channels;
    private OnChannelClickListener listener;
    private int selectedPosition = -1;
    private FavoriteManager favoriteManager;
    private boolean isHorizontal = false;

    public interface OnChannelClickListener {
        void onChannelClick(Channel channel, int position);
    }

    public ChannelListAdapter(List<Channel> channels, OnChannelClickListener listener,
            FavoriteManager favoriteManager) {
        this.channels = channels;
        this.listener = listener;
        this.favoriteManager = favoriteManager;
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

    public void setHorizontal(boolean horizontal) {
        isHorizontal = horizontal;
    }

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = isHorizontal ? R.layout.item_player_fvt : R.layout.item_channel;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
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
        private ImageButton btnFavorite;
        private View itemView;

        public ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            ivChannelLogo = itemView.findViewById(R.id.ivChannelLogo);
            tvChannelName = itemView.findViewById(R.id.tvChannelName);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);

            // Only find description if it exists in the layout
            tvChannelDescription = itemView.findViewById(R.id.tvChannelDescription);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onChannelClick(channels.get(position), position);
                }
            });

            btnFavorite.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Channel channel = channels.get(position);
                    favoriteManager.toggleFavorite(channel);
                    updateFavoriteButton(channel);
                }
            });
        }

        public void bind(Channel channel, boolean isSelected) {
            if (channel == null)
                return;

            // Set channel name
            tvChannelName.setText(channel.getName() != null ? channel.getName() : "");

            // Hide description if it exists
            if (tvChannelDescription != null) {
                tvChannelDescription.setVisibility(View.GONE);
            }

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

            // Update favorite button state
            updateFavoriteButton(channel);

            // Highlight selected channel
            if (isSelected) {
                itemView.setBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.selected_channel_background));
                tvChannelName
                        .setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.selected_channel_text));
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.background));
                tvChannelName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.default_channel_text));
            }
        }

        private void updateFavoriteButton(Channel channel) {
            if (btnFavorite != null) {
                btnFavorite.setImageResource(
                        channel.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_favorite_outline);
            }
        }
    }
}