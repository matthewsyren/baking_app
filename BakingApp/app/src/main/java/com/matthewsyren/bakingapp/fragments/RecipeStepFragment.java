package com.matthewsyren.bakingapp.fragments;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.matthewsyren.bakingapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Used to create a Fragment to show the details for a recipe step
 */

public class RecipeStepFragment
        extends Fragment {
    //View bindings
    @BindView(R.id.ep_recipe_video) SimpleExoPlayerView epRecipeVideo;
    @BindView(R.id.tv_recipe_step_description) TextView tvRecipeStepDescription;

    //Variables
    private Uri mUri;
    private String mRecipeStepDescription;

    public RecipeStepFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflates the Fragment's layout
        View view = inflater.inflate(R.layout.fragment_recipe_step, container, false);
        ButterKnife.bind(this, view);
        if(mRecipeStepDescription != null){
            tvRecipeStepDescription.setText(mRecipeStepDescription);
        }

        return view;
    }

    public void setVideoUri(Uri uri){
        mUri = uri;
    }

    public void setRecipeStepDescription(String stepDescription){
        mRecipeStepDescription = stepDescription;
    }
}