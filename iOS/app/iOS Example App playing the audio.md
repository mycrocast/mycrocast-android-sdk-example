# iOS Example App playing the audio



The StreamPlayer class is responsible for playing the audio received from the mycrocast SDK

This is just an example implementation how you could go about [playing the audio](../iOS Connecting to a live stream.html). 

The StreamPlayer is a member variable of the AudioSession class.

We use mainly the 3 swift classes from the AVFoundation

```
private let audioEngine: AVAudioEngine
private let playerNode: AVAudioPlayerNode
private let converter: AVAudioConverter
```

Because we want to mute our live stream when an advertisement is played we also provide the functionality to set the volume to zero.

As always when wanting to play audio, we need to make sure that we have an active audio session.

````swift
/**
 This is an example class of playing the live audio stream.
 The live audio stream data is received in AVAUDIOPCMBuffer with 48000khz and 16 pcm data and 960 frames of data
 And played with an AVAudioPlayerNode and AVAudioEngine

 The StreamPlayer reacts on updates for advertisement plays and sets the volume to zero when an advertisement is
 started and afterwards resets the volume to the previous level
 */
public class StreamPlayer: AdPlayStateChangeDelegate {

    private let audioEngine: AVAudioEngine
    private let playerNode: AVAudioPlayerNode
    private let converter: AVAudioConverter

    private let inputFormat: AVAudioFormat
    private let outputFormat: AVAudioFormat

    init() {
        self.audioEngine = AVAudioEngine()
        self.playerNode = AVAudioPlayerNode()
        do {
            self.outputFormat = self.audioEngine.outputNode.outputFormat(forBus: 0)
            self.inputFormat = AVAudioFormat(commonFormat: .pcmFormatInt16, sampleRate: 48000, channels: AVAudioChannelCount(1), interleaved: false)!

            self.converter = AVAudioConverter(from: inputFormat, to: outputFormat)!
            self.converter.downmix = true
            configureAudioSession()

            self.audioEngine.attach(self.playerNode)
            self.audioEngine.connect(self.playerNode, to: self.audioEngine.outputNode, format: self.outputFormat)
            self.audioEngine.prepare()
            try self.audioEngine.start()
            self.playerNode.prepare(withFrameCount: 960)
        } catch {
            print("Player error: \(error)")
        }

        Broadcaster.register(AdPlayStateChangeDelegate.self, observer: self)
    }

    deinit {
        Broadcaster.unregister(AdPlayStateChangeDelegate.self, observer: self)
    }

    /**
     Schedule the play of a single buffer of data
     - Parameter buffer:
     */
    func play(_ buffer: AVAudioPCMBuffer) {
        let outputBuffer = AVAudioPCMBuffer(pcmFormat: self.outputFormat, frameCapacity: 960)!

        self.converter.convert(to: outputBuffer, error: nil) { inNumPackets, outStatus in
            outStatus.pointee = .haveData
            return buffer
        }

        self.playerNode.scheduleBuffer(outputBuffer)
        self.playerNode.volume = 1
        self.playerNode.play()
    }

    private func configureAudioSession() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
        }
    }

    /**
     An advertisement play started, therefore we reduce the volume to zero
     */
    func onAdPlayStarted() {
        self.playerNode.volume = 0
    }

    /**
     Advertisement finished, restore volume to hear the streamer again
     */
    func onAdPlayFinished() {
        self.playerNode.volume = 1
    }
}
````

