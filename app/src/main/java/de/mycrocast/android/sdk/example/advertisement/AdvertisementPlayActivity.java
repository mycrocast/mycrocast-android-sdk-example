package de.mycrocast.android.sdk.example.advertisement;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import de.mycrocast.android.sdk.example.R;
import de.mycrocast.android.sdk.example.utility.BroadcastIntent;
import de.mycrocast.android.sdk.spot.play.banner.SpotBanner;
import de.mycrocast.android.sdk.spot.play.data.SpotPlay;

/**
 * Example activity that displays an advertisement with the provided information
 * and also plays the audio spot. This could be extended with a ProgressBar.
 */
public class AdvertisementPlayActivity extends AppCompatActivity {

    private static final String AUDIO_URL_KEY = "audio_url";
    private static final String DURATION_KEY = "duration";
    private static final String HAS_BANNER_KEY = "has_banner";
    private static final String BANNER_IMAGE_URL = "banner_image_url";
    private static final String BANNER_TARGET_URL = "banner_target_url";
    private static final String BANNER_DESCRIPTION = "banner_description";

    /**
     * Create a new instance of this activity as intent to be opened and
     * pass the specific information of the provided ad as extras in the bundle
     *
     * @param context           - context required to create the intent
     * @param advertisementPlay - the advertisement we want to display
     * @return the intent that can be used to start this activity
     */
    public static Intent newInstance(Context context, SpotPlay advertisementPlay) {
        Intent result = new Intent(context, AdvertisementPlayActivity.class);
        result.putExtra(AUDIO_URL_KEY, advertisementPlay.getAudioUrl());
        result.putExtra(DURATION_KEY, advertisementPlay.getDuration());

        // the banner of a advertisement is not always present
        SpotBanner banner = advertisementPlay.getBanner();
        boolean hasBanner = banner != null;
        result.putExtra(HAS_BANNER_KEY, hasBanner);
        if (hasBanner) {
            result.putExtra(BANNER_IMAGE_URL, banner.getImageUrl());
            result.putExtra(BANNER_TARGET_URL, banner.getTargetUrl());
            result.putExtra(BANNER_DESCRIPTION, banner.getDescription());
        }

        return result;
    }

    private String bannerImageUrl;
    private String bannerTargetUrl;
    private String bannerDescription;

    private MediaPlayer mediaPlayer;
    private TextView descriptionView;
    private ImageView bannerImageView;
    private Button learnMoreButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_advertisement_play);

        Intent intent = this.getIntent();
        if (intent == null) {
            return;
        }

        String audioUrl = intent.getStringExtra(AUDIO_URL_KEY);

        // if a banner was configured during the creation of the advertisement we want to show it
        // but the banner of an advertisement is not always present
        boolean hasBanner = intent.getBooleanExtra(HAS_BANNER_KEY, false);
        if (hasBanner) {
            this.bannerImageUrl = intent.getStringExtra(BANNER_IMAGE_URL);
            this.bannerTargetUrl = intent.getStringExtra(BANNER_TARGET_URL);
            this.bannerDescription = intent.getStringExtra(BANNER_DESCRIPTION);
        }

        this.descriptionView = this.findViewById(R.id.tv_advertisement_description);
        this.bannerImageView = this.findViewById(R.id.iv_advertisement_banner);
        this.learnMoreButton = this.findViewById(R.id.btn_advertisement_learn_more);
        this.learnMoreButton.setOnClickListener(v -> {
            String urlToOpen = AdvertisementPlayActivity.this.bannerTargetUrl;
            if (urlToOpen == null) {
                v.setVisibility(View.INVISIBLE);
                return;
            }

            this.startActivity(WebViewActivity.newInstance(this, urlToOpen));
        });

        this.setDataToViews();

        this.mediaPlayer = MediaPlayer.create(this, Uri.parse(audioUrl));
        this.mediaPlayer.setOnCompletionListener(m -> this.onAdvertisementPlayFinished());
        this.mediaPlayer.start();
    }

    private void setDataToViews() {
        this.descriptionView.setText(this.bannerDescription);
        this.loadBannerImage();
        this.adjustLearnMoreButtonVisibility();
    }

    private void loadBannerImage() {
        if (this.bannerImageUrl == null) {
            return;
        }

        Glide.with(this)
                .load(this.bannerImageUrl)
                .placeholder(R.drawable.ic_mycrocast_logo)
                .fallback(R.drawable.ic_mycrocast_logo)
                .into(this.bannerImageView);
    }

    private void adjustLearnMoreButtonVisibility() {
        if (this.bannerTargetUrl == null) {
            this.learnMoreButton.setVisibility(View.INVISIBLE);
            return;
        }

        this.learnMoreButton.setVisibility(View.VISIBLE);
    }

    /**
     * We are done playing the advertisement, so we can close this view.
     * <p>
     * We also need to communicate to our currently running LiveStreamListenerService,
     * that we finished playing the advertisement, so the service can continue playing
     * the livestream or starts the play of a new advertisement.
     * (In this example we use intents for this.)
     */
    private void onAdvertisementPlayFinished() {
        this.sendBroadcast(new Intent(BroadcastIntent.ON_ADVERTISEMENT_PLAY_FINISHED));
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (this.mediaPlayer != null) {
            this.mediaPlayer.release();
        }

        this.mediaPlayer = null;
    }

    @Override
    public void onBackPressed() {
        // we do not want that the user can navigate back while an advertisement is playing
    }
}
