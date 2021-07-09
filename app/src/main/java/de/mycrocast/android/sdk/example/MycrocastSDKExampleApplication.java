package de.mycrocast.android.sdk.example;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;

import de.mycrocast.android.sdk.core.Mycrocast;
import de.mycrocast.android.sdk.error.ErrorReceiving;
import de.mycrocast.android.sdk.error.MycrocastError;
import de.mycrocast.android.sdk.example.network.InternetConnectionWatcher;
import de.mycrocast.android.sdk.example.network.InternetConnectionWatcherImpl;

/**
 * Starting point of the application, also the point where we initialise the sdk,
 * because we want to initialise it as early as possible.
 */
public class MycrocastSDKExampleApplication extends Application implements ErrorReceiving.Observer {

    private static final String API_KEY = "fYt_CchkLQNzjIoD"; // replace with your api key
    private static final String CUSTOMER_TOKEN = "aaaaa"; // replace with your customer token

    private InternetConnectionWatcher connectionWatcher;

    @Override
    public void onCreate() {
        super.onCreate();

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.connectionWatcher = new InternetConnectionWatcherImpl(connectivityManager);

        // add observer for receiving any error occurring in the sdk
        Mycrocast.getErrorReceiving().addObserver(this);

        // initialize the sdk with your credentials, that you can get via the mycrocast-Studio
        Mycrocast.initialize(API_KEY, CUSTOMER_TOKEN, PreferenceManager.getDefaultSharedPreferences(this));
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
