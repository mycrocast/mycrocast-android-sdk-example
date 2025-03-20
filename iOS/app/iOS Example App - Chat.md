# iOS Example App - Chat

The [chat](../iOS Chat.html) is as an example implemented in the ChatController.

Is uses a UITableView and the displayed cell is the ChatCell. It provides an input to send messages as well as a button to actually send the message.

The ChatCell changes the visual placement depending on who is the sender of the message.

In case the chat is disabled, the table view is hidden with all the control elements and only a message is displayed that the chat is hidden.

Selecting a ChatCell prompts an option to report the message/user if you are not the sender yourself. 

````swift
import Foundation
import UIKit
import MycrocastSDK

/**
 This controller represent the chat for a single stream.
 It consist of an input field to enter a chat message, a button to send it and a table view containing all the
 chat messages

 If the chat was disabled by the streamer, we provide this information instead of showing the actual chat
 This class is part of the example to show how to use the Chat part of the mycrocast SDK
 */
class ChatController: UIViewController, UITableViewDelegate, UITableViewDataSource, ChatDelegate {

    private let chatTable: UITableView
    private let input: UITextField
    private let sender: UIButton
    private let chatStatus: UILabel

    private let streamId: Int
    private let chat: Chat

    private var chatMessages: [Message] = []

    init(_ streamId: Int) {
        self.chatTable = UITableView(frame: .zero, style: .plain)
        self.input = UITextField(frame: .zero)
        self.sender = UIButton(frame: .zero)
        self.chat = Mycrocast.shared.chat
        self.chatStatus = UILabel(frame: .zero)
        self.streamId = streamId

        super.init(nibName: nil, bundle: nil)
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.view.backgroundColor = Colors.lightBackground

        self.sender.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(self.sender)
        self.sender.trailingAnchor.constraint(equalTo: self.view.trailingAnchor, constant: -5).isActive = true
        self.sender.topAnchor.constraint(equalTo: self.view.safeAreaLayoutGuide.topAnchor, constant: 5).isActive = true
        self.sender.setTitle("Send", for: .normal)
        self.sender.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        self.sender.setContentHuggingPriority(.defaultHigh, for: .vertical)
        self.sender.addTarget(self, action: #selector(sendChat), for: .touchUpInside)

        self.input.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(self.input)
        self.input.centerYAnchor.constraint(equalTo: self.sender.centerYAnchor).isActive = true
        self.input.leadingAnchor.constraint(equalTo: self.view.leadingAnchor, constant: 10).isActive = true
        self.input.trailingAnchor.constraint(equalTo: self.sender.leadingAnchor, constant: -10).isActive = true
        self.input.backgroundColor = .white
        self.input.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        self.input.placeholder = "Enter chat message"
        self.input.layer.cornerRadius = 5

        self.chatTable.register(ChatCell.self, forCellReuseIdentifier: ChatCell.IDENTIFIER)
        self.chatTable.translatesAutoresizingMaskIntoConstraints = false
        self.chatTable.backgroundColor = Colors.lightBackground
        self.chatTable.estimatedRowHeight = 85
        self.chatTable.rowHeight = UITableView.automaticDimension
        self.chatTable.delegate = self
        self.chatTable.dataSource = self

        self.view.addSubview(self.chatTable)

        self.chatTable.topAnchor.constraint(equalTo: self.sender.bottomAnchor, constant: 15).isActive = true
        self.chatTable.trailingAnchor.constraint(equalTo: self.view.trailingAnchor).isActive = true
        self.chatTable.leadingAnchor.constraint(equalTo: self.view.leadingAnchor).isActive = true
        self.chatTable.bottomAnchor.constraint(equalTo: self.view.safeAreaLayoutGuide.bottomAnchor, constant: -5).isActive = true
        self.chatTable.backgroundColor = .clear

        self.chatStatus.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(self.chatStatus)

        self.chatStatus.widthAnchor.constraint(equalTo: self.view.widthAnchor, multiplier: 0.75).isActive = true
        self.chatStatus.centerXAnchor.constraint(equalTo: self.view.centerXAnchor).isActive = true
        self.chatStatus.centerYAnchor.constraint(equalTo: self.view.centerYAnchor).isActive = true

        self.chatStatus.isHidden = true
        self.chatStatus.text = "Chat is disabled by the streamer"
        self.chatStatus.textColor = .white

        self.checkIfChatJoined()
    }

    /**
       We check if we have already joined (opened) the chat previously otherwise we join the chat
       We need to be in the chat to receive updates for the chat and also we need to have joined to receive the last messages
       and the state of the chat
     */
    private func checkIfChatJoined() {
        if !self.chat.chatJoined(self.streamId) {
            self.chat.joinChat(self.streamId) { messages, chatStatus, error in
                if let error = error {

                } else {
                    self.chatMessages.append(contentsOf: messages)
                    DispatchQueue.main.async {
                        self.chatTable.reloadData()
                    }

                    self.onChatStatusChanged(self.streamId, status: chatStatus)
                }
            }
        }
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

        // We subscribe for updates to the chat so that we can display them again
        self.chat.addObserver(self)
        // we clear everything and get the internal chat messages because we could have missed
        // messages when this view was closed and we did unsubscribe from the observer
        // the internal messages are only received when we joined the chat
        self.chatMessages.removeAll()
        self.chatMessages.append(contentsOf: chat.getChatMessages(self.streamId)) // get internal messages
        DispatchQueue.main.async {
            self.chatTable.reloadData()
            // update the status of the chat in case we missed an update
            self.onChatStatusChanged(self.streamId, status: self.chat.getChatStatus(self.streamId))
        }

        AppDelegate.rootViewController = self
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)

        // unsubscribe again as this view will close
        self.chat.removeObserver(self)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    /**
      We send a chat message if the text input is not empty
      We could be blocked by the streamer, we get this information in the response to the message
      We can either display this to the user or not
     */
    @objc private func sendChat() {
        if let message = self.input.text {
            self.input.text = ""
            self.input.resignFirstResponder()
            self.chat.sendChatMessage(self.streamId, message: message) { success, error in
                // success is false in case you are blocked by the streamer
            }
        }
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.chatMessages.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: ChatCell.IDENTIFIER, for: indexPath) as! ChatCell
        let message = self.chatMessages[indexPath.row]

        cell.update(message)
        return cell
    }

    /**
     After we subscribed to the observer we receive here new chat message
     This update is called for all currently open chats, therefore we need to check if this messages belongs
     to our current stream
     - Parameter message: the new chat message
     */
    func onMessage(_ message: Message) {
        if (self.streamId != message.streamId) {
            return
        }
        self.chatMessages.append(message)
        DispatchQueue.main.async {
            self.chatTable.reloadData()
        }
    }

    /**
     After we subscribed to the observer we receive here updates for the chat status in general,
      this can be that the streamer decided to disable or enable the chat
      Because we get this update for each currently open chat, we need to check if this change is for our currently shown
      chat
     - Parameters:
       - streamId: id of the stream where the status update happened
       - status:   the new status of the chat
     */
    func onChatStatusChanged(_ streamId: Int, status: ChatStatus) {
        if (self.streamId != streamId) {
            return
        }

        DispatchQueue.main.async {
            self.chatTable.isHidden = status == .disabled
            self.input.isHidden = status == .disabled
            self.sender.isHidden = status == .disabled
            self.chatStatus.isHidden = status == .enabled
        }
    }
}
````

First we need to register ourself with the chat by confirming to the ChatDelegate protocol and adding ourself as Observer in the Chat

Second we need to determine if we have already joined the chat, if so we just get the current messages for this chat to display, otherwise we start by joining the chat.

Third we check the state of the chatroom and display or hide the control elements accordingly.

This is all done in ViewWillAppear.

#### ChatCell

````swift
import Foundation
import UIKit

import MycrocastSDK


/**
 This is a single chat cell (visual representation of a single chat message)
 Based on who has written the message we display it right or left and in a different color for the name
 of the sender.
 Selecting the cell will open a context menu where the user can report the message
 */
class ChatCell: UITableViewCell {

    public static let IDENTIFIER: String = "chat"

    private let messageContainer = UIView()
    private let message = UILabel()
    private let author = UILabel()
    private let time = UILabel()

    private var leftConstraint: NSLayoutConstraint?
    private var rightConstraint: NSLayoutConstraint?

    private var chatMessage: Message?

    override init(style: CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)

        self.createInitialLayout()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
    }

    private func createInitialLayout() {
        self.contentView.backgroundColor = Colors.lightBackground

        self.messageContainer.translatesAutoresizingMaskIntoConstraints = false
        self.messageContainer.backgroundColor = Colors.darkBackground
        self.messageContainer.layer.cornerRadius = 15
        self.messageContainer.layer.borderWidth = 1
        self.messageContainer.layer.borderColor = Colors.lightGrey.cgColor

        self.contentView.addSubview(messageContainer)

        self.message.translatesAutoresizingMaskIntoConstraints = false
        self.message.numberOfLines = 0
        self.message.lineBreakMode = .byWordWrapping
        self.message.textColor = .white
        self.message.font = .systemFont(ofSize: 15)

        self.messageContainer.addSubview(self.message)

        self.message.topAnchor.constraint(equalTo: self.messageContainer.topAnchor, constant: 5).isActive = true
        self.message.trailingAnchor.constraint(equalTo: self.messageContainer.trailingAnchor, constant: -5).isActive = true
        self.message.leadingAnchor.constraint(equalTo: self.messageContainer.leadingAnchor, constant: 5).isActive = true
        self.message.bottomAnchor.constraint(equalTo: self.messageContainer.bottomAnchor, constant: -5).isActive = true

        self.author.translatesAutoresizingMaskIntoConstraints = false
        self.author.font = .systemFont(ofSize: 12)
        self.contentView.addSubview(self.author)
        self.author.topAnchor.constraint(equalTo: self.contentView.topAnchor, constant: 10).isActive = true
        self.author.leadingAnchor.constraint(equalTo: self.message.leadingAnchor, constant: 5).isActive = true

        self.time.translatesAutoresizingMaskIntoConstraints = false
        self.time.textColor = Colors.lightGrey
        self.time.font = .systemFont(ofSize: 10)
        self.contentView.addSubview(self.time)

        self.time.topAnchor.constraint(equalTo: self.author.topAnchor).isActive = true
        self.time.bottomAnchor.constraint(equalTo: self.author.bottomAnchor).isActive = true
        self.time.trailingAnchor.constraint(equalTo: self.messageContainer.trailingAnchor, constant: -10).isActive = true

        self.messageContainer.widthAnchor.constraint(equalTo: self.contentView.widthAnchor, multiplier: 0.8).isActive = true
        self.messageContainer.topAnchor.constraint(equalTo: self.author.bottomAnchor, constant: -1).isActive = true
        self.messageContainer.bottomAnchor.constraint(equalTo: self.contentView.bottomAnchor).isActive = true

        self.rightConstraint = self.messageContainer.leadingAnchor.constraint(equalTo: self.contentView.leadingAnchor)
        self.leftConstraint = self.messageContainer.trailingAnchor.constraint(equalTo: self.contentView.trailingAnchor)

        let gestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(self.onCellClicked(_:)))
        self.addGestureRecognizer(gestureRecognizer)
    }

    /**
     Open the context menu to allow the user to report a chat message if the message is not from myself
     - Parameter recognizer:
     */
    @objc private func onCellClicked(_ recognizer: UITapGestureRecognizer) {
        if (self.chatMessage?.getSender() == .myself) {
            return
        }

        let optionMenu = UIAlertController(title: nil, message: "Choose Option", preferredStyle: .actionSheet)

        let reportAction = UIAlertAction(title: "Report", style: .default) { action in
            if let chatMessage = self.chatMessage {
                let rootView = AppDelegate.rootViewController?.view

                let reportView = ChatReport(chatMessage)
                reportView.translatesAutoresizingMaskIntoConstraints = false

                rootView!.addSubview(reportView)
                reportView.topAnchor.constraint(equalTo: rootView!.safeAreaLayoutGuide.topAnchor, constant: -5).isActive = true
                reportView.leadingAnchor.constraint(equalTo: rootView!.leadingAnchor).isActive = true
                reportView.trailingAnchor.constraint(equalTo: rootView!.trailingAnchor).isActive = true
                reportView.bottomAnchor.constraint(equalTo: rootView!.safeAreaLayoutGuide.bottomAnchor).isActive = true
            }
        }
        let cancelAction = UIAlertAction(title: "Cancel", style: .cancel)
        optionMenu.addAction(reportAction)
        optionMenu.addAction(cancelAction)

        AppDelegate.rootViewController?.present(optionMenu, animated: true)
    }

    /**
     Update the view with the new content of the message
     Based on the sender of the message we adjust the layout
     - Parameter message: the new message
     */
    func update(_ message: Message) {
        self.rightConstraint?.isActive = false
        self.leftConstraint?.isActive = false

        let sender = message.getSender()
        switch sender {
        case .streamer:
            self.author.textColor = Colors.streamerChatName
            self.rightConstraint?.isActive = true
        case .myself:
            self.author.textColor = Colors.chatName
            self.leftConstraint?.isActive = true
        case .other:
            self.author.textColor = Colors.chatName
            self.rightConstraint?.isActive = true
        }

        self.author.text = message.senderName
        let formatter = DateFormatter()
        formatter.timeStyle = DateFormatter.Style.medium
        self.time.text = formatter.string(from: message.creationTime)
        self.message.text = message.message

        self.chatMessage = message
    }
}
````

#### Reporting a user

To report a user/chat message we provide the user a view where he can select the reason for the report and additionally provide more information if he chooses too.



````swift
import Foundation
import UIKit
import MycrocastSDK

/**
 An example view where a user can report a chat message and therefore the specific user

 To open this view, the user needs to select a chat cell and hit the report entry
 */
class ChatReport: UIView, UIPickerViewDelegate, UIPickerViewDataSource {

    private let reasonTitle: UILabel = UILabel(frame: .zero)
    private let reasonPicker: UIPickerView = UIPickerView(frame: .zero)
    private let reportTitle: UILabel = UILabel(frame: .zero)
    private let reportDescriptionTitle: UILabel = UILabel(frame: .zero)
    private let reportDescription: UITextView = UITextView(frame: .zero)
    private let cancelButton: UIButton = UIButton(frame: .zero)
    private let sendReport: UIButton = UIButton(frame: .zero)

    private let chatMessage: Message

    init(_ chatMessage: Message) {
        self.chatMessage = chatMessage
        super.init(frame: .zero)

        self.backgroundColor = Colors.darkBackground
        self.createViews()
    }

    private func createViews() {

        self.cancelButton.setTitle("X", for: .normal)
        self.cancelButton.setTitleColor(.white, for: .normal)
        self.cancelButton.addTarget(self, action: #selector(onCancel), for: .touchUpInside)
        self.cancelButton.translatesAutoresizingMaskIntoConstraints = false
        self.cancelButton.setContentHuggingPriority(.required, for: .horizontal)
        self.cancelButton.setContentHuggingPriority(.required, for: .vertical)
        self.addSubview(self.cancelButton)

        self.cancelButton.topAnchor.constraint(equalTo: self.topAnchor, constant: 10).isActive = true
        self.cancelButton.trailingAnchor.constraint(equalTo: self.trailingAnchor, constant: -10).isActive = true

        self.reportTitle.text = "Report chat user"
        self.reportTitle.textColor = .white
        self.reportTitle.font = .systemFont(ofSize: 18)
        self.reportTitle.translatesAutoresizingMaskIntoConstraints = false
        self.addSubview(self.reportTitle)

        self.reportTitle.topAnchor.constraint(equalTo: self.cancelButton.bottomAnchor).isActive = true
        self.reportTitle.leadingAnchor.constraint(equalTo: self.leadingAnchor, constant: 5).isActive = true
        self.reportTitle.trailingAnchor.constraint(equalTo: self.cancelButton.leadingAnchor).isActive = true

        self.reasonTitle.text = "Please select a reason for this report"
        self.reasonTitle.textColor = .white
        self.reasonTitle.font = .systemFont(ofSize: 12)
        self.reasonTitle.translatesAutoresizingMaskIntoConstraints = false
        self.addSubview(self.reasonTitle)

        self.reasonTitle.topAnchor.constraint(equalTo: self.reportTitle.bottomAnchor, constant: 5).isActive = true
        self.reasonTitle.leadingAnchor.constraint(equalTo: self.reportTitle.leadingAnchor).isActive = true
        self.reasonTitle.trailingAnchor.constraint(equalTo: self.reportTitle.trailingAnchor).isActive = true

        self.reasonPicker.delegate = self
        self.reasonPicker.dataSource = self
        self.reasonPicker.selectRow(0, inComponent: 0, animated: false)
        self.reasonPicker.translatesAutoresizingMaskIntoConstraints = false
        self.addSubview(self.reasonPicker)

        self.reasonPicker.topAnchor.constraint(equalTo: self.reportTitle.bottomAnchor, constant: -5).isActive = true
        self.reasonPicker.leadingAnchor.constraint(equalTo: self.reasonTitle.leadingAnchor).isActive = true
        self.reasonPicker.trailingAnchor.constraint(equalTo: self.reasonTitle.trailingAnchor).isActive = true

        self.reportDescriptionTitle.font = .systemFont(ofSize: 12)
        self.reportDescriptionTitle.text = "Provide further information (optional)"
        self.reportDescriptionTitle.textColor = .white
        self.reportDescriptionTitle.translatesAutoresizingMaskIntoConstraints = false
        self.addSubview(self.reportDescriptionTitle)

        self.reportDescriptionTitle.topAnchor.constraint(equalTo: self.reasonPicker.bottomAnchor, constant: -10).isActive = true
        self.reportDescriptionTitle.leadingAnchor.constraint(equalTo: self.reasonPicker.leadingAnchor).isActive = true
        self.reportDescriptionTitle.trailingAnchor.constraint(equalTo: self.reasonPicker.trailingAnchor).isActive = true

        self.reportDescription.layer.cornerRadius = 5
        self.reportDescription.translatesAutoresizingMaskIntoConstraints = false
        self.addSubview(self.reportDescription)

        self.reportDescription.topAnchor.constraint(equalTo: self.reportDescriptionTitle.bottomAnchor, constant: 0).isActive = true
        self.reportDescription.leadingAnchor.constraint(equalTo: self.reasonPicker.leadingAnchor).isActive = true
        self.reportDescription.trailingAnchor.constraint(equalTo: self.reasonPicker.trailingAnchor).isActive = true
        self.reportDescription.heightAnchor.constraint(equalToConstant: 50).isActive = true
        self.reportDescription.textColor = .white
        self.reportDescription.backgroundColor = Colors.lightBackground

        self.sendReport.translatesAutoresizingMaskIntoConstraints = false
        self.sendReport.addTarget(self, action: #selector(self.onSendReport), for: .touchUpInside)
        self.sendReport.setTitle("Send report", for: .normal)
        self.sendReport.setTitleColor(.white, for: .normal)
        self.sendReport.backgroundColor = Colors.lightBackground
        self.addSubview(self.sendReport)
        self.sendReport.layer.cornerRadius = 5

        self.sendReport.topAnchor.constraint(equalTo: self.reportDescription.bottomAnchor, constant: 10).isActive = true
        self.sendReport.centerXAnchor.constraint(equalTo: self.centerXAnchor).isActive = true

        let gesture = UITapGestureRecognizer(target: self, action: #selector(self.onViewClicked(_:)))
        self.addGestureRecognizer(gesture)
    }

    @objc private func onCancel() {
        self.removeFromSuperview()
    }

    @objc private func onViewClicked(_ gestureRecognizer: UITapGestureRecognizer) {
        self.reportDescription.resignFirstResponder()
    }

    /**
     We send the report with the selected reason and the optional provided additional information
     */
    @objc private func onSendReport() {
        let reportReason = ReportReason.allCases[self.reasonPicker.selectedRow(inComponent: 0)]
        var info = ""
        if let additional = self.reportDescription.text {
           info = additional
        }
        Mycrocast.shared.chat.reportChatMessage(self.chatMessage, reason: reportReason, additionalInformation: info) { success, error in
            // we could show some kind of message here
        }
        self.removeFromSuperview()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }

    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return ReportReason.allCases.count
    }

    public func pickerView(_ pickerView: UIPickerView, attributedTitleForRow row: Int, forComponent component: Int) -> NSAttributedString? {
        let reportReason = ReportReason.allCases[row].rawValue

        let reportReasonTitle = NSAttributedString(string: reportReason, attributes: [NSAttributedString.Key.foregroundColor:UIColor.white])
        return reportReasonTitle
    }
}

````

