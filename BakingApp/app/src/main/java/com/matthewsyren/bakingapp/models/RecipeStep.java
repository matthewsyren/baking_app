package com.matthewsyren.bakingapp.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Used as a template for a RecipeStep object
 */

public class RecipeStep
        implements Parcelable {
    private String shortDescription;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Uri getVideoUri(){
        if(videoUrl == null || videoUrl.equals("")){
            return null;
        }
        else{
            return Uri.parse(videoUrl);
        }
    }

    private RecipeStep(Parcel in) {
        shortDescription = in.readString();
        description = in.readString();
        videoUrl = in.readString();
        thumbnailUrl = in.readString();
    }

    public static final Creator<RecipeStep> CREATOR = new Creator<RecipeStep>() {
        @Override
        public RecipeStep createFromParcel(Parcel in) {
            return new RecipeStep(in);
        }

        @Override
        public RecipeStep[] newArray(int size) {
            return new RecipeStep[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(shortDescription);
        dest.writeString(description);
        dest.writeString(videoUrl);
        dest.writeString(thumbnailUrl);
    }
}