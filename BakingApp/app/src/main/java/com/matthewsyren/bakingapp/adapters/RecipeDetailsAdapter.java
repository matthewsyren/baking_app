package com.matthewsyren.bakingapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.matthewsyren.bakingapp.R;
import com.matthewsyren.bakingapp.models.RecipeStep;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Used to display the recipe information
 */

public class RecipeDetailsAdapter
        extends RecyclerView.Adapter<RecipeDetailsAdapter.RecipeDetailsViewHolder> {
    private IRecyclerViewOnClickListener mRecyclerViewOnClickListener;
    private ArrayList<RecipeStep> mSteps;
    private int mSelectedPosition = 0;
    private boolean mIsTwoPane;

    public RecipeDetailsAdapter(ArrayList<RecipeStep> steps, IRecyclerViewOnClickListener recyclerViewOnClickListener, boolean isTwoPane) {
        mSteps = steps;
        mRecyclerViewOnClickListener = recyclerViewOnClickListener;
        mIsTwoPane = isTwoPane;
    }

    public void setSelectedPosition(int position){
        mSelectedPosition = position;
    }

    @Override
    @NonNull
    public RecipeDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.recipe_steps_list_item, parent, false);
        return new RecipeDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeDetailsViewHolder holder, int position) {
        Context context = holder.tvRecipeStep.getContext();

        //Displays description
        holder.tvRecipeStep.setText(mSteps
                .get(position)
                .getShortDescription());

        //Hides icon if there is no video, or displays it if there is a video
        if(mSteps.get(position).getVideoUri() == null){
            holder.ivVideo.setVisibility(View.INVISIBLE);
        }
        else{
            holder.ivVideo.setVisibility(View.VISIBLE);
        }

        //Highlights the selected recipe step for tablet devices
        if(mIsTwoPane){
            if(position == mSelectedPosition){
                holder.clRecipeStepsListItem
                        .setBackgroundColor(context
                                .getResources()
                                .getColor(R.color.colorAccent));
            }
            else{
                holder.clRecipeStepsListItem
                        .setBackgroundColor(context
                                .getResources()
                                .getColor(R.color.colorWhite));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mSteps.size();
    }

    class RecipeDetailsViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        //View bindings
        @BindView(R.id.tv_recipe_step) TextView tvRecipeStep;
        @BindView(R.id.cl_recipe_steps_list_item) ConstraintLayout clRecipeStepsListItem;
        @BindView(R.id.iv_video) ImageView ivVideo;

        RecipeDetailsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mSelectedPosition = position;
            mRecyclerViewOnClickListener.onItemClick(position);
            notifyDataSetChanged();
        }
    }
}