package com.matthewsyren.bakingapp;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.matthewsyren.bakingapp.fragments.RecipeStepFragment;
import com.matthewsyren.bakingapp.models.RecipeStep;
import com.matthewsyren.bakingapp.utilities.DeviceUtilities;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeStepActivity
        extends AppCompatActivity {
    //View bindings
    @Nullable
    @BindView(R.id.btn_previous_step) Button btnPreviousStep;
    @Nullable
    @BindView(R.id.btn_next_step) Button btnNextStep;
    @Nullable
    @BindView(R.id.ll_navigate_steps) LinearLayout llNavigateSteps;
    @BindView(R.id.v_navigation_buttons_separator) View vNavigationButtonsSeparator;

    //Variables
    private ArrayList<RecipeStep> mRecipeSteps;
    private int mSelectedStepIndex = 0;
    private String mRecipeName;
    public static final String RECIPE_STEPS_BUNDLE_KEY = "recipe_bundle_key";
    public static final String SELECTED_STEP_INDEX_BUNDLE_KEY = "selected_step_index_bundle_key";
    public static final String RECIPE_NAME_BUNDLE_KEY = "recipe_name_bundle_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        determineActionBarVisibility();
        setContentView(R.layout.activity_recipe_step);
        ButterKnife.bind(this);

        Bundle bundle = getIntent()
                .getExtras();

        //Restores data or fetches the data from the Bundle passed from the previous Intent
        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }
        else if(bundle != null) {
            if(bundle.containsKey(SELECTED_STEP_INDEX_BUNDLE_KEY)){
                mSelectedStepIndex = bundle.getInt(SELECTED_STEP_INDEX_BUNDLE_KEY);
            }

            if(bundle.containsKey(RECIPE_STEPS_BUNDLE_KEY)){
                mRecipeSteps = bundle.getParcelableArrayList(RECIPE_STEPS_BUNDLE_KEY);

                if(mRecipeSteps != null){
                    displayRecipeStepDetails(mRecipeSteps.get(mSelectedStepIndex));
                }
            }

            if(bundle.containsKey(RECIPE_NAME_BUNDLE_KEY)){
                mRecipeName = bundle.getString(RECIPE_NAME_BUNDLE_KEY, "");
                setTitle(mRecipeName);
            }
        }

        //Determines the visibility of the Buttons
        determineButtonVisibility();
    }

    /*
     * Hides the ActionBar and NotificationBar in landscape orientation
     * Adapted from https://stackoverflow.com/questions/11856886/hiding-title-bar-notification-bar-when-device-is-oriented-to-landscape?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
     */
    private void determineActionBarVisibility(){
        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null){
            if(DeviceUtilities.getDeviceOrientation(this) == Configuration.ORIENTATION_LANDSCAPE){
                //Hides ActionBar and NotificationBar
                actionBar.hide();
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            else{
                //Displays ActionBar and NotificationBar
                actionBar.show();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mRecipeSteps != null){
            outState.putParcelableArrayList(RECIPE_STEPS_BUNDLE_KEY, mRecipeSteps);
        }

        outState.putInt(SELECTED_STEP_INDEX_BUNDLE_KEY, mSelectedStepIndex);
        outState.putString(RECIPE_NAME_BUNDLE_KEY, mRecipeName);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreData(savedInstanceState);
    }

    //Restores the appropriate data
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(SELECTED_STEP_INDEX_BUNDLE_KEY)) {
            mSelectedStepIndex = savedInstanceState.getInt(SELECTED_STEP_INDEX_BUNDLE_KEY);
        }

        if(savedInstanceState.containsKey(RECIPE_STEPS_BUNDLE_KEY)){
            mRecipeSteps = savedInstanceState.getParcelableArrayList(RECIPE_STEPS_BUNDLE_KEY);
        }

        if(savedInstanceState.containsKey(RECIPE_NAME_BUNDLE_KEY)){
            mRecipeName = savedInstanceState.getString(RECIPE_NAME_BUNDLE_KEY, "");
            setTitle(mRecipeName);
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

        recipeStepFragment.setRecipeStep(recipeStep);

        fragmentManager.beginTransaction()
                .replace(R.id.fl_recipe_step, recipeStepFragment, "")
                .commit();
    }

    //Displays the previous step for the recipe
    public void previousStepOnClick(View view) {
        if(mSelectedStepIndex > 0){
            mSelectedStepIndex--;
            displayRecipeStepDetails(mRecipeSteps.get(mSelectedStepIndex));
            determineButtonVisibility();
        }
    }

    //Displays the next step for the recipe
    public void nextStepOnClick(View view) {
        if(mSelectedStepIndex < mRecipeSteps.size() - 1){
            mSelectedStepIndex++;
            displayRecipeStepDetails(mRecipeSteps.get(mSelectedStepIndex));
            determineButtonVisibility();
        }
    }

    //Determines whether the next and previous step Buttons should be visible
    private void determineButtonVisibility(){
        if(mRecipeSteps != null){
            RecipeStep recipeStep = mRecipeSteps.get(mSelectedStepIndex);
            String thumbnailUrl = recipeStep.getThumbnailUrl();

            //Displays the Buttons when appropriate (when the device is in portrait mode or if there is no video or thumbnail to display)
            if(DeviceUtilities.getDeviceOrientation(this) == Configuration.ORIENTATION_PORTRAIT ||
                    (recipeStep.getVideoUri() == null && (thumbnailUrl == null || thumbnailUrl.equals("")))){
                vNavigationButtonsSeparator.setVisibility(View.VISIBLE);

                if(btnPreviousStep != null){
                    if(mSelectedStepIndex == 0){
                        btnPreviousStep.setVisibility(View.INVISIBLE);
                    }
                    else{
                        btnPreviousStep.setVisibility(View.VISIBLE);
                    }
                }

                if(btnNextStep != null){
                    if(mSelectedStepIndex == mRecipeSteps.size() - 1){
                        btnNextStep.setVisibility(View.INVISIBLE);
                    }
                    else{
                        btnNextStep.setVisibility(View.VISIBLE);
                    }
                }
            }
            else{
                //Hides all buttons when there is a video playing in landscape orientation
                if(llNavigateSteps != null){
                    llNavigateSteps.setVisibility(View.GONE);
                    vNavigationButtonsSeparator.setVisibility(View.GONE);
                }
            }
        }
    }
}