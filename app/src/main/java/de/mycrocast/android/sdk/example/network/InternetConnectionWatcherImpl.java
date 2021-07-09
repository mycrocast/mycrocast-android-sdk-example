package de.mycrocast.android.sdk.example.network;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InternetConnectionWatcherImpl implements InternetConnectionWatcher {

    private final Set<Observer> observerList;
    private final ReadWriteLock observerLock;
    private boolean isAvailable;

    public InternetConnectionWatcherImpl(@NonNull ConnectivityManager connectivityManager) {
        this.observerList = new HashSet<>();
        this.observerLock = new ReentrantReadWriteLock();

        ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {

            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                if (InternetConnectionWatcherImpl.this.isAvailable) {
                    return;
                }

                InternetConnectionWatcherImpl.this.isAvailable = true;

                Lock lock = InternetConnectionWatcherImpl.this.observerLock.readLock();
                lock.lock();
                try {
                    for (Observer observer : InternetConnectionWatcherImpl.this.observerList) {
                        observer.onConnectionEstablished();
                    }
                } finally {
                    lock.unlock();
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                if (!InternetConnectionWatcherImpl.this.isAvailable) {
                    return;
                }

                InternetConnectionWatcherImpl.this.isAvailable = false;

                Lock lock = InternetConnectionWatcherImpl.this.observerLock.readLock();
                lock.lock();
                try {
                    for (Observer observer : InternetConnectionWatcherImpl.this.observerList) {
                        observer.onConnectionLost();
                    }
                } finally {
                    lock.unlock();
                }
            }
        };

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            NetworkRequest request = new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
            connectivityManager.registerNetworkCallback(request, callback);
            return;
        }

        connectivityManager.registerDefaultNetworkCallback(callback);
    }

    @Override
    public void addObserver(Observer observer) {
        Lock lock = InternetConnectionWatcherImpl.this.observerLock.writeLock();
        lock.lock();
        try {
            this.observerList.add(observer);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeObserver(Observer observer) {
        Lock lock = InternetConnectionWatcherImpl.this.observerLock.writeLock();
        lock.lock();
        try {
            this.observerList.remove(observer);
        } finally {
            lock.unlock();
        }
    }
}
