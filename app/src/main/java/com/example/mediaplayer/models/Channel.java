package com.example.mediaplayer.models;

public class Channel {
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
    }

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

    @Override
    public String toString() {
        return name;
    }
}