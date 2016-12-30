package com.example.jessemitchell.popularmovies.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.jessemitchell.popularmovies.app.BuildConfig.MOVIE_DB_API_KEY;

/**
 * Created by jesse.mitchell on 12/28/2016.
 */
public class PopularMoviesFragment extends Fragment
{

    private final String LOG_TAG = PopularMoviesFragment.class.getSimpleName();

    private ImageAdapter movieDetailsAdapter;

    @Override
    public void onStart() {
        super.onStart();
        selectList();
    }

    private void selectList()
    {
        FetchMoviesTask movieTask = new FetchMoviesTask();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String listType = sharedPrefs.getString(getString(R.string.pref_list_key),getString(R.string.pref_list_default));
        movieTask.execute(listType);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.movies_main, container, false);

        movieDetailsAdapter = new  ImageAdapter(this.getContext(), new ArrayList<MovieDetails>());

        GridView gView = (GridView)rootView.findViewById(R.id.movies_grid_view);
        gView.setAdapter(movieDetailsAdapter);

        gView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MovieDetails movie = movieDetailsAdapter.getItem(i);
                Intent movieDetailIntent = new Intent(getContext(),DisplayMovieDetailsActivity.class);
                movieDetailIntent.putExtra(getString(R.string.movie_details_data), (Parcelable) movie);
                startActivity(movieDetailIntent);
            }
        });
        return rootView;
    }

    public class FetchMoviesTask extends AsyncTask<String, Void,  ArrayList<MovieDetails>>
    {

        @Override
        protected void onPostExecute( ArrayList<MovieDetails> results) {
            if(results != null)
            {
                movieDetailsAdapter.clear();
                for(MovieDetails movie : results)
                {
                    movieDetailsAdapter.add(movie);
                }
            }
        }

        @Override
        protected  ArrayList<MovieDetails> doInBackground(String... parameters) {

            int READ_TIMEOUT = 1000;
            int CONN_TIMEOUT = 1500;

            String BASE_URL = getString(R.string.movie_base_url);
            String VER = getString(R.string.movie_api_version_3);
            String BASE_PATH = getString(R.string.movie_base_path_movie);
            String Q_FIELD_API_KEY = getString(R.string.movie_param_api_key);
            String Q_FIELD_LANG = getString(R.string.movie_param_lang);
            String Q_PARAM_LANG = getString(R.string.movie_param_lang_type);
            String Q_FIELD_PAGE = getString(R.string.movie_param_page);
            String Q_PARAM_PAGE = "1";
            String REQ_METHOD = getString(R.string.movie_request_type_get);

            if (parameters.length == 0)
                return null;

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr;

            try{

                Uri movieUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(VER)
                        .appendPath(BASE_PATH)
                        .appendPath(parameters[0])
                        .appendQueryParameter(Q_FIELD_API_KEY, MOVIE_DB_API_KEY)
                        .appendQueryParameter(Q_FIELD_LANG, Q_PARAM_LANG)
                        .appendQueryParameter(Q_FIELD_PAGE, Q_PARAM_PAGE)
                        .build();

                URL dataUrl = new URL(movieUri.toString());
                urlConnection = (HttpURLConnection) dataUrl.openConnection();
                urlConnection.setReadTimeout(READ_TIMEOUT);
                urlConnection.setConnectTimeout(CONN_TIMEOUT);
                urlConnection.setRequestMethod(REQ_METHOD);
                urlConnection.setDoInput(true);
                urlConnection.connect();

                InputStream inStrm = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inStrm == null)
                    return null;

                reader = new BufferedReader(new InputStreamReader(inStrm));

                String line;
                while ((line = reader.readLine()) != null)
                {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0)
                    return null;

                movieJsonStr = buffer.toString();
                return extractMovieDetails(movieJsonStr);

            }
            catch(Exception e){
                Log.e("PopularMovieFragment", "error", e);
                return null;
            }
            finally {
                if (urlConnection != null)
                    urlConnection.disconnect();

                if (reader != null)
                    try {
                        reader.close();
                    }
                    catch (final IOException ioe)
                    {
                        Log.e("PopularMovieFragment", "Reader did not close properly.", ioe);
                    }
            }
        }
    }

    private ArrayList<MovieDetails> extractMovieDetails(String jsonStr) throws JSONException
    {
        String RESULTS = getString(R.string.movie_json_results);
        String ORIG_TITLE = getString(R.string.movie_json_original_title);
        String OVERVIEW = getString(R.string.movie_json_overview);
        String POSTER_PATH = getString(R.string.movie_json_poster_path);
        String RELEASE_DATE = getString(R.string.movie_json_release_date);
        String VOTE_AVERAGE = getString(R.string.movie_json_vote_average);

        JSONObject movieArray = new JSONObject(jsonStr);
        JSONArray results = movieArray.getJSONArray(RESULTS);

        ArrayList<MovieDetails> movieDetails = new ArrayList<>();
        for(int movie = 0; movie < results.length(); movie++)
        {
            MovieDetails movieDetail = new MovieDetails();
            JSONObject movieObject = results.getJSONObject(movie);

            movieDetail.setTitle(movieObject.getString(ORIG_TITLE));
            movieDetail.setOverView(movieObject.getString(OVERVIEW));
            movieDetail.setPosterPath(movieObject.getString(POSTER_PATH));
            movieDetail.setReleaseDate(movieObject.getString(RELEASE_DATE));
            movieDetail.setVoteAverage(movieObject.getDouble(VOTE_AVERAGE));

            movieDetails.add(movieDetail);
        }

        return movieDetails;
    }

    public class ImageAdapter extends ArrayAdapter<MovieDetails>
    {
        private final String LOG_TAG = ImageAdapter.class.getSimpleName();

        public ImageAdapter(Context context, List<MovieDetails> movieDetails)
        {
            super(context, 0, movieDetails);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            MovieDetails details = getItem(position);

            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(getContext());
                imageView.setLayoutParams(new GridView.LayoutParams(740, 1112));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(10, 10, 10, 10);
            } else {
                imageView = (ImageView) convertView;
            }

            Picasso.with(parent.getContext()).load(details.getPosterPath()).into(imageView);
            return imageView;
        }
    }
}
