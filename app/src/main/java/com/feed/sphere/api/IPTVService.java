package com.feed.sphere.api;

import android.util.Log;

import com.feed.sphere.models.Category;
import com.feed.sphere.models.Channel;
import com.feed.sphere.models.Episode;
import com.feed.sphere.models.Movie;
import com.feed.sphere.models.Series;
import com.feed.sphere.models.UserInfo;
import com.feed.sphere.utils.IPTVAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IPTVService implements Serializable {
    private static final String TAG = "IPTVService";
    private static final long serialVersionUID = 1L;
    private static final int TIMEOUT = 15000; // 15 seconds timeout

    private final String baseUrl;
    private final String username;
    private final String password;
    private final boolean isXUI;
    private final IPTVAPI iptvAPI;

    public IPTVService(String baseUrl, String username, String password, boolean isXUI) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.isXUI = isXUI;
        this.iptvAPI = new IPTVAPI();
    }

    public UserInfo getUserInfo() throws IOException, JSONException {
        String url = IPTVAPI.getLoginUrl(baseUrl, username, password);
        String response = makeRequest(url);
        return parseUserInfo(response);
    }

    public List<Category> getLiveCategories() throws IOException, JSONException {
        String url = IPTVAPI.getDataUrl(baseUrl, username, password, "get_live_categories");
        String response = makeRequest(url);
        return parseCategories(response);
    }

    public List<Channel> getLiveStreams(String categoryId) throws IOException, JSONException {
        String url = IPTVAPI.getDataUrl(baseUrl, username, password, "get_live_streams") + "&category_id=" + categoryId;
        String response = makeRequest(url);
        return parseChannels(response);
    }

    public List<Category> getVodCategories() throws IOException, JSONException {
        String url = IPTVAPI.getDataUrl(baseUrl, username, password, "get_vod_categories");
        String response = makeRequest(url);
        return parseCategories(response);
    }

    public List<Movie> getVodStreams(String categoryId) throws IOException, JSONException {
        String url = IPTVAPI.getDataUrl(baseUrl, username, password, "get_vod_streams") + "&category_id=" + categoryId;
        String response = makeRequest(url);
        return parseMovies(response);
    }

    public List<Category> getSeriesCategories() throws IOException, JSONException {
        String url = IPTVAPI.getDataUrl(baseUrl, username, password, "get_series_categories");
        String response = makeRequest(url);
        return parseCategories(response);
    }

    public List<Series> getSeries(String categoryId) throws IOException, JSONException {
        String url = IPTVAPI.getDataUrl(baseUrl, username, password, "get_series") + "&category_id=" + categoryId;
        String response = makeRequest(url);
        return parseSeries(response);
    }

    public Series getSeriesInfo(String seriesId) throws IOException, JSONException {
        String url = IPTVAPI.getDataUrl(baseUrl, username, password, "get_series_info") + "&series_id=" + seriesId;
        String response = makeRequest(url);
        return parseSeriesInfo(response);
    }

    public String getStreamUrl(String streamId, String format) {
        return IPTVAPI.getLiveStreamUrl(isXUI, baseUrl, username, password, streamId, format);
    }

    public String getLiveStreamUrl(String streamId, String format) {
        return IPTVAPI.getLiveStreamUrl(isXUI, baseUrl, username, password, streamId, format);
    }

    public String getMovieUrl(String streamId, String format) {
        return IPTVAPI.getMovieUrl(isXUI, baseUrl, username, password, streamId, format);
    }

    public String getSeriesUrl(String episodeId, String format) {
        return IPTVAPI.getEpisodeUrl(baseUrl, username, password, episodeId, format);
    }

    public String getEpisodeUrl(String episodeId, String format) {
        return IPTVAPI.getEpisodeUrl(baseUrl, username, password, episodeId, format);
    }

    private String makeRequest(String url) throws IOException {
        Log.d(TAG, "Making request to: " + url);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Connection", "keep-alive");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            return response.toString();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // Helper function to safely parse boolean values
    private boolean parseBoolean(String value) {
        return "1".equals(value) || "true".equalsIgnoreCase(value);
    }

    private UserInfo parseUserInfo(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);
        JSONObject userInfo = obj.getJSONObject("user_info");

        return new UserInfo(
                userInfo.getString("username"),
                userInfo.getString("status"),
                userInfo.getInt("exp_date"),
                parseBoolean(userInfo.getString("is_trial")),
                parseBoolean(userInfo.getString("active_cons")),
                userInfo.getInt("created_at"),
                userInfo.getInt("max_connections"),
                userInfo.getString("allowed_output_formats"));
    }

    private List<Category> parseCategories(String json) throws JSONException {
        List<Category> categories = new ArrayList<>();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            categories.add(new Category(
                    obj.getString("category_id"),
                    obj.getString("category_name"),
                    obj.optString("parent_id", "0")));
        }
        return categories;
    }

    private List<Channel> parseChannels(String json) throws JSONException {
        List<Channel> channels = new ArrayList<>();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            channels.add(new Channel(
                    obj.getString("stream_id"),
                    obj.getString("name"),
                    obj.getString("stream_icon"),
                    obj.getString("epg_channel_id"),
                    obj.getString("added"),
                    obj.getString("category_id"),
                    obj.getString("custom_sid"),
                    obj.getString("tv_archive"),
                    obj.getString("direct_source"),
                    obj.getString("tv_archive_duration")));
        }
        return channels;
    }

    private List<Movie> parseMovies(String json) throws JSONException {
        List<Movie> movies = new ArrayList<>();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            movies.add(new Movie(
                    obj.getString("stream_id"),
                    obj.getString("name"),
                    obj.optString("stream_icon", ""),
                    obj.optString("stream_url", ""),
                    obj.optString("added", ""),
                    obj.optString("category_id", ""),
                    obj.optString("container_extension", "mp4"),
                    obj.optString("custom_sid", ""),
                    obj.optString("direct_source", ""),
                    obj.optString("tmdb_id", ""),
                    obj.optString("plot", ""),
                    obj.optString("cast", ""),
                    obj.optString("director", ""),
                    obj.optString("genre", ""),
                    obj.optString("release_date", ""),
                    obj.optString("rating", ""),
                    obj.optString("rating_5based", ""),
                    obj.optString("backdrop_path", ""),
                    obj.optString("youtube_trailer", ""),
                    obj.optString("episode_run_time", ""),
                    obj.optString("category_id", "")));
        }
        return movies;
    }

    private List<Series> parseSeries(String json) throws JSONException {
        List<Series> seriesList = new ArrayList<>();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            seriesList.add(new Series(
                    obj.getString("series_id"),
                    obj.getString("name"),
                    obj.optString("cover", ""),
                    obj.optString("plot", ""),
                    obj.optString("cast", ""),
                    obj.optString("director", ""),
                    obj.optString("genre", ""),
                    obj.optString("release_date", ""),
                    obj.optString("rating", ""),
                    obj.optString("rating_5based", ""),
                    obj.optString("backdrop_path", ""),
                    obj.optString("youtube_trailer", ""),
                    obj.optString("episode_run_time", ""),
                    obj.optString("category_id", "")));
        }
        return seriesList;
    }

    private Series parseSeriesInfo(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);
        JSONObject info = obj.getJSONObject("info");
        JSONObject episodes = obj.getJSONObject("episodes");

        // Create series object with info
        Series series = new Series(
                info.optString("id", ""), // Using optString since id might not be present
                info.getString("name"),
                info.optString("cover", ""),
                info.optString("plot", ""),
                info.optString("cast", ""),
                info.optString("director", ""),
                info.optString("genre", ""),
                info.optString("releaseDate", ""), // Changed from release_date to releaseDate
                info.optString("rating", ""),
                info.optString("rating_5based", ""),
                info.optString("backdrop_path", ""),
                info.optString("youtube_trailer", ""),
                info.optString("episode_run_time", ""),
                info.optString("category_id", ""));

        // Add episodes to series
        for (Iterator<String> it = episodes.keys(); it.hasNext();) {
            String season = it.next();
            JSONArray seasonEpisodes = episodes.getJSONArray(season);
            for (int i = 0; i < seasonEpisodes.length(); i++) {
                JSONObject episode = seasonEpisodes.getJSONObject(i);
                JSONObject episodeInfo = episode.optJSONObject("info");

                series.addEpisode(season, new Episode(
                        episode.getString("id"),
                        String.valueOf(episode.getInt("episode_num")), // Convert int to String
                        episode.getString("title"),
                        episode.optString("container_extension", "mp4"),
                        episodeInfo != null ? episodeInfo.optString("plot", "") : "", // Get plot from info object
                        "", // subtitles not available in the API
                        episode.optString("custom_sid", ""),
                        episode.optString("added", ""),
                        season,
                        episode.optString("direct_source", "")));
            }
        }

        return series;
    }
}
