package de.mycrocast.android.sdk.example;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;

import androidx.preference.PreferenceManager;

import de.mycrocast.android.sdk.core.Mycrocast;
import de.mycrocast.android.sdk.error.ErrorReceiving;
import de.mycrocast.android.sdk.error.MycrocastError;
import de.mycrocast.android.sdk.example.network.InternetConnectionWatcher;
import de.mycrocast.android.sdk.example.network.InternetConnectionWatcherImpl;
import de.mycrocast.android.sdk.live.checker.domain.LivestreamAvailabilityChecker;

/**
 * Starting point of the application, also the point where we initialise the sdk,
 * because we want to initialise it as early as possible.
 */
public class MycrocastSDKExampleApplication extends Application implements ErrorReceiving.Observer {

    private static final String API_KEY = "fHDYOI1SDw8e5P12"; // replace with your api key (can be found in mycrocast-Studio)
    private static final String CUSTOMER_TOKEN = "1567504890375_8741a554-c25e-428f-a807-a69bac373315-9999"; // replace with your customer token (can be found in mycrocast-Studio)

    private InternetConnectionWatcher connectionWatcher;

    @Override
    public void onCreate() {
        super.onCreate();

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.connectionWatcher = new InternetConnectionWatcherImpl(connectivityManager);

        // add observer for receiving any error occurring in the sdk
        Mycrocast.getErrorReceiving().addObserver(this);

        // check if a livestream of your club is currently online
        final LivestreamAvailabilityChecker checker = Mycrocast.getLivestreamAvailabilityChecker();
        checker.isClubLive(CUSTOMER_TOKEN, new LivestreamAvailabilityChecker.ResultCallback() {
            @Override
            public void onLivestreamAvailable() {
                System.out.println("At least one livestream is currently online for your club!");
            }
            @Override
            public void onNoLivestreamOnline() {
                System.out.println("No livestream online for your club.");
            }
        });

        // initialize the sdk with your credentials
        Mycrocast.initialize(API_KEY, CUSTOMER_TOKEN, PreferenceManager.getDefaultSharedPreferences(this), 30);
    }

    public InternetConnectionWatcher getConnectionWatcher() {
        return this.connectionWatcher;
    }

    /**
     * Method that will be called, if an error in the sdk occurred.
     *
     * @param error - the error that was send from the sdk
     */
    @Override
    public void onError(MycrocastError error) {
        System.out.println(error.getDescription());
    }
}
