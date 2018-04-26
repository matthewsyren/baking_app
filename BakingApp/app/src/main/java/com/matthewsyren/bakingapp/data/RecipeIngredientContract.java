package com.matthewsyren.bakingapp.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.ConflictResolutionType;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.References;

/**
 * Used to define the RecipeIngredients database table for the SQLite database
 */

public class RecipeIngredientContract {
    @DataType(DataType.Type.INTEGER)
    @PrimaryKey(onConflict = ConflictResolutionType.REPLACE)
    @AutoIncrement
    public static final String COLUMN_ID = "_id";

    @DataType(DataType.Type.INTEGER)
    @References(table = RecipeDatabase.TABLE_RECIPES, column = RecipeContract.COLUMN_ID)
    public static final String COLUMN_RECIPE_ID = "recipe_id";

    @DataType(DataType.Type.TEXT)
    public static final String COLUMN_INGREDIENT = "ingredient";

    @DataType(DataType.Type.TEXT)
    public static final String COLUMN_QUANTITY = "quantity";

    @DataType(DataType.Type.TEXT)
    public static final String COLUMN_MEASURE = "measure";
}