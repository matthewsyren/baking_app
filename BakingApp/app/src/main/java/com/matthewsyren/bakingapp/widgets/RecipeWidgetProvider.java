package com.matthewsyren.bakingapp.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.matthewsyren.bakingapp.MainActivity;
import com.matthewsyren.bakingapp.R;
import com.matthewsyren.bakingapp.RecipeDetailActivity;
import com.matthewsyren.bakingapp.utilities.NetworkUtilities;

/**
 * Creates a Widget for the home screen
 */

public class RecipeWidgetProvider
        extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId){
        //Creates the RemoteViews object and uses the AppWidgetManager to update the Widgets
        RemoteViews views = getRemoteViewsList(context);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    //Creates a RemoteViews object that has a list of recipes and their ingredients
    private static RemoteViews getRemoteViewsList(Context context){
        //Creates RemoteViews object and its adapter
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.recipe_widget);
        Intent adapterIntent = new Intent(context, WidgetListService.class);
        remoteViews.setRemoteAdapter(R.id.lv_widget_recipes, adapterIntent);

        //Creates the PendingIntentTemplate to handle click events for the individual Recipes
        Intent detailActivityIntent = new Intent(context, RecipeDetailActivity.class);
        PendingIntent detailActivityPendingIntent = PendingIntent.getActivity(context, 0, detailActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.lv_widget_recipes, detailActivityPendingIntent);

        //Creates PendingIntent that will open MainActivity when the user clicks on the Widget heading
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.tv_widget_heading, mainActivityPendingIntent);

        //Sets the EmptyView for the List
        remoteViews.setEmptyView(R.id.lv_widget_recipes, R.id.tv_empty_view);

        return remoteViews;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        //Updates all Widgets
        for (int appWidgetId : appWidgetIds) {
            //Updates the Widget if there is an Internet connection
            if(NetworkUtilities.isOnline(context)){
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv_widget_recipes);
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {

    }
}