package com.example.mediaplayer.models;

public class Movie {
    private final String streamId;
    private final String name;
    private final String streamIcon;
    private final String streamUrl;
    private final String added;
    private final String categoryId;
    private final String containerExtension;
    private final String customSid;
    private final String directSource;
    private final String tmdbId;
    private final String plot;
    private final String cast;
    private final String director;
    private final String genre;
    private final String releaseDate;
    private final String rating;
    private final String rating5based;
    private final String backdropPath;
    private final String youtubeTrailer;
    private final String episodeRunTime;

    public Movie(String streamId, String name, String streamIcon, String streamUrl,
            String added, String categoryId, String containerExtension,
            String customSid, String directSource,
            String tmdbId, String plot, String cast, String director, String genre,
            String releaseDate, String rating, String rating5based, String backdropPath,
            String youtubeTrailer, String episodeRunTime, String categoryId2) {
        this.streamId = streamId;
        this.name = name;
        this.streamIcon = streamIcon;
        this.streamUrl = streamUrl;
        this.added = added;
        this.categoryId = categoryId;
        this.containerExtension = containerExtension;
        this.customSid = customSid;
        this.directSource = directSource;
        this.tmdbId = tmdbId;
        this.plot = plot;
        this.cast = cast;
        this.director = director;
        this.genre = genre;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.rating5based = rating5based;
        this.backdropPath = backdropPath;
        this.youtubeTrailer = youtubeTrailer;
        this.episodeRunTime = episodeRunTime;
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

    public String getStreamUrl() {
        return streamUrl;
    }

    public String getAdded() {
        return added;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getContainerExtension() {
        return containerExtension;
    }

    public String getCustomSid() {
        return customSid;
    }

    public String getDirectSource() {
        return directSource;
    }

    public String getTmdbId() {
        return tmdbId;
    }

    public String getPlot() {
        return plot;
    }

    public String getCast() {
        return cast;
    }

    public String getDirector() {
        return director;
    }

    public String getGenre() {
        return genre;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getRating() {
        return rating;
    }

    public String getRating5based() {
        return rating5based;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public String getYoutubeTrailer() {
        return youtubeTrailer;
    }

    public String getEpisodeRunTime() {
        return episodeRunTime;
    }

    @Override
    public String toString() {
        return name;
    }
}