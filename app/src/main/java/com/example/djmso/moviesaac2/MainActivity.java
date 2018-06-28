package com.example.djmso.moviesaac2;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.djmso.moviesaac2.database.AppDatabase;
import com.example.djmso.moviesaac2.database.Movie;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String API_KEY = BuildConfig.API_KEY;
    private final String MOVIES_TOP_RATED
            = "https://api.themoviedb.org/3/movie/top_rated?api_key=" + API_KEY;
    private final String MOVIES_POPULAR
            = "https://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY;

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
                loadMovies(MainViewModel.API, MOVIES_POPULAR);
            }
        });

        Button btnTopRated = findViewById(R.id.btn_top);
        btnTopRated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMovies(MainViewModel.API, MOVIES_TOP_RATED);
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
            new MoviesTask(this).execute(sortOrder);
        } else {
            mViewModel.getMoviesFromDb().observe(this, mObserver);
        }
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

    private static class MoviesTask extends AsyncTask<String, Void, String> {
        private final WeakReference<MainActivity> activityReference;

        MoviesTask(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... params) {
            String searchUrl = params[0];
            String response = null;
            try {
                response = Utils.getResponseFromHttpUrl(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String results) {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mViewModel.setMoviesFromApi(Utils.parseJSON(results));
            activity.displayMovies(activity.mViewModel.getMoviesFromApi());
        }
    }
}
