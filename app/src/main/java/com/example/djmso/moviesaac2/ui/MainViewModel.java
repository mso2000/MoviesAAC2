package com.example.djmso.moviesaac2.ui;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.example.djmso.moviesaac2.database.AppDatabase;
import com.example.djmso.moviesaac2.model.Movie;

import java.util.ArrayList;
import java.util.List;

class MainViewModel extends AndroidViewModel {
    public static final int API = 0;
    public static final int DB = 1;

    private int sortOrder = API;
    private List<Movie> moviesFromApi = new ArrayList<>();
    private final LiveData<List<Movie>> moviesFromDb;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        moviesFromDb = database.movieDao().loadAllMovies();
    }

    public LiveData<List<Movie>> getMoviesFromDb() {
        return moviesFromDb;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setMoviesFromApi(List<Movie> moviesFromApi) {
        this.moviesFromApi = moviesFromApi;
    }

    public List<Movie> getMoviesFromApi() {
        return moviesFromApi;
    }
}
