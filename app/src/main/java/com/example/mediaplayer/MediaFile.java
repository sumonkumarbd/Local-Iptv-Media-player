package com.example.mediaplayer;

public class MediaFile {
    private String name;
    private String path;
    private String type;
    private long duration;

    public MediaFile(String name, String path, String type, long duration) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.duration = duration;
    }

    public String getName() { return name; }
    public String getPath() { return path; }
    public String getType() { return type; }
    public long getDuration() { return duration; }
}
