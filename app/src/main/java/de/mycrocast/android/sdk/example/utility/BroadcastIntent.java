package de.mycrocast.android.sdk.example.utility;

import android.content.Intent;

/**
 * Utility class containing all in the app used intent-actions.
 */
public class BroadcastIntent {

    private static final String PACKAGE_NAME = "de.mycrocast.android.sdk.example";

    /**
     * Intent-Action, telling the LiveStreamListenerService to pause the play of the livestream.
     */
    public static final String PAUSE_LIVE_STREAM = PACKAGE_NAME + ".pause_live_stream";

    /**
     * Intent-Action, telling the LiveStreamListenerService to resume the play of the livestream.
     */
    public static final String RESUME_LIVE_STREAM = PACKAGE_NAME + ".resume_live_stream";

    /**
     * Intent-Action, telling the LiveStreamListenerService to stop the play of the livestream and stop itself.
     */
    public static final String STOP_LIVE_STREAM = PACKAGE_NAME + ".stop_live_stream";

    /**
     * Intent-Action, telling the LiveStreamListenerService that a AdvertisementPlay was finished.
     */
    public static final String ON_ADVERTISEMENT_PLAY_FINISHED = PACKAGE_NAME + ".advertisement_play_finished";

    /**
     * Intent-Action, telling the LiveStreamListenerService to adjust the delay accordingly.
     */
    public static final String ADJUST_DELAY = PACKAGE_NAME + ".adjust_live_stream_delay";

    /**
     * Intent-Action, telling the LiveStreamListenerService to remove the existing delay.
     */
    public static final String REMOVE_DELAY = PACKAGE_NAME + ".remove_live_stream_delay";


    public static Intent pauseLivestream() {
        final Intent intent = new Intent(BroadcastIntent.PAUSE_LIVE_STREAM);
        intent.setPackage(BroadcastIntent.PACKAGE_NAME);
        return intent;
    }

    public static Intent resumeLivestream() {
        final Intent intent = new Intent(BroadcastIntent.RESUME_LIVE_STREAM);
        intent.setPackage(BroadcastIntent.PACKAGE_NAME);
        return intent;
    }

    public static Intent stopLivestream() {
        final Intent intent = new Intent(BroadcastIntent.STOP_LIVE_STREAM);
        intent.setPackage(BroadcastIntent.PACKAGE_NAME);
        return intent;
    }

    public static Intent advertisementPlayFinished() {
        final Intent intent = new Intent(BroadcastIntent.ON_ADVERTISEMENT_PLAY_FINISHED);
        intent.setPackage(BroadcastIntent.PACKAGE_NAME);
        return intent;
    }

    public static Intent adjustDelay() {
        final Intent intent = new Intent(BroadcastIntent.ADJUST_DELAY);
        intent.setPackage(BroadcastIntent.PACKAGE_NAME);
        return intent;
    }

    public static Intent removeDelay() {
        final Intent intent = new Intent(BroadcastIntent.REMOVE_DELAY);
        intent.setPackage(BroadcastIntent.PACKAGE_NAME);
        return intent;
    }
}
