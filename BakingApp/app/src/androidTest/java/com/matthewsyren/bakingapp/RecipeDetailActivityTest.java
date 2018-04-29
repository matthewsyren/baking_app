package com.matthewsyren.bakingapp;

import android.content.Intent;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.matthewsyren.bakingapp.models.Recipe;
import com.matthewsyren.bakingapp.models.RecipeIngredient;
import com.matthewsyren.bakingapp.models.RecipeStep;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.startsWith;

/**
 * Used to test the RecipeDetailActivity
 */

@RunWith(AndroidJUnit4.class)
public class RecipeDetailActivityTest {
    @Rule
    public final ActivityTestRule<RecipeDetailActivity> mActivityRule = new ActivityTestRule<>(RecipeDetailActivity.class);

    private Recipe mRecipe;

    @Test
    public void stepWithNoVideoClick_OpensStepFragmentWithVideoErrorMessage(){
        //Sends the data to the Activity (no video URL is passed in as this tests a case when there is no video)
        sendDataToActivity("");

        //Scrolls to ensure the RecyclerView is visible
        onView(withId(R.id.rv_recipe_steps))
                .perform(ViewActions.scrollTo());

        //Clicks on the first Recipe Step in the RecyclerView
        onView(withId(R.id.rv_recipe_steps))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Ensures that the app displays a message saying there is no video for the step
        onView(withId(R.id.tv_recipe_step_description))
                .check(matches(withText(startsWith(
                        "This step has no video"
                ))));
    }

    @Test
    public void stepWithVideoClick_OpensStepFragmentWithCorrectDescription(){
        //Sends the data to the Activity (a video URL is passed in, as this tests a case when there is a video to be displayed)
        sendDataToActivity("https://d17h27t6h515a5.cloudfront.net/topher/2017/April/58ffdae8_-intro-cheesecake/-intro-cheesecake.mp4");

        //Scrolls to ensure the RecyclerView is visible
        onView(withId(R.id.rv_recipe_steps))
                .perform(ViewActions.scrollTo());

        //Clicks on the first Recipe Step in the RecyclerView
        onView(withId(R.id.rv_recipe_steps))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Ensures that the app displays the correct description when there is a video
        onView(withId(R.id.tv_recipe_step_description))
                .check(matches(withText(startsWith(
                        mRecipe.getSteps()
                        .get(0)
                        .getDescription()
                ))));
    }

    //Generates a dummy Recipe object
    private Recipe getDummyRecipe(String videoUrl){
        //Sets up required data for the Activity
        ArrayList<RecipeIngredient> recipeIngredients = new ArrayList<>();
        recipeIngredients.add(new RecipeIngredient(
                "1",
                "kg",
                "Chocolate"
        ));

        ArrayList<RecipeStep> recipeSteps = new ArrayList<>();
        recipeSteps.add(new RecipeStep(
                "Recipe Introduction",
                "Introduction to the brownie recipe",
                videoUrl,
                ""
        ));

        return new Recipe(1, "Brownies", "8", "", recipeIngredients, recipeSteps);
    }

    /*
     * Starts the Activity with the appropriate data
     * Adapted from https://stackoverflow.com/questions/31752303/espresso-startactivity-that-depends-on-intent?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
     */
    private void sendDataToActivity(String videoUrl){
        mRecipe = getDummyRecipe(videoUrl);
        Intent intent = new Intent();
        intent.putExtra(RecipeDetailActivity.RECIPE_BUNDLE_KEY, mRecipe);
        mActivityRule.launchActivity(intent);
    }
}