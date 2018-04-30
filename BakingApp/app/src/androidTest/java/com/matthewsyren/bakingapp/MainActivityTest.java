package com.matthewsyren.bakingapp;

import android.content.res.Configuration;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.matthewsyren.bakingapp.utilities.NetworkUtilities;

import junit.framework.AssertionFailedError;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

/**
 * Used to test functionality on MainActivity
 */

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public final ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    private IdlingResource mIdlingResource;

    @Before
    public void registerIdlingResource(){
        //Gets the IdlingResource
        mIdlingResource = mActivityRule.getActivity()
                .getIdlingResource();

        //Registers the IdlingResource
        IdlingRegistry.getInstance()
                .register(mIdlingResource);
    }

    @Test
    public void recipesDisplayedOrRefreshButtonDisplayed(){
        /*
         * Ensures that either the rv_recipes RecyclerView or the btn_refresh Button is visible at any given time
         * Adapted from https://stackoverflow.com/questions/29250506/espresso-how-to-check-if-one-of-the-view-is-displayed/29262156?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
         */
        try{
            onView(withId(R.id.rv_recipes))
                    .check(matches(isDisplayed()));
        }
        catch(AssertionFailedError a){
            onView(withId(R.id.btn_refresh))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void recipeButtonClick_OpensRecipeDetailActivity(){
        if(NetworkUtilities.isOnline(mActivityRule.getActivity())){
            //Changes device to portrait orientation
            mActivityRule.getActivity()
                    .setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);

            //Clicks on the first Recipe in the RecyclerView
            onView(withId(R.id.rv_recipes))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

            //Ensures that the Ingredients list is not empty on the RecipeDetailActivity
            onView(withId(R.id.tv_recipe_ingredients))
                    .check(matches(not(withText(""))));
        }
    }

    @After
    public void unregisterIdlingResource(){
        //Unregisters the IdlingResource
        if(mIdlingResource != null){
            IdlingRegistry.getInstance().unregister(mIdlingResource);
        }
    }
}