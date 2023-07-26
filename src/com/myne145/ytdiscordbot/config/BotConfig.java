package com.myne145.ytdiscordbot.config;

import com.myne145.ytdiscordbot.youtube.YoutubeChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class BotConfig {
    private static String API_KEY;
    private static ArrayList<YoutubeChannel> youtubeChannels = new ArrayList<>();
    private final static File CONFIG_FILE = new File("config.json");
    private static String notificationsChannelID;

    public static String readFileString(File fileToRead) throws IOException {
        StringBuilder fileToReadReader = new StringBuilder();
        for(String fileLine : Files.readAllLines(fileToRead.toPath())) {
            fileToReadReader.append(fileLine);
        }
        return fileToReadReader.toString();
    }

    public static void createConfig() throws IOException {
        JSONObject config = new JSONObject(readFileString(CONFIG_FILE));
        API_KEY = config.getString("api_key");
        notificationsChannelID = config.getString("notifications_channel_id");
        JSONArray channels = new JSONArray(config.getJSONArray("channels"));
//        System.out.println(channels.toString(4));
        for(int i = 0; i < channels.length(); i++) {
            BotConfig.youtubeChannels.add(new YoutubeChannel(channels.getJSONObject(i).getString("id"), channels.getJSONObject(i).getString("name")));
        }
    }

    public static void updateNotificationChannel(TextChannel channel) throws IOException {
        JSONObject object = new JSONObject(readFileString(CONFIG_FILE));
        object.put(channel.getId(), "notifications_channel_id");
        notificationsChannelID = channel.getId();

        try(FileWriter writer = new FileWriter(CONFIG_FILE)) {
            writer.write(object.toString(4));
        }
    }

    public static String getApiKey() {
        return API_KEY;
    }

    public static ArrayList<YoutubeChannel> getChannels() {
        return youtubeChannels;
    }

    public static String getNotificationsChannelID() {
        return notificationsChannelID;
    }
}
