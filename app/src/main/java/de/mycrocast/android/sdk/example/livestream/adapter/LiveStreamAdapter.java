package de.mycrocast.android.sdk.example.livestream.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.mycrocast.android.sdk.example.R;
import de.mycrocast.android.sdk.example.livestream.adapter.viewholder.LiveStreamViewHolder;
import de.mycrocast.android.sdk.example.utility.ClickObserver;
import de.mycrocast.android.sdk.live.listener.state.LiveStreamListenerState;
import de.mycrocast.android.sdk.live.data.LiveStream;

public class LiveStreamAdapter extends RecyclerView.Adapter<LiveStreamViewHolder> {

    private final LiveStreamListenerState listenerState;
    private final ClickObserver<LiveStream> playButtonClickObserver;
    private final ClickObserver<LiveStream> itemCardClickObserver;
    private final List<LiveStream> liveStreamList;

    public LiveStreamAdapter(LiveStreamListenerState listenerState, ClickObserver<LiveStream> playButtonClickObserver, ClickObserver<LiveStream> itemCardClickObserver) {
        this.listenerState = listenerState;
        this.playButtonClickObserver = playButtonClickObserver;
        this.itemCardClickObserver = itemCardClickObserver;

        this.liveStreamList = new ArrayList<>();
    }

    @NonNull
    @Override
    public LiveStreamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_livestream, parent, false);
        return new LiveStreamViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LiveStreamViewHolder holder, int position) {
        final LiveStream liveStream = this.liveStreamList.get(position);
        if (liveStream == null) {
            return;
        }

        holder.initialize(liveStream, this.listenerState.isCurrentPlayingLiveStream(liveStream));
        holder.setCardClickListener(view -> this.itemCardClickObserver.onClicked(liveStream));
        holder.setPlayButtonClickListener(view -> this.playButtonClickObserver.onClicked(liveStream));
    }

    @Override
    public int getItemCount() {
        return this.liveStreamList.size();
    }

    public void add(LiveStream liveStream) {
        this.liveStreamList.remove(liveStream);
        this.liveStreamList.add(liveStream);
    }

    public void update(LiveStream liveStream) {
        this.add(liveStream);
    }

    public void remove(LiveStream liveStream) {
        this.liveStreamList.remove(liveStream);
    }

    public void setAll(List<LiveStream> liveStreams) {
        this.liveStreamList.clear();
        this.liveStreamList.addAll(liveStreams);
    }
}
