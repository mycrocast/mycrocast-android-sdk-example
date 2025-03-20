A General description about some concepts for the mycrocast SDK.

Table of contents:

[[TOC]]

# What is new

The following will describe what has changed in the different versions of the sdk.
This is a general description that should fit both android and iOS but if specifics are required, a remark is made within the version description.

## Version 1.3 (Released 01.07.2023)

You can now configure buffering as well as delay within the sdk.

### Buffering

Buffering describes the amount of seconds of audio data that are collected before the first audio package is transmitted from the sdk for playback.

Disabling buffering has the potencial to negatively affect the listener as more stops/artifacts within the audio transmission can be heard in case of chaning network conditons.
Setting the buffering to a too high value, will lead to a long wait time, until the first audio is heard. 
Something between 2s-5s could be a good measure.

### Delay

The delay functionality enables the user to somewhat sync any tv screen picture with the heard audio. Normally the audio is quite far ahead when you compare it to internet tv transmissions (sometimes more than 2 minutes).

We internally store the audio packages so that the user can jump back in time or delay the audio transmission to match the screen.
You need to configure a max delay, this is the amount of audio packages we keep internally available.

A good measure is between 3-5 minutes.
Setting it too high could lead to memory issues, as this is all kept in memory.

When a delay is configured, all events are also processed delayed. Meaning, that for example you have 30s delay configured and receive a push to play an advertisement. This push is also delayed 30s. Of any delayed events are "queued" and the user decided to reduce the delay, (basically jumping ahead in time) the events are triggered at the specific time.

It is a good idea to provide the user with some user interface, where the user can jump more than 1s (maybe buttons like 1s,5s, 30s).

### Breaking changes in Android

#### Naming and Namespace Changes

Due to consistency AdvertisementPlay was renamed to SpotPlay as well as AdvertisementBanner to SpotBanner.


### Breaking changes in IOS

#### Removal of Cocoapods

With the support of the Swift Package Manager, the Cocoapods are no longer updated. The project is not open source and only the xcframework is available. For integration into other technologies (flutter, react native etc...) is the xcframework required (if this would be open source, this would not be true). This can be directly downloaded.
If you need support of cocoapods, please get in touch with us.


#### Connecting to the stream

The play function was updated and now requires 2 additional information:
 sessionControl.play(streamId: self.stream.id, bufferDuration: 2000, maxDelay: 300000)

- bufferDuration: The amount of time in milliseconds how much audio should be buffered
- maxDelay: The maximum amount of time in milliseconds we keep audio in the memory for the delay functionality.


# General requirements

To be able to test and depending on your internal setup regarding ports, the following ports must be opend:

- 8080
- 10011
- 20022
- 8001
- 8000
- 7777

# iOS

The iOS SDK relevant sections are listed below. The iOS SDK provides an example application where each SDK functionality is implemented.

## Getting started

For a quick start refer to the [getting started](iOS/iOS Getting started.html) documentation

## iOS Example

The documentation about the [example app](iOS/app/Overview.html)

[example app repo](https://github.com/mycrocast/mycrocast-ios-sdk-example)

## iOS SDK Documentation

[Documentation](iOS/docs/index.html) about the classes and functions

# Android

The Android SDK relevant sections are listed below. The Android SDK provides an example application where each SDK functionality is implemented.

## Getting started

For a quick start refer to the [Getting Started](android/Android Getting Started.html) documentation

## Android Example

The documentation about the [example app](android/app/Example Application.html)

[example app repo](https://github.com/mycrocast/mycrocast-android-sdk-example)

## Android SDK Documentation

[Documentation](android/documentation/index.html) about the classes and functions

# SDK Concepts

Description of some concepts and workflows that are independent of the SDK platform as well as design goals for the SDK are described below

## Flexibility

The SDK is build with the goal to provide the most flexibility for the developers side to adjust and implement as required from their side.There are no provided visual elements, these need to be developed by the implementing side to fit them natively into the app.

To further increase the flexibility we only pass most of the events to the developer to react on them. This has the drawback that the development effort is a bit higher on their side but enables custom features that otherwise would not be possible.

Both the iOS and Android SDK are accompanied by an example app demonstrating each aspect of the SDK and also example implementations for visual elements and other workflow related concepts like playing the actual audio.

## Audio

The audio is provided based on the platform directly as playable audio packages or just bytes. The audio playback needs to be implemented by the developing side. The received audio is already decoded.

The audio is 48000khz 16 bit pcm data with 960 frames (1920 bytes).

## Streams

A stream represents a live transmission that was started from within the mycrocast app. There can be multiple streamers online for you club, depending on how many users were created. This could for example be the case if you want to provide coverage of a game in different languages.

### Stream types

There are in general 2 distinct types of stream that can be configured from the streamer side.

#### General Stream

This is a general stream, not necessary belonging to a game transmission. This is usually used for press conferences, interview or anything that is not a sport event.

#### Scoring/Sport Event

This kind of stream is normally used during a game as it contains the 2 teams playing and also the current scoring of the 2 teams.The streamer can update the score per team and this is also propagated through the system.

### Updates

A stream is updated in regular intervals and these updates are propagated through the SDK. Not every user starting to listen will update the listener count. Updates are provided on a short time interval.

### Mute Streams

A streamer can mute himself during the transmission (for example within the break). During this time there will be a mute music that is provided by the SDK. It just needs to be played from the developer side. The SDK only provides the remote URL to be played from. If you actually want to play the mute music is up to you

### Rating of a stream

Each user can rate a stream. Either upvote or downvote or remove a provided vote


### Buffering

Buffering describes the amount of seconds of audio data that are collected before the first audio package is transmitted from the sdk for playback.

Disabling buffering has the potencial to negatively affect the listener as more stops/artifacts within the audio transmission can be heard in case of chaning network conditons.
Setting the buffering to a too high value, will lead to a long wait time, until the first audio is heard. 
Something between 2s-5s could be a good measure.

### Delay

The delay functionality enables the user to somewhat sync any tv screen picture with the heard audio. Normally the audio is quite far ahead when you compare it to internet tv transmissions (sometimes more than 2 minutes).

We internally store the audio packages so that the user can jump back in time or delay the audio transmission to match the screen.
You need to configure a max delay, this is the amount of audio packages we keep internally available.

A good measure is between 3-5 minutes.
Setting it too high could lead to memory issues, as this is all kept in memory.

When a delay is configured, all events are also processed delayed. Meaning, that for example you have 30s delay configured and receive a push to play an advertisement. This push is also delayed 30s. Of any delayed events are "queued" and the user decided to reduce the delay, (basically jumping ahead in time) the events are triggered at the specific time.

It is a good idea to provide the user with some user interface, where the user can jump more than 1s (maybe buttons like 1s,5s, 30s).


## Advertisements

When an advertisement is available to play, an event is triggered based on the platform. The developer can decide if the ad should actually be played or can be ignored.

An advertisement can contain a short description (which service or product this ad is for), and besides the audio file an optional image to display going along with the audio.

### Playing an advertisement

The SDK only informs you that an advertisement is available to be played. The developer decides if the advertisement should actually be played or ignored. This provides maximal flexibility for the implementing side.

### PreStream Advertisements

During the connection to a live stream the SDK can respond with an advertisement if the user did not receive an ad play within a certain time span. A pre stream advertisement is treated like a normal advertisement. The system just informs you that a new advertisement is available for playing.

### InStream Advertisements

During the broadcast the streamer can decide at which points to trigger an advertisement. There are no fixed intervals at which ads are played. The streamer is in complete control of how many and what ads are going to be played.

## Chat

For each stream there exists a chatroom. A chatroom is either enabled or disabled and the status of the chatroom can change during the stream as the streamer is in control over the status of the chat.

Users of the SDK and registered mycrocast app users can participate in the chat. A user can be blocked from the chat though the streamer only. Even if the user is blocked the SDK does not prevent the user from sending a message, it will just not be displayed for other users. 

The SDK provides the information if a user is blocked but it is up to the developer to either display that information or not.

## Other

### Potential custom features

Some examples of what could be developed in addition to what the SDK provides.

#### Local Block list for a chatter

The streamer can already block chatter but it could also be that a SDK user is annoyed or does not want to see messages from certain users. A local ignore list could be implemented that filters our messages from certain users.
