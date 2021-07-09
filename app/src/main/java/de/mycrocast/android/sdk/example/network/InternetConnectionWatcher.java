package de.mycrocast.android.sdk.example.network;

/**
 * Watches changes in the internet connectivity.
 */
public interface InternetConnectionWatcher {

    /**
     * Observer that will be notified everytime an connection was lost or established.
     */
    interface Observer {

        /**
         * The connection to the internet was (re-)established.
         */
        void onConnectionEstablished();

        /**
         * The connection to the internet was lost.
         */
        void onConnectionLost();
    }

    void addObserver(Observer observer);

    void removeObserver(Observer observer);
}
