package com.myne145.ytdiscordbot.youtube;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;

public class Checker {
    private static JSONObject lastVideoFromPreviousCheck;

    public static String readFileString(File fileToRead) throws IOException {
        StringBuilder fileToReadReader = new StringBuilder();
        for(String fileLine : Files.readAllLines(fileToRead.toPath())) {
            fileToReadReader.append(fileLine);
        }
        return fileToReadReader.toString();
    }

    public static boolean hasNewVideos(Channel channel) throws URISyntaxException, IOException {
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
        String result = readFileString(new File("example.json"));

        JSONObject response = new JSONObject(result);
        JSONArray videos = new JSONArray(response.getJSONArray("items"));
        JSONObject lastVideoFromCurrentCheck = videos.getJSONObject(0);

        if(lastVideoFromPreviousCheck == null) {
            lastVideoFromPreviousCheck = lastVideoFromCurrentCheck;
        }
        boolean areVideosTheSame = false;
        if(lastVideoFromCurrentCheck.getJSONObject("id").getString("videoId")
                .equals(lastVideoFromPreviousCheck.getJSONObject("id").getString("videoId"))) {
            areVideosTheSame = true;
        }

        lastVideoFromPreviousCheck = lastVideoFromCurrentCheck;
//        System.out.println(areVideosTheSame);
        return areVideosTheSame;
    }
}
