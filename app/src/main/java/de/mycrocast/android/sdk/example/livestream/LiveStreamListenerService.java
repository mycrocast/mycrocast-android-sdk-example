package de.mycrocast.android.sdk.example.livestream;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import de.mycrocast.android.sdk.core.Mycrocast;
import de.mycrocast.android.sdk.core.util.Optional;
import de.mycrocast.android.sdk.example.MycrocastSDKExampleApplication;
import de.mycrocast.android.sdk.example.R;
import de.mycrocast.android.sdk.example.advertisement.AdvertisementPlayActivity;
import de.mycrocast.android.sdk.example.network.InternetConnectionWatcher;
import de.mycrocast.android.sdk.example.utility.BroadcastIntent;
import de.mycrocast.android.sdk.example.utility.Constants;
import de.mycrocast.android.sdk.live.container.LiveStreamContainer;
import de.mycrocast.android.sdk.live.data.LiveStream;
import de.mycrocast.android.sdk.live.listener.LiveStreamListener;
import de.mycrocast.android.sdk.live.listener.LiveStreamListenerFactory;
import de.mycrocast.android.sdk.live.listener.state.LiveStreamListenerState;
import de.mycrocast.android.sdk.live.listener.state.PlayState;
import de.mycrocast.android.sdk.spot.play.data.SpotPlay;

/**
 * Service running for a single live stream that we are currently listening to
 * <p>
 * The service reacts on a number of intents via a BroadcastReceiver and play the audio of the live stream
 * <p>
 * We react on the LiveStreamListener.Observer to play advertisements that are available for this
 * live stream as well as receiving audio data to play
 * <p>
 * We react on the LiveStreamContainer.Observer to close this service in case our stream has ended
 * <p>
 * We react on LiveStreamListenerState.Observer to update the play state of our audio track
 */
public class LiveStreamListenerService extends Service implements LiveStreamListener.Observer, LiveStreamContainer.Observer, LiveStreamListenerState.Observer, InternetConnectionWatcher.Observer {

    /**
     * Creates a new intent for starting a new LiveStreamListenerService for the given LiveStream.
     *
     * @param context
     * @param liveStream the livestream we want to play
     * @return Intent for starting the LiveStreamListenerService
     */
    public static Intent newInstance(Context context, LiveStream liveStream) {
        long streamId = liveStream == null ? Constants.INVALID_ID : liveStream.getId();

        Intent result = new Intent(context, LiveStreamListenerService.class);
        result.putExtra(Constants.LIVE_STREAM_ID_KEY, streamId);
        return result;
    }

    private final LiveStreamListenerState listenerState = Mycrocast.getListenerState();
    private final LiveStreamContainer liveStreamContainer = Mycrocast.getLiveStreamContainer();
    private final LiveStreamListenerFactory listenerFactory = Mycrocast.getLiveStreamListenerFactory();

    private long liveStreamId;
    private boolean isStopped;
    private boolean isLiveStreamMuted;
    private boolean isAdvertisementPlaying;
    private InternetConnectionWatcher connectionWatcher;
    private LiveStreamListener listener;
    private AudioTrack audioTrack;
    private BroadcastReceiver receiver;
    private MediaPlayer muteMusicPlayer;
    private Toast bufferInformationToast;
    private int currentMaxDelay = 0;
    private int currentDelay = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        this.isStopped = false;
        this.isAdvertisementPlaying = false;

        this.connectionWatcher = ((MycrocastSDKExampleApplication) this.getApplication()).getConnectionWatcher();

        // add observer, we want to get notified
        this.connectionWatcher.addObserver(this);
        this.liveStreamContainer.addObserver(this);
        this.listenerState.addObserver(this);

        // create broadcast receiver, to receive necessary intents
        this.receiver = this.createBroadcastReceiver();
        this.registerReceiver(this.receiver, this.createIntentFilter());

        // create audio-track
        this.audioTrack = this.createAudioTrack();
        this.audioTrack.setVolume(1.0f);

        // create notification
        Notification notification = this.createNotification();
        this.startForeground(1, notification);
    }

    // create a broadcast-receiver that processes all BroadcastIntents that will be send.
    private BroadcastReceiver createBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case BroadcastIntent.PAUSE_LIVE_STREAM:
                        LiveStreamListenerService.this.pausePlay();
                        break;

                    case BroadcastIntent.RESUME_LIVE_STREAM:
                        LiveStreamListenerService.this.resumePlay();
                        break;

                    case BroadcastIntent.STOP_LIVE_STREAM:
                        LiveStreamListenerService.this.stopPlay();
                        break;

                    case BroadcastIntent.ON_ADVERTISEMENT_PLAY_FINISHED:
                        LiveStreamListenerService.this.onAdvertisementPlayFinished();
                        break;

                    case BroadcastIntent.ADJUST_DELAY:
                        LiveStreamListenerService.this.adjustDelay(intent.getIntExtra("delay", 0));
                        break;

                    case BroadcastIntent.REMOVE_DELAY:
                        LiveStreamListenerService.this.removeDelay();
                }
            }
        };
    }

    private void pausePlay() {
        this.listener.pause();

        // pause currently playing mute music (if it is currently playing)
        if (this.muteMusicPlayer != null && this.muteMusicPlayer.isPlaying()) {
            this.muteMusicPlayer.pause();
        }
    }

    private void resumePlay() {
        this.listener.resume();

        // resume playing mute music (if it needs to be played)
        if (this.muteMusicPlayer != null) {
            this.muteMusicPlayer.start();
        }
    }

    private void stopPlay() {
        this.stopService();
    }

    private void onAdvertisementPlayFinished() {
        // is there another AdvertisementPlay queued?
        // if yes, we want to start playing this one
        Optional<SpotPlay> optional = this.listener.getNextAdvertisementPlay();
        if (optional.isPresent()) {
            this.startAdvertisementPlay(optional.get());
            return;
        }

        // else we stop playing advertisements and start playing the livestream again
        this.isAdvertisementPlaying = false;

        // resume playing mute music
        if (this.muteMusicPlayer != null) {
            this.muteMusicPlayer.start();
        }

        // unmute the livestream
        this.audioTrack.flush();
        this.audioTrack.setVolume(1.0f);
    }

    private void adjustDelay(int delay) {
        int newDelay = this.currentDelay + delay;
        newDelay = Math.max(0, Math.min(newDelay, this.currentMaxDelay));

        this.listener.setDelay(newDelay);
        this.currentDelay = newDelay;
    }

    private void removeDelay() {
        this.listener.setDelay(0);
        this.currentDelay = 0;
    }

    /**
     * Creates and starts a new AdvertisementPlayActivity for playing and displaying the AdvertisementPlay.
     *
     * @param advertisementPlay to play and display in an AdvertisementPlayActivity
     */
    private void startAdvertisementPlay(SpotPlay advertisementPlay) {
        Intent startIntent = AdvertisementPlayActivity.newInstance(this, advertisementPlay);
        startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // needed for starting an activity from a non-activity context
        this.startActivity(startIntent);
    }

    /**
     * Creates an IntentFilter with all needed actions.
     *
     * @return created IntentFilter
     */
    private IntentFilter createIntentFilter() {
        IntentFilter result = new IntentFilter();
        result.addAction(BroadcastIntent.PAUSE_LIVE_STREAM);
        result.addAction(BroadcastIntent.RESUME_LIVE_STREAM);
        result.addAction(BroadcastIntent.STOP_LIVE_STREAM);
        result.addAction(BroadcastIntent.ON_ADVERTISEMENT_PLAY_FINISHED);
        result.addAction(BroadcastIntent.ADJUST_DELAY);
        result.addAction(BroadcastIntent.REMOVE_DELAY);
        return result;
    }

    /**
     * Creates the audioTrack needed to play the received audio-data from the sdk.
     * <p>
     * Must-have settings for the audioTrack:
     * <p>
     * - SampleRate: 48000
     * <p>
     * - Channel: Mono
     * <p>
     * - Encoding: PCM-16Bit
     * <p>
     * - Buffer size: 8 * 1024
     *
     * @return created AudioTrack
     */
    private AudioTrack createAudioTrack() {
        final int sampleRate = 48000;
        final int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        final int audioEncodingFormat = AudioFormat.ENCODING_PCM_16BIT;
        final int bufferSize = 8 * 1024;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConfig, audioEncodingFormat, bufferSize, AudioTrack.MODE_STREAM);
        }

        final AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build();

        final AudioFormat audioFormat = new AudioFormat.Builder().setEncoding(audioEncodingFormat)
                .setSampleRate(sampleRate)
                .setChannelMask(channelConfig)
                .build();

        return new AudioTrack.Builder().setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(bufferSize)
                .build();
    }

    /**
     * Creates a rudimentary example Notification.
     * This notification can easily be replaced with a custom one, that has a custom layout.
     * (e.g: with information about the livestream as well as a play/pause/stop button)
     *
     * @return created Notification
     */
    private Notification createNotification() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return new Notification();
        }

        String NOTIFICATION_CHANNEL_ID = "de.mycrocast.sdk.example";
        String channelName = "ListenerService";
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        Intent stopIntent = new Intent(BroadcastIntent.STOP_LIVE_STREAM);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        return notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_mycrocast_logo)
                .setContentTitle("Currently listening to a livestream.")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .addAction(R.drawable.ic_mycrocast_logo, this.getString(R.string.button_stop), stopPendingIntent)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            this.stopService();
            return Service.START_NOT_STICKY;
        }

        this.liveStreamId = intent.getLongExtra(Constants.LIVE_STREAM_ID_KEY, Constants.INVALID_ID);

        // checking if the current live stream still exists or the provided id is
        // valid at all, otherwise we stop the service
        Optional<LiveStream> optional = this.liveStreamContainer.find(this.liveStreamId);
        if (optional.isEmpty()) {
            this.stopService();
            return Service.START_NOT_STICKY;
        }

        LiveStream liveStream = optional.get();

        // try to create new LiveStreamListener
        Optional<LiveStreamListener> optionalListener = this.listenerFactory.create(liveStream, this);
        if (optionalListener.isEmpty()) {
            this.stopService();
            return Service.START_NOT_STICKY;
        }

        // start playing from the stream
        this.listener = optionalListener.get();
        this.listener.play();

        // play the mute music if the livestream is muted
        this.isLiveStreamMuted = liveStream.isMuted();
        if (this.isLiveStreamMuted) {
            this.startMuteMusic(liveStream.getMuteMusicUrl());
        }

        return Service.START_STICKY;
    }

    /**
     * Finish all playing sources (Livestream as well as Mute-Music) and stops this service,
     * deleting the notification as well.
     */
    private void stopService() {
        if (this.isStopped) {
            return;
        }

        this.isStopped = true;

        // ensure mute music is stopped
        this.stopMuteMusic();

        // ensure listener is stopped
        if (this.listener != null) {
            this.listener.stop();
        }

        // remove observer, we do not need to get notified anymore
        this.connectionWatcher.removeObserver(this);
        this.liveStreamContainer.removeObserver(this);
        this.listenerState.removeObserver(this);

        // clean up audioTrack
        this.audioTrack.flush();
        this.audioTrack = null;

        this.unregisterReceiver(this.receiver);

        this.stopForeground(true);
        this.stopSelf();
    }

    /**
     * Stops playing the mute music, if it is currently playing.
     */
    private void stopMuteMusic() {
        if (this.muteMusicPlayer == null) {
            return;
        }

        if (this.muteMusicPlayer.isPlaying()) {
            this.muteMusicPlayer.stop();
            this.muteMusicPlayer.release();
        }

        this.muteMusicPlayer = null;
    }

    /**
     * Starts playing the mute music.
     * Stops old playing of the mute music, if one is currently playing.
     *
     * @param muteMusicUrl url to play from
     */
    private void startMuteMusic(String muteMusicUrl) {
        this.stopMuteMusic();

        this.muteMusicPlayer = MediaPlayer.create(this, Uri.parse(muteMusicUrl));
        this.muteMusicPlayer.setLooping(true); // we want to loop the music till the livestream is not muted anymore or has ended
        this.muteMusicPlayer.start();
    }

    /**
     * New advertisements are available, we get them and display them
     * during the playback of the audio spot we mute the live track
     */
    @Override
    public void onAdvertisementPlayQueued() {
        Optional<SpotPlay> optional = this.listener.getNextAdvertisementPlay();
        if (optional.isEmpty() || this.isAdvertisementPlaying) {
            return;
        }

        this.isAdvertisementPlaying = true;

        // mute the livestream while playing an advertisement
        this.audioTrack.setVolume(0.0f);

        // mute the mute music, if it is currently playing
        if (this.muteMusicPlayer != null && this.muteMusicPlayer.isPlaying()) {
            this.muteMusicPlayer.pause();
        }

        this.startAdvertisementPlay(optional.get());
    }

    /**
     * We received a new audio package to play, we forward it to the
     * audiotrack for playing
     *
     * @param pcmData      - the data received
     * @param amountToRead - the amount
     */
    @Override
    public void onAudioDataReceived(short[] pcmData, int amountToRead) {
        if (this.audioTrack == null) {
            return;
        }

        this.audioTrack.write(pcmData, 0, amountToRead);
    }

    @Override
    public void onIsBufferingChanged(final boolean isBuffering) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (LiveStreamListenerService.this.bufferInformationToast != null) {
                    LiveStreamListenerService.this.bufferInformationToast.cancel();
                }

                LiveStreamListenerService.this.bufferInformationToast = Toast.makeText(LiveStreamListenerService.this, isBuffering ? "Currently " +
                        "Buffering" : "Finished Buffering", Toast.LENGTH_SHORT);
                LiveStreamListenerService.this.bufferInformationToast.show();
            }
        });
    }

    @Override
    public void onDelayChanged(long milliseconds) {
        this.currentMaxDelay = (int) (milliseconds / 1000);
    }

    /**
     * The connection to the broadcast was successfully established.
     */
    @Override
    public void onAudioConnectionEstablished() {
    }

    /**
     * The connection to the broadcast of the livestream could not be established (after several tries)
     */
    @Override
    public void onAudioConnectionFailed() {
        this.stopService();
    }

    /**
     * A livestream was added.
     * <p>
     * We need to check if the added livestream is our current one.
     * If yes, we need to update our mute-music state (and the custom notification).
     *
     * @param liveStream - the new livestream
     */
    @Override
    public void onLiveStreamAdded(LiveStream liveStream) {
        this.onLiveStreamUpdated(liveStream);
    }

    /**
     * A livestream was changed.
     * <p>
     * We need to check if the changed livestream is our current one.
     * If yes, we need to update our mute-music state (and the custom notification).
     *
     * @param liveStream - the new livestream
     */
    @Override
    public void onLiveStreamUpdated(LiveStream liveStream) {
        if (this.liveStreamId == liveStream.getId()) {
            if (this.listenerState.isCurrentPlayingLiveStream(liveStream)) {
                this.checkForMuteMusic(liveStream);
            }

            // here you could update your custom notification
        }
    }

    private void checkForMuteMusic(LiveStream liveStream) {
        if (this.isAdvertisementPlaying) {
            // not starting to play mute music if ad is playing
            return;
        }

        if (liveStream.isMuted() == this.isLiveStreamMuted) {
            // the mute state has not changed -> nothing to do
            return;
        }

        // mute state has changed, either start or stop playing of the mute music
        this.isLiveStreamMuted = liveStream.isMuted();
        if (this.isLiveStreamMuted) {
            this.audioTrack.setVolume(0.0f);
            this.startMuteMusic(liveStream.getMuteMusicUrl());
            return;
        }

        this.audioTrack.setVolume(1.0f);
        this.stopMuteMusic();
    }

    /**
     * A livestream has ended.
     * <p>
     * We need to check if the ended livestream is our current one.
     * If yes, we need to stop this service.
     *
     * @param liveStream - the ended livestream
     */
    @Override
    public void onLiveStreamRemoved(LiveStream liveStream) {
        if (this.liveStreamId == liveStream.getId()) {
            this.stopService();
        }
    }

    /**
     * The list of current livestreams has changed.
     * <p>
     * We need to check if our currently livestream is still in this list of all currently active livestreams.
     * If it is not, it went offline, so we need to stop this service.
     * If yes, we can update all our views.
     */
    @Override
    public void onLiveStreamListChanged() {
        Optional<LiveStream> optional = this.liveStreamContainer.find(this.liveStreamId);
        if (optional.isEmpty()) {
            this.stopService();
            return;
        }

        this.onLiveStreamUpdated(optional.get());
    }

    /**
     * The play state has changed we configure the audio track accordingly, if the
     * change was to our current live stream
     *
     * @param liveStream - the live stream that this update is belonging too
     * @param playState  - the new play state
     */
    @Override
    public void onPlayStateChanged(LiveStream liveStream, PlayState playState) {
        // play state updates of other LiveStreams are not interesting
        if (this.liveStreamId != liveStream.getId()) {
            return;
        }

        switch (playState) {
            case PLAYING:
                this.audioTrack.play();
                break;

            case PAUSED:
                this.audioTrack.pause();
                break;

            case STOPPED:
                this.audioTrack.stop();
                break;
        }
    }

    /**
     * Connection to the internet was reestablished.
     * We can start playing the livestream again.
     */
    @Override
    public void onConnectionEstablished() {
        Optional<LiveStream> optional = this.liveStreamContainer.find(this.liveStreamId);
        if (optional.isEmpty()) {
            this.stopService();
            return;
        }

        this.listener.play();

        LiveStream liveStream = optional.get();
        this.isLiveStreamMuted = liveStream.isMuted();
        if (this.isLiveStreamMuted) {
            this.startMuteMusic(liveStream.getMuteMusicUrl());
        } else {
            this.stopMuteMusic();
        }
    }

    /**
     * Connection to the internet was lost.
     * We need to stop the play of the livestream or mute-music.
     */
    @Override
    public void onConnectionLost() {
        this.listener.stop();

        if (this.isLiveStreamMuted) {
            this.stopMuteMusic();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
