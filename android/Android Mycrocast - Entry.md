# Android Mycrocast - Entry 

The class Mycrocast, that only contains static methods, is your entry point into anything related to the Android mycrocast SDK.

It provides:
- initialisation of the SDK, including configuration of the maximum audio delay while playing a livestream
- access to the [LiveStreamContainer](documentation/de/mycrocast/android/sdk/live/container/LiveStreamContainer.html)
- access to the [LiveStreamRefresher](documentation/de/mycrocast/android/sdk/live/refresh/LiveStreamRefresher.html)
- access to the [LiveStreamRater](documentation/de/mycrocast/android/sdk/live/rating/LiveStreamRater.html)
- access to the [LiveStreamListenerState](documentation/de/mycrocast/android/sdk/live/listener/state/LiveStreamListenerState.html)
- access to the [LiveStreamListenerFactory](documentation/de/mycrocast/android/sdk/live/listener/LiveStreamListenerFactory.html)
- access to the [Chat](documentation/de/mycrocast/android/sdk/chat/Chat.html)
- access to the [ErrorReceiving](documentation/de/mycrocast/android/sdk/error/ErrorReceiving.html)
- termination of the SDK

``` java
/**  
 * Entry point to access the different parts of the android sdk. * The first step before anything else is done is to initialize the sdk with your customer credentials */public final class Mycrocast {  
  
    private static final SDKState state = new MycrocastSDKState();  
  
    /**  
     * Initialise the sdk with your customer information     
     *     
     * @param apiKey        - your api key  
     * @param customerToken - your customer token  
     * @param preferences   - android preferences  
     */    
     public static void initialize(String apiKey, String customerToken, SharedPreferences preferences) {  
        state.onInitialize(apiKey, customerToken, new SharedPreferenceStorage(preferences));  
    }  
  
    /**  
     * Initialise the sdk with your customer information     
     *     
     * @param apiKey        - your api key  
     * @param customerToken - your customer token  
     * @param preferences   - android preferences  
     * @param maxDelayInSeconds - maximum delay while playing a livestream in seconds  
     */    
     public static void initialize(String apiKey, String customerToken, SharedPreferences preferences, int maxDelayInSeconds) {  
        state.onInitialize(apiKey, customerToken, new SharedPreferenceStorage(preferences), maxDelayInSeconds);  
    }  
  
    /**  
     * @return the LiveStreamContainer to interact with the live streams  
     */    
    @NonNull  
    public static LiveStreamContainer getLiveStreamContainer() {  
        return state.getLiveStreamContainer();  
    }  
  
    /**  
     * Can only be null, if you did not initialize the sdk before accessing this.     
     *     
     * @return the live stream refresher  
     */    
    @Nullable  
    public static LiveStreamRefresher getLiveStreamRefresher() {  
        return state.getLiveStreamRefresher();  
    }  
  
    /**  
     * @return the live stream rater  
     */    
    @NonNull  
    public static LiveStreamRater getLiveStreamRater() {  
        return state.getLiveStreamRater();  
    }  
  
    /**  
     * @return the live stream listener state  
     */    
    @NonNull  
    public static LiveStreamListenerState getListenerState() {  
        return state.getListenerState();  
    }  
  
    /**  
     * Getter for the interaction for anything related to the chat     
     *     
     * @return - the chat interface  
     */    
    @NonNull  
    public static Chat getChat() {  
        return state.getChat();  
    }  
  
    /**  
     * @return the interface, that can be used for receiving error message provided by the sdk  
     */    
    @NonNull  
    public static ErrorReceiving getErrorReceiving() {  
        return state.getErrorReceiving();  
    }  
  
    /**  
     * Can only be null, if you did not initialize the sdk before accessing  this.     
     *     
     * @return the factory, that can be used for creating a LiveStreamListener for a specific livestream.  
     */    
    @Nullable  
    public static LiveStreamListenerFactory getLiveStreamListenerFactory() {  
        return state.getLiveStreamListenerFactory();  
    }  
  
    /**  
     * Terminates the sdk.     
     */    
    public static void terminate() {  
        state.onTerminate();  
    }  
}
```