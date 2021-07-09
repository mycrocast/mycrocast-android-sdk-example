package de.mycrocast.android.sdk.example.livestream;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import de.mycrocast.android.sdk.core.Mycrocast;
import de.mycrocast.android.sdk.example.R;
import de.mycrocast.android.sdk.example.livestream.adapter.LiveStreamAdapter;
import de.mycrocast.android.sdk.example.utility.VerticalItemSpacing;
import de.mycrocast.android.sdk.example.utility.BroadcastIntent;
import de.mycrocast.android.sdk.live.listener.state.LiveStreamListenerState;
import de.mycrocast.android.sdk.live.listener.state.PlayState;
import de.mycrocast.android.sdk.live.container.LiveStreamContainer;
import de.mycrocast.android.sdk.live.data.LiveStream;
import de.mycrocast.android.sdk.live.refresh.LiveStreamRefresher;

/**
 * Activity which is the first activity displayed in the example app.
 * This activity displays all currently available live streams, reacts to changes to the status of each live stream
 * as well as the playing state of a stream.
 * Each LiveStream is displayed in the recyclerview and hitting the play button will start the
 * play logic of the mycrocast sdk, selecting the cell at any other point will navigate to the detail
 * view of that stream where you see more information, can rate the stream or move to the chat of the stream
 * <p>
 * It therefore demonstrates the usage of the
 * - LiveStreamContainer
 * - LiveStreamRefresher
 * - LiveStreamListenerState
 */
public class LiveStreamListActivity extends AppCompatActivity implements LiveStreamContainer.Observer, LiveStreamRefresher.Observer, LiveStreamListenerState.Observer {

    private final LiveStreamContainer liveStreamContainer = Mycrocast.getLiveStreamContainer();
    private final LiveStreamRefresher liveStreamRefresher = Mycrocast.getLiveStreamRefresher();
    private final LiveStreamListenerState liveStreamListenerState = Mycrocast.getListenerState();

    private LiveStreamAdapter adapter;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.adapter = new LiveStreamAdapter(this.liveStreamListenerState, this::onPlayButtonClicked, this::onLiveStreamClicked);

        RecyclerView recyclerView = this.findViewById(R.id.rv_livestreams);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(this.adapter);

        recyclerView.addItemDecoration(new VerticalItemSpacing(8));

        this.refreshLayout = this.findViewById(R.id.layout_refresh_swipe);
        this.refreshLayout.setOnRefreshListener(LiveStreamListActivity.this::refreshLiveStreamList);
    }

    /**
     * We clicked on the playButton of a livestream. This either:
     * <p>
     * - pauses if we are already playing the given livestream by sending a broadcast intent to the currently running service
     * <p>
     * - resumes if we already paused the given livestream by sending a broadcast intent to the currently running service
     * <p>
     * - stops the currently running service, if this service is playing another livestream than the given one,
     * and then starts a new foreground service to start playing the given livestream
     * <p>
     * - starts a new foreground service to start playing the given livestream, if no livestream is currently running
     *
     * @param liveStream the livestream of the playButton we clicked
     */
    private void onPlayButtonClicked(LiveStream liveStream) {
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

        this.startService(LiveStreamListenerService.NewInstance(this, liveStream));
    }

    /**
     * We clicked on the card of a livestream.
     * This will open a view, showing mor information and actions for the given livestream.
     *
     * @param liveStream the livestream of the card we clicked
     */
    private void onLiveStreamClicked(LiveStream liveStream) {
        this.startActivity(LiveStreamDetailActivity.newInstance(this, liveStream));
    }

    /**
     * We want to refresh our current list of livestreams.
     */
    private void refreshLiveStreamList() {
        boolean isRefreshing = this.liveStreamRefresher.refresh();
        this.refreshLayout.setRefreshing(isRefreshing);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // clean up and remove observer, so we don't get any updates while this view is not in the foreground
        this.liveStreamListenerState.removeObserver(this);
        this.liveStreamContainer.removeObserver(this);
        this.liveStreamRefresher.removeObserver(this);
    }

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

    /**
     * A new stream was received, this was because a new streamer of your club started streaming
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

    /**
     * Refreshing the streams from the server has finished
     */
    @Override
    public void OnRefreshFinished() {
        this.runOnUiThread(() -> this.refreshLayout.setRefreshing(false));
    }

    /**
     * A playState of a livestream has changed.
     * We need to update our Adapter to show the change.
     *
     * @param liveStream - the live stream that this update is belonging too
     * @param playState  - the new play state
     */
    @Override
    public void onPlayStateChanged(LiveStream liveStream, PlayState playState) {
        this.runOnUiThread(() -> this.adapter.notifyDataSetChanged());
    }
}