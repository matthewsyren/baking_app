package com.matthewsyren.bakingapp.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Used to define the database tables for the SQLite database
 */

@Database(version = RecipeDatabase.VERSION)
class RecipeDatabase {
    static final int VERSION = 1;

    @Table(RecipeContract.class)
    static final String TABLE_RECIPES = "recipes";

    @Table(RecipeIngredientContract.class)
    static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    @Table(RecipeStepContract.class)
    static final String TABLE_RECIPE_STEPS = "recipe_steps";
}