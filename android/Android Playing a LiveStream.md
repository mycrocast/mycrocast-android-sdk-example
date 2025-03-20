# Playing the audio of a LiveStream

This document describes everything related to controlling the play of a LiveStream, as well as receiving any updates related to the play of a LiveStream.

## Relevant classes
- [LiveStreamListenerFactory](documentation/de/mycrocast/android/sdk/live/listener/LiveStreamListenerFactory.html) -> interface for the creation of a LiveStreamListener of a specific LiveStream
- [LiveStreamListener](documentation/de/mycrocast/android/sdk/live/listener/LiveStreamListener.html) -> interface for controlling the play of a specific LiveStream, as well as the container/queue for available Advertisements that are ready to play
- [LiveStreamListener.Observer](documentation/de/mycrocast/android/sdk/live/listener/LiveStreamListener.Observer.html) -> interface to implement to receive updates to the audio data, the decoded audio data as well as the addition of an AdvertisementPlay to the queue 
- [LiveStreamListenerState](documentation/de/mycrocast/android/sdk/live/listener/state/LiveStreamListenerState.html) -> interface, that can be used to check the current play state of any LiveStream
- [LiveStreamListenerState.Observer](documentation/de/mycrocast/android/sdk/live/listener/state/LiveStreamListenerState.Observer.html) -> interface to implement to receive any play state updates of every LiveStream
- [PlayState](documentation/de/mycrocast/android/sdk/live/listener/state/PlayState.html) -> enumeration representing the current play state of a LiveStream

## Concepts

### Playing, Pausing, Resuming, Stopping a LiveStream

Starting, pausing, resuming and stopping of a LiveStream can be done via the LiveStreamListener:

``` java
public interface LiveStreamListener {  
  
	//...
  
	/**  
	* Start the play of the stream 
	*/ 
	void play();  

	/**  
	* Pause the live audio. Resuming will jump to the live state and not
	* continue where you paused the stream 
	*/
	void pause();  

	/**  
	* Resume the stream, same behaviour like play 
	*/
	void resume();  

	/**  
	* Stop the audio stream 
	*/ 
	void stop();

	/**  
	* Set's the delay for a livestream.
	* 
	* @param seconds Time (in seconds) the playback of the livestream should be delayed.  
	*/
	void setDelay(int seconds);

	//...
}
```

To create aLiveStreamListener you need to use the LiveStreamListenerFactory. The create-process can fail (only if the sdk could not find the LiveStream internally) and the method will then return an empty optional:

``` java
public interface LiveStreamListenerFactory {  
  
	/**  
	 * Creates a new instance to prepare the play of the livestream. 
	 * Will lead to a bufferDuration of 0. 
	 * 
	 * @param liveStream - the livestream to play  
	 * @param observer   - observable to pass in to receive updates  
	 */Optional<LiveStreamListener> create(LiveStream liveStream, LiveStreamListener.Observer observer);  
	  
	/**  
	 * Creates a new instance to prepare the play of the livestream
	 * 
	 * @param liveStream     - the livestream to play  
	 * @param bufferDuration - the duration of the audio buffer in milliseconds  
	 * @param observer       - observable to pass in to receive updates  
	 */Optional<LiveStreamListener> create(LiveStream liveStream, long bufferDuration, LiveStreamListener.Observer observer);
}
```

Only if a LiveStream is currently playing, you will receive any updates corresponding to the audio data via the LiveStreamListener.Observer:

``` java
public interface LiveStreamListener {  
  
	/**  
	* Implement this interface to receive information when new 
	* advertisements to play are available 
	*/ 
	interface Observer extends LiveStreamPlayer.Observer {
	
		/**  
		 * Received a package of audio data 
		 * 
		 * @param pcmData - the data received  
		 * @param amountToRead - the amount  
		 */
		void onAudioDataReceived(short[] pcmData, int amountToRead);  

		/**  
		 * State of buffering changed. 
		 * 
		 * @param isBuffering Whether the sdk is currently buffering audio data
		 * or not.  
		 */
		 void onIsBufferingChanged(boolean isBuffering);  
		  
		/**  
		 * Maximum delay for the livestream changed.
		 * 
		 * @param milliseconds The time (in milliseconds) the livestream can be
		 * delayed at max.  
		 */
		 void onDelayChanged(long milliseconds);

		/**  
		 * Audio connection was established successfully 
		 */
		void onAudioConnectionEstablished();  

		/**  
		 * Connection could not be established. 
		 * This could be because you have no internet or the stream is just not online 
		 */
		void onAudioConnectionFailed();  
		
		//...
	}

	//...
}
```

It is recommended to play the audio data in a foreground service, so that Android does not kill the play-process if your application moves to the background (of Android).

For an example you can look into the LiveStreamListenerService of the example Application.

To determine if a LiveStream is currently playing or if a specific LiveStream is the same one as the currently playing one, you can use the LiveStreamListenerState. It will store the current state of the currently playing (or paused) LiveStream, if one is currently playing (or paused):

```java
public interface LiveStreamListenerState extends Observable<LiveStreamListenerState.Observer> {  

	//...

	/**  
	* @return true if we have currently a live stream in any play state that is not stopped  
	*/
	boolean hasCurrentLiveStream();  

	/**  
	* @param liveStream - the live stream we want to check  
	* @return true if this stream is our current live stream, false otherwise  
	*/
	boolean isCurrentLiveStream(final LiveStream liveStream);  

	/**  
	* @param liveStream - the live stream to check  
	* @return true if the livestream is our current stream and the stream is in paused state  
	*/
	boolean isCurrentPausedLiveStream(final LiveStream liveStream);  

	/**  
	* @param liveStream the live stream to check  
	* @return true if the livestream is our current stream and the stream is in playing state  
	*/
	boolean isCurrentPlayingLiveStream(final LiveStream liveStream);  
}
```

If you want to receive any updates corresponding to a PlayState of any LiveStream, to update a view for example, you can add a LiveStreamListenerState.Observer to the LiveStreamListenerState:

```java
public interface LiveStreamListenerState extends Observable<LiveStreamListenerState.Observer> {  
	/**  
	* Implement this interface to receive updates for the livestreams play state 
	*/
	interface Observer {  
		/**  
		* Receive a play state update for a live stream * * @param liveStream - the live stream that this update is belonging too  
		* @param playState - the new play state  
		*/
		void onPlayStateChanged(final LiveStream liveStream, final PlayState playState);  
	}  
	
	//...
}
```

Calling start, pause, resume or stop on a LiveStreamListener will automatically update the state in the LiveStreamListenerState, it will also notify each LiveStreamListenerState.Observe that is currently added to the LiveStreamListenerState.

### Mute music

A streamer could be muted during your connection time or mute himself anytime within the current broadcast.

When a streamer is muted you should play the mute music, the remote URL to that file is contained in the LiveStream object.

After connecting check the muted flag in the LiveStream and afterwards react to the specific callbacks to changes in the mute state of the streamer.

### Advertisement within the stream

The streamer can at anytime decide that advertisements should be played within the broadcast.

If you decide to actually play the advertisement, reduce the audio of the stream, or the mute music to zero during the play and increase them afterwards again.

### Pause/Resume

We do not provide the functionality to actually pause a stream and later on resume at that point in time. Internally each resume/play just starts from the current live moment. If you want to provide that functionality you can implement it. You should never call pause/stop on our SDK but just reduce the volume to zero and keep storing the audio packages.

### Buffering

During the connection to a live stream you can configure the buffering amount. That is the amount of data to collect before providing the first audio packages.
A short buffer is useful for a better user experience and to mask connectivity issues.
The higher the buffer, the higher the delay and initial wait time. A value between 1s - 5s is recommended.

How does it work?
We initially start collecting audio packages until we have collected enough to satisfy the configured buffer time.
(We start in the buffering state)
Afterwards we change to the playing state and start providing audio packages for playback. When chaning from buffering to playing up to 10 packages are provided as burst.

When the buffer is empty again (bad network for example), we start buffering again until we reached the configured value.

### Delay

You can now configure a delay so that the user can sync the audio with any potencial video source.
The general flow of the delay is as following:
1. You configure the maximum delay that is internally stored (how much audio we internally store) during initialization. 5 minutes is a good value
2. As soon as the stream is connected, we collect the audio packages and update you how much we currently have collected.
3. If enough is collected, the user can configure a delay up to the initially configured maximum
4. Any event that occurs is also processed delayed (playing of advertisement, changing the mute state and so on)
5. If the user moves the delay and "jumps" over an event, the event is executed (when moving to the live moment) or undone (when moving in the past)

When the user pauses/stops the stream and resumes, collection of audio needs to start again.