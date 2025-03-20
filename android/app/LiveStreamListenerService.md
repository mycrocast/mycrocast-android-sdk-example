# Android example app - LiveStreamListenerService

This foreground service is responsible for:
- start, pause, resume and stop the playing of a LiveStream
- playing the (configured) mute-music of a LiveStream, if the LiveStream is currently muted
- starting to play an AdvertisementPlay (via starting an AdvertisementPlayActivity)
- reacts to changes in the internet-connectivity of the example application
- configuring audio buffer and audio delay

This demonstrates following functionalities of the SDK:
- usage of the LiveStreamListenerFactory
- usage of the LiveStreamListener and the LiveStreamListener.Observer
- usage of the LiveStreamContainer and the LiveStreamContainer.Observer
- usage of the LiveStreamListenerState and the LiveStreamListenerState.Observer

It also demonstrates other functionalities, that should considered:
- a Notification (that is essential for a Foreground Service)
- a BroadcastReceiver and Intents (for communication between Activities and Service)
- a Connection observer, that will inform you if the application lost/reestablished the internet connection, e.g. via the InternetConnectionWatcher of the example app

## LiveStreamListenerFactory

This factory is responsible for the creation of a LiveStreamListener for a specific LiveStream.
Here it is possible to configure an amount of milliseconds for the audio buffer. This value defines the duration in which audio data are buffered before you receive it via the observer method (see below). Every time the audio buffer is empty it will always store audio data till the buffer is full.

If the creation was not a success (and therefore the Optional is empty), we want to stop the service right away.

``` java
@Override  
public int onStartCommand(Intent intent, int flags, int startId) {  
	// ...

	LiveStream liveStream = optional.get();

	// try to create new LiveStreamListener  
	Optional<LiveStreamListener> optionalListener = this.listenerFactory.create(liveStream, this);  
	if (optionalListener.isEmpty()) {  
		this.stopService();  
		return Service.START_STICKY;  
	}  
		
	// ...
}
```

## Configure the Audio Buffer

While creating the LivestreamListener via the factory above it is also possible to configure your desired Audio Buffer duration in milliseconds:

``` java



@Override  
public int onStartCommand(Intent intent, int flags, int startId) {  
	// ...

	final LiveStream liveStream = optional.get();
	final long audioBufferMS = 3000; // some specific value you want.

	// try to create new LiveStreamListener  
	Optional<LiveStreamListener> optionalListener = this.listenerFactory.create(liveStream, audioBufferMS, this);  
		
	// ...
}
```

Once you configured the audio buffer duration for a LivestreamListener, you cant change it to another value. So its only configurable while the creation.

If you do not configure an audio buffer at all or you configure a value which is not supported (e.g. negative numbers), the buffer will be set to 0.

For changes of the buffering state, you will get notified by the LivestreamListener.Observer:

``` java
/**  
 * Show the user some information about the buffering state. 
 * 
 * @param isBuffering Whether the sdk is currently buffering audio data or not.  
 */
@Override  
public void onIsBufferingChanged(final boolean isBuffering) {
	// ...
}
```

## Adjust the Audio Delay

While configuring the Mycrocast SDK you have the possibility to configure the maximum possible audio delay (in seconds) possible for the application:

``` java
public class MycrocastExampleApplication extends Application {

	// replace with the maximum number of seconds for the audio delay you want
	// to support
	private static final int MAX_AUDIO_DELAY = 30;

    @Override
    public void onCreate() {
        super.onCreate();

		// initialize with the sdk delay
		Mycrocast.initialize(API_KEY, CUSTOMER_TOKEN, PreferenceManager.getDefaultSharedPreferences(this), MAX_AUDIO_DELAY);
    }
}
```

Now while listening you can adjust the delay to the live audio via the delay(int seconds) method of the LivestreamListener. The default value is 0, which indicates a non existing delay. (Therefor if you want to remove the delay set the seconds to 0.)

After starting to listen to a livestream the Mycrocast SDK must collect audio data.
And of course it is impossible to set a delay which is greater than the duration of the collected audio data. Therefore your value for the delay must be in the range from 0 to the currently maximum amount possible. This maximum amount of time possible will be given to you by the LivestreamListener.Observer. This value increases till it reaches your maximum configured amount (in your application class). After this the method of the observer will not be called again.

``` java 

private LiveStreamListener listener;
private long currentDelay = 0; // start value for delay
private long currentMaxDelay = 0; // current possible max delay

/// ...

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

/// ...

/**  
 * Adjust the maximum delay the user can select.
 * 
 * @param milliseconds The time (in milliseconds) the livestream can be delayed
 * at max.  
 */
@Override  
public void onDelayChanged(long milliseconds) {  
	this.currentMaxDelay = (int) (milliseconds / 1000);
}

/// ...

```

## LiveStreamListener & LiveStreamListener.Observer

The LiveStreamListener is responsible for starting, pausing, resuming and stopping the play of a LiveStream as well as storing all the AdvertisementPlays, that should be played. 

The LiveStreamListener will automatically start playing after its successful creation.
``` java
@Override  
public int onStartCommand(Intent intent, int flags, int startId) {  
	//...

	// start playing from the stream  
	this.listener = optionalListener.get();  
	this.listener.play();  

	//...
}
```

The pausing, resuming as well as the stopping of the LiveStreamListener will be primarly triggered by receiving (via BroadcastReceiver) and processing Intents, that are send by the activities.

``` java
private void pausePlay() {  
	this.listener.pause();  

	//...
}  
  
private void resumePlay() {  
	this.listener.resume();  

	//...
}  
  
private void stopPlay() {  
	this.stopService();  
}

private void stopService() {  
	//...  

	// ensure listener is stopped  
	if (this.listener != null) {  
		this.listener.stop();  
	}
	
	//...
}
```

Via the LiveStreamListener.Observer it will also notify you 
for new queued AdvertisementPlay:

``` java
/**  
 * New advertisements are available, we get them and display them 
 * during the playback of the audio spot we mute the live track 
 */
 @Override  
public void onAdvertisementPlayQueued() {
	// ...
}

/**  
 * Creates and starts a new AdvertisementPlayActivity for playing and displaying the AdvertisementPlay. 
 *
 * @param advertisementPlay to play and display in an AdvertisementPlayActivity  
 */
private void startAdvertisementPlay(SpotPlay advertisementPlay) {  
	// ...
}
```

... as well as changes related to the audio data of the LiveStream:

``` java
/**  
 * We received a new audio package to play, we forward it to the 
 * audiotrack for playing 
 * 
 * @param pcmData - the data received  
 * @param amountToRead - the amount  
 */
@Override  
public void onAudioDataReceived(short[] pcmData, int amountToRead) {  
	// ...
}  

/**  
 * The connection to the broadcast was successfully established. 
 */
@Override  
public void onAudioConnectionEstablished() {  
	// ...
}  
  
/**  
 * The connection to the broadcast of the livestream could not be established (after several tries) 
 */
@Override  
public void onAudioConnectionFailed() {  
	// ...
}
```

## LiveStreamContainer & LiveStreamContainer.Observer

We need the LiveStreamContainer to find the LiveStream via the id we stored in the Bundle at the creation of the LiveStreamListenerService.

``` java

@Override  
public int onStartCommand(Intent intent, int flags, int startId) {  
	this.liveStreamId = intent.getLongExtra(Constants.LIVE_STREAM_ID_KEY, Constants.INVALID_ID);  

	// checking if the current live stream still exists or the provided id is  
	// valid at all, otherwise we stop the service
	Optional<LiveStream> optional = this.liveStreamContainer.find(this.liveStreamId);  
	if (optional.isEmpty()) {  
		this.stopService();  
		return Service.START_STICKY;  
	}

	//...
}
```

We also need to observe the LiveStreamContainer for all possible changes of our current LiveStream, especially if the LiveStream was updated or removed.

- Update
		- we need to check if the mute-state of the LiveStream changed and start/stop the mute-music of the LiveStream accordingly
		- also if you implemented a custom Notification, that displays some informations of the LiveStream, should also be updated
		
``` java
@Override  
public void onLiveStreamUpdated(LiveStream liveStream) {  
	if (this.liveStreamId == liveStream.getId()) {  
		this.checkForMuteMusic(liveStream);  

		// here you could update your custom notification  
	}  
}

private void checkForMuteMusic(LiveStream liveStream) {  
	if (liveStream.isMuted() == this.isLiveStreamMuted) {  
		// the mute state has not changed -> nothing to do  
		return;  
	}  

	// mute state has changed, either start or stop playing of the mute music  
	this.isLiveStreamMuted = liveStream.isMuted();  
	if (this.isLiveStreamMuted) {  
		this.startMuteMusic(liveStream.getMuteMusicUrl());  
		return;
	}  

	this.stopMuteMusic();  
}
```

- Remove
		- if our LiveStream was removed from the LiveStreamContainer, we need to stop our service and stop all currently playing media

``` java
@Override  
public void onLiveStreamRemoved(LiveStream liveStream) {  
	if (this.liveStreamId == liveStream.getId()) {  
		this.stopService();  
	}  
}

@Override  
public void onLiveStreamListChanged() {  
	Optional<LiveStream> optional = this.liveStreamContainer.find(this.liveStreamId);  
	if (optional.isEmpty()) {  
		this.stopService();  
		return; 
	}  
  
	//...  
}
```

## LiveStreamListenerState & LiveStreamListenerState.Observer

We need to get informed when the PlayState of our LiveStream changes to adjust the playing of the broadcast of the LiveStream via the audio-track accordingly. For this we need to implement a LiveStreamListenerState.Observer as following:

``` java
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
```

## Observing the Internet Connection

It is possible that during the play of the Broadcast or Mute-Music of the LiveStream the internet connection of the running device was lost. Then we need to make sure, that the playing of the Broadcast as well as Mute-Music will be stopped.

``` java
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
```

It will be started accordingly again, if the internet connection was reestablished and our LiveStream is still present in our container.

``` java
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
```

## Playing of the audio from the broadcast of the LiveStream

For the playing of the audio data in this example application we simply use an audio track.

For the creation, that is shown below, some settings are mandatory for playing our decoded audio data and MUST be set accordingly, like:
- sample rate of 48000 Hz
- channel out: mono (at this point: we do not support stereo)
- encoding: PCM 16Bit
- buffer size: 8 * 1024 bytes

Every other settings can be changed as you like and /or prefer.

The is the creation of the audio track for Android API 22 and lower:
``` java
private AudioTrack createAudioTrack() {  
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {  
		return new AudioTrack(AudioManager.STREAM_MUSIC,  
							  48000,  
							  AudioFormat.CHANNEL_OUT_MONO,  
							  AudioFormat.ENCODING_PCM_16BIT,  
							  8 * 1024, 
							  AudioTrack.MODE_STREAM);  
	}
	
	//...
}
```

The is the creation of the audio track for Android API 23 and greater:
``` java
private AudioTrack createAudioTrack() {
	//...

	AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)  
			.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)  
			.setLegacyStreamType(AudioManager.STREAM_MUSIC)  
			.build();  

	AudioFormat audioFormat = new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
			.setSampleRate(48000)
			.setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
			.build();

	return new AudioTrack.Builder().setAudioAttributes(audioAttributes)
			.setAudioFormat(audioFormat)
			.setBufferSizeInBytes(8 * 1024)
			.build();
}
```

After starting to play a LiveStream via the LiveStreamListener was a success, you will receive the decoded audio data via the implementation of the onAudioDataReceived-method from the LiveStreamListener.Observer:
```java
@Override  
public void onAudioDataReceived(short[] pcmData, int amountToRead) {  
	if (this.audioTrack == null) {  
		return;  
	}  

	this.audioTrack.write(pcmData, 0, amountToRead);  
} 
```

