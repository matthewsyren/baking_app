package com.matthewsyren.bakingapp.widgets;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.matthewsyren.bakingapp.R;
import com.matthewsyren.bakingapp.RecipeDetailActivity;
import com.matthewsyren.bakingapp.models.Recipe;
import com.matthewsyren.bakingapp.models.RecipeIngredient;
import com.matthewsyren.bakingapp.utilities.IApiClient;
import com.matthewsyren.bakingapp.utilities.NetworkUtilities;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Used to display the Recipe details in a Widget
 */

public class WidgetListService
        extends RemoteViewsService{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent){
        return new ListRemoteViewsService(this.getApplicationContext());
    }
}

class ListRemoteViewsService
        implements RemoteViewsService.RemoteViewsFactory{
    private Context mContext;
    private ArrayList<Recipe> mRecipes;

    ListRemoteViewsService(Context context){
        mContext = context;
        mRecipes = new ArrayList<>();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        //Attempts to fetch the recipe information using Retrofit if there is an Internet connection
        if(NetworkUtilities.isOnline(mContext)) {
            try{
                //Sets up the connection to the API
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(IApiClient.RECIPE_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                IApiClient client = retrofit.create(IApiClient.class);

                //Fetches the recipe information
                Call<ArrayList<Recipe>> call = client.getRecipes();
                mRecipes = call.execute()
                        .body();
            }
            catch(IOException i){
                i.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if(mRecipes == null){
            return 0;
        }
        return mRecipes.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);

        remoteViews.setTextViewText(R.id.tv_widget_recipe_label, mRecipes.get(position)
                .getName());

        //Displays the list of ingredients for the recipe
        String ingredients = "";

        ArrayList<RecipeIngredient> recipeIngredients = mRecipes.get(position)
                .getIngredients();

        for(RecipeIngredient recipeIngredient : recipeIngredients){
            ingredients += mContext.getString(R.string.recipe_ingredient_bullet_point,
                    recipeIngredient.getQuantity(),
                    recipeIngredient.getMeasure(),
                    recipeIngredient.getIngredient());
        }

        remoteViews.setTextViewText(R.id.tv_widget_recipe_ingredients, ingredients);

        //Sets the FillInIntent for the row
        Bundle bundle = new Bundle();
        bundle.putParcelable(RecipeDetailActivity.RECIPE_BUNDLE_KEY, mRecipes.get(position));
        Intent intent = new Intent();
        intent.putExtras(bundle);
        remoteViews.setOnClickFillInIntent(R.id.ll_recipe_widget, intent);
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}