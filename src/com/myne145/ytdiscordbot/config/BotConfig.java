package com.myne145.ytdiscordbot.config;

import com.myne145.ytdiscordbot.youtube.YoutubeChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class BotConfig {
    private static String apiKey;
    private static final ArrayList<YoutubeChannel> youtubeChannels = new ArrayList<>();
    private final static File CONFIG_FILE = new File("config.json");
    private static String notificationsChannelID;
    private static String ownerUserId;
    private static String newVideoMessage;
    private static String livestreamMessage;
    private static String token;
    private static String activityText;
    private static String activityType;
    private static int checkIntervalMilliSeconds;

    /**
     * Reads content of the specified file.
     * @param fileToRead file you want to read
     * @return content of the file as String
     * @throws IOException
     */
    public static String readFileString(File fileToRead) throws IOException {
        StringBuilder fileToReadReader = new StringBuilder();
        for(String fileLine : Files.readAllLines(fileToRead.toPath())) {
            fileToReadReader.append(fileLine);
        }
        return fileToReadReader.toString();
    }

    /**
     * Initializes the config.json file.
     * @throws IOException
     */
    public static void createConfig() throws IOException {
        if(!CONFIG_FILE.exists()) {
            if(!CONFIG_FILE.createNewFile()) {
                System.out.println("Cannot create the config.json file.");
                System.exit(1);
            }
        }
        File lastYoutubeVideoDirectory = new File("last_youtube_videos");
        if(!lastYoutubeVideoDirectory.exists()) {
            if(!lastYoutubeVideoDirectory.mkdir()) {
                System.out.println("Cannot create the \"last_youtube_videos\" directory.");
                System.exit(1);
            }
        }

        try {
            new JSONObject(readFileString(CONFIG_FILE));
        } catch (Exception e) {
            throw new JSONException("The config file is not a valid JSON. Follow the instructions at https://github.com/myne145/Youtube-Notification-Discord-Bot to set the bot up.");
        }

        JSONObject config = new JSONObject(readFileString(CONFIG_FILE));
        token = config.getString("token");
        ownerUserId = config.getString("owner");
        apiKey = config.getString("youtube_api_key");
        notificationsChannelID = config.getString("notifications_channel_id");
        activityText = config.getString("status_message");
        activityType = config.getString("status_type");
        if(!config.keySet().contains("check_interval_seconds")) {
            checkIntervalMilliSeconds = 1000 * 60 * 15; //default: 15 minutes
        } else {
            checkIntervalMilliSeconds = 1000 * config.getInt("check_interval_seconds");
        }

        JSONArray channels = new JSONArray(config.getJSONArray("youtube_channels"));
        for (int i = 0; i < channels.length(); i++) {
            BotConfig.youtubeChannels.add(new YoutubeChannel(channels.getJSONObject(i).getString("id"), channels.getJSONObject(i).getString("name")));
        }

        JSONObject messages = config.getJSONObject("messages");
        newVideoMessage = messages.getString("new_video");
        livestreamMessage = messages.getString("livestream");
    }

    /**
     * Updates the Discord notifications channel and writes it to the config file.
     * @param channel new notifications Discord channel
     * @throws IOException
     */
    public static void updateNotificationChannel(TextChannel channel) throws IOException {
        JSONObject object = new JSONObject(readFileString(CONFIG_FILE));
        object.put("notifications_channel_id", channel.getId());
        notificationsChannelID = channel.getId();

        try(FileWriter writer = new FileWriter(CONFIG_FILE)) {
            writer.write(object.toString(4));
        }
    }

    public static String getApiKey() {
        return apiKey;
    }

    public static ArrayList<YoutubeChannel> getChannels() {
        return youtubeChannels;
    }

    public static String getNotificationsChannelID() {
        return notificationsChannelID;
    }

    public static String getOwnerUserId() {
        return ownerUserId;
    }

    public static String getNewVideoMessage(YoutubeChannel youtubeChannel, String youtubeVideoID) {
        return newVideoMessage.replace("$CHANNEL", youtubeChannel.name()).replace("$VIDEO_LINK", "https://www.youtube.com/watch?v=" + youtubeVideoID);
    }

    public static String getLivestreamMessage(YoutubeChannel youtubeChannel, String youtubeVideoID) {
        return livestreamMessage.replace("$CHANNEL", youtubeChannel.name()).replace("$VIDEO_LINK", "https://www.youtube.com/watch?v=" + youtubeVideoID);
    }

    public static String getToken() {
        return token;
    }

    public static String getActivityText() {
        return activityText;
    }

    public static String getActivityType() {
        return activityType;
    }

    public static int getCheckIntervalInMilliSeconds() {
        return checkIntervalMilliSeconds;
    }
}
