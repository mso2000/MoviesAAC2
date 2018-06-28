package com.example.djmso.moviesaac2;

import com.example.djmso.moviesaac2.database.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Utils {

    private Utils() {
    }

    private static URL buildUrl(String searchQuery) {
        URL url = null;
        try {
            url = new URL(searchQuery);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String getResponseFromHttpUrl(String searchQuery) throws IOException {
        URL url = buildUrl(searchQuery);

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static List<Movie> parseJSON(String response) {
        if (response == null)
            return null;

        List<Movie> movies = new ArrayList<>();
        try {
            JSONObject jsonMovieObject = new JSONObject(response);
            JSONArray movieDetailsArray = jsonMovieObject.getJSONArray("results");
            for (int i = 0; i < movieDetailsArray.length(); i++) {
                JSONObject moviePosition = movieDetailsArray.getJSONObject(i);
                movies.add(new Movie(moviePosition.optString("original_title")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return movies;
    }
}
