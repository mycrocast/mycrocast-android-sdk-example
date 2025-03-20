# Android Advertisements

## Relevant classes
- [SpotPlay](documentation/de/mycrocast/android/sdk/spot/play/data/SpotPlay.html) -> interface, representing an advertisement
- [SpotBanner](documentation/de/mycrocast/android/sdk/spot/play/banner/SpotBanner.html) -> banner of an advertisement
- [LiveStreamListener](documentation/de/mycrocast/android/sdk/live/listener/LiveStreamListener.html) -> interface, that contains/queues available advertisements that are ready to play
- [LiveStreamListener.Observer](documentation/de/mycrocast/android/sdk/live/listener/LiveStreamListener.Observer.html) -> interface to implement to receive updates to the addition of an SpotPlay to the internal queue

## SpotPlay
The SpotPlay class is the representation of an advertisement for the mycrocast Android SDK.

``` java
/**  
 * Received when want to play an advertisement and 
 * providing all information required to do so 
 */
public interface SpotPlay {  
  
    /**  
     * @return the remote url from which to play the audio spot from  
     */    
     String getAudioUrl();  
  
    /**  
     * @return the duration of the spot in seconds  
     */    
     int getDuration();  
  
    /**  
     * This will return null, if no banner was configured for this advertisement in mycrocast-Studio.     
     *     
     * @return get the configured banner  
     */    
     @Nullable  
    SpotBanner getBanner();  
}
```

An SpotPlay instance can have a valid SpotBanner, but it is also possible that it does not have one, then the getBanner()-method will return null.

``` java
/**  
 * The configured spot banner containing an image, 
 * a target website to be moved to when clicked 
 * and a description to be shown with the banner 
 */
public interface SpotBanner {  
  
	/**  
	* The image is quadratic 
	* @return the remote url to load the banner image from  
	*/
	String getImageUrl();  

	/**  
	* * @return the url to navigate to when the user clicks the banner  
	*/
	String getTargetUrl();  

	/**  
	* * @return description of the advertisement to be shown with the banner  
	*/
	String getDescription();  
}
``` 

## Playing an SpotPlay

Your application is responsible for the playing of an SpotPlay, as well as the showing of the optional SpotBanner.

An SpotPlay can only be received from the LiveStreamListener, because the SDK will only receive advertisements from the mycrocast backend-server only if a user is listening to a LiveStream.

The SDK will only store the SpotPlays in a internal queue.
Your application is responsible for getting them from the queue as well as playing them, if you want to.

If you want to play an SpotPlay you can get the next one in the queue via the LiveStreamListener. If there is currently nothing queued, you will receive an empty Optional. Alternatively you can check if at least one SpotPlay is currently queued

``` java
public interface LiveStreamListener {  
  
	//...

	/**  
	*
	* @return true if more advertisements to play are queued up  
	*/
	boolean hasMoreAdvertisementPlays();  

	/**  
	*
	* @return the next advertisement available or an empty optional  
	*/
	Optional<SpotPlay> getNextAdvertisementPlay();  
}
```

As soon as the internal queue of SpotPlays changes you will be notified via your implemented LiveStreamListener.Observer.

``` java
public interface LiveStreamListener {  
  
	interface Observer {  
	
		//...
	
		void onAdvertisementPlayQueued();  
	}
	
	//...
}
```

An example can be found in the mycrocast SDK example application of Android.