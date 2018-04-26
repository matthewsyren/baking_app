package com.matthewsyren.bakingapp.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Used to create a ContentProvider for the Recipes database
 */

@ContentProvider(
        authority = RecipeProvider.AUTHORITY,
        database = RecipeDatabase.class
)
public class RecipeProvider {
    static final String AUTHORITY = "com.matthewsyren.bakingapp";

    @TableEndpoint(table = RecipeDatabase.TABLE_RECIPES)
    public static class Recipes {
        @ContentUri(
                path = "recipes",
                type = "vnd.android.cursor.dir/recipe",
                defaultSort = RecipeContract.COLUMN_ID + " ASC")
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/recipes");
    }

    @TableEndpoint(table = RecipeDatabase.TABLE_RECIPE_INGREDIENTS)
    public static class RecipeIngredients {
        @ContentUri(
                path = "recipe_ingredients",
                type = "vnd.android.cursor.dir/recipe_ingredient",
                defaultSort = RecipeIngredientContract.COLUMN_ID + " ASC")
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/recipe_ingredients");
    }

    @TableEndpoint(table = RecipeDatabase.TABLE_RECIPE_STEPS)
    public static class RecipeSteps {
        @ContentUri(
                path = "recipe_steps",
                type = "vnd.android.cursor.dir/recipe_step",
                defaultSort = RecipeStepContract.COLUMN_ID + " ASC")
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/recipe_steps");
    }
}