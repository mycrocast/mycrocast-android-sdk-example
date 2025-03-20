# iOS LiveStreamCell

This visual element represents a single live stream.

You can play/pause the stream from here and move to the details view of the stream.

For each stream a cell is created or removed again.



````swift
import Foundation
import UIKit
import MycrocastSDK

/**
 This view element represents a single live stream for a single streamer that is currently online from your club
 This is used in the overview stackview showing all currently available streams.

 Hitting the play button will start the connection process to the stream, selecting any other place will navigate to
 the details view of this stream
 */
class LiveStreamCell: UIView {

    private let mainSpacing: CGFloat = 10

    private let logo: UIImageView = UIImageView()
    private let title: UILabel = UILabel()
    private let streamDescription: UILabel = UILabel()

    private let listener: LabelWithHint = LabelWithHint(true)
    private let genre: LabelWithHint = LabelWithHint(true)
    private let language: LabelWithHint = LabelWithHint(false)

    private let playButton: PlayButton = PlayButton()
    private var stream: LiveStream?

    var cellCallback: ((LiveStream) -> ())?

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    init() {
        super.init(frame: .zero)
        self.createViews()

        self.backgroundColor = Colors.lightBackground
        self.layer.cornerRadius = 15
    }

    func createViews() {
			// content removed to keep the example smaller
      // here only the visual setup happens
    }

    /**
     Update the view after we received an update from the SDK that something changed,
     this could for example be the listener count etc.
     - Parameter stream: - the stream with the new information
     */
    func updateView(stream: LiveStream) {

        self.stream = stream
        self.title.text = stream.streamer.login
        self.streamDescription.text = stream.title

        self.listener.updateValue(value: String(stream.listeners))
        self.genre.updateValue(value: stream.genre)
        self.language.updateValue(value: stream.language.native)

        if (Mycrocast.shared.sessionControl.currentPlaying(streamId: stream.id)) {
            self.playButton.playing()
        } else {
            self.playButton.paused()
        }

        if let logo = stream.logo() {
            self.logo.downloaded(from: logo)
        }
    }

    private func onPlayUpdate(_ state: Bool) {
        if let stream = self.stream {
            if (state) {
                AppState.shared.audioState.play(stream)
                return;
            }
            AppState.shared.audioState.stop()
        }
    }

    @objc private func onCellClicked(_ gestureRecognizer: UITapGestureRecognizer) {
        if let stream = self.stream {
            self.cellCallback?(stream)
        }
    }
}

````

