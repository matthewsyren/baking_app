package com.matthewsyren.bakingapp;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.matthewsyren.bakingapp.fragments.RecipeStepFragment;
import com.matthewsyren.bakingapp.models.RecipeStep;

public class RecipeStepActivity extends AppCompatActivity {
    //Variables
    private RecipeStep mRecipeStep;
    public static final String RECIPE_STEP_BUNDLE_KEY = "recipe_step_bundle_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_step);

        Bundle bundle = getIntent()
                .getExtras();

        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }
        else if (bundle != null && bundle.containsKey(RECIPE_STEP_BUNDLE_KEY)) {
            mRecipeStep = bundle.getParcelable(RECIPE_STEP_BUNDLE_KEY);
            displayRecipeStepDetails(mRecipeStep);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mRecipeStep != null){
            outState.putParcelable(RECIPE_STEP_BUNDLE_KEY, mRecipeStep);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreData(savedInstanceState);
    }

    //Restores the appropriate data
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(RECIPE_STEP_BUNDLE_KEY)){
            mRecipeStep = savedInstanceState.getParcelable(RECIPE_STEP_BUNDLE_KEY);
            displayRecipeStepDetails(mRecipeStep);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Displays the recipe step details in the Fragment
    private void displayRecipeStepDetails(RecipeStep recipeStep){
        FragmentManager fragmentManager = getSupportFragmentManager();
        RecipeStepFragment recipeStepFragment = new RecipeStepFragment();

        recipeStepFragment.setRecipeStepDescription(recipeStep.getDescription());
        recipeStepFragment.setVideoUri(recipeStep.getVideoUri());

        fragmentManager.beginTransaction()
                .replace(R.id.fl_recipe_step, recipeStepFragment)
                .commit();
    }
}