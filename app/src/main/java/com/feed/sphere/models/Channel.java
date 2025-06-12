package com.feed.sphere.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Channel implements Parcelable {
    private final String streamId;
    private final String name;
    private final String streamIcon;
    private final String epgChannelId;
    private final String added;
    private final String categoryId;
    private final String customSid;
    private final String tvArchive;
    private final String directSource;
    private final String tvArchiveDuration;
    private boolean isFavorite;

    public Channel(String streamId, String name, String streamIcon, String epgChannelId,
            String added, String categoryId, String customSid, String tvArchive,
            String directSource, String tvArchiveDuration) {
        this.streamId = streamId;
        this.name = name;
        this.streamIcon = streamIcon;
        this.epgChannelId = epgChannelId;
        this.added = added;
        this.categoryId = categoryId;
        this.customSid = customSid;
        this.tvArchive = tvArchive;
        this.directSource = directSource;
        this.tvArchiveDuration = tvArchiveDuration;
        this.isFavorite = false;
    }

    protected Channel(Parcel in) {
        streamId = in.readString();
        name = in.readString();
        streamIcon = in.readString();
        epgChannelId = in.readString();
        added = in.readString();
        categoryId = in.readString();
        customSid = in.readString();
        tvArchive = in.readString();
        directSource = in.readString();
        tvArchiveDuration = in.readString();
        isFavorite = in.readByte() != 0;
    }

    public static final Creator<Channel> CREATOR = new Creator<Channel>() {
        @Override
        public Channel createFromParcel(Parcel in) {
            return new Channel(in);
        }

        @Override
        public Channel[] newArray(int size) {
            return new Channel[size];
        }
    };

    public String getStreamId() {
        return streamId;
    }

    public String getName() {
        return name;
    }

    public String getStreamIcon() {
        return streamIcon;
    }

    public String getEpgChannelId() {
        return epgChannelId;
    }

    public String getAdded() {
        return added;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCustomSid() {
        return customSid;
    }

    public String getTvArchive() {
        return tvArchive;
    }

    public String getDirectSource() {
        return directSource;
    }

    public String getTvArchiveDuration() {
        return tvArchiveDuration;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(streamId);
        dest.writeString(name);
        dest.writeString(streamIcon);
        dest.writeString(epgChannelId);
        dest.writeString(added);
        dest.writeString(categoryId);
        dest.writeString(customSid);
        dest.writeString(tvArchive);
        dest.writeString(directSource);
        dest.writeString(tvArchiveDuration);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
    }
}