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

/**
 * Created by jesse.mitchell on 12/28/2016.
 */
public class PopularMoviesFragment extends Fragment
{

    private final String LOG_TAG = PopularMoviesFragment.class.getSimpleName();

    private ImageAdapter movieDetailsAdapter;
    public PopularMoviesFragment()
    {

    }

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
                movieDetailIntent.putExtra("MovieDetails_data", (Parcelable) movie);
                startActivity(movieDetailIntent);
//                Toast.makeText(getContext(), "You have selected position: " + i + "  Movie Info:" + movie.getTitle(), Toast.LENGTH_LONG).show();
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
                    Log.v(LOG_TAG, movie.getTitle());
                }
//                movieDetailsAdapter.addAll(results);

            }
        }

        @Override
        protected  ArrayList<MovieDetails> doInBackground(String... parameters) {

            if (parameters.length == 0)
                return null;

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr;

            try{
                String baseUrl = "https://api.themoviedb.org";
                String version = "3";
                String section = "movie";
                String subSection = "popular";
                String keyParam = "api_key";
                String languageParam = "en-US";
                String pageParam = "1";

                Uri movieUri = Uri.parse(baseUrl).buildUpon()
                        .appendPath(version)
                        .appendPath(section)
                        .appendPath(parameters[0])
                        .appendQueryParameter(keyParam, "")
                        .appendQueryParameter("language", languageParam)
                        .appendQueryParameter("page", pageParam)
                        .build();
                InputStream inStrm = null;

                URL dataUrl = new URL(movieUri.toString());
                Log.v(LOG_TAG, dataUrl.toString());

                urlConnection = (HttpURLConnection) dataUrl.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();

                inStrm = urlConnection.getInputStream();
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
        JSONObject movieArray = new JSONObject(jsonStr);
        JSONArray results = movieArray.getJSONArray("results");

        ArrayList<MovieDetails> movieDetails = new ArrayList<>();
        for(int movie = 0; movie < results.length(); movie++)
        {
            MovieDetails movieDetail = new MovieDetails();
            JSONObject movieObject = results.getJSONObject(movie);

            movieDetail.setTitle(movieObject.getString("original_title"));
            movieDetail.setOverView(movieObject.getString("overview"));
            movieDetail.setPosterPath(movieObject.getString("poster_path"));
            movieDetail.setReleaseDate(movieObject.getString("release_date"));
            movieDetail.setVoteAverage(movieObject.getDouble("vote_average"));

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
            Log.v(LOG_TAG, details.getPosterPath());
            return imageView;
        }
    }
}
