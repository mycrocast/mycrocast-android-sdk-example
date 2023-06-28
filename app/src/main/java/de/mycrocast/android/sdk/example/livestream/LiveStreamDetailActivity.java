package de.mycrocast.android.sdk.example.livestream;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import de.mycrocast.android.sdk.core.Mycrocast;
import de.mycrocast.android.sdk.core.util.Optional;
import de.mycrocast.android.sdk.example.R;
import de.mycrocast.android.sdk.example.chat.ListenerChatActivity;
import de.mycrocast.android.sdk.example.utility.BroadcastIntent;
import de.mycrocast.android.sdk.example.utility.Constants;
import de.mycrocast.android.sdk.live.container.LiveStreamContainer;
import de.mycrocast.android.sdk.live.data.LiveStream;
import de.mycrocast.android.sdk.live.data.description.Team;
import de.mycrocast.android.sdk.live.listener.state.LiveStreamListenerState;
import de.mycrocast.android.sdk.live.listener.state.PlayState;
import de.mycrocast.android.sdk.live.rating.LiveStreamRater;
import de.mycrocast.android.sdk.live.rating.LiveStreamRating;

/**
 * Example implementation of a details view for a specific live stream
 * <p>
 * Here you can play or pause the livestream.
 * As well as rate the livestream (via like and dislike).
 * <p>
 * See the detailed description of the stream as provided by the streamer, meaning
 * that is could either be a general stream (only with a title and description) or a scoring stream
 * (a stream with 2 teams and also changing scores for both teams).
 * <p>
 * Additionally you can move from here to the chat of this stream
 */
public class LiveStreamDetailActivity extends AppCompatActivity implements LiveStreamContainer.Observer, LiveStreamRater.Observer, LiveStreamListenerState.Observer {

    /**
     * Creates an intent for starting the LiveStreamDetailActivity for the given LiveStream
     *
     * @param context
     * @param liveStream The LiveStream we want to display in the LiveStreamDetailActivity
     * @return Intent for starting a LiveStreamDetailActivity
     */
    public static Intent newInstance(Context context, LiveStream liveStream) {
        long streamId = liveStream == null ? Constants.INVALID_ID : liveStream.getId();

        Intent result = new Intent(context, LiveStreamDetailActivity.class);
        result.putExtra(Constants.LIVE_STREAM_ID_KEY, streamId);
        return result;
    }

    private final LiveStreamRater liveStreamRater = Mycrocast.getLiveStreamRater();
    private final LiveStreamContainer liveStreamContainer = Mycrocast.getLiveStreamContainer();
    private final LiveStreamListenerState liveStreamListenerState = Mycrocast.getListenerState();

    private long liveStreamId;

    private ImageView clubHeaderView;
    private ImageView clubLogoView;
    private TextView streamerNameView;
    private TextView clubNameView;
    private MaterialCardView likeCardView;
    private ImageView likeImageView;
    private TextView likeTextView;
    private MaterialCardView dislikeCardView;
    private ImageView dislikeImageView;
    private TextView dislikeTextView;
    private MaterialCardView scoringCardView;
    private TextView homeTeamScoreView;
    private TextView guestTeamScoreView;
    private TextView homeTeamNameView;
    private TextView guestTeamNameView;
    private ImageView playButtonView;
    private TextView listenerCountView;
    private TextView genreView;
    private TextView languageView;
    private TextView titleView;
    private TextView descriptionView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_livestream);

        // add back button to toolbar
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // get id of the livestream from bundle
        this.liveStreamId = this.getIntent().getLongExtra(Constants.LIVE_STREAM_ID_KEY, Constants.INVALID_ID);

        // checking if the current live stream still exists or the provided id is
        // valid at all, otherwise we navigate back
        Optional<LiveStream> optional = this.liveStreamContainer.find(this.liveStreamId);
        if (optional.isEmpty()) {
            this.finish();
            return;
        }


        this.clubHeaderView = this.findViewById(R.id.iv_club_header);
        this.clubLogoView = this.findViewById(R.id.iv_club_logo);
        this.streamerNameView = this.findViewById(R.id.tv_streamer);
        this.clubNameView = this.findViewById(R.id.tv_club);

        this.likeCardView = this.findViewById(R.id.card_like);
        this.likeImageView = this.findViewById(R.id.iv_like);
        this.likeTextView = this.findViewById(R.id.tv_like);
        this.dislikeCardView = this.findViewById(R.id.card_dislike);
        this.dislikeImageView = this.findViewById(R.id.iv_dislike);
        this.dislikeTextView = this.findViewById(R.id.tv_dislike);

        this.scoringCardView = this.findViewById(R.id.card_scoring);
        this.homeTeamScoreView = this.findViewById(R.id.tv_home_score);
        this.guestTeamScoreView = this.findViewById(R.id.tv_guest_score);
        this.homeTeamNameView = this.findViewById(R.id.tv_home_name);
        this.guestTeamNameView = this.findViewById(R.id.tv_guest_name);

        this.playButtonView = this.findViewById(R.id.btn_play);
        this.playButtonView.setOnClickListener(v -> LiveStreamDetailActivity.this.onPlayButtonClicked());

        this.listenerCountView = this.findViewById(R.id.tv_stream_listener);
        this.genreView = this.findViewById(R.id.tv_stream_genre);
        this.languageView = this.findViewById(R.id.tv_stream_language);

        this.titleView = this.findViewById(R.id.tv_title);
        this.descriptionView = this.findViewById(R.id.tv_description);

        this.likeCardView.setOnClickListener(v -> LiveStreamDetailActivity.this.onLikeButtonClicked());
        this.dislikeCardView.setOnClickListener(v -> LiveStreamDetailActivity.this.onDislikeButtonClicked());

        MaterialCardView chatCardView = this.findViewById(R.id.card_chat);
        chatCardView.setOnClickListener(v -> LiveStreamDetailActivity.this.openChat());

        MaterialButton addDelayBtn = this.findViewById(R.id.btn_add_delay);
        addDelayBtn.setOnClickListener(v -> LiveStreamDetailActivity.this.adjustDelay(+1));

        MaterialButton removeDelayBtn = this.findViewById(R.id.btn_remove_delay);
        removeDelayBtn.setOnClickListener(v -> LiveStreamDetailActivity.this.adjustDelay(-1));

        MaterialButton noDelayBtn = this.findViewById(R.id.btn_delay_live);
        noDelayBtn.setOnClickListener(v -> LiveStreamDetailActivity.this.removeDelay());
    }

    /**
     * We selected the play button this either pauses if we are already playing the stream by
     * sending a broadcast intent to the currently running service
     * <p>
     * If no stream is currently running we start a new foreground service to start playing
     */
    private void onPlayButtonClicked() {
        Optional<LiveStream> optional = this.liveStreamContainer.find(this.liveStreamId);
        if (optional.isEmpty()) {
            this.finish();
            return;
        }

        LiveStream liveStream = optional.get();
        if (this.liveStreamListenerState.isCurrentLiveStream(liveStream)) {
            if (this.liveStreamListenerState.isCurrentPlayingLiveStream(liveStream)) {
                this.sendBroadcast(new Intent(BroadcastIntent.PAUSE_LIVE_STREAM));
                return;
            }

            this.sendBroadcast(new Intent(BroadcastIntent.RESUME_LIVE_STREAM));
            return;
        }

        if (this.liveStreamListenerState.hasCurrentLiveStream()) {
            this.sendBroadcast(new Intent(BroadcastIntent.STOP_LIVE_STREAM));
        }

        Intent startIntent = LiveStreamListenerService.newInstance(this, liveStream);
        this.startService(startIntent);
    }

    private void onLikeButtonClicked() {
        Optional<LiveStream> optional = this.liveStreamContainer.find(this.liveStreamId);
        if (optional.isEmpty()) {
            this.finish();
            return;
        }

        this.liveStreamRater.Like(optional.get());
    }

    private void onDislikeButtonClicked() {
        Optional<LiveStream> optional = this.liveStreamContainer.find(this.liveStreamId);
        if (optional.isEmpty()) {
            this.finish();
            return;
        }

        this.liveStreamRater.Dislike(optional.get());
    }

    private void openChat() {
        Optional<LiveStream> optional = this.liveStreamContainer.find(this.liveStreamId);
        if (optional.isEmpty()) {
            this.finish();
            return;
        }

        this.startActivity(ListenerChatActivity.newInstance(this, optional.get()));
    }

    private void adjustDelay(int secondsToAdd) {
        final Intent adjustDelayIntent = new Intent(BroadcastIntent.ADJUST_DELAY);
        adjustDelayIntent.putExtra("delay", secondsToAdd);
        this.sendBroadcast(adjustDelayIntent);
    }

    private void removeDelay() {
        this.sendBroadcast(new Intent(BroadcastIntent.REMOVE_DELAY));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // remove the observer, we do not get updates anymore
        this.liveStreamRater.removeObserver(this);
        this.liveStreamContainer.removeObserver(this);
        this.liveStreamListenerState.removeObserver(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // checking if the current live stream still exists or the provided id is
        // valid at all, otherwise we navigate back
        Optional<LiveStream> optional = this.liveStreamContainer.find(this.liveStreamId);
        if (optional.isEmpty()) {
            this.finish();
            return;
        }

        this.updateViews(optional.get());

        //register the observer again
        this.liveStreamRater.addObserver(this);
        this.liveStreamContainer.addObserver(this);
        this.liveStreamListenerState.addObserver(this);
    }

    private void updateViews(LiveStream liveStream) {
        // it is optional but would be better for the user experience and to reduce bandwidth usage
        // to usage a caching library for the images as demonstrated below
        Glide.with(this)
                .load(liveStream.getClubHeaderUrl())
                .into(this.clubHeaderView);

        Glide.with(this)
                .load(liveStream.getClubLogoUrl())
                .placeholder(R.drawable.ic_mycrocast_logo)
                .fallback(R.drawable.ic_mycrocast_logo)
                .into(this.clubLogoView);

        this.streamerNameView.setText(liveStream.getStreamerName());
        this.clubNameView.setText(liveStream.getClubName());

        this.updateRatingViews(liveStream);
        this.updateScoringViews(liveStream);
        this.updatePlayButton(liveStream);
        this.updateDetailViews(liveStream);
        this.updateDescriptionViews(liveStream);
    }

    private void updateRatingViews(LiveStream liveStream) {
        this.updateLikeViews(liveStream);
        this.updateDislikeViews(liveStream);
    }

    /**
     * update the like rating view and display it depending on if we have liked it or not
     */
    private void updateLikeViews(LiveStream liveStream) {
        this.likeTextView.setText(String.valueOf(liveStream.getLikeCount()));

        LiveStreamRating currentRating = this.liveStreamRater.getCurrentRating(liveStream);

        int strokeColorId = currentRating == LiveStreamRating.POSITIVE ? R.color.logo_blue : R.color.light_blue;
        this.likeCardView.setStrokeColor(this.getResources().getColor(strokeColorId));
        this.likeCardView.invalidate();

        int iconTintColorId = currentRating == LiveStreamRating.POSITIVE ? R.color.logo_blue : R.color.white;
        this.likeImageView.setImageTintList(ColorStateList.valueOf(this.getResources().getColor(iconTintColorId)));
    }

    /**
     * Update the dislike view and change the color depending if we disliked it or not
     */
    private void updateDislikeViews(LiveStream liveStream) {
        this.dislikeTextView.setText(String.valueOf(liveStream.getDislikeCount()));

        LiveStreamRating currentRating = this.liveStreamRater.getCurrentRating(liveStream);

        int strokeColorId = currentRating == LiveStreamRating.NEGATIVE ? R.color.logo_blue : R.color.light_blue;
        this.dislikeCardView.setStrokeColor(this.getResources().getColor(strokeColorId));
        this.dislikeCardView.invalidate();

        int iconTintColorId = currentRating == LiveStreamRating.NEGATIVE ? R.color.logo_blue : R.color.white;
        this.dislikeImageView.setImageTintList(ColorStateList.valueOf(this.getResources().getColor(iconTintColorId)));
    }

    /**
     * If this stream is of type scoring (hasScoring is true) we update the scoring visuals
     * Otherwise we just hide them
     */
    private void updateScoringViews(LiveStream liveStream) {
        if (liveStream.hasScoring()) {
            this.scoringCardView.setVisibility(View.VISIBLE);

            Team home = liveStream.getScoring().getHomeTeam();
            this.homeTeamScoreView.setText(String.valueOf(home.getScore()));
            this.homeTeamNameView.setText(home.getName());

            Team guest = liveStream.getScoring().getGuestTeam();
            this.guestTeamScoreView.setText(String.valueOf(guest.getScore()));
            this.guestTeamNameView.setText(guest.getName());
        } else {
            this.scoringCardView.setVisibility(View.GONE);
        }
    }

    /**
     * Update the play button state depending on if we are currently playing
     */
    private void updatePlayButton(LiveStream liveStream) {
        if (this.liveStreamListenerState.isCurrentPlayingLiveStream(liveStream)) {
            this.playButtonView.setImageResource(R.drawable.ic_mycrocast_pause);
            return;
        }

        this.playButtonView.setImageResource(R.drawable.ic_mycrocast_play);
    }

    /**
     * Update the view of listener count, genre and language
     */
    private void updateDetailViews(LiveStream liveStream) {
        this.listenerCountView.setText(String.valueOf(liveStream.getListenerCount()));
        this.genreView.setText(liveStream.getGenre());
        this.languageView.setText(liveStream.getLanguage().getNativeLanguage());
    }

    /**
     * Update the general fields of each stream
     */
    private void updateDescriptionViews(LiveStream liveStream) {
        this.titleView.setText(liveStream.getTitle());
        this.descriptionView.setText(liveStream.getDescription());
    }

    /**
     * A livestream was added.
     * <p>
     * We need to check if the added livestream is our currently displayed one.
     * If yes, we need to update our views.
     *
     * @param liveStream - the new livestream
     */
    @Override
    public void onLiveStreamAdded(LiveStream liveStream) {
        this.onLiveStreamUpdated(liveStream);
    }

    /**
     * A livestream was changed.
     * <p>
     * We need to check if the changed livestream is our currently displayed one.
     * If yes, we need to update our views.
     *
     * @param liveStream - the changed livestream
     */
    @Override
    public void onLiveStreamUpdated(LiveStream liveStream) {
        if (this.liveStreamId != liveStream.getId()) {
            return;
        }

        this.runOnUiThread(() -> LiveStreamDetailActivity.this.updateViews(liveStream));
    }

    /**
     * A livestream has ended.
     * <p>
     * We need to check if the ended livestream is our currently displayed one.
     * If yes, we need to close this activity.
     *
     * @param liveStream - the ended livestream
     */
    @Override
    public void onLiveStreamRemoved(LiveStream liveStream) {
        if (this.liveStreamId == liveStream.getId()) {
            this.finish();
        }
    }

    /**
     * The list of current livestreams has changed.
     * <p>
     * We need to check if our currently displayed livestream is still in this list of all currently active livestreams.
     * If it is not, it went offline, so we need to close this view.
     * If yes, we can update all our views.
     */
    @Override
    public void onLiveStreamListChanged() {
        Optional<LiveStream> optional = this.liveStreamContainer.find(this.liveStreamId);
        if (optional.isEmpty()) {
            this.finish();
            return;
        }

        this.onLiveStreamUpdated(optional.get());
    }

    /**
     * The rating of a livestream has been changed.
     * <p>
     * We need to check if the changed livestream is our currently displayed one.
     * If yes, we need to update our rating views.
     *
     * @param liveStream - the stream for which the rating changed
     * @param oldRating  - the old rating
     * @param newRating  - the new and current rating
     */
    @Override
    public void onRatingChanged(@NonNull LiveStream liveStream, LiveStreamRating oldRating, LiveStreamRating newRating) {
        if (this.liveStreamId != liveStream.getId()) {
            return;
        }

        this.runOnUiThread(() -> LiveStreamDetailActivity.this.updateRatingViews(liveStream));
    }

    /**
     * The playState of a livestream has been changed.
     * <p>
     * We need to check if the changed livestream is our currently displayed one.
     * If yes, we need to update our playButton;
     *
     * @param liveStream - the live stream that this update is belonging too
     * @param playState  - the new play state
     */
    @Override
    public void onPlayStateChanged(LiveStream liveStream, PlayState playState) {
        if (this.liveStreamId != liveStream.getId()) {
            return;
        }

        this.runOnUiThread(() -> LiveStreamDetailActivity.this.updatePlayButton(liveStream));
    }
}