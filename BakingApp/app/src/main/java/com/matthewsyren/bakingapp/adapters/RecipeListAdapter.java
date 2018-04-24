package com.matthewsyren.bakingapp.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.matthewsyren.bakingapp.R;
import com.matthewsyren.bakingapp.models.Recipe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Used to display the recipes in a RecyclerView
 */

public class RecipeListAdapter
        extends RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder> {
    private IRecyclerViewOnClickListener mRecyclerViewOnClickListener;
    private ArrayList<Recipe> mRecipes;

    public RecipeListAdapter(ArrayList<Recipe> recipes, IRecyclerViewOnClickListener recyclerViewOnClickListener){
        mRecipes = recipes;
        mRecyclerViewOnClickListener = recyclerViewOnClickListener;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recipe_list_item, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        Context context = holder.tvRecipeName.getContext();

        holder.tvRecipeName.setText(mRecipes.get(position).getName());

        holder.tvRecipeServings.setText(
                context.getString(
                        R.string.recipe_servings,
                        mRecipes.get(position).getServings())
        );

        //Displays the image for the recipe, or a default image if there is no image for the recipe
        if(mRecipes.get(position) != null && !mRecipes.get(position).getImageUrl().equals("")){
            Picasso.with(context)
                    .load(mRecipes.get(position)
                            .getImageUrl())
                    .placeholder(R.drawable.ic_cake_black_24dp)
                    .error(R.drawable.ic_cake_black_24dp)
                    .fit()
                    .centerInside()
                    .into(holder.ivRecipeImage);
        }
        else{
            holder.ivRecipeImage.setImageDrawable(context
                    .getResources()
                    .getDrawable(R.drawable.ic_cake_black_24dp));

            holder.ivRecipeImage.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

    //Provides the ViewHolder for the RecyclerView Views
    class RecipeViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        //View bindings
        @BindView(R.id.tv_recipe_name) TextView tvRecipeName;
        @BindView(R.id.tv_recipe_servings) TextView tvRecipeServings;
        @BindView(R.id.iv_recipe_image) ImageView ivRecipeImage;

        RecipeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mRecyclerViewOnClickListener.onItemClick(getAdapterPosition());
        }
    }
}