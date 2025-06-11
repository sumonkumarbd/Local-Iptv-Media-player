package com.feed.mediaplayer.utils;

import java.io.Serializable;

public class IPTVAPI implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String PLAYER_API = "player_api.php";
    private static final String LIVE_PATH = "live";
    private static final String MOVIE_PATH = "movie";
    private static final String SERIES_PATH = "series";

    /**
     * Get login API URL
     * 
     * @param baseURL  Base URL of the IPTV service
     * @param username Username for authentication
     * @param password Password for authentication
     * @return Complete login API URL
     */
    public static String getLoginUrl(String baseURL, String username, String password) {
        return buildBaseUrl(baseURL) + PLAYER_API + "?username=" + username + "&password=" + password;
    }

    /**
     * Get data API URL for specific action
     * 
     * @param baseURL  Base URL of the IPTV service
     * @param username Username for authentication
     * @param password Password for authentication
     * @param action   Action to perform (e.g., get_live_categories,
     *                 get_vod_categories)
     * @return Complete data API URL
     */
    public static String getDataUrl(String baseURL, String username, String password, String action) {
        return buildBaseUrl(baseURL) + PLAYER_API + "?username=" + username + "&password=" + password + "&action="
                + action;
    }

    /**
     * Get live stream URL
     * 
     * @param isXUI    Whether the service uses XUI format
     * @param baseURL  Base URL of the IPTV service
     * @param username Username for authentication
     * @param password Password for authentication
     * @param streamId Stream ID
     * @param format   Stream format (e.g., m3u8, ts)
     * @return Complete live stream URL
     */
    public static String getLiveStreamUrl(boolean isXUI, String baseURL, String username, String password,
            String streamId, String format) {
        return buildBaseUrl(baseURL) + LIVE_PATH + "/" + username + "/" + password + "/" + streamId + "." + format;
    }

    /**
     * Get movie URL
     * 
     * @param isXUI    Whether the service uses XUI format
     * @param baseURL  Base URL of the IPTV service
     * @param username Username for authentication
     * @param password Password for authentication
     * @param movieId  Movie ID
     * @param format   Movie format (e.g., mp4, mkv)
     * @return Complete movie URL
     */
    public static String getMovieUrl(boolean isXUI, String baseURL, String username, String password, String movieId,
            String format) {
        return buildBaseUrl(baseURL) + MOVIE_PATH + "/" + username + "/" + password + "/" + movieId + "." + format;
    }

    /**
     * Get series episode URL
     * 
     * @param baseURL   Base URL of the IPTV service
     * @param username  Username for authentication
     * @param password  Password for authentication
     * @param episodeId Episode ID
     * @param format    Episode format (e.g., mp4, mkv)
     * @return Complete episode URL
     */
    public static String getEpisodeUrl(String baseURL, String username, String password, String episodeId,
            String format) {
        return buildBaseUrl(baseURL) + SERIES_PATH + "/" + username + "/" + password + "/" + episodeId + "." + format;
    }

    /**
     * Get general stream URL
     * 
     * @param isXUI    Whether the service uses XUI format
     * @param baseURL  Base URL of the IPTV service
     * @param username Username for authentication
     * @param password Password for authentication
     * @param streamId Stream ID
     * @param format   Stream format (e.g., m3u8, ts)
     * @return Complete stream URL
     */
    public static String getStreamUrl(boolean isXUI, String baseURL, String username, String password,
            String streamId, String format) {
        String url = buildBaseUrl(baseURL);
        if (isXUI) {
            url += username + "/" + password + "/" + streamId + "." + format;
        } else {
            url += streamId + "." + format;
        }
        return url;
    }

    /**
     * Build base URL ensuring it ends with a forward slash
     * 
     * @param baseURL Base URL to process
     * @return Processed base URL
     */
    private static String buildBaseUrl(String baseURL) {
        if (baseURL == null || baseURL.isEmpty()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty");
        }
        return baseURL.endsWith("/") ? baseURL : baseURL + "/";
    }
}