package com.matthewsyren.bakingapp.fragments;


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
    private Uri mUri;
    private String mRecipeStepDescription;
    private SimpleExoPlayer mExoPlayer;
    private long mCurrentVideoPosition = 0;
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private static final String TAG = RecipeStepFragment.class.getSimpleName();
    private static final String CURRENT_POSITION_BUNDLE_KEY = "current_position_bundle_key";
    private static final String RECIPE_STEP_DESCRIPTION_BUNDLE_KEY = "recipe_step_description_bundle_key";
    private static final String RECIPE_VIDEO_URI_BUNDLE_KEY = "recipe_video_uri_bundle_key";

    public RecipeStepFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflates the Fragment's layout
        View view = inflater.inflate(R.layout.fragment_recipe_step, container, false);
        ButterKnife.bind(this, view);

        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }

        if(mRecipeStepDescription != null && tvRecipeStepDescription != null){
            tvRecipeStepDescription.setText(mRecipeStepDescription);
        }

        if(mUri != null){
            initialiseMediaSession();
            initialiseExoPlayer(mUri);
        }
        else{
            if(tvRecipeStepDescription != null){
                tvRecipeStepDescription.setText(
                        getString(R.string.error_no_video_for_step,
                                tvRecipeStepDescription.getText())
                );

                if(tvRecipeStepDescription.getVisibility() == View.GONE){
                    tvRecipeStepDescription.setVisibility(View.VISIBLE);
                }
            }

            epRecipeVideo.setVisibility(View.GONE);
            pbRecipeVideo.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(CURRENT_POSITION_BUNDLE_KEY, mCurrentVideoPosition);
        outState.putParcelable(RECIPE_VIDEO_URI_BUNDLE_KEY, mUri);
        outState.putString(RECIPE_STEP_DESCRIPTION_BUNDLE_KEY, mRecipeStepDescription);
    }

    //Restores the appropriate data
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(CURRENT_POSITION_BUNDLE_KEY)){
            mCurrentVideoPosition = savedInstanceState.getLong(CURRENT_POSITION_BUNDLE_KEY);
        }

        if(savedInstanceState.containsKey(RECIPE_STEP_DESCRIPTION_BUNDLE_KEY)){
            mRecipeStepDescription = savedInstanceState.getString(RECIPE_STEP_DESCRIPTION_BUNDLE_KEY);
        }

        if(savedInstanceState.containsKey(RECIPE_VIDEO_URI_BUNDLE_KEY)){
            mUri = savedInstanceState.getParcelable(RECIPE_VIDEO_URI_BUNDLE_KEY);
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

        if(mUri != null){
            initialiseExoPlayer(mUri);
        }
    }

    public void setVideoUri(Uri uri){
        mUri = uri;
    }

    public void setRecipeStepDescription(String stepDescription){
        mRecipeStepDescription = stepDescription;
    }

    /*
     * Creates a MediaSession with the appropriate attributes
     * Adapted from the lesson on MediaPlayers
     */
    private void initialiseMediaSession(){
        mMediaSession = new MediaSessionCompat(getContext(), TAG);

        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS |
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);

        mMediaSession.setMediaButtonReceiver(null);

        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_REWIND
                );

        mMediaSession.setPlaybackState(mStateBuilder.build());
        mMediaSession.setCallback(new MediaSessionCallback());
        mMediaSession.setActive(true);
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

            mExoPlayer.addListener(this);
            epRecipeVideo.setPlayer(mExoPlayer);

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
                //todo display textview when no video
            }

            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(true);
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
        mMediaSession.setPlaybackState(mStateBuilder.build());

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