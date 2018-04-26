package com.matthewsyren.bakingapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import com.matthewsyren.bakingapp.data.RecipeContract;
import com.matthewsyren.bakingapp.data.RecipeIngredientContract;
import com.matthewsyren.bakingapp.data.RecipeProvider;
import com.matthewsyren.bakingapp.data.RecipeStepContract;
import com.matthewsyren.bakingapp.models.Recipe;
import com.matthewsyren.bakingapp.models.RecipeIngredient;
import com.matthewsyren.bakingapp.models.RecipeStep;
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
        implements IRecyclerViewOnClickListener,
        LoaderManager.LoaderCallbacks<Cursor>{
    //View bindings
    @BindView(R.id.tv_recipe_error) TextView tvRecipeError;
    @BindView(R.id.pb_recipes) ProgressBar pbRecipes;
    @BindView(R.id.btn_refresh) Button btnRefresh;
    @BindView(R.id.rv_recipes) RecyclerView rvRecipes;

    //Variables and constants
    private ArrayList<Recipe> mRecipes;
    public static final String RECIPES_BUNDLE_KEY = "recipes_bundle_key";
    private static final int RECIPES_LOADER_ID = 101;
    private static final int RECIPE_INGREDIENT_LOADER_ID = 102;
    private static final int RECIPE_STEPS_LOADER_ID = 103;

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

            //Requests the data
            call.enqueue(new Callback<ArrayList<Recipe>>() {
                @Override
                public void onResponse(@NonNull Call<ArrayList<Recipe>> call, @NonNull Response<ArrayList<Recipe>> response) {
                    //Retrieves the data
                    mRecipes = response.body();

                    //Writes all Recipes to the SQLite database, to ensure data is up to date
                    if(mRecipes != null){
                        //Deletes existing data
                        deleteRecipeData();

                        //Inserts the new data
                        for(int i = 0; i < mRecipes.size(); i++){
                            insertRecipe(mRecipes.get(i));
                        }

                        //Displays the data
                        displayRecipes(mRecipes);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ArrayList<Recipe>> call, @NonNull Throwable t) {
                    //Hides the ProgressBar and displays the TextView and refresh Button
                    pbRecipes.setVisibility(View.GONE);
                    setRefreshButtonVisibility(View.VISIBLE);
                }
            });
        }
        else{
            //Starts a Loader to check if the SQLite database has the Recipes
            getSupportLoaderManager().restartLoader(RECIPES_LOADER_ID, null, this);
        }
    }

    //Deletes existing data in the database
    private void deleteRecipeData(){
        //Deletes all data from the RecipeSteps table
        getContentResolver().delete(
                RecipeProvider.RecipeSteps.CONTENT_URI,
                null,
                null
        );

        //Deletes all data from the RecipeIngredients table
        getContentResolver().delete(
                RecipeProvider.RecipeIngredients.CONTENT_URI,
                null,
                null
        );

        //Deletes all data from the Recipes table
        getContentResolver().delete(
                RecipeProvider.Recipes.CONTENT_URI,
                null,
                null
        );
    }

    //Writes Recipe data to the database
    private void insertRecipe(Recipe recipe){
        ContentValues contentValues = new ContentValues();
        contentValues.put(RecipeContract.COLUMN_NAME, recipe.getName());
        contentValues.put(RecipeContract.COLUMN_SERVINGS, recipe.getServings());
        contentValues.put(RecipeContract.COLUMN_IMAGE_URL, recipe.getImageUrl());

        //Inserts the Recipe to the SQLite database
        Uri newUri = getContentResolver()
                .insert(
                        RecipeProvider.Recipes.CONTENT_URI,
                        contentValues
                );

        //If the insertion of the Recipe was successful, then insert the RecipeIngredients and RecipeSteps
        if(newUri != null){
            long id = ContentUris.parseId(newUri);
            insertRecipeIngredients(recipe.getIngredients(), id);
            insertRecipeSteps(recipe.getSteps(), id);
        }
    }

    //Writes RecipeIngredients data to the database
    private void insertRecipeIngredients(ArrayList<RecipeIngredient> recipeIngredients, long id){
        ContentValues[] contentValues = new ContentValues[recipeIngredients.size()];

        //Adds all RecipeIngredients to a ContentValues array
        for(int i = 0; i < recipeIngredients.size(); i++){
            ContentValues values = new ContentValues();

            values.put(RecipeIngredientContract.COLUMN_INGREDIENT, recipeIngredients.get(i)
                    .getIngredient());

            values.put(RecipeIngredientContract.COLUMN_MEASURE, recipeIngredients.get(i)
                    .getMeasure());

            values.put(RecipeIngredientContract.COLUMN_QUANTITY, recipeIngredients.get(i)
                    .getQuantity());

            values.put(RecipeIngredientContract.COLUMN_RECIPE_ID, id);

            contentValues[i] = values;
        }

        //Inserts the data to the SQLite database
        getContentResolver().bulkInsert(
                RecipeProvider.RecipeIngredients.CONTENT_URI,
                contentValues
        );
    }

    //Writes all RecipeSteps data to the database
    private void insertRecipeSteps(ArrayList<RecipeStep> recipeSteps, long id){
        ContentValues[] contentValues = new ContentValues[recipeSteps.size()];

        //Adds all RecipeIngredients to a ContentValues array
        for(int i = 0; i < recipeSteps.size(); i++){
            ContentValues values = new ContentValues();

            values.put(RecipeStepContract.COLUMN_DESCRIPTION, recipeSteps.get(i)
                    .getDescription());

            values.put(RecipeStepContract.COLUMN_SHORT_DESCRIPTION, recipeSteps.get(i)
                    .getShortDescription());

            values.put(RecipeStepContract.COLUMN_VIDEO_URL, recipeSteps.get(i)
                    .getVideoUrl());

            values.put(RecipeStepContract.COLUMN_THUMBNAIL_URL, recipeSteps.get(i)
                    .getThumbnailUrl());

            values.put(RecipeStepContract.COLUMN_RECIPE_ID, id);

            contentValues[i] = values;
        }

        //Inserts the data to the SQLite database
        getContentResolver().bulkInsert(
                RecipeProvider.RecipeSteps.CONTENT_URI,
                contentValues
        );
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

    /*
     * Used to calculate the number of columns that can be displayed in the RecyclerView
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
        Recipe recipe = mRecipes.get(position);
        if(recipe.getIngredients() != null && recipe.getSteps() != null){
            //Opens the RecipeDetailActivity, passing in the appropriate data
            Intent intent = new Intent(MainActivity.this, RecipeDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(RecipeDetailActivity.RECIPE_BUNDLE_KEY, mRecipes.get(position));
            intent.putExtras(bundle);
            startActivity(intent);
        }
        else if(!NetworkUtilities.isOnline(this)){
            //Checks the SQLite database to check for the missing information
            getSupportLoaderManager().restartLoader(RECIPE_INGREDIENT_LOADER_ID, null, this);
        }
    }

    //Attempts to fetch the recipes again
    public void refreshRecipesOnClick(View view) {
        getRecipes();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch(id){
            case RECIPES_LOADER_ID:
                pbRecipes.setVisibility(View.VISIBLE);
                return new CursorLoader(
                        this,
                        RecipeProvider.Recipes.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );
            case RECIPE_INGREDIENT_LOADER_ID:
                pbRecipes.setVisibility(View.VISIBLE);
                return new CursorLoader(
                        this,
                        RecipeProvider.RecipeIngredients.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );
            case RECIPE_STEPS_LOADER_ID:
                pbRecipes.setVisibility(View.VISIBLE);
                return new CursorLoader(
                        this,
                        RecipeProvider.RecipeSteps.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );
        }
        throw new RuntimeException();
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();

        //Executes the appropriate action based on which Loader has returned a result
        switch(id){
            case RECIPES_LOADER_ID:
                parseRecipesCursor(data);
                getSupportLoaderManager().destroyLoader(RECIPES_LOADER_ID);
                break;
            case RECIPE_INGREDIENT_LOADER_ID:
                parseRecipeIngredientsCursor(data);
                getSupportLoaderManager().destroyLoader(RECIPE_INGREDIENT_LOADER_ID);
                break;
            case RECIPE_STEPS_LOADER_ID:
                parseRecipeStepsCursor(data);
                getSupportLoaderManager().destroyLoader(RECIPE_STEPS_LOADER_ID);
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    //Parses Recipe data from the Cursor and adds the Recipe data to the mRecipes ArrayList
    private void parseRecipesCursor(Cursor cursor){
        if(cursor != null){
            mRecipes = new ArrayList<>();

            //Loops through the Cursor data and adds Recipes to the ArrayList
            while(cursor.moveToNext()){
                //Creates new Recipe
                Recipe recipe = new Recipe(
                        cursor.getLong(cursor.getColumnIndex(RecipeContract.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(RecipeContract.COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndex(RecipeContract.COLUMN_SERVINGS)),
                        cursor.getString(cursor.getColumnIndex(RecipeContract.COLUMN_IMAGE_URL)),
                        null,
                        null
                );

                mRecipes.add(recipe);
            }

            //Displays the Recipe data and fetches the Ingredient and Step data for the Recipes
            if(mRecipes.size() > 0){
                displayRecipes(mRecipes);
                getSupportLoaderManager().restartLoader(RECIPE_INGREDIENT_LOADER_ID, null, this);
                getSupportLoaderManager().restartLoader(RECIPE_STEPS_LOADER_ID, null, this);
            }
            else{
                Toast.makeText(getApplicationContext(), getString(R.string.error_no_recipes_retrieved), Toast.LENGTH_LONG).show();

                //Hides the ProgressBar and displays the TextView and refresh Button
                pbRecipes.setVisibility(View.GONE);
                setRefreshButtonVisibility(View.VISIBLE);
            }
        }
        else{
            //Displays error message saying there is no Internet connection
            Toast.makeText(getApplicationContext(), getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG).show();

            //Hides the ProgressBar and displays the TextView and refresh Button
            pbRecipes.setVisibility(View.GONE);
            setRefreshButtonVisibility(View.VISIBLE);
        }
    }

    //Parses the Cursor of RecipeIngredients and adds the ingredients to the appropriate Recipe in the mRecipe ArrayList
    private void parseRecipeIngredientsCursor(Cursor cursor){
        if(cursor != null){
            //Initialises all the RecipeIngredients ArrayLists for the mRecipes ArrayList
            for(int i = 0; i < mRecipes.size(); i++){
                mRecipes.get(i)
                        .setIngredients(new ArrayList<RecipeIngredient>());
            }

            //Initialises the variables used to assign RecipeIngredients in the upcoming while loop
            long previousId = 0;
            long recipeId = 0;
            ArrayList<RecipeIngredient> recipeIngredients = new ArrayList<>();

            //Loops through the Cursor and adds all RecipeIngredients with the same RecipeID to an ArrayList
            while(cursor.moveToNext()){
                recipeId = cursor.getLong(cursor.getColumnIndex(RecipeIngredientContract.COLUMN_RECIPE_ID));

                //Adds the RecipeIngredients to the appropriate Recipe once all RecipeIngredients with the same RecipeID have been added to the recipeIngredients ArrayList
                if(previousId != recipeId){
                    if(recipeIngredients.size() > 0){
                        //Adds the ingredients to the appropriate Recipe and updates the appropriate variables
                        addIngredientsToRecipe(recipeIngredients, previousId);
                        previousId = recipeId;
                        recipeIngredients = new ArrayList<>();
                    }
                    else if(previousId == 0){
                        previousId = recipeId;
                    }
                }

                //Creates a new RecipeIngredient object
                RecipeIngredient recipeIngredient = new RecipeIngredient(
                        cursor.getString(cursor.getColumnIndex(RecipeIngredientContract.COLUMN_QUANTITY)),
                        cursor.getString(cursor.getColumnIndex(RecipeIngredientContract.COLUMN_MEASURE)),
                        cursor.getString(cursor.getColumnIndex(RecipeIngredientContract.COLUMN_INGREDIENT))
                );

                //Adds the object to the recipeIngredients ArrayList
                recipeIngredients.add(recipeIngredient);
            }

            //Adds the final list of ingredients to the appropriate Recipe and hides the ProgressBar
            addIngredientsToRecipe(recipeIngredients, recipeId);
            pbRecipes.setVisibility(View.GONE);
        }
    }

    //Adds an ArrayList of RecipeIngredients to the appropriate Recipe in the global mRecipes ArrayList, using the ID of the Recipe to determine which Recipe object to add the ingredients to
    private void addIngredientsToRecipe(ArrayList<RecipeIngredient> recipeIngredients, long recipeId){
        for(int i = 0; i < mRecipes.size(); i++){
            long id = mRecipes.get(i).getId();
            if(id == recipeId){
                mRecipes.get(i).setIngredients(recipeIngredients);
            }
        }
    }

    //Parses the Cursor of RecipeSteps and adds the steps to the appropriate Recipe in the mRecipe ArrayList
    private void parseRecipeStepsCursor(Cursor cursor){
        if(cursor != null){
            //Initialises all the RecipeSteps ArrayLists for the mRecipes ArrayList
            for(int i = 0; i < mRecipes.size(); i++){
                mRecipes.get(i)
                        .setSteps(new ArrayList<RecipeStep>());
            }

            //Initialises the variables used to assign RecipeSteps in the upcoming while loop
            long previousId = 0;
            long recipeId = 0;
            ArrayList<RecipeStep> recipeSteps = new ArrayList<>();

            //Loops through the Cursor and adds all RecipeSteps with the same RecipeID to an ArrayList
            while(cursor.moveToNext()){
                recipeId = cursor.getLong(cursor.getColumnIndex(RecipeStepContract.COLUMN_RECIPE_ID));

                //Adds the RecipeSteps to the appropriate Recipe once all RecipeSteps with the same RecipeID have been added to the recipeSteps ArrayList
                if(previousId != recipeId){
                    if(recipeSteps.size() > 0){
                        //Adds the steps to the appropriate Recipe and updates the appropriate variables
                        addStepsToRecipe(recipeSteps, previousId);
                        previousId = recipeId;
                        recipeSteps = new ArrayList<>();
                    }
                    else if(previousId == 0){
                        previousId = recipeId;
                    }
                }

                //Creates a new RecipeStep object
                RecipeStep recipeStep = new RecipeStep(
                        cursor.getString(cursor.getColumnIndex(RecipeStepContract.COLUMN_SHORT_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(RecipeStepContract.COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(RecipeStepContract.COLUMN_VIDEO_URL)),
                        cursor.getString(cursor.getColumnIndex(RecipeStepContract.COLUMN_THUMBNAIL_URL))
                );

                //Adds the object to the recipeSteps ArrayList
                recipeSteps.add(recipeStep);
            }

            //Adds final list of steps to the appropriate Recipe and hides the ProgressBar
            addStepsToRecipe(recipeSteps, recipeId);
            pbRecipes.setVisibility(View.GONE);
        }
    }

    //Adds an ArrayList of RecipeSteps to the appropriate Recipe in the global mRecipes ArrayList, using the ID of the Recipe to determine which Recipe object to add the steps to
    private void addStepsToRecipe(ArrayList<RecipeStep> recipeSteps, long recipeId){
        for(int i = 0; i < mRecipes.size(); i++){
            long id = mRecipes.get(i).getId();
            if(id == recipeId){
                mRecipes.get(i).setSteps(recipeSteps);
            }
        }
    }
}