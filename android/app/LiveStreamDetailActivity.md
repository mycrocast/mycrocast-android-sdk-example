# Android example App - LiveStreamDetailActivity

This activity can be reached by selecting a live stream cell from the LiveStreamListActivity.

This represents the details view of the selected stream.

This view shows the following concepts:

- Rating, you can rate the stream by hitting either the like or dislike
- Listening to the stream - you can toggle the play state though the provided play button
- Stream Details - either showing a general stream view or Scoring depending on what the streamer had configured
- Chat Navigation - You can move from here to the chat of the specific stream



### Rating

Rating is done via the LiveStreamRater

```java
public interface LiveStreamRater extends Observable<LiveStreamRater.Observer> {

    interface Observer {
        /**
         * Get informed about changes of the rating from the user. This is called after a user changed the
         * Rating
         * @param liveStream - the stream for which the rating changed
         * @param oldRating - the old rating
         * @param newRating - the new and current rating
         */
        void onRatingChanged(@NonNull LiveStream liveStream, LiveStreamRating oldRating, LiveStreamRating newRating);
    }

    /**
     * Like a certain stream. If current stream was already liked by the user, reset the rating to neutral
     * @param liveStream - the stream to like
     */
    void Like(@Nullable LiveStream liveStream);

    /**
     * Dislike a certain stream. If the current stream rating was already dislike the rating is reset to neutral
     * @param liveStream - the stream to dislike
     */
    void Dislike(@Nullable LiveStream liveStream);

    /**
     * Request the users current rating for the provided live stream
     * @param liveStream - the live stream in question
     * @return the rating of the current user for this stream
     */
    LiveStreamRating getCurrentRating(@Nullable LiveStream liveStream);
}
```



To react to changes for the rating you can subscribe yourself to the onRatingChanged Observer



### Listening to the stream / toggling the play state

```java
    private void onPlayButtonClicked() {
        if (this.liveStreamListenerState.isCurrentLiveStream(this.liveStream)) {
            if (this.liveStreamListenerState.isCurrentPlayingLiveStream(this.liveStream)) {
                this.sendBroadcast(new Intent(BroadcastIntent.PAUSE_LIVE_STREAM));
                return;
            }

            this.sendBroadcast(new Intent(BroadcastIntent.RESUME_LIVE_STREAM));
            return;
        }

        if (this.liveStreamListenerState.hasCurrentLiveStream()) {
            this.sendBroadcast(new Intent(BroadcastIntent.STOP_LIVE_STREAM));
        }

        Intent startIntent = LiveStreamListenerService.NewInstance(this, this.liveStream);
        this.startService(startIntent);
    }
```

Hitting the play button if nothing is running, we instanciate a new Instance of LiveStreamListenerService, our custom implementation of a foreground service responsible for playing the audio and controlling the play state in general as well as playing advertisements.



### Stream Details

A stream has either only a title and a description or an additional scoring field with the 2 competing teams as well as the scoring for each of those teams.

We just check the hasScoring function of the LiveStream to determine what to show



```java
        /**
     * If this stream is of type scoring (hasScoring is true) we update the scoring visuals
     * Otherwise we just hide them
     */
    private void updateScoringViews() {
        if (this.liveStream.hasScoring()) {
            this.scoringCardView.setVisibility(View.VISIBLE);

            Team home = this.liveStream.getScoring().getHomeTeam();
            this.homeTeamScoreView.setText(String.valueOf(home.getScore()));
            this.homeTeamNameView.setText(home.getName());

            Team guest = this.liveStream.getScoring().getGuestTeam();
            this.guestTeamScoreView.setText(String.valueOf(guest.getScore()));
            this.guestTeamNameView.setText(guest.getName());
        } else {
            this.scoringCardView.setVisibility(View.GONE);
        }
    }
    
    /**
     * Update the view of listener count, genre and language
     */
    private void updateDetailViews() {
        this.listenerCountView.setText(String.valueOf(this.liveStream.getListenerCount()));
        this.genreView.setText(this.liveStream.getGenre().toString());
        this.languageView.setText(this.liveStream.getLanguage().getNativeLanguage());
    }

    /**
     * Update the general fields of each stream
     */
    private void updateDescriptionViews() {
        this.titleView.setText(this.liveStream.getTitle());
        this.descriptionView.setText(this.liveStream.getDescription());
    }
```

### Chat

To open the chat we just navigate to the ListenerChatActivity on click

```java
this.chatCardView.setOnClickListener(v -> 
	{
		this.startActivity(ListenerChatActivity.newInstance(this, this.liveStream));
	});
```

