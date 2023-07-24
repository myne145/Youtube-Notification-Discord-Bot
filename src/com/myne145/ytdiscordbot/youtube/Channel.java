package com.myne145.ytdiscordbot.youtube;

public class Channel {
    private final String id;
    private final String name;

    public Channel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
