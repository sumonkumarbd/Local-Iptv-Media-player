package com.feed.sphere.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.feed.sphere.models.Channel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoriteManager {
    private static final String TAG = "FavoriteManager";
    private static final String PREF_NAME = "favorites";
    private static final String KEY_FAVORITE_CHANNELS = "favorite_channels";
    private static FavoriteManager instance;
    private final SharedPreferences preferences;
    private final Set<String> favoriteChannelIds;

    private FavoriteManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        favoriteChannelIds = new HashSet<>(preferences.getStringSet(KEY_FAVORITE_CHANNELS, new HashSet<>()));
    }

    public static synchronized FavoriteManager getInstance(Context context) {
        if (instance == null) {
            instance = new FavoriteManager(context);
        }
        return instance;
    }

    public boolean isFavorite(Channel channel) {
        return favoriteChannelIds.contains(channel.getStreamId());
    }

    public void toggleFavorite(Channel channel) {
        String channelId = channel.getStreamId();
        if (favoriteChannelIds.contains(channelId)) {
            favoriteChannelIds.remove(channelId);
            channel.setFavorite(false);
        } else {
            favoriteChannelIds.add(channelId);
            channel.setFavorite(true);
        }
        saveFavorites();
    }

    public void setFavorite(Channel channel, boolean isFavorite) {
        String channelId = channel.getStreamId();
        if (isFavorite) {
            favoriteChannelIds.add(channelId);
        } else {
            favoriteChannelIds.remove(channelId);
        }
        channel.setFavorite(isFavorite);
        saveFavorites();
    }

    private void saveFavorites() {
        try {
            preferences.edit()
                    .putStringSet(KEY_FAVORITE_CHANNELS, new HashSet<>(favoriteChannelIds))
                    .apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving favorites", e);
        }
    }

    public void loadFavorites(List<Channel> channels) {
        for (Channel channel : channels) {
            channel.setFavorite(favoriteChannelIds.contains(channel.getStreamId()));
        }
    }
}