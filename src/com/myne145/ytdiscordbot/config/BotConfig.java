package com.myne145.ytdiscordbot.config;

import com.myne145.ytdiscordbot.youtube.Channel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class BotConfig {
    public static String API_KEY;
    public static ArrayList<Channel> CHANNELS = new ArrayList<>();

    public static String readFileString(File fileToRead) throws IOException {
        StringBuilder fileToReadReader = new StringBuilder();
        for(String fileLine : Files.readAllLines(fileToRead.toPath())) {
            fileToReadReader.append(fileLine);
        }
        return fileToReadReader.toString();
    }

    public static void createConfig() throws IOException {
        JSONObject config = new JSONObject(readFileString(new File("config.json")));
        API_KEY = config.getString("api_key");
        JSONArray channels = new JSONArray(config.getJSONArray("channels"));
//        System.out.println(channels.toString(4));
        for(int i = 0; i < channels.length(); i++) {
            CHANNELS.add(new Channel(channels.getJSONObject(i).getString("id"), channels.getJSONObject(i).getString("name")));
        }
    }
}
