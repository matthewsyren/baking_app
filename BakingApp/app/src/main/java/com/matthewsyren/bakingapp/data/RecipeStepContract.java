package com.matthewsyren.bakingapp.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.ConflictResolutionType;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.References;

/**
 * Used to define the RecipeSteps database table for the SQLite database
 */

public class RecipeStepContract {
    @DataType(DataType.Type.INTEGER)
    @PrimaryKey(onConflict = ConflictResolutionType.REPLACE)
    @AutoIncrement
    public static final String COLUMN_ID = "_id";

    @DataType(DataType.Type.INTEGER)
    @References(table = RecipeDatabase.TABLE_RECIPES, column = RecipeContract.COLUMN_ID)
    public static final String COLUMN_RECIPE_ID = "recipe_id";

    @DataType(DataType.Type.TEXT)
    public static final String COLUMN_SHORT_DESCRIPTION = "short_description";

    @DataType(DataType.Type.TEXT)
    public static final String COLUMN_DESCRIPTION = "description";

    @DataType(DataType.Type.TEXT)
    public static final String COLUMN_VIDEO_URL = "video_url";

    @DataType(DataType.Type.TEXT)
    public static final String COLUMN_THUMBNAIL_URL = "thumbnail_url";
}