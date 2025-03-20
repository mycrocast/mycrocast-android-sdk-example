# iOS Example App - StreamerView

This view represents the details view for a single live stream.

You can see here the number of listeners, likes and dislikes and also rate the stream yourself (if you are listening to it).

In addition it displays different view elements if it is a LiveStream or a LiveScoringStream. 

You can move to the chat of this stream from here. And you see the status of the chat here (are you joined ? etc.)

You can see, in case you are listening the maximum available delay as well as the current configured delay and can add/remove delay.

We are listening to live stream changes to move away from this view in case the streamer stops exactly this stream.



````swift
import Foundation
import UIKit
import MycrocastSDK

/**
 Example view of a streamer
 that could be extended to also include a stop/play button
 currently you see the header of the club, the name of the streamer,
 you can rate and see the number of listeners

 In this screen we subscribe to updates for the stream, update the view if the stream is updated
 and navigate back when the stream has ended.
 We can navigate to the chat for this stream from this view

 We show the current status of the chat (joined, and if enabled or disabled) we can only now the status of the chat after
 we joined. This is more for the developer intended and should probably not be displayed in any real app
 */
class StreamerView: UIViewController, StreamsDelegate, ChatDelegate {

    private let header: UIImageView = UIImageView()
    private let streamerName: UILabel = UILabel()
    private let listener: UIButton = UIButton()
    private let listenerCount: UILabel = UILabel()

    private let likeButton: UIButton = UIButton()
    private let likeCount: UILabel = UILabel()

    private let dislikeButton: UIButton = UIButton()
    private let dislikeCount: UILabel = UILabel()

    private let chatContainer: UIView = UIView()
    private let chatText: UILabel = UILabel()
    private let chatStatus: UILabel = UILabel()
    private let chatOpened: UILabel = UILabel()

    private var streamDescription: DescriptionContaining?

    private var liveStream: LiveStream;

    init(liveStream: LiveStream) {
        self.liveStream = liveStream
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }


    override func viewDidLoad() {
        super.viewDidLoad()
        self.createViews()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

        AppDelegate.rootViewController = self

        // subscribe to receive updates for streams
        Mycrocast.shared.streams.addObserver(self)

        // we could have missed some updates as we unsubscribe on viewDidDisappear, therefore we
        // fetch the latest version from the SDK
        let stream = Mycrocast.shared.streams.getStream(streamId: self.liveStream.id)
        if let stream = stream {
            self.liveStream = stream
            self.updateView(stream: self.liveStream)

            if (Mycrocast.shared.chat.chatJoined(self.liveStream.id)) {
                self.chatOpened.text = "true"
                self.chatOpened.textColor = .green
                self.chatStatus.text = Mycrocast.shared.chat.getChatStatus(self.liveStream.id) == .enabled ? "enabled" :
                        "disabled"
                self.chatStatus.textColor = Mycrocast.shared.chat.getChatStatus(self.liveStream.id) == .enabled ? .green :
                        .red
            } else {
                self.chatOpened.text = "false"
                self.chatStatus.text =  "unknown"
                self.chatOpened.textColor = .red
            }
            return
        }

        // the stream is no longer available therefore we leave this view
        self.onStreamRemoved(self.liveStream)
    }

    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)

        // clean unsubscribe to not receive updates anymore when the view disappears
        Mycrocast.shared.streams.removeObserver(self)
    }

    private func createViews() {
      // removed for brevity sake
      // just the setup of the different views
    }

    /**
      A user hit the like button, therefore we send an update to the server
      The update from the server with the new numbers does not come directly but with the next update cycle
      therefore we adjust the numbers internally until the update arrives to show the user immediate feedback
     -Parameter gestureRecognizer:
     */
    @objc private func likePressed(_ gestureRecognizer: UITapGestureRecognizer) {
        let ratingError = Mycrocast.shared.rating.like(streamId: self.liveStream.id)
        if let error = ratingError {
            print("rating failed")
            print(error)
        }
    }

    /**
     The user selected the dislike
     The update from the server with the new numbers does not come directly but with the next update cycle
     therefore we adjust the numbers internally until the update arrives to show the user immediate feedback
     - Parameter gestureRecognizer:
     */
    @objc private func dislikePressed(_ gestureRecognizer: UITapGestureRecognizer) {
        let ratingError = Mycrocast.shared.rating.dislike(streamId: self.liveStream.id)
        if let error = ratingError {
            print("rating failed")
            print(error)
        }
    }

        /**
     Callback executed when the delay button is clicked, which will add 1s of delay.
     Moving further away from the live moment
     - Parameter sender:
     */
    @objc private func onDelayAdded(_ sender: UIButton) {
        let currentDelay = Mycrocast.shared.sessionControl.currentDelay() + 1000
        Mycrocast.shared.sessionControl.setDelay(delay: currentDelay)
        self.currentConfiguredDelay.text = String(currentDelay) + " ms"
    }


    /**
     Callback executed when the delay removal button is clicked, this will remove a second delay moving closer to the live moment
     - Parameter sender:
     */
    @objc private func onDelayRemoved(_ sender: UIButton) {
        let currentDelay = Mycrocast.shared.sessionControl.currentDelay()
        Mycrocast.shared.sessionControl.setDelay(delay: currentDelay - 1000)
        self.currentConfiguredDelay.text = String(currentDelay - 1000) + " ms"
    }

    /**
     Move to the chat of this stream
     - Parameter gestureRecognizer:
     */
    @objc private func chatClicked(_ gestureRecognizer: UITapGestureRecognizer) {
        let chatController = ChatController(self.liveStream.id)
        self.navigationController?.pushViewController(chatController, animated: true)
    }

    private func updateView(stream: LiveStream) {
        DispatchQueue.main.async {
            self.streamDescription?.update(stream: stream)

            self.listenerCount.text = String(stream.listeners)
            self.likeCount.text = String(stream.likes)
            self.dislikeCount.text = String(stream.dislikes)

            self.likeButton.tintColor = .white
            self.dislikeButton.tintColor = .white

            if (stream.myStreamRating == UserStreamRating.negative) {
                self.dislikeButton.tintColor = .blue
            }

            if (stream.myStreamRating == UserStreamRating.positive) {
                self.likeButton.tintColor = .blue
            }
        }
    }

    func onStreamAdded(_ stream: LiveStream) {
        // not of interest here
        // but could show an information about that a new stream is available
    }

    func onStreamUpdated(_ stream: LiveStream) {
        // has our current stream been updated?
        if (stream.id == self.liveStream.id) {
            self.liveStream = stream
            self.updateView(stream: stream)
        }
    }

    func onStreamRemoved(_ stream: LiveStream) {
        // has our stream been removed -> the streamer stopped
        // therefore we could move back or display something meaningful
        // we currently just leave the view

        if (stream.id == self.liveStream.id) {
            DispatchQueue.main.async {
                self.navigationController?.popViewController(animated: true)
            }
        }
    }

    func onMessage(_ message: Message) {
        // not of interest here
    }

    /**
     Update the state of the chatroom if the current stream is the stream for which the update occurred
     - Parameters:
       - streamId: the if of the stream from where the chatroom changed itself
       - status: the new status of the chatroom
     */
    func onChatStatusChanged(_ streamId: Int, status: ChatStatus) {
        if (self.liveStream.id == streamId) {
            DispatchQueue.main.async {
                self.chatStatus.text = status == .enabled ? "enabled" : "disabled"
            }
        }
    }
        /**
     Receiver of the event that the maximum available delay has changed.
     - Parameter delay: The new maximum
     */
    func onChangedTotalDelay(delay: Int) {
        DispatchQueue.main.async {
            self.totalAvailableDelay.text = String(delay) + " s"
        }
    }
}

````

### StreamType

To determine if it is a general or scoring stream we check if we can cast it and load the specific subview

```swift
  if (self.liveStream as? LiveScoringStream) != nil {
            let scoringView = ScoringView()
            self.streamDescription = scoringView
            scoringView.translatesAutoresizingMaskIntoConstraints = false;
            lightWrapper.addSubview(scoringView)

            scoringView.topAnchor.constraint(equalTo: dislikeWrapper.bottomAnchor, constant: 10).isActive = true
            scoringView.leadingAnchor.constraint(equalTo: lightWrapper.leadingAnchor, constant: 10).isActive = true
            scoringView.trailingAnchor.constraint(equalTo: lightWrapper.trailingAnchor, constant: -10).isActive = true

            topView = scoringView

        } else {
            let description = GeneralDescription(10)
            self.streamDescription = description
            description.translatesAutoresizingMaskIntoConstraints = false;
            lightWrapper.addSubview(description)

            description.backgroundColor = Colors.darkBackground
            description.topAnchor.constraint(equalTo: dislikeWrapper.bottomAnchor, constant: 10).isActive = true
            description.leadingAnchor.constraint(equalTo: lightWrapper.leadingAnchor, constant: 10).isActive = true
            description.trailingAnchor.constraint(equalTo: lightWrapper.trailingAnchor, constant: -10).isActive = true

            topView = description
        }
```

#### GeneralDescription

This view only contains a title and a short description as provided by the streamer

````swift
import MycrocastSDK

/**
 This view represents the details view of a general stream, meaning that
 during the stream creation the streamer did not select a scoring stream

 This view consist of the from the streamer provided title and description
 */
class GeneralDescription: UIView, DescriptionContaining {
    private let title: UILabel = UILabel()
    private let streamDescription: UILabel = UILabel()

    init(_ cornerRadius: Int) {
        super.init(frame: .zero)
        self.createViews(cornerRadius)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func createViews(_ cornerRadius: Int) {

        self.layer.cornerRadius = CGFloat(cornerRadius)
        self.backgroundColor = Colors.darkBackground

        let descriptionLabel = UILabel()
        descriptionLabel.translatesAutoresizingMaskIntoConstraints = false
        descriptionLabel.textColor = .gray
        descriptionLabel.font = UIFont.systemFont(ofSize: 12, weight: .light)
        descriptionLabel.text = "Description"

        self.addSubview(descriptionLabel)
        descriptionLabel.leadingAnchor.constraint(equalTo: self.leadingAnchor, constant: 5).isActive = true
        descriptionLabel.topAnchor.constraint(equalTo: self.topAnchor, constant: 5).isActive = true

        self.title.translatesAutoresizingMaskIntoConstraints = false
        self.title.textColor = .white

        self.addSubview(self.title)
        self.title.text = "title"
        self.title.topAnchor.constraint(equalTo: descriptionLabel.bottomAnchor, constant: 15).isActive = true
        self.title.leadingAnchor.constraint(equalTo: descriptionLabel.leadingAnchor).isActive = true
        self.title.trailingAnchor.constraint(equalTo: descriptionLabel.trailingAnchor).isActive = true

        self.streamDescription.translatesAutoresizingMaskIntoConstraints = false
        self.streamDescription.textColor = .white
        self.streamDescription.numberOfLines = 0
        self.streamDescription.lineBreakMode = .byWordWrapping

        self.addSubview(self.streamDescription)
        self.streamDescription.text = "description"
        self.streamDescription.topAnchor.constraint(equalTo: self.title.bottomAnchor, constant: 5).isActive = true
        self.streamDescription.leadingAnchor.constraint(equalTo: descriptionLabel.leadingAnchor).isActive = true
        self.streamDescription.trailingAnchor.constraint(equalTo: descriptionLabel.trailingAnchor).isActive = true
        self.streamDescription.bottomAnchor.constraint(equalTo: self.bottomAnchor).isActive = true
    }

    func update(stream: LiveStream) {
        self.title.text = stream.title
        self.streamDescription.text = stream.description
    }
}
````



#### Scoring

This view contains 2 teams that are playing against each other as well as the scoring for each of those teams as configured by the streamer

````swift
import MycrocastSDK

/**
 This view represents the details view of a stream where the streamer selected the scoring mode
 In this mode the streamer provides 2 team names and can adjust the score for both teams at any given time

 This example view display both team names and the score in addition to the title and description provided by the streamer
 */
class ScoringView: UIView, DescriptionContaining {
    private let homeTeamName: UILabel = UILabel()
    private let homeTeamScore: UILabel = UILabel()

    private let guestTeamName: UILabel = UILabel()
    private let guestTeamScore: UILabel = UILabel()

    private let generalDescription: GeneralDescription = GeneralDescription(0)

    init() {
        super.init(frame: .zero)
        self.createViews()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func createViews() {

        let scoringWrapper = UIView()
        scoringWrapper.translatesAutoresizingMaskIntoConstraints = false
        self.addSubview(scoringWrapper)

        scoringWrapper.trailingAnchor.constraint(equalTo: self.trailingAnchor).isActive = true
        scoringWrapper.topAnchor.constraint(equalTo: self.topAnchor).isActive = true
        scoringWrapper.leadingAnchor.constraint(equalTo: self.leadingAnchor).isActive = true

        let homeWrapper = UIView()
        homeWrapper.translatesAutoresizingMaskIntoConstraints = false
        scoringWrapper.addSubview(homeWrapper)

        homeWrapper.topAnchor.constraint(equalTo: scoringWrapper.topAnchor).isActive = true
        homeWrapper.bottomAnchor.constraint(greaterThanOrEqualTo: scoringWrapper.bottomAnchor, constant: 5).isActive = true
        homeWrapper.leadingAnchor.constraint(equalTo: scoringWrapper.leadingAnchor, constant: 5).isActive = true
        homeWrapper.widthAnchor.constraint(equalTo: scoringWrapper.widthAnchor, multiplier: 0.4).isActive = true

        self.homeTeamName.translatesAutoresizingMaskIntoConstraints = false
        self.homeTeamName.textColor = .yellow
        self.homeTeamName.textAlignment = .center
        self.homeTeamName.text = "home team"

        homeWrapper.backgroundColor = .brown
        homeWrapper.addSubview(self.homeTeamName)
        self.homeTeamName.topAnchor.constraint(equalTo: homeWrapper.topAnchor, constant: 5).isActive = true
        self.homeTeamName.leadingAnchor.constraint(equalTo: homeWrapper.leadingAnchor).isActive = true
        self.homeTeamName.trailingAnchor.constraint(equalTo: homeWrapper.trailingAnchor).isActive = true

        self.homeTeamScore.translatesAutoresizingMaskIntoConstraints = false
        self.homeTeamScore.textColor = .white
        self.homeTeamScore.textAlignment = .center
        self.homeTeamScore.text = "0"

        homeWrapper.addSubview(self.homeTeamScore)
        self.homeTeamScore.topAnchor.constraint(equalTo: self.homeTeamName.bottomAnchor, constant: 5).isActive = true
        self.homeTeamScore.bottomAnchor.constraint(greaterThanOrEqualTo: scoringWrapper.bottomAnchor, constant: 0).isActive = true
        self.homeTeamScore.leadingAnchor.constraint(equalTo: homeWrapper.leadingAnchor).isActive = true
        self.homeTeamScore.trailingAnchor.constraint(equalTo: homeWrapper.trailingAnchor).isActive = true

        let guestWrapper = UIView()
        guestWrapper.translatesAutoresizingMaskIntoConstraints = false
        scoringWrapper.addSubview(guestWrapper)

        guestWrapper.topAnchor.constraint(equalTo: scoringWrapper.topAnchor).isActive = true
        guestWrapper.bottomAnchor.constraint(greaterThanOrEqualTo: scoringWrapper.bottomAnchor, constant: 5).isActive = true
        guestWrapper.trailingAnchor.constraint(equalTo: scoringWrapper.trailingAnchor, constant: 5).isActive = true
        guestWrapper.widthAnchor.constraint(equalTo: scoringWrapper.widthAnchor, multiplier: 0.4).isActive = true

        self.guestTeamName.translatesAutoresizingMaskIntoConstraints = false
        self.guestTeamName.textColor = .white
        self.guestTeamName.textAlignment = .center
        self.guestTeamName.text = "guest team"

        guestWrapper.addSubview(self.guestTeamName)
        self.guestTeamName.topAnchor.constraint(equalTo: guestWrapper.topAnchor, constant: 5).isActive = true
        self.guestTeamName.leadingAnchor.constraint(equalTo: guestWrapper.leadingAnchor).isActive = true
        self.guestTeamName.trailingAnchor.constraint(equalTo: guestWrapper.trailingAnchor).isActive = true

        self.guestTeamScore.translatesAutoresizingMaskIntoConstraints = false
        self.guestTeamScore.textColor = .white
        self.guestTeamScore.textAlignment = .center
        self.guestTeamScore.text = "0"

        guestWrapper.addSubview(self.guestTeamScore)
        self.guestTeamScore.topAnchor.constraint(equalTo: self.guestTeamName.bottomAnchor, constant: 5).isActive = true
        self.guestTeamScore.bottomAnchor.constraint(equalTo: scoringWrapper.bottomAnchor).isActive = true
        self.guestTeamScore.leadingAnchor.constraint(equalTo: guestWrapper.leadingAnchor).isActive = true
        self.guestTeamScore.trailingAnchor.constraint(equalTo: guestWrapper.trailingAnchor).isActive = true

        let dividerWrapper = UIView()
        dividerWrapper.translatesAutoresizingMaskIntoConstraints = false

        scoringWrapper.addSubview(dividerWrapper)
        dividerWrapper.topAnchor.constraint(equalTo: homeWrapper.topAnchor).isActive = true
        dividerWrapper.leadingAnchor.constraint(equalTo: homeWrapper.trailingAnchor).isActive = true
        dividerWrapper.trailingAnchor.constraint(equalTo: guestWrapper.leadingAnchor).isActive = true
        dividerWrapper.bottomAnchor.constraint(equalTo: scoringWrapper.bottomAnchor, constant: 5).isActive = true
        scoringWrapper.backgroundColor = .purple

        let divider = UILabel()
        divider.text = "-"
        divider.textColor = .white
        divider.font = UIFont.systemFont(ofSize: 40)
        divider.translatesAutoresizingMaskIntoConstraints = false

        dividerWrapper.addSubview(divider)
        divider.centerXAnchor.constraint(equalTo: dividerWrapper.centerXAnchor).isActive = true
        divider.centerYAnchor.constraint(equalTo: dividerWrapper.centerYAnchor).isActive = true

        self.generalDescription.translatesAutoresizingMaskIntoConstraints = false
        self.addSubview(self.generalDescription)

        self.generalDescription.backgroundColor = .black
        self.generalDescription.leadingAnchor.constraint(equalTo: scoringWrapper.leadingAnchor).isActive = true
        self.generalDescription.trailingAnchor.constraint(equalTo: scoringWrapper.trailingAnchor).isActive = true
        self.generalDescription.topAnchor.constraint(equalTo: scoringWrapper.bottomAnchor, constant: 5).isActive = true
        self.generalDescription.bottomAnchor.constraint(equalTo: self.bottomAnchor).isActive = true
    }

    func update(stream: LiveStream) {
        if let scoring = stream as? LiveScoringStream {
            self.homeTeamScore.text = String(scoring.homeTeam.score)
            self.homeTeamName.text = scoring.homeTeam.name

            self.guestTeamName.text = scoring.guestTeam.name
            self.guestTeamScore.text = String(scoring.guestTeam.score)

            self.generalDescription.update(stream: stream)
        }
    }
}
````

