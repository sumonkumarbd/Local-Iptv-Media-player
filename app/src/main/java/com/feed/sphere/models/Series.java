package com.feed.sphere.models;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Series {
    private final String seriesId;
    private final String name;
    private final String cover;
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
    private final String categoryId;
    private final Map<String, List<Episode>> episodes;

    public Series(String seriesId, String name, String cover, String plot,
            String cast, String director, String genre, String releaseDate,
            String rating, String rating5based, String backdropPath,
            String youtubeTrailer, String episodeRunTime, String categoryId) {
        this.seriesId = seriesId;
        this.name = name;
        this.cover = cover;
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
        this.categoryId = categoryId;
        this.episodes = new HashMap<>();
    }

    public void addEpisode(String season, Episode episode) {
        episodes.computeIfAbsent(season, k -> new ArrayList<>()).add(episode);
    }

    public List<String> getSeasons() {
        return new ArrayList<>(episodes.keySet());
    }

    public List<Episode> getEpisodes(String season) {
        return episodes.getOrDefault(season, new ArrayList<>());
    }

    public String getSeriesId() {
        return seriesId;
    }

    public String getName() {
        return name;
    }

    public String getCover() {
        return cover;
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

    public String getCategoryId() {
        return categoryId;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}