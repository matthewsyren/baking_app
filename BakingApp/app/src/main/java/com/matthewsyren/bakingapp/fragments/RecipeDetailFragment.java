package com.matthewsyren.bakingapp.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.matthewsyren.bakingapp.R;
import com.matthewsyren.bakingapp.adapters.IRecyclerViewOnClickListener;
import com.matthewsyren.bakingapp.adapters.RecipeDetailsAdapter;
import com.matthewsyren.bakingapp.models.Recipe;
import com.matthewsyren.bakingapp.models.RecipeIngredient;

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

    //Variables and constants
    private Recipe mRecipe;
    private IRecyclerViewOnClickListener mRecyclerViewOnClickListener;
    private int mSelectedPosition;
    private boolean mIsTwoPane;

    public RecipeDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflates the Fragment's layout
        View view = inflater.inflate(R.layout.fragment_recipe_detail, container, false);
        ButterKnife.bind(this, view);
        if(mRecipe != null){
            displayRecipeInformation();
        }
        return view;
    }

    //Displays the recipe's information in the appropriate Views
    private void displayRecipeInformation(){
        //Displays recipe's ingredients
        for(RecipeIngredient ingredient : mRecipe.getIngredients()){
            tvRecipeIngredients.append(getString(R.string.recipe_ingredient_bullet_point, ingredient.getQuantity(), ingredient.getMeasure(), ingredient.getIngredient()));
        }

        //Displays the steps for the recipe in a RecyclerView
        rvRecipeSteps.setFocusable(false);
        rvRecipeSteps.setNestedScrollingEnabled(false);

        RecipeDetailsAdapter recipeDetailsAdapter = new RecipeDetailsAdapter(mRecipe.getSteps(), this, mIsTwoPane);
        recipeDetailsAdapter.setSelectedPosition(mSelectedPosition);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rvRecipeSteps.setLayoutManager(linearLayoutManager);
        rvRecipeSteps.setAdapter(recipeDetailsAdapter);
    }

    public void setRecipe(Recipe recipe){
        mRecipe = recipe;
    }

    public void setSelectedPosition(int position){
        mSelectedPosition = position;
    }

    public void setIsTwoPane(boolean isTwoPane){
        mIsTwoPane = isTwoPane;
    }

    public void setRecyclerViewOnClickListener(IRecyclerViewOnClickListener recyclerViewOnClickListener){
        mRecyclerViewOnClickListener = recyclerViewOnClickListener;
    }

    @Override
    public void onItemClick(int position) {
        if(mRecyclerViewOnClickListener != null){
            mRecyclerViewOnClickListener.onItemClick(position);
        }
    }
}