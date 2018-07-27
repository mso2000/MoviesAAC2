package com.example.djmso.moviesaac2.network;

import com.example.djmso.moviesaac2.model.MovieResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MovieService {
    @GET("movie/{sortBy}")
    Call<MovieResponse> getMovies(@Path("sortBy") String sortBy);
}