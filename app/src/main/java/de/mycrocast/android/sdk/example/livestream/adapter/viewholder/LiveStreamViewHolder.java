package de.mycrocast.android.sdk.example.livestream.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import de.mycrocast.android.sdk.example.R;
import de.mycrocast.android.sdk.live.data.LiveStream;

/**
 * A sample implementation for a livestream element.
 */
public class LiveStreamViewHolder extends RecyclerView.ViewHolder {

    private final MaterialCardView cardView;
    private final ImageView clubLogoView;
    private final TextView streamerNameView;
    private final TextView titleView;
    private final TextView listenerCountView;
    private final TextView genreView;
    private final TextView languageView;
    private final View playButtonLayout;
    private final ImageView playButtonView;
    private final TextView playButtonText;

    public LiveStreamViewHolder(@NonNull View itemView) {
        super(itemView);

        this.cardView = itemView.findViewById(R.id.card_view);
        this.clubLogoView = itemView.findViewById(R.id.iv_club_logo);
        this.streamerNameView = itemView.findViewById(R.id.tv_streamer);
        this.titleView = itemView.findViewById(R.id.tv_title);
        this.listenerCountView = itemView.findViewById(R.id.tv_stream_listener);
        this.genreView = itemView.findViewById(R.id.tv_stream_genre);
        this.languageView = itemView.findViewById(R.id.tv_stream_language);
        this.playButtonLayout = itemView.findViewById(R.id.layout_play_button);
        this.playButtonView = itemView.findViewById(R.id.btn_play);
        this.playButtonText = itemView.findViewById(R.id.tv_play_button);
    }

    public void initialize(LiveStream liveStream, boolean isPlaying) {

        this.streamerNameView.setText(liveStream.getStreamerName());
        this.titleView.setText(liveStream.getTitle());
        this.listenerCountView.setText(String.valueOf(liveStream.getListenerCount()));
        this.genreView.setText(liveStream.getGenre());
        this.languageView.setText(liveStream.getLanguage().getLanguage());

        this.setClubLogo(liveStream);

        if (isPlaying) {
            this.playButtonView.setImageResource(R.drawable.ic_mycrocast_pause);
            this.playButtonText.setText(R.string.button_pause);
        } else {
            this.playButtonView.setImageResource(R.drawable.ic_mycrocast_play);
            this.playButtonText.setText(R.string.button_play);
        }
    }

    private void setClubLogo(LiveStream liveStream) {
        Glide.with(this.itemView)
                .load(liveStream.getClubLogoUrl())
                .placeholder(R.drawable.ic_mycrocast_logo)
                .fallback(R.drawable.ic_mycrocast_logo)
                .into(this.clubLogoView);
    }

    public void setPlayButtonClickListener(View.OnClickListener onClickListener) {
        this.playButtonLayout.setOnClickListener(onClickListener);
    }

    public void setCardClickListener(View.OnClickListener onClickListener) {
        this.cardView.setOnClickListener(onClickListener);
    }
}
