package com.myne145.ytdiscordbot.youtube;

public class YoutubeChannel {
    private final String id;
    private final String name;

    public YoutubeChannel(String id, String name) {
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
