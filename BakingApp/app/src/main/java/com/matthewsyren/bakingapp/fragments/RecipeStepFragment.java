package com.matthewsyren.bakingapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.matthewsyren.bakingapp.R;
import com.matthewsyren.bakingapp.models.RecipeStep;
import com.matthewsyren.bakingapp.utilities.NetworkUtilities;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Used to create a Fragment to show the details for a recipe step
 */

public class RecipeStepFragment
        extends Fragment
        implements ExoPlayer.EventListener {
    //View bindings
    @Nullable
    @BindView(R.id.tv_recipe_step_description) TextView tvRecipeStepDescription;
    @BindView(R.id.ep_recipe_video) SimpleExoPlayerView epRecipeVideo;
    @BindView(R.id.pb_recipe_video) ProgressBar pbRecipeVideo;

    //Variables and constants
    private RecipeStep mRecipeStep;
    private SimpleExoPlayer mExoPlayer;
    private long mCurrentVideoPosition = 0;
    private static MediaSessionCompat sMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private static final String TAG = RecipeStepFragment.class.getSimpleName();
    private static final String CURRENT_POSITION_BUNDLE_KEY = "current_position_bundle_key";
    private static final String RECIPE_STEP_BUNDLE_KEY = "recipe_step_bundle_key";
    private BroadcastReceiver mNetworkChangeReceiver;

    public RecipeStepFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflates the Fragment's layout
        View view = inflater.inflate(R.layout.fragment_recipe_step, container, false);
        ButterKnife.bind(this, view);

        //Registers the network connectivity change Receiver
        registerReceiver();

        //Restores data if possible
        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }

        displayAppropriateViews();

        return view;
    }

    //Registers a BroadcastReceiver to detect a change in Internet connectivity, and plays the video if the device connects to the Internet
    private void registerReceiver(){
        //Creates the Receiver
        mNetworkChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(NetworkUtilities.isOnline(getContext())){
                    //Restarts the ExoPlayer if the device is connected to the Internet. This will cause the ExoPlayer to begin fetching the required data automatically
                    if(mExoPlayer != null){
                        releaseExoPlayer();
                    }
                    displayAppropriateViews();
                }
            }
        };

        //Registers the Receiver
        getContext().registerReceiver(mNetworkChangeReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Unregisters the Receiver
        getContext().unregisterReceiver(mNetworkChangeReceiver);
    }

    //Displays the appropriate Views based on the RecipeStep's data
    private void displayAppropriateViews(){
        //Displays the recipe description
        if(mRecipeStep.getDescription() != null && tvRecipeStepDescription != null){
            tvRecipeStepDescription.setText(mRecipeStep
                    .getDescription());
        }

        //Displays an error message if there is a video for the step but the device has no Internet connection
        if(!NetworkUtilities.isOnline(getContext()) && mRecipeStep.getVideoUri() != null){
            Toast.makeText(getContext(), getString(R.string.error_no_internet_connection_video), Toast.LENGTH_LONG).show();
        }

        //Displays the video or thumbnail if there is one, otherwise displays a message saying there is no video for the step
        if(mRecipeStep.getVideoUri() != null){
            initialiseMediaSession();
            initialiseExoPlayer(mRecipeStep.getVideoUri());
        }
        else{
            //Displays the thumbnail if there is no video for the step
            if(mRecipeStep.getThumbnailUrl() != null && !mRecipeStep.getThumbnailUrl().equals("")){
                Picasso.with(getContext())
                        .load(mRecipeStep.getThumbnailUrl())
                        .placeholder(R.drawable.ic_cake_black_24dp)
                        .error(R.drawable.ic_cake_black_24dp)
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                if(bitmap != null){
                                    epRecipeVideo.setDefaultArtwork(bitmap);
                                    initialiseExoPlayer(null);
                                    epRecipeVideo.setUseController(false);
                                }
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        });
            }
            else{
                //Displays message saying there is no video for the step
                if(tvRecipeStepDescription != null){
                    tvRecipeStepDescription.setText(
                            getString(R.string.error_no_video_for_step,
                                    tvRecipeStepDescription.getText())
                    );
                    epRecipeVideo.setVisibility(View.GONE);

                    if(tvRecipeStepDescription.getVisibility() == View.GONE){
                        tvRecipeStepDescription.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(CURRENT_POSITION_BUNDLE_KEY, mCurrentVideoPosition);
        outState.putParcelable(RECIPE_STEP_BUNDLE_KEY, mRecipeStep);
    }

    //Restores the appropriate data
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(CURRENT_POSITION_BUNDLE_KEY)){
            mCurrentVideoPosition = savedInstanceState.getLong(CURRENT_POSITION_BUNDLE_KEY);
        }

        if(savedInstanceState.containsKey(RECIPE_STEP_BUNDLE_KEY)){
            mRecipeStep = savedInstanceState.getParcelable(RECIPE_STEP_BUNDLE_KEY);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseExoPlayer();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mRecipeStep.getVideoUri() != null){
            initialiseExoPlayer(mRecipeStep.getVideoUri());
        }
    }

    public void setRecipeStep(RecipeStep recipeStep){
        mRecipeStep = recipeStep;
    }

    /*
     * Creates a MediaSession with the appropriate attributes
     * Adapted from the lesson on MediaPlayers
     */
    private void initialiseMediaSession(){
        sMediaSession = new MediaSessionCompat(getContext(), TAG);

        sMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS |
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);

        sMediaSession.setMediaButtonReceiver(null);

        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_REWIND
                );

        sMediaSession.setPlaybackState(mStateBuilder.build());
        sMediaSession.setCallback(new MediaSessionCallback());
        sMediaSession.setActive(true);
    }

    /*
     * Initialises the ExoPlayer and begins the video playback
     * Adapted from the lesson on MediaPlayers
     */
    private void initialiseExoPlayer(Uri uri){
        if(mExoPlayer == null){
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(
                    getContext(),
                    new DefaultTrackSelector(),
                    new DefaultLoadControl()
            );

            epRecipeVideo.setPlayer(mExoPlayer);

            if(uri != null){
                mExoPlayer.addListener(this);

                MediaSource mediaSource = new ExtractorMediaSource(
                        uri,
                        new DefaultDataSourceFactory(getContext(), "RecipeStepFragment"),
                        new DefaultExtractorsFactory(),
                        null,
                        null
                );

                //Restores the user's previous position in the video
                if(mCurrentVideoPosition > 0){
                    mExoPlayer.seekTo(mCurrentVideoPosition);
                }

                mExoPlayer.prepare(mediaSource);
                mExoPlayer.setPlayWhenReady(true);
            }
        }
    }

    //Releases the ExoPlayer
    private void releaseExoPlayer(){
        if(mExoPlayer != null){
            //Updates the user's current position in the video
            mCurrentVideoPosition = mExoPlayer.getCurrentPosition();

            //Releases the SimpleExoPlayer
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        //Updates the MediaSession
        if(playbackState == ExoPlayer.STATE_READY && playWhenReady){
            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mExoPlayer.getCurrentPosition(),
                    1);
        }
        else if(playbackState == ExoPlayer.STATE_READY){
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mExoPlayer.getCurrentPosition(),
                    1);
        }

        if(mStateBuilder != null){
            sMediaSession.setPlaybackState(mStateBuilder.build());
        }

        //Displays ProgressBar when buffering
        if(playbackState == ExoPlayer.STATE_BUFFERING){
            pbRecipeVideo.setVisibility(View.VISIBLE);
        }
        else{
            pbRecipeVideo.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
    }

    @Override
    public void onPositionDiscontinuity() {
        //Updates the user's current position in the video
        mCurrentVideoPosition = mExoPlayer.getCurrentPosition();
    }

    private class MediaSessionCallback
            extends MediaSessionCompat.Callback{
        @Override
        public void onPlay() {
            mExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mExoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onRewind() {
            mExoPlayer.seekTo(0);
        }
    }
}