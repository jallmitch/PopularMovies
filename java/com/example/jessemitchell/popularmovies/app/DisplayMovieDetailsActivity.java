package com.example.jessemitchell.popularmovies.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DisplayMovieDetailsActivity extends AppCompatActivity {

    private final String LOG_TAG_ACT = DisplayMovieDetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_movie_details);

        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction().add(R.id.activity_display_movie_details, new MovieDetailFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.title_activity_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class MovieDetailFragment extends Fragment
    {
        private final String LOG_TAG_FRAG = MovieDetailFragment.class.getSimpleName();

        private MovieDetails movie;
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            Intent intent = getActivity().getIntent();
            String movieDetailData = getString(R.string.movie_details_data);
            View rootView = inflater.inflate(R.layout.movie_detail_main,container,false);


            if(intent != null && intent.hasExtra(movieDetailData))
            {
                movie = intent.getParcelableExtra(movieDetailData);

                // Set Title
                ((TextView)rootView.findViewById(R.id.movie_text_view)).setText(movie.getTitle());

                // Set Voter Average
                ((RatingBar)rootView.findViewById(R.id.vote_average_bar)).setNumStars(5);
                ((RatingBar)rootView.findViewById(R.id.vote_average_bar)).setRating(movie.getVoteAverage().floatValue());

                // Change Image Size and display
                ImageView imageView = (ImageView)rootView.findViewById(R.id.movie_image_view);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setPadding(10, 10, 10, 10);
                Picasso.with(getContext()).load(movie.getPosterPath()).into(imageView);

                // Set Release Date
                ((TextView)rootView.findViewById(R.id.date_text_view)).setText(movie.getReleaseDate());

                // Set Overview text.
                // setMovementMethod http://stackoverflow.com/questions/1748977/making-textview-scrollable-in-android
                ((TextView)rootView.findViewById(R.id.overview_text_view)).setText(movie.getOverView());
                ((TextView)rootView.findViewById(R.id.overview_text_view))
                        .setMovementMethod(new ScrollingMovementMethod());
            }
            else
                Log.e(LOG_TAG_FRAG, "Intent was null or data was not sent.");
            return rootView;
        }

    }
}
