# iOS Connecting to a live stream

This document describes everything related to the connection to a live stream and the workflows behind it.

First step should be to add the permission in your .plist file to allow background play otherwise the stream will be killed when you lock the screen or go in the background with the app.

## Relevant classes

- [SessionControl](docs/Protocols/SessionControl.html) - the protocol to interact with playing audio and controlling the play state
- [StreamSessionDelegate](docs/Protocols/StreamSessionDelegate.html) - protocol to implement to receive the actual audio data and other audio session related events
- [SessionState](docs/Enums/SessionState.html) - enum describing the different states the session is in

## Concepts

The complete control of the audio session is done through the SessionControl

When the user stops the app (swiping it out for example) you need to call stop on the sessionControl!.

### Receiving Audio

The first step should be the creation of a class that conforms to the protocol of StreamSessionDelegate. 

```swift
**
 Implement this protocol to receive stream session related updates
 */
public protocol StreamSessionDelegate {
    /**
       A change in the session state occurred
     - Parameter state: the new state
     */
    func onSessionStateUpdate(_ state: SessionState)
    /**
     A new audio package of data is available to be played
     - Parameters:
       - data: the new data package
       - duration: the duration of audio stored in the package
     */
    func onAudioDataAvailable(_ data: AVAudioPCMBuffer, _ duration: Int)
    /**
     The streamer muted himself
     - Parameter stream: the stream where this change occurred
     */
    func streamerMuted(_ stream: LiveStream)
    /**
     The streamer unmuted himself
     - Parameter stream: the stream where this happened
     */
    func streamerUnMuted(_ stream: LiveStream)

     /**
     - We have a change in the internally stored audio data and therefore a change in the total amount of delay (in ms) available
     - Parameter delay the maximum available delay currently in ms
     */
    func onAvailableDelayChanged(_ delay: Int)
}
```

The next step is to register this created class as an observer in the SessionControl to actually receive session state updates and also the audio itself.

```swift
 Mycrocast.shared.sessionControl.addObserver(streamDelegate: self)
```

You will now receive audio updates as an AVAudioPCMBuffer that contains 960 frames, at 48000khz pcm16 mono data.

You are responsible for playing the audio though your preferred way yourself.

In the example app the StreamPlayer is responsible for playing the audio packages.

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
1. You configure the maximum delay that is internally stored (how much audio we internally store) during connection. 5 minutes is a good value
2. As soon as the stream is connected, we collect the audio packages and update you how much we currently have collected. (see onAvailableDelayChanged from StreamSessionDelegate)
3. If enough is collected, the user can configure a delay up to the initally configured maximum
4. Any event that occurs is also processed delayed (playing of advertisement, changing the mute state and so on)
5. If the user moves the delay and "jumps" over an event, the event is executed (when moving to the live moment) or undone (when moving in the past)

When the user pauses/stops the stream and resumes, collection of audio needs to start again.