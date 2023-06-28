package de.mycrocast.android.sdk.example.utility;

/**
 * Utility class containing all in the app used intent-actions.
 */
public class BroadcastIntent {

    /**
     * Intent-Action, telling the LiveStreamListenerService to pause the play of the livestream.
     */
    public static final String PAUSE_LIVE_STREAM = "pause_live_stream";

    /**
     * Intent-Action, telling the LiveStreamListenerService to resume the play of the livestream.
     */
    public static final String RESUME_LIVE_STREAM = "resume_live_stream";

    /**
     * Intent-Action, telling the LiveStreamListenerService to stop the play of the livestream and stop itself.
     */
    public static final String STOP_LIVE_STREAM = "stop_live_stream";

    /**
     * Intent-Action, telling the LiveStreamListenerService that a AdvertisementPlay was finished.
     */
    public static final String ON_ADVERTISEMENT_PLAY_FINISHED = "advertisement_play_finished";

    /**
     * Intent-Action, telling the LiveStreamListenerService to adjust the delay accordingly.
     */
    public static final String ADJUST_DELAY = "adjust_live_stream_delay";

    /**
     * Intent-Action, telling the LiveStreamListenerService to remove the existing delay.
     */
    public static final String REMOVE_DELAY = "remove_live_stream_delay";
}
