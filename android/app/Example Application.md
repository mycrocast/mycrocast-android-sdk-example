# Example Application

The example project implements a simple app demonstrating the different aspects of the mycrocast sdk.

This should only be used as a starting point to see how to use it and enhance on it, especially in the visual elements as you see fit.

## Activities

- [LiveStreamListActivity](LiveStreamListActivity.html) - the starting activity, here you see all currently available LiveStreams, visualised with a RecyclerView. You can start (or pause) the listening to a LiveStream via the PlayButton right here or move to the details view of a single LiveStream via the card.

- [LiveStreamDetailActivity](LiveStreamDetailActivity.html) - activity for a detailed view of a single LiveStream. You can start (or pause) the listening to a LiveStream via the PlayButton right here, rate the LiveStream or move to the chat of that LiveStream. It shows the additional information of the LiveStream provided by the streamer.

- [ListenerChatActivity](ListenerChatActivity.html) - activity for displaying the chat for a certain LiveStream. Only during the LiveStream the chat is present, but can also be disabled by the streamer.

- [AdvertisementPlayActivity](AdvertisementPlayActivity.html) - activity for displaying an playing an AdvertisementPlay and showing the corresponding AdvertisementBanner.

## Services

- [LiveStreamListenerService](LiveStreamListenerService.html) - this foreground service is used to play the actual LiveStream and reacts to events like the end of the stream.

