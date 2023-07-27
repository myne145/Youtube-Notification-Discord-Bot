package com.myne145.ytdiscordbot.youtube;

import com.myne145.ytdiscordbot.Bot;
import com.myne145.ytdiscordbot.config.BotConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class YoutubeChannelChecker {
    private JSONObject lastVideoFromPreviousCheck;
    private final YoutubeChannel channelToCheck;

    public YoutubeChannelChecker(YoutubeChannel channelToCheck) {
        this.channelToCheck = channelToCheck;
    }

    /**
     * Determines whether the new video is a livestream.
     * @return is video a livestream.
     */
    public boolean isLiveStream() {
        if(lastVideoFromPreviousCheck.getJSONObject("snippet").has("liveBroadcastContent")) {
            String type = lastVideoFromPreviousCheck.getJSONObject("snippet").getString("liveBroadcastContent");
            return type.equals("live");
        }
        return false;
    }

    /**
     * Checks for new videos on a YouTube channel.
     * @return if the channel has new videos.
     * @throws URISyntaxException
     * @throws IOException
     */
    public boolean hasNewVideos() throws URISyntaxException, IOException {
        URL requestURL =
                new URI("https://www.googleapis.com/youtube/v3/search?key=" +
                        BotConfig.getApiKey() + "&channelId=" + channelToCheck.id() + "&part=snippet,id&order=date&maxResults=20").toURL();
        HttpURLConnection urlConnection = (HttpURLConnection) requestURL.openConnection();
        urlConnection.setRequestMethod("GET");

        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(urlConnection.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line).append("\n");
            }
        }
//        String result = BotConfig.readFileString(new File(channelToCheck.name() + ".json"));

        JSONObject response = new JSONObject(result.toString());
        JSONArray videos = new JSONArray(response.getJSONArray("items"));
        JSONObject lastVideoFromCurrentCheck = videos.getJSONObject(0);

        if(lastVideoFromPreviousCheck == null) {
            lastVideoFromPreviousCheck = lastVideoFromCurrentCheck;
        }
        boolean areVideosTheSame = lastVideoFromCurrentCheck.getJSONObject("id").getString("videoId")
                .equals(lastVideoFromPreviousCheck.getJSONObject("id").getString("videoId"));

        lastVideoFromPreviousCheck = lastVideoFromCurrentCheck;
        return !areVideosTheSame;
    }


    /**
     * Calls {@link #hasNewVideos()} in a loop, every 15 minutes.
     */
    public void checkForNewVideosInLoop() {
        while(true) {
            try {
                boolean tempNewVideos = hasNewVideos();
                if(tempNewVideos) {
                    Bot.broadcastNewVideoMessage(isLiveStream(), this);
                }
                Thread.sleep(1000 * 60 * 15); //sleep for 15 minutes
//                Thread.sleep(1000);
            } catch (InterruptedException | URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public YoutubeChannel getYoutubeChannel() {
        return channelToCheck;
    }

    /**
     * If there was a new video uploaded, gets its YouTube ID.
     * @return latest video's YouTube ID
     */
    public String getLatestUploadedVideoId() {
        return lastVideoFromPreviousCheck.getJSONObject("id").getString("videoId");
    }
}
