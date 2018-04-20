package com.matthewsyren.bakingapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Used as a template for a Recipe object
 */

public class Recipe
        implements Parcelable {
    @SerializedName("name")
    private String name;
    @SerializedName("servings")
    private String servings;
    @SerializedName("image")
    private String imageUrl;
    @SerializedName("ingredients")
    private ArrayList<RecipeIngredient> ingredients;
    @SerializedName("steps")
    private ArrayList<RecipeStep> steps;

    public String getName() {
        return name;
    }

    public String getServings() {
        return servings;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ArrayList<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public ArrayList<RecipeStep> getSteps() {
        return steps;
    }

    private Recipe(Parcel in) {
        name = in.readString();
        servings = in.readString();
        imageUrl = in.readString();
        ingredients = in.readArrayList(RecipeIngredient.class.getClassLoader());
        steps = in.readArrayList(RecipeStep.class.getClassLoader());
    }

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(servings);
        dest.writeString(imageUrl);
        dest.writeList(ingredients);
        dest.writeList(steps);
    }
}