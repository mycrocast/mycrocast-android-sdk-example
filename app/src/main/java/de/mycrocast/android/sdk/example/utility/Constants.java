package de.mycrocast.android.sdk.example.utility;

/**
 * Utility class containing all in the app used constants.
 */
public class Constants {

    /**
     * Invalid id of a LiveStream.
     * Every valid LiveStream will have an id, that is grater than 0.
     *
     * Used in Bundles, where we want to save the id of a livestream, but this livestream is null.
     * So when we get the id from the bundle and try to find the livestream in the LiveStreamContainer,
     * we will always get an empty optional.
     */
    public static final long INVALID_ID = -1;

    /**
     * Key specifically used for storing (or getting) the id of a livestream in a (or from) Bundle.
     */
    public static final String LIVE_STREAM_ID_KEY = "live_stream_id";
}
