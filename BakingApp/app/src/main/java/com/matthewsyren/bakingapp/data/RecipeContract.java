package com.matthewsyren.bakingapp.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.ConflictResolutionType;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Used to define the Recipes database table for the SQLite database
 */

public class RecipeContract {
    @DataType(DataType.Type.INTEGER)
    @PrimaryKey(onConflict = ConflictResolutionType.REPLACE)
    @AutoIncrement
    public static final String COLUMN_ID = "_id";

    @DataType(DataType.Type.TEXT)
    public static final String COLUMN_NAME = "name";

    @DataType(DataType.Type.TEXT)
    public static final String COLUMN_SERVINGS = "servings";

    @DataType(DataType.Type.TEXT)
    public static final String COLUMN_IMAGE_URL = "image_url";
}