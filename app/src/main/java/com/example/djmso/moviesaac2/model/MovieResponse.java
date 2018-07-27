package com.example.djmso.moviesaac2.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieResponse {
    @SerializedName("results")
    private List<Movie> mMoviesList = null;

    public MovieResponse() {
    }

    public List<Movie> getMoviesList() {
        return mMoviesList;
    }

    public void setMoviesList(List<Movie> moviesList) {
        mMoviesList = moviesList;
    }
}
