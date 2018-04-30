package com.matthewsyren.bakingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.matthewsyren.bakingapp.models.RecipeStep;

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

/**
 * Used to test the RecipeStepActivity
 */

@RunWith(AndroidJUnit4.class)
public class RecipeStepActivityTest {
    @Rule
    public final ActivityTestRule<RecipeStepActivity> mActivityRule = new ActivityTestRule<>(RecipeStepActivity.class);

    private ArrayList<RecipeStep> mRecipeSteps;

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