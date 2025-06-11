package com.feed.sphere.models;

public class Server {
    String base_url_db;
    String isActive;

    public Server(String base_url_db, String isActive) {
        this.base_url_db = base_url_db;
        this.isActive = isActive;
    }

    public String getBase_url_db() {
        return base_url_db;
    }

    public void setBase_url_db(String base_url_db) {
        this.base_url_db = base_url_db;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }
}
