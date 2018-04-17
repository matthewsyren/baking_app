package com.matthewsyren.bakingapp.utilities;

import com.matthewsyren.bakingapp.models.Recipe;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Used to provide a template for the Retrofit methods
 * Adapted from https://stackoverflow.com/questions/32311531/service-methods-cannot-return-void-retrofit?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
 */

public interface IApiClient {
    String RECIPE_BASE_URL = "https://d17h27t6h515a5.cloudfront.net";

    @GET("/topher/2017/May/59121517_baking/baking.json")
    Call<ArrayList<Recipe>> getRecipes();
}