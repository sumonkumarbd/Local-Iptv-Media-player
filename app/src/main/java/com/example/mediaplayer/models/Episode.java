package com.example.mediaplayer.models;

public class Episode {
    private final String id;
    private final String episodeNum;
    private final String title;
    private final String containerExtension;
    private final String info;
    private final String subtitles;
    private final String customSid;
    private final String added;
    private final String season;
    private final String directSource;

    public Episode(String id, String episodeNum, String title, String containerExtension,
                  String info, String subtitles, String customSid, String added,
                  String season, String directSource) {
        this.id = id;
        this.episodeNum = episodeNum;
        this.title = title;
        this.containerExtension = containerExtension;
        this.info = info;
        this.subtitles = subtitles;
        this.customSid = customSid;
        this.added = added;
        this.season = season;
        this.directSource = directSource;
    }

    public String getId() { return id; }
    public String getEpisodeNum() { return episodeNum; }
    public String getTitle() { return title; }
    public String getContainerExtension() { return containerExtension; }
    public String getInfo() { return info; }
    public String getSubtitles() { return subtitles; }
    public String getCustomSid() { return customSid; }
    public String getAdded() { return added; }
    public String getSeason() { return season; }
    public String getDirectSource() { return directSource; }

    @Override
    public String toString() {
        return title;
    }
} 