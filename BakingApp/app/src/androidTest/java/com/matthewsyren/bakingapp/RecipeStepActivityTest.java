package com.matthewsyren.bakingapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.matthewsyren.bakingapp.models.RecipeStep;
import com.matthewsyren.bakingapp.utilities.DeviceUtilities;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

/**
 * Used to test the RecipeStepActivity
 */

@RunWith(AndroidJUnit4.class)
public class RecipeStepActivityTest {
    @Rule
    public final ActivityTestRule<RecipeStepActivity> mActivityRule = new ActivityTestRule<>(RecipeStepActivity.class);

    private ArrayList<RecipeStep> mRecipeSteps;

    @Test
    public void stepWithVideoInLandscapeOrientation_VideoFullScreen(){
        //Tests for non-tablet devices only, as tablets in landscape orientation won't show the video in fullscreen
        if(!DeviceUtilities.isTablet(mActivityRule.getActivity())){
            //Sends data to the Activity (a video URL is sent, as this tests a case when a video is displayed)
            sendDataToActivity("https://d17h27t6h515a5.cloudfront.net/topher/2017/April/58ffdae8_-intro-cheesecake/-intro-cheesecake.mp4");

            //Sets the Activity to landscape orientation
            mActivityRule.getActivity()
                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            //Ensures that the appropriate Views are hidden when there is a video in landscape mode (in other words, the video must be full screen)
            onView(withId(R.id.tv_recipe_step_description)).check(matches(not(isDisplayed())));
            onView(withId(R.id.ll_navigate_steps)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void stepWithNoVideo_NavigationButtonsAndDescriptionDisplayed(){
        //Sends the data to the Activity (a video URL is not passed in, as this tests a case when there is no video to be displayed)
        sendDataToActivity("");

        //Ensures that the description TextView and navigation Buttons are visible
        onView(withId(R.id.tv_recipe_step_description)).check(matches(isDisplayed()));
        onView(withId(R.id.ll_navigate_steps)).check(matches(isDisplayed()));
    }

    @Test
    public void navigateStepClick_ChangesStep(){
        //Sends the data to the Activity (a video URL is not passed in, as this tests a case when there is no video to be displayed)
        sendDataToActivity("");

        //Clicks on next Button
        onView(withId(R.id.btn_next_step)).perform(click());

        //Ensures that the description TextView has updated to the next item in the ArrayList
        onView(withId(R.id.tv_recipe_step_description))
                .check(matches(withText(containsString(
                        mRecipeSteps.get(1)
                        .getDescription()
                ))));
    }

    //Generates a dummy RecipeStep ArrayList
    private ArrayList<RecipeStep> getDummyRecipeSteps(String videoUrl){
        //Sets up required data for the Activity
        ArrayList<RecipeStep> recipeSteps = new ArrayList<>();

        recipeSteps.add(new RecipeStep(
                "Recipe Introduction",
                "Introduction to the brownie recipe",
                videoUrl,
                ""
        ));

        recipeSteps.add(new RecipeStep(
                "Starting Prep",
                "Preheat the oven to 350 degrees",
                videoUrl,
                ""
        ));

        return recipeSteps;
    }

    /*
     * Starts the Activity with the appropriate data
     * Adapted from https://stackoverflow.com/questions/31752303/espresso-startactivity-that-depends-on-intent?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
     */
    private void sendDataToActivity(String videoUrl){
        mRecipeSteps = getDummyRecipeSteps(videoUrl);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(RecipeStepActivity.RECIPE_STEPS_BUNDLE_KEY, mRecipeSteps);
        bundle.putString(RecipeStepActivity.RECIPE_NAME_BUNDLE_KEY, "Brownies");
        bundle.putInt(RecipeStepActivity.SELECTED_STEP_INDEX_BUNDLE_KEY, 0);
        intent.putExtras(bundle);
        mActivityRule.launchActivity(intent);
    }
}