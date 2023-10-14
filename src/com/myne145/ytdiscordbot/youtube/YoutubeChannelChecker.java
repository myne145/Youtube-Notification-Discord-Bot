package com.myne145.ytdiscordbot.youtube;

import com.myne145.ytdiscordbot.Bot;
import com.myne145.ytdiscordbot.config.BotConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
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

        if(videos.length() <= 0) {
            return false; //channel has no videos uploaded
        }
        JSONObject lastVideoFromCurrentCheck = videos.getJSONObject(0);

        File lastYoutubeVideoFile = new File("last_youtube_videos/last_youtube_video_" + getYoutubeChannel().name() + ".json");
        if(!lastYoutubeVideoFile.exists())
            lastYoutubeVideoFile.createNewFile();

        boolean isLastVideoJSONFileValid = true;
        try {
            new JSONObject(BotConfig.readFileString(lastYoutubeVideoFile));
        } catch (Exception e) {
            isLastVideoJSONFileValid = false;
        }

        if(lastVideoFromPreviousCheck == null || !isLastVideoJSONFileValid) {
            lastVideoFromPreviousCheck = lastVideoFromCurrentCheck;
        }
        if(isLastVideoJSONFileValid) {
            lastVideoFromPreviousCheck = new JSONObject(BotConfig.readFileString(lastYoutubeVideoFile));
        }
        boolean areVideosTheSame = lastVideoFromCurrentCheck.getJSONObject("id").getString("videoId")
                .equals(lastVideoFromPreviousCheck.getJSONObject("id").getString("videoId"));

        lastVideoFromPreviousCheck = lastVideoFromCurrentCheck;
        try(FileWriter writer = new FileWriter(lastYoutubeVideoFile)) {
            writer.write(lastVideoFromCurrentCheck.toString(4));
        }
        return !areVideosTheSame;
    }


    /**
     * Calls {@link #hasNewVideos()} in a loop, every 15 minutes.
     */
    public void checkForNewVideosInLoop() throws InterruptedException {
        while(true) {
            try {
                boolean tempNewVideos = hasNewVideos();
                if(tempNewVideos) {
                    Bot.broadcastNewVideoMessage(isLiveStream(), this);
                }
                Thread.sleep(BotConfig.getCheckIntervalInMilliSeconds()); //sleep for time specified in config
//                Thread.sleep(1000);
            } catch (IOException | URISyntaxException e) {
                System.out.println("Cannot connect to Youtube's API, response code 403");
                Thread.sleep(BotConfig.getCheckIntervalInMilliSeconds()); //great.
            } catch (InterruptedException ignored) {

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
