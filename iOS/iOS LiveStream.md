# iOS - LiveStream

This document describes the concepts of livestreams and how to receive and update them in the iOS SDK

Usage of Livestreams can be seen in the example app in the ViewController and StreamerView

## Relevant classes

- [LiveStream](docs/Classes/LiveStream) - this class represents a single general live stream
- [LiveScoringStream](docs/Classes/LiveScoringStream) - extension of the LiveStream that has the additional information about the teams that are playing and the current score
- [Streams](docs/Protocols/Streams) - protocol to interact with to get the streams, register/unregister observer 
- [StreamsDelegate](docs/Protocols/StreamsDelegate) - protocol to implement to receive updates for streams and to be registered as observer in the Streams protocol
- [StreamLanguage](docs/Structs/StreamLanguage.html) - the language the streamer selected during stream creation
- [UserStreamRating](docs/Enums/UserStreamRating.html) - enum indicating the current rating for the stream from the user
- [Streamer](docs/Classes/Streamer) - the streamer of the livestream
- [Team](docs/Structs/Team) - Part of the LiveScoringStream representing a team within a match with a name and a scoring

## Concepts

Receiving the streams and also registering for update/adding/removing is done through the Streams protocol.

### Receiving LiveStream Updates

To receive updates the first step is to create a class that conforms to the StreamsDelegate protocol

```Swift
/**
 Protocol to implement to receive updates for LiveStreams
 This needs to be registered in the Streams protocol to be called
 */
public protocol StreamsDelegate {
    /**
     A new streamer from your account went online
     - parameters:
      - stream: The new stream that has been added
    */
    func onStreamAdded(_ stream: LiveStream)

    /**
     Received an update for current stream, like listener count change ...
     - parameters:
        - stream: The updated stream
     */
    func onStreamUpdated(_ stream: LiveStream)

    /**
     A streamer went offline
     - Parameter stream: the stream that went offline
     */
    func onStreamRemoved(_ stream: LiveStream)
}
```

The next step is to register this class with the Streams protocol to be called when an update is received internally

```swift
Mycrocast.shared.streams.addObserver(self)
```

Not every interaction triggers a direct change. For example changes in the listener count are only propagated in fixed time intervals to reduce the load on the server. Same is true for rating a stream

### Getting the current LiveStreams

There are 2 functions to receive the LiveStreams in the Streams protocol

```swift
public protocol Streams {
  // other functions are removed for brevity
    /**
    Get the currently available streams

    - returns: List of currently available live streams
    */
    func getStreams() -> [LiveStream]
    
    /**
     Request the currently live streams again from the server from your club
     This should not be used frequently, because normally calling play will return the current
     Streams and everything else is provided by update.
     In case of a short connection loss, of when the app was idle for a while this makes sense
     - Parameter callback: Function that is executed with the result of the request to the server
     */
    func requestStreams(_ callback: @escaping ([LiveStream]) -> ())
}
```

The livestream updates are also stored internally in the SDK therefore it is most of the time sufficient to call getStreams() which just returns the internal state of the SDK. This can only occur as long as an internet connection is present.

When the connection was lost, or the app was in the background for a longer time it is better to use requestStreams to get the fresh state directly from the server.

## Rating a stream

Users have the possibility to rate a stream. Providing a like or a dislike. If the user already provided a rating, sending the same rating will reset the rating to a neutral state. To provide better feedback for the streamer, a rating can only be applied if the user has started listening to the stream.

### Relevant classes

- Rating - Protocol to provide the rating functionality
- RatingError - Error enum in case the rating failed.

````swift
public protocol Rating {
    /**
     Execute the like action for the stream identified by the provided id.
     A stream can only be rated after we at least listened to it once
     Liking a stream that we already have liked resets the rating to neutral

     - parameters:
        - streamId: The id of the stream we want to rate

     - returns: an optional RatingError if anything with the rating has not succeeded, see RatingError for more details
    */
    func like(streamId: Int) -> RatingError?

    /**
     Execute the dislike action for the stream identified by the provided id.
     A stream can only be rated after we at least listened to it once
     Liking a stream that we already have disliked resets the rating to neutral

     - parameters:
        - streamId: The id of the stream we want to rate

     - returns: an optional RatingError if anything with the rating has not succeeded, see RatingError for more details
     */
    func dislike(streamId: Int) -> RatingError?
}
````







