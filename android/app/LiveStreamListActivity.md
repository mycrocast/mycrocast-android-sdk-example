# Android example App - LiveStreamListActivity

The LiveStreamListActivity is the first activity of the app and is responsible to show an overview of all currently available LiveStreams for your club.

This is visually accomplished by using a recyclerview with the usage of the LiveStreamAdapter and LiveStreamViewholder classes.

This activity demonstrates:
- usage of the LiveStreamContainer and the LiveStreamContainer.Observer
- usage of the LiveStreamRefresher and the LiveStreamRefresher.Observer
- usage of the LiveStreamListenerState and the LiveStreamListenerState.Observer

### LiveStreamContainer.Observer

All currently available LiveStreams are stored in the LiveStreamContainer.

As this list of all available LiveStreams can change (as well as a single LiveStream of this list) we have the opportunity to observe the LiveStreamContainer for the following changes:
- the addition of a LiveStream to the container
- the update of a LiveStream, that was already present in the container
- the end a LiveStream, that was already in the container (by the streamer or the mycrocast backend-server)
- multiple changes in the container (will also be invoked, if the refreshing via the LiveStreamRefresher was a success)

Below the implementation of the LiveStreamContainer.Observer interface, which updates the adapter of the list and afterwards forces and visual update.

```java
    /**
     * A new stream was received, this was because a new streamer of you club started streaming
     * We add it to the adapter and force an update of the recycler view
     *
     * @param liveStream - the new live stream
     */
    @Override
    public void onLiveStreamAdded(LiveStream liveStream) {
        this.adapter.add(liveStream);
        this.runOnUiThread(() -> this.adapter.notifyDataSetChanged());
    }

    /**
     * We received an update for a specific stream. This could be for example because the
     * number of listener changed.
     * We force an update for the recycler view
     *
     * @param liveStream - the stream that changed
     */
    @Override
    public void onLiveStreamUpdated(LiveStream liveStream) {
        this.adapter.update(liveStream);
        this.runOnUiThread(() -> this.adapter.notifyDataSetChanged());
    }

    /**
     * A stream went offline, this could be because a streamer ended his stream
     * We need to remove it visually and therefore force an update of the recycler view
     *
     * @param liveStream - the stream that went offline
     */
    @Override
    public void onLiveStreamRemoved(LiveStream liveStream) {
        this.adapter.remove(liveStream);
        this.runOnUiThread(() -> this.adapter.notifyDataSetChanged());
    }

  /**
     * multiple entries have changed, therefore we update everything
     */
    @Override
    public void onLiveStreamListChanged() {
        this.adapter.setAll(Mycrocast.getLiveStreamContainer().getAll());
        this.runOnUiThread(() -> this.adapter.notifyDataSetChanged());
    }
```

### LiveStreamRefresher.Observer

For manually refreshing (via reload from the backend-server) the list of available LiveStreams we need to use the LiveStreamRefresher. Only one refresh can be active at the same time. (So if a refresh is currently in progress, you can't demand a refresh again.)

To get informed if a refresh was finished (either successfully or not), you need to add a LiveStreamRefresher.Observer to the LiveStreamRefresher.

Below the Implementation of the LiveStreamRefresher.Observer interface to receive the callback when the refresh is done. As the internal list of all currently available LiveStreams changes, you automatically you will be notified via the LiveStreamContainer.Observer.

```java
    /**
     * Refreshing the streams from the server has finished
     */
    @Override
    public void OnRefreshFinished() {
        this.runOnUiThread(() -> this.refreshLayout.setRefreshing(false));
    }
```

### LiveStreamListenerState.Observer

The LiveStreamListenerState stores the state of the currently playing LiveStream, if any is currently playing/paused.

To get informed if a PlayState of a LiveStream changes we can implement and the LiveStreamListenerState.Observer to the LiveStreamListenerState.

Below the implementation of the LiveStreamListenerState.Observer to react to the PlayState-Changes, redrawing the list of the LiveStreams to update the image of the PlayButton.

````java
   @Override
    public void onPlayStateChanged(LiveStream liveStream, PlayState playState) {
        this.runOnUiThread(() -> this.adapter.notifyDataSetChanged());
    }
````



### OnResume

Lifecycle function of the activity. 
Here we get the all currently active LiveStreams, register to our observer-interfaces to receive the updates and refresh the current view.

```java
	@Override  
	protected void onResume() {  
		super.onResume();  

		 // we could have missed some updates in the list of currently active livestreams,  
		 // so we get all currently active livestreams and update our adapter accordingly 
		 this.adapter.setAll(this.liveStreamContainer.getAll());  
		 this.adapter.notifyDataSetChanged();  

		 // register the observers again, so we get any updates  
		 this.liveStreamListenerState.addObserver(this);  
		 this.liveStreamContainer.addObserver(this);  
		 this.liveStreamRefresher.addObserver(this);  

		 // if a refresh of our list of currently active livestream is in progress,  
		 // we want to show this progress via our refreshLayout 
		 boolean isRefreshInProgress = this.liveStreamRefresher.isRefreshInProgress();
		 this.refreshLayout.setRefreshing(isRefreshInProgress);  
	}
```

### OnPause

Unregister from the observer as the view is no longer visible and therefore we are not interested to keep it updated.

````java
	@Override  
	protected void onPause() {  
		super.onPause();  

	 	// clean up and remove observer, so we don't get any updates while this view is not in the foreground  
	 	this.liveStreamListenerState.removeObserver(this);  
	 	this.liveStreamContainer.removeObserver(this);  
	 	this.liveStreamRefresher.removeObserver(this);  
	}

````

