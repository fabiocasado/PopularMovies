package com.fcasado.popularmovies.datatypes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Datatype to contain basic movie data. Also has custom param to signal as favorite or not.
 */
public class Movie implements Parcelable {
    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    private long mId;
    private String mTitle;
    private String mOriginalTitle;
    private String mOverview;
    private String mPosterPath;
    private String mReleaseDate;
    private double mPopularity;
    private double mUserRating;
    private boolean mIsFavorite;

    public Movie(long id) {
        mId = id;
    }

    public Movie(long id, String title, String originalTitle, String overview, String posterPath,
                 String releaseDate, double popularity, double userRating) {
        mId = id;
        mTitle = title;
        mOriginalTitle = originalTitle;
        mOverview = overview;
        mPosterPath = posterPath;
        mReleaseDate = releaseDate;
        mPopularity = popularity;
        mUserRating = userRating;
        mIsFavorite = false;
    }

    // Parcelling part
    public Movie(Parcel in) {
        this.mId = in.readLong();
        this.mTitle = in.readString();
        this.mOriginalTitle = in.readString();
        this.mOverview = in.readString();
        this.mPosterPath = in.readString();
        this.mReleaseDate = in.readString();
        this.mPopularity = in.readDouble();
        this.mUserRating = in.readDouble();
        this.mIsFavorite = false;
    }

    public long getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getOriginalTitle() {
        return mOriginalTitle;
    }

    public String getOverview() {
        return mOverview;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public double getPopularity() {
        return mPopularity;
    }

    public double getUserRating() {
        return mUserRating;
    }

    public boolean isFavorite() {
        return mIsFavorite;
    }

    public void setFavorite(Boolean isFavorite) {
        mIsFavorite = isFavorite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mTitle);
        dest.writeString(mOriginalTitle);
        dest.writeString(mOverview);
        dest.writeString(mPosterPath);
        dest.writeString(mReleaseDate);
        dest.writeDouble(mPopularity);
        dest.writeDouble(mUserRating);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Movie movie = (Movie) o;

        return mId == movie.mId;

    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }
}
