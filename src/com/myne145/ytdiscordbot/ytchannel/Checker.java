package com.myne145.ytdiscordbot.ytchannel;

import com.myne145.ytdiscordbot.config.BotConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.file.Files;

public class Checker {

    public static String readFileString(File fileToRead) throws IOException {
        StringBuilder fileToReadReader = new StringBuilder();
        for(String fileLine : Files.readAllLines(fileToRead.toPath())) {
            fileToReadReader.append(fileLine);
        }
        return fileToReadReader.toString();
    }

    public static boolean hasNewVideos() throws URISyntaxException, IOException {
//        URL requestURL =
//                new URI("https://www.googleapis.com/youtube/v3/search?key=" +
//                        BotConfig.API_KEY + "&channelId=" + BotConfig.CHANNEL_IDS.get(0) + "&part=snippet,id&order=date&maxResults=20").toURL();
//        HttpURLConnection urlConnection = (HttpURLConnection) requestURL.openConnection();
//        urlConnection.setRequestMethod("GET");

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


        return false;
    }
}
