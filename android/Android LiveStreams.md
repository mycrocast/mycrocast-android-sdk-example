# Android LiveStreams

As already described in the concept overview of the sdk, we have can either have a general stream (normally used to broadcast something that is not directly tied to a match, like a press conference or an "Ask me anything" ...) consisting of a title and a description or a match (scoring) that has additional fields of the 2 teams playing, which have each a name and an actual scoring, which is updated by the streamer.

## Relevant classes

- [LiveStreamContainer](documentation/de/mycrocast/android/sdk/live/container/LiveStreamContainer.html) -> Interface for getting the streams and for registering to updates for streams
- [LiveStreamContainer.Observer](documentation/de/mycrocast/android/sdk/live/container/LiveStreamContainer.Observer.html) -> Interface to implement to actually receive events regarding live streams
- [LiveStreamRefresher](documentation/de/mycrocast/android/sdk/live/refresh/LiveStreamRefresher.html) -> Interface for requesting the current streams again from the server
- [LiveStreamRefresher.Observer](documentation/de/mycrocast/android/sdk/live/refresh/LiveStreamRefresher.Observer.html) -> Interface to implement to receive updates for the refresh status
- [LiveStream](documentation/de/mycrocast/android/sdk/live/data/LiveStream.html) -> Class representing a single live stream
- [Scoring](documentation/de/mycrocast/android/sdk/live/data/Scoring.html) -> Can be part of a LiveStream and represent the match part, if the streamer started a scoring stream
- [Team](documentation/de/mycrocast/android/sdk/live/data/description/Team.html) -> Part of the scoring and represent one of the 2 teams playing
- [StreamLanguage](documentation/de/mycrocast/android/sdk/live/data/language/StreamLanguage.html) -> The language of the stream as selected by the streamer

## Concepts

### Structure of a live stream

Refer to the documentation about the LiveStream // TODO link to the documentation

Check the hasScoring to determine if the additional fields of the Scoring are present // TODO link to documentation of scoring

##### Image resources

The liveStream has an nullable field clubLogoUrl which contains the url to the configured image resource of the club (if any)

the liveStream has an nullable field clubHeaderUrl which contains the url to the configured image resource of the club (if any)

### Managing Live streams

Interactions with the retrieval and update of the LiveSreams are handled by the LiveStreamContainer and the LiveStreamRefresher.

#### Getting The LiveStreams

The SDK internally updates the states of the livestreams and therefore it is normally sufficient to request the streams from the LiveStreamContainer via the call to "getAll()"

These internal update can only occur if an active internet connection is existing, therefore if the internet connection was lost or the app was in the background for a longer period it makes sense to refresh the live streams with the help of the LiveStreamRefresher

#### Stream Changes

To be informed about stream changes, this includes adding/updating and removing streams you first need to create a class that conforms to the interface of the LiveStreamContainer.Observer

````java
    interface Observer {
        /**
         * Called when a new live stream was added.
         * This is the case when a new streamer for you club started a stream
         *
         * @param liveStream - the new live stream
         */
        void onLiveStreamAdded(LiveStream liveStream);

        /**
         * Called when a live stream is updated.
         * This could be, because the streamer changed some settings,
         * the listener count changed ...
         *
         * @param liveStream - the stream that changed
         */
        void onLiveStreamUpdated(LiveStream liveStream);

        /**
         * A stream was removed, this is usually the case when a streamer went offline.
         *
         * @param liveStream - the stream that went offline
         */
        void onLiveStreamRemoved(LiveStream liveStream);

        /**
         * Called when more than a single stream changed.
         * This is normally the case when a refresh was successfully finished
         */
        void onLiveStreamListChanged();
    }
````

The next step is to register your class with the LiveStreamContainer

````java
Mycrocast.getLiveStreamContainer().addObserver(this);
````

Afterwards your class is receiving updates for each stream change. This can be for example because the streamer updated the description, scoring changed, the number of listeners changed, a new stream was added, a stream was removed ...

