package com.example.jessemitchell.popularmovies.app;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jesse.mitchell on 12/29/2016.
 * Followed http://www.developerphil.com/parcelable-vs-serializable/ to add Parcelable to this object.
 */

public class MovieDetails implements Parcelable
{
    private final String LOG_TAG = MovieDetails.class.getSimpleName();
    private String originalTitle;
    private String posterPath;
    private String overView;
    private String releaseDate;
    private Double voteAverage;

    public MovieDetails()
    {
    }

    public MovieDetails(Parcel in)
    {
        originalTitle = in.readString();
        posterPath = in.readString();
        overView = in.readString();
        releaseDate = in.readString();
        voteAverage = in.readDouble();
    }

    public void setTitle(String originalTitle)
    {
        this.originalTitle = originalTitle;
    }

    public String getTitle()
    {
        return this.originalTitle;
    }

    public void setPosterPath(String posterPath)
    {
        this.posterPath = posterPath;
    }

    public String getPosterPath()
    {
        StringBuilder sb = new StringBuilder("http://image.tmdb.org/t/p/")
                .append("w185")
                .append(this.posterPath);

        return sb.toString();
    }

    public void setOverView(String overView)
    {
        this.overView = overView;
    }

    public String getOverView()
    {
        return this.overView;
    }

    public void setVoteAverage(Double voteAverage)
    {
        this.voteAverage = voteAverage/2;
    }

    public Double getVoteAverage()
    {
        return this.voteAverage;
    }

    public void setReleaseDate(String releaseDate)
    {
        this.releaseDate = releaseDate;
    }

    public String getReleaseDate()
    {
        return  "Release Date: " + this.releaseDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(originalTitle);
        dest.writeString(posterPath);
        dest.writeString(overView);
        dest.writeString(releaseDate);
        dest.writeDouble(voteAverage);
    }

    public static final Parcelable.Creator<MovieDetails> CREATOR = new Parcelable.Creator<MovieDetails>() {
        @Override
        public MovieDetails createFromParcel(Parcel parcel) {
            return new MovieDetails(parcel);
        }

        @Override
        public MovieDetails[] newArray(int i) {
            return new MovieDetails[i];
        }

    };
}
