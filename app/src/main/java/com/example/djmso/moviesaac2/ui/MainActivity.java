package com.example.djmso.moviesaac2.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.djmso.moviesaac2.AppExecutors;
import com.example.djmso.moviesaac2.R;
import com.example.djmso.moviesaac2.database.AppDatabase;
import com.example.djmso.moviesaac2.model.Movie;
import com.example.djmso.moviesaac2.model.MovieResponse;
import com.example.djmso.moviesaac2.network.MovieClient;
import com.example.djmso.moviesaac2.network.MovieService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView resultsView;
    private MainViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.loading_indicator);
        resultsView = findViewById(R.id.results);

        Button btnPopular = findViewById(R.id.btn_popular);
        btnPopular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMovies(MainViewModel.API, MovieClient.SORT_BY_POPULAR);
            }
        });

        Button btnTopRated = findViewById(R.id.btn_top);
        btnTopRated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMovies(MainViewModel.API, MovieClient.SORT_BY_TOP_RATED);
            }
        });

        Button btnFavorite = findViewById(R.id.btn_favorites);
        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMovies(MainViewModel.DB, null);
            }
        });

        Button btnAddMovie = findViewById(R.id.btn_add_movie);
        btnAddMovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase database = AppDatabase.getInstance(getApplicationContext());
                        database.movieDao().insertMovie(new Movie(getString(R.string.dummy_movie)));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, R.string.toast_db_insert, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

        Button btnDeleteMovies = findViewById(R.id.btn_delete_all);
        btnDeleteMovies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase database = AppDatabase.getInstance(getApplicationContext());
                        database.movieDao().deleteMovies();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, R.string.toast_db_delete, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        if (mViewModel.getSortOrder() == MainViewModel.API) {
            if (mViewModel.getMoviesFromApi().size() > 0) {
                displayMovies(mViewModel.getMoviesFromApi());
            }
        } else {
            mViewModel.getMoviesFromDb().observe(this, mObserver);
        }
    }

    private final Observer<List<Movie>> mObserver = new Observer<List<Movie>>() {
        @Override
        public void onChanged(@Nullable List<Movie> movies) {
            displayMovies(movies);
        }
    };

    private void loadMovies(int source, String sortOrder) {
        mViewModel.setSortOrder(source);
        if (source == MainViewModel.API) {
            mViewModel.getMoviesFromDb().removeObserver(mObserver);
        }
        displayProgress();

        if (source == MainViewModel.API) {
            getMoviesFromAPI(sortOrder);

        } else {
            mViewModel.getMoviesFromDb().observe(this, mObserver);
        }
    }

    private void getMoviesFromAPI(String sortOrder) {
        MovieService service = MovieClient.getRetrofitInstance().create(MovieService.class);
        Call<MovieResponse> call = service.getMovies(sortOrder);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                MovieResponse movieResponse = response.body();
                if (movieResponse == null) {
                    displayError();
                } else {
                    mViewModel.setMoviesFromApi(response.body().getMoviesList());
                    displayMovies(mViewModel.getMoviesFromApi());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                displayError();
            }
        });
    }

    private void displayError() {
        resultsView.setText(R.string.error_api_empty);
        progressBar.setVisibility(View.GONE);
        resultsView.setVisibility(View.VISIBLE);
    }

    private void displayProgress() {
        resultsView.setText("");
        progressBar.setVisibility(View.VISIBLE);
        resultsView.setVisibility(View.GONE);
    }

    private void displayMovies(List<Movie> movies) {
        StringBuilder movieList = new StringBuilder();
        if (movies == null || movies.isEmpty()) {
            if (mViewModel.getSortOrder() == MainViewModel.DB) {
                movieList = new StringBuilder(getString(R.string.error_db_empty));
            } else {
                movieList = new StringBuilder(getString(R.string.error_api_empty));
            }
        } else {
            for (int i = 0; i < movies.size(); i++) {
                movieList.append(i + 1).append(") ").append(movies.get(i).getTitle()).append("\n");
            }
        }

        resultsView.setText(movieList.toString());
        progressBar.setVisibility(View.GONE);
        resultsView.setVisibility(View.VISIBLE);
    }
}
