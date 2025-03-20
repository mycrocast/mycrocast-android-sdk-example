# mycrocast SDK example implementation

The provided example implements every functionality of the current mycrocast SDK with some example views.

## Visual elements

- ViewController - Main Controller starting the SDK and showing the live streams
  - LiveStreamCell - Displaying visual information about a single stream/ providing navigation to details and control play/pause
- StreamerView - Displays the details of a stream, provides the possibility to rate the stream, shows the chat status of the stream and move to the chat view
  - General Description - Details view for the StreamerView in case the stream is of type LiveStream
  - ScoringView- Details view for the StreamerView in case the stream is of type ScoringLiveStream
- ChatController - Controller displaying the current chat for the stream, provides the possibility to send chat messages and also to report a chat user
  - ChatCell - A single chat message, on click opens a context menu to provide reporting functionality
  - ChatReport - View to enter additional information for the report
- AdBannerView - Overlay view shown during the play of an advertisement displaying the banner of the ad, the title and provide a button to open the link of the ad 

## Audio Helper

- AdvertisementPlayer - in case an advertisement was received this player is responsible for playing the audio spot
- MuteMusicAudioPlayer - in case the current stream is muted, this player is looping the mute music
- StreamPlayer - this player is responsible for playing the audio packages received from the SDK
- AudioSession - This class is responsible to receive the events from the mycrocast SDK and forward them to the corresponding players above, for example when an ad start call the AdvertisementPlayer and mute the StreamPlayer, when the ad is finished unmute the StreamPlayer again. It interacts with the MycrocastSDK to start and stop streams

## State

- AppState - Singleton to hold the AudioState and also receives MycrocastErrors from the SDK which could be processed further if required
- AudioState - Provides the play and stop functionality to create an AudioSession, also reacts on stream changes and if the current stream is removed, cleans up
- AdPlayManager - calls the advertisementPlayer to start playing an advertisement and also displays the AdBannerView



 

