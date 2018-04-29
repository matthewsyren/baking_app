package com.matthewsyren.bakingapp.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.matthewsyren.bakingapp.R;
import com.matthewsyren.bakingapp.RecipeStepActivity;
import com.matthewsyren.bakingapp.adapters.IRecyclerViewOnClickListener;
import com.matthewsyren.bakingapp.adapters.RecipeDetailsAdapter;
import com.matthewsyren.bakingapp.models.Recipe;
import com.matthewsyren.bakingapp.models.RecipeIngredient;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Used to create a Fragment to show recipe details
 */

public class RecipeDetailFragment
        extends Fragment
        implements IRecyclerViewOnClickListener {
    //View bindings
    @BindView(R.id.tv_recipe_ingredients) TextView tvRecipeIngredients;
    @BindView(R.id.rv_recipe_steps) RecyclerView rvRecipeSteps;
    @BindView(R.id.sv_recipe_details) ScrollView svRecipeDetails;

    //Variables and constants
    private Recipe mRecipe;
    private IRecyclerViewOnClickListener mRecyclerViewOnClickListener;
    private int mSelectedPosition = 0;
    private boolean mIsTwoPane;
    private static final String SELECTED_POSITION_BUNDLE_KEY = "selected_position_bundle_key";
    private static final String RECIPE_BUNDLE_KEY = "recipe_bundle_key";
    private static final String IS_TWO_PANE_BUNDLE_KEY = "is_two_pane_bundle_key";
    private static final String SCROLL_VIEW_POSITION_BUNDLE_KEY = "scroll_view_position_bundle_key";

    public RecipeDetailFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflates the Fragment's layout
        View view = inflater.inflate(R.layout.fragment_recipe_detail, container, false);
        ButterKnife.bind(this, view);

        //Restores data
        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }

        //Displays data
        if(mRecipe != null){
            displayRecipeInformation();
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(RECIPE_BUNDLE_KEY, mRecipe);
        outState.putBoolean(IS_TWO_PANE_BUNDLE_KEY, mIsTwoPane);
        outState.putInt(SELECTED_POSITION_BUNDLE_KEY, mSelectedPosition);

        /*
         * Puts the ScrollView information into the Bundle
         * Adapted from https://stackoverflow.com/questions/29208086/save-the-position-of-scrollview-when-the-orientation-changes?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
         */
        outState.putIntArray(SCROLL_VIEW_POSITION_BUNDLE_KEY, new int[]{
                svRecipeDetails.getScrollX(),
                svRecipeDetails.getScrollY()});
    }

    //Restores the appropriate data
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(RECIPE_BUNDLE_KEY)){
            mRecipe = savedInstanceState.getParcelable(RECIPE_BUNDLE_KEY);
        }

        if(savedInstanceState.containsKey(SELECTED_POSITION_BUNDLE_KEY)){
            mSelectedPosition = savedInstanceState.getInt(SELECTED_POSITION_BUNDLE_KEY);
        }

        if(savedInstanceState.containsKey(IS_TWO_PANE_BUNDLE_KEY)){
            mIsTwoPane = savedInstanceState.getBoolean(IS_TWO_PANE_BUNDLE_KEY);
        }

        //Restores the onclick callback for the RecyclerView
        Activity activity = getActivity();
        if(activity != null && activity instanceof IRecyclerViewOnClickListener){
            mRecyclerViewOnClickListener = (IRecyclerViewOnClickListener) activity;
        }

        /*
         * Restores the ScrollView's position
         * Adapted from https://stackoverflow.com/questions/29208086/save-the-position-of-scrollview-when-the-orientation-changes?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
         */
        if(savedInstanceState.containsKey(SCROLL_VIEW_POSITION_BUNDLE_KEY)){
            final int[] scrollViewPosition = savedInstanceState.getIntArray(SCROLL_VIEW_POSITION_BUNDLE_KEY);
            if(scrollViewPosition != null)
                svRecipeDetails.post(new Runnable(){
                    public void run() {
                        svRecipeDetails.scrollTo(scrollViewPosition[0], scrollViewPosition[1]);
                    }
                });
        }
    }

    //Displays the recipe's information in the appropriate Views
    private void displayRecipeInformation(){
        //Displays the recipe's ingredients
        ArrayList<RecipeIngredient> ingredients = mRecipe.getIngredients();
        for(RecipeIngredient ingredient : ingredients){
            tvRecipeIngredients.append(getString(R.string.recipe_ingredient_bullet_point, ingredient.getQuantity(), ingredient.getMeasure(), ingredient.getIngredient()));
        }

        //Displays the steps for the recipe in a RecyclerView
        RecipeDetailsAdapter recipeDetailsAdapter = new RecipeDetailsAdapter(mRecipe.getSteps(), this, mIsTwoPane);
        recipeDetailsAdapter.setSelectedPosition(mSelectedPosition);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvRecipeSteps.setLayoutManager(linearLayoutManager);
        rvRecipeSteps.setAdapter(recipeDetailsAdapter);

        //Makes the RecyclerView non-scrollable so that the user can use the ScrollView that the RecyclerView is nested in to scroll
        rvRecipeSteps.setFocusable(false);
        rvRecipeSteps.setNestedScrollingEnabled(false);
    }

    public void setRecipe(Recipe recipe){
        mRecipe = recipe;
    }

    public void setIsTwoPane(boolean isTwoPane){
        mIsTwoPane = isTwoPane;
    }

    public void setRecyclerViewOnClickListener(IRecyclerViewOnClickListener recyclerViewOnClickListener){
        mRecyclerViewOnClickListener = recyclerViewOnClickListener;
    }

    @Override
    public void onItemClick(int position) {
        mSelectedPosition = position;

        if(mRecyclerViewOnClickListener != null){
            //Displays the details in the appropriate place (based on whether the device is a tablet or not)
            if(mIsTwoPane){
                mRecyclerViewOnClickListener.onItemClick(position);
            }
        }

        if(!mIsTwoPane){
            //Opens Activity to display the recipe step's information
            Intent intent = new Intent(getContext(), RecipeStepActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(RecipeStepActivity.RECIPE_STEPS_BUNDLE_KEY, mRecipe.getSteps());
            bundle.putString(RecipeStepActivity.RECIPE_NAME_BUNDLE_KEY, mRecipe.getName());
            bundle.putInt(RecipeStepActivity.SELECTED_STEP_INDEX_BUNDLE_KEY, position);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }
}