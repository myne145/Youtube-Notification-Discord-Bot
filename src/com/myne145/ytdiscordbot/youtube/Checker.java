package com.myne145.ytdiscordbot.youtube;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;

public class Checker {
    private static JSONObject lastVideoFromPreviousCheck;

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
     * Checks for new videos on a YouTube channel.
     * @param youtubeChannel YouTube channel you want to check videos on.
     * @return if the channel has new videos.
     * @throws URISyntaxException
     * @throws IOException
     */
    public static boolean hasNewVideos(YoutubeChannel youtubeChannel) throws URISyntaxException, IOException {
//        URL requestURL =
//                new URI("https://www.googleapis.com/youtube/v3/search?key=" +
//                        BotConfig.API_KEY + "&channelId=" + BotConfig.CHANNELS.get(0).getId() + "&part=snippet,id&order=date&maxResults=20").toURL();
//        HttpURLConnection urlConnection = (HttpURLConnection) requestURL.openConnection();
//        urlConnection.setRequestMethod("GET");
//
//        StringBuilder result = new StringBuilder();
//        try (BufferedReader reader = new BufferedReader(
//                new InputStreamReader(urlConnection.getInputStream()))) {
//            for (String line; (line = reader.readLine()) != null; ) {
//                result.append(line);
//            }
//        }
//        System.out.println(result);
        String result = readFileString(new File("exampleLive.json"));

        JSONObject response = new JSONObject(result);
        JSONArray videos = new JSONArray(response.getJSONArray("items"));
        JSONObject lastVideoFromCurrentCheck = videos.getJSONObject(0);

        if(lastVideoFromPreviousCheck == null) {
            lastVideoFromPreviousCheck = lastVideoFromCurrentCheck;
        }
        boolean areVideosTheSame = lastVideoFromCurrentCheck.getJSONObject("id").getString("videoId")
                .equals(lastVideoFromPreviousCheck.getJSONObject("id").getString("videoId"));

        lastVideoFromPreviousCheck = lastVideoFromCurrentCheck;
//        System.out.println(areVideosTheSame);
        return !areVideosTheSame;
    }

    /**
     * If there was a new video uploaded, gets its YouTube ID.
     * @return latest video's YouTube ID
     */
    public static String getLatestUploadedVideoId() {
        return lastVideoFromPreviousCheck.getJSONObject("id").getString("videoId");
    }

    /**
     * Determines whether the new video is a livestream.
     * @return is video a livestream.
     */
    public static boolean isLiveStream() {
        if(lastVideoFromPreviousCheck.getJSONObject("snippet").has("liveBroadcastContent")) {
            String type = lastVideoFromPreviousCheck.getJSONObject("snippet").getString("liveBroadcastContent");
            return type.equals("live");
        }
        return false;
    }
}
