# Android example App - AdvertisementPlayActivity

This activity is opened when we decide we want to play an advertisement after we received an event that new advertisements are available.

This class does:

- Start playing the audio spot
- Shows the information about the advertisement
- Determine if a banner is configured and shows it if present
- Closes itself as soon as the audio spot is over

The information for the advertisement are passed in the extras bundle of the intent



```java
    /**
     * Create a new instance of this activity as intent to be opened and
     * pass the specific information of the provided ad as extras in the bundle
     * @param context - context required to create the intent
     * @param advertisementPlay - the advertisement we want to display
     * @return the intent that can be used to start this activity
     */
    public static Intent newInstance(Context context, SpotPlay advertisementPlay) {
        Intent result = new Intent(context, AdvertisementPlayActivity.class);
        result.putExtra(AUDIO_URL_KEY, advertisementPlay.getAudioUrl());
        result.putExtra(DURATION_KEY, advertisementPlay.getDuration());

        AdvertisementBanner banner = advertisementPlay.getBanner();
        boolean hasBanner = banner != null;
        result.putExtra(HAS_BANNER_KEY, hasBanner);
        if (hasBanner) {
            result.putExtra(BANNER_IMAGE_URL, banner.getImageUrl());
            result.putExtra(BANNER_TARGET_URL, banner.getTargetUrl());
            result.putExtra(BANNER_DESCRIPTION, banner.getDescription());
        }

        return result;
    }
```



### Show the information

We need to determine if a banner was configured if so we can show it. This is a square image.

```java
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
		// ...
		// removed parts for brevity sake!

        // if a banner was configured during the creation of the advertisement
        // we want to show it
        boolean hasBanner = intent.getBooleanExtra(HAS_BANNER_KEY, false);
        if (hasBanner) {
            this.bannerImageUrl = intent.getStringExtra(BANNER_IMAGE_URL);
            this.bannerTargetUrl = intent.getStringExtra(BANNER_TARGET_URL);
            this.bannerDescription = intent.getStringExtra(BANNER_DESCRIPTION);
        }

        this.descriptionView = this.findViewById(R.id.tv_advertisement_description);
        this.bannerImageView = this.findViewById(R.id.iv_advertisement_banner);
       
        this.currentProgressView = this.findViewById(R.id.tv_advertisement_play_progress);
        this.maxProgressView = this.findViewById(R.id.tv_advertisement_duration);
        this.progressBar = this.findViewById(R.id.pb_advertisement_play);

        this.setDataToViews();
    }
     
```

#### Learn more

When a banner is configured it is required to provide a target url, a website that should be opened in case the user wants more information. This could happen for example on click of the image, we decided to add a "Learn more" button that opens the url on click


### Start the audio

As the audio spot is just a remote mp3 file or any other playable format we just create a new Mediaplayer and play it.

```java
      this.mediaPlayer = MediaPlayer.create(this, Uri.parse(this.audioUrl));
      this.mediaPlayer.setOnCompletionListener(m -> this.onAdvertisementPlayFinished());
      this.mediaPlayer.start();
```



#### Advertisement Finished

When the audio spot has ended we remove the activity again.

```java
    /**
     * We are done playing the advertisement, therefore we close the activity
     */
    private void onAdvertisementPlayFinished() {
        this.sendBroadcast(new Intent(BroadcastIntent.ON_ADVERTISEMENT_PLAY_FINISHED));
        this.finish();
    }
```

