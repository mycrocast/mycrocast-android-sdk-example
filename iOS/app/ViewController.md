## ViewController

This is the starting controller for this app and also start the initialization of the SDK as well as displaying each current live stream as an visual element within a stackview.

In addition to that is also subscribes to change events for the streams so it can display new streams remove stopped streams and update current streams.

Each element of the stackview (a LiveStreamCell) displays the current number of listeners, the title and provides a media control as play/pause button.

````swift
import UIKit

import MycrocastSDK

/**
 The initial view of the app that starts the SDK,
 loads the currently live streams and displays them in the stackview
 */
class ViewController: UIViewController, StreamsDelegate {

    private static let YOUR_CUSTOMER_ID = "" // replace with your customerId as visible in the mycrocast studio
    private static let YOUR_API_KEY = "" // replace with your api key as visible in the mycrocast studio

    private let streamStack: UIStackView = UIStackView()
    private var cells: [Int: LiveStreamCell] = [:]

    init() {
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad();

        // start the SDK with your credentials
        Mycrocast.shared.start(ViewController.YOUR_API_KEY, customerToken: ViewController.YOUR_CUSTOMER_ID) { streams, error in
            if let error = error {
                print(error)
                return
            }
            // we received a successful response from the server without any errors
            // we now have all currently streaming streamers of the club in the streams list
            // we can also access this from the SDKs streamManager
            for stream in streams {
                self.onStreamAdded(stream)
            }
        }

        self.view.backgroundColor = .red
        self.streamStack.axis = .vertical
        self.streamStack.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(self.streamStack)

        self.streamStack.leadingAnchor.constraint(equalTo: self.view.leadingAnchor, constant: 5).isActive = true
        self.streamStack.topAnchor.constraint(equalTo: self.view.safeAreaLayoutGuide.topAnchor, constant: 10).isActive = true
        self.streamStack.trailingAnchor.constraint(equalTo: self.view.trailingAnchor, constant: -5).isActive = true

        self.view.backgroundColor = Colors.darkBackground
        navigationController?.navigationBar.barTintColor = Colors.richBlack
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

        AppDelegate.rootViewController = self

        // register to receive updates for streams
        Mycrocast.shared.streams.addObserver(self)

        self.repopulate()
    }

    override func viewDidDisappear(_ animated: Bool) {
        // Unregister to receive updates for streams
        Mycrocast.shared.streams.removeObserver(self)
    }

    /**
     We selected a cell and now move to the details view of that stream
     - Parameter stream: the stream we want to see the details of
     */
    private func onCellSelected(stream: LiveStream) {
        DispatchQueue.main.async {
            let streamerView = StreamerView(liveStream: stream)
            self.navigationController?.pushViewController(streamerView, animated: true)
        }
    }

    // this is redrawing the cells
    // normally you would instead create a diff here for what was added /removed
    // this is needed because we unsubscribe from the delegate in on viewDidDisappear
    private func repopulate() {
        while (true) {
            let first = self.cells.popFirst()
            guard let entry = first else {
                break;
            }
            entry.value.removeFromSuperview()
        }

        for stream in Mycrocast.shared.streams.getStreams() {
            self.onStreamAdded(stream)
        }
    }

    /**
     A new stream was added therefore we add it to the stack view
     To display it
     - Parameter stream: the added stream
     */
    func onStreamAdded(_ stream: LiveStream) {
        DispatchQueue.main.async {
            let cell = LiveStreamCell();
            cell.cellCallback = self.onCellSelected
            self.cells[stream.id] = cell;
            cell.updateView(stream: stream)
            self.streamStack.addArrangedSubview(cell)
        }
    }

    /**
     A stream was updated so we refresh the specific stream
     This could be due to change of rating, change of listener count etc.
     - Parameter stream:
     */
    func onStreamUpdated(_ stream: LiveStream) {
        let cell = self.cells[stream.id]
        if let cell = cell {
            DispatchQueue.main.async {
                cell.updateView(stream: stream)
            }
        }
    }

    /**
     A stream was removed this is most likely to the streamer just ending the transmission
     - Parameter stream: the stream that ended
     */
    func onStreamRemoved(_ stream: LiveStream) {
        let cell = self.cells.removeValue(forKey: stream.id)
        if let cell = cell {
            DispatchQueue.main.async {
                cell.removeFromSuperview()
            }
        }
    }
}


````



