package com.matthewsyren.bakingapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.matthewsyren.bakingapp.adapters.IRecyclerViewOnClickListener;
import com.matthewsyren.bakingapp.fragments.RecipeDetailFragment;
import com.matthewsyren.bakingapp.fragments.RecipeStepFragment;
import com.matthewsyren.bakingapp.models.Recipe;
import com.matthewsyren.bakingapp.models.RecipeStep;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeDetailActivity
        extends AppCompatActivity
        implements IRecyclerViewOnClickListener {
    //View bindings
    @BindView(R.id.fl_recipe_details) FrameLayout flRecipeDetails;
    @Nullable
    @BindView(R.id.ep_recipe_video) SimpleExoPlayerView epRecipeVideo;
    @Nullable
    @BindView(R.id.tv_recipe_step_description) TextView tvRecipeStepDescription;
    @Nullable
    @BindView(R.id.fl_recipe_step) FrameLayout flRecipeStep;

    //Variables
    public static final String RECIPE_BUNDLE_KEY = "recipe_bundle_key";
    private Recipe mRecipe;
    private boolean mIsTwoPane = false;
    private boolean mIsDataRestored = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        ButterKnife.bind(this);

        //Determines whether the device should have two panes by checking if the device is a tablet
        if(flRecipeStep != null){
            mIsTwoPane = true;
        }

        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }
        else{
            Bundle bundle = getIntent().getExtras();
            if(bundle != null && bundle.containsKey(RECIPE_BUNDLE_KEY)){
                mRecipe = bundle.getParcelable(RECIPE_BUNDLE_KEY);

                if(mRecipe != null){
                    setTitle(mRecipe.getName());
                    displayRecipeInformation(mRecipe);
                }
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }
    }

    //Restores the appropriate data
    private void restoreData(Bundle savedInstanceState){
        mIsDataRestored = true;

        if(savedInstanceState.containsKey(RECIPE_BUNDLE_KEY)){
            mRecipe = savedInstanceState.getParcelable(RECIPE_BUNDLE_KEY);

            if(mRecipe != null){
                setTitle(mRecipe.getName());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mRecipe != null){
            outState.putParcelable(RECIPE_BUNDLE_KEY, mRecipe);
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

    //Displays the recipe information in the appropriate Views
    private void displayRecipeInformation(Recipe recipe){
        if(!mIsDataRestored){
            FragmentManager fragmentManager = getSupportFragmentManager();
            RecipeDetailFragment recipeDetailFragment = new RecipeDetailFragment();
            recipeDetailFragment.setRecipe(recipe);
            recipeDetailFragment.setRecyclerViewOnClickListener(this);
            recipeDetailFragment.setIsTwoPane(mIsTwoPane);

            fragmentManager.beginTransaction()
                    .replace(R.id.fl_recipe_details, recipeDetailFragment)
                    .commit();

            //Displays the RecipeStepDetails Fragment if the device has a two-pane layout
            if(mIsTwoPane && !mIsDataRestored){
                displayRecipeStepDetails(0);
            }
        }
    }

    //Displays the step details for the recipe step in a two-pane layout
    private void displayRecipeStepDetails(int position){
        FragmentManager fragmentManager = getSupportFragmentManager();
        RecipeStepFragment recipeStepFragment = new RecipeStepFragment();
        RecipeStep recipeStep = mRecipe.getSteps()
                .get(position);

        recipeStepFragment.setRecipeStep(recipeStep);

        fragmentManager.beginTransaction()
                .replace(R.id.fl_recipe_step, recipeStepFragment)
                .commit();
    }

    @Override
    public void onItemClick(int position) {
        displayRecipeStepDetails(position);
    }
}