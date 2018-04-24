package com.matthewsyren.bakingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewsyren.bakingapp.adapters.IRecyclerViewOnClickListener;
import com.matthewsyren.bakingapp.adapters.RecipeListAdapter;
import com.matthewsyren.bakingapp.models.Recipe;
import com.matthewsyren.bakingapp.utilities.IApiClient;
import com.matthewsyren.bakingapp.utilities.NetworkUtilities;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity
        extends AppCompatActivity
        implements IRecyclerViewOnClickListener{
    //View bindings
    @BindView(R.id.tv_recipe_error) TextView tvRecipeError;
    @BindView(R.id.pb_recipes) ProgressBar pbRecipes;
    @BindView(R.id.btn_refresh) Button btnRefresh;
    @BindView(R.id.rv_recipes) RecyclerView rvRecipes;

    //Variables and constants
    private ArrayList<Recipe> mRecipes;
    public static final String RECIPES_BUNDLE_KEY = "recipes_bundle_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //Restores data if there is any saved data, otherwise fetches the data from the API
        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }
        else{
            getRecipes();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mRecipes != null && mRecipes.size() > 0){
            outState.putParcelableArrayList(RECIPES_BUNDLE_KEY, mRecipes);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //Restores data if there is any saved data, otherwise fetches the data from the API
        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }
    }

    //Restores the appropriate data
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(RECIPES_BUNDLE_KEY)){
            mRecipes = savedInstanceState.getParcelableArrayList(RECIPES_BUNDLE_KEY);
            displayRecipes(mRecipes);
        }
    }

    //Uses Retrofit to fetch the recipe information
    private void getRecipes(){
        //Displays ProgressBar and hides the TextView
        pbRecipes.setVisibility(View.VISIBLE);
        setRefreshButtonVisibility(View.GONE);

        //Attempts to fetch the recipe information asynchronously using Retrofit if there is an Internet connection
        if(NetworkUtilities.isOnline(this)){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(IApiClient.RECIPE_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            IApiClient client = retrofit.create(IApiClient.class);
            Call<ArrayList<Recipe>> call = client.getRecipes();
            call.enqueue(new Callback<ArrayList<Recipe>>() {
                @Override
                public void onResponse(Call<ArrayList<Recipe>> call, Response<ArrayList<Recipe>> response) {
                    mRecipes = response.body();
                    displayRecipes(mRecipes);
                }

                @Override
                public void onFailure(Call<ArrayList<Recipe>> call, Throwable t) {
                    //Hides ProgressBar and displays the TextView
                    pbRecipes.setVisibility(View.GONE);
                    setRefreshButtonVisibility(View.VISIBLE);
                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(), getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG).show();

            //Hides ProgressBar and displays the TextView
            pbRecipes.setVisibility(View.GONE);
            setRefreshButtonVisibility(View.VISIBLE);
        }
    }

    //Displays the recipes in a RecyclerView
    private void displayRecipes(ArrayList<Recipe> recipes){
        if(recipes != null && recipes.size() > 0){
            RecipeListAdapter recipeListAdapter = new RecipeListAdapter(recipes, MainActivity.this);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, numberOfColumns());
            gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
            rvRecipes.setLayoutManager(gridLayoutManager);
            rvRecipes.setAdapter(recipeListAdapter);
        }
        else{
            setRefreshButtonVisibility(View.VISIBLE);
        }
        pbRecipes.setVisibility(View.GONE);
    }

    /* Used to calculate the number of columns that can be displayed in the RecyclerView
     * Adapted from the feedback received for Stage 1 of Popular Movies
     */
    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        //Calculates how many columns can be displayed based on the screen's width, with 1 column being the minimum
        int widthDivider = 750;

        int width = displayMetrics.widthPixels;
        int numberOfColumns = width / widthDivider;

        //Ensures that there is at least 1 column regardless of screen width
        if (numberOfColumns < 1){
            return 1;
        }

        return numberOfColumns;
    }

    //Hides or displays the refresh Button and its corresponding TextView
    private void setRefreshButtonVisibility(int visibility){
        tvRecipeError.setVisibility(visibility);
        btnRefresh.setVisibility(visibility);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(MainActivity.this, RecipeDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(RecipeDetailActivity.RECIPE_BUNDLE_KEY, mRecipes.get(position));
        intent.putExtras(bundle);
        startActivity(intent);
    }

    //Attempts to fetch the recipes again
    public void refreshRecipesOnClick(View view) {
        getRecipes();
    }
}