package com.feed.mediaplayer.models;

public class UserInfo {
    private final String username;
    private final String status;
    private final String expDate;
    private final boolean isTrial;
    private final boolean activeCons;
    private final int createdAt;
    private final int maxConnections;
    private final String allowedOutputFormats;

    public UserInfo(String username, String status, String expDate, boolean isTrial,
            boolean activeCons, int createdAt, int maxConnections, String allowedOutputFormats) {
        this.username = username;
        this.status = status;
        this.expDate = expDate;
        this.isTrial = isTrial;
        this.activeCons = activeCons;
        this.createdAt = createdAt;
        this.maxConnections = maxConnections;
        this.allowedOutputFormats = allowedOutputFormats;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    public String getExpDate() {
        return expDate;
    }

    public boolean isTrial() {
        return isTrial;
    }

    public boolean isActiveCons() {
        return activeCons;
    }

    public int getCreatedAt() {
        return createdAt;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public String getAllowedOutputFormats() {
        return allowedOutputFormats;
    }
}