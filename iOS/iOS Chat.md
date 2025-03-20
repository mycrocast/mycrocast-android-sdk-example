# iOS Chat

This document describes everything you need to know to get started with the chat

Check the example app to see the implementation in use

## Relevant classes

- [Chat](docs/Protocol/Chat.html) - Protocol for interacting with the mycrocast SDK chat
- [ChatDelegate](docs/Protocol/ChatDelegate.html) - Protocol to implement the receiving of chat message and chat room updates
- [Message](docs/Structs/Message.html) - A single chat message
- [Sender](docs/Enums/Sender.html) - enum describing who was the sender of the message
- [ChatStatus](docs/Enums/ChatStatus.html)  - enum describing the status of the chat, either enabled or disabled as configured by the streamer
- [ReportReason](docs/Enums/ReportReason.html) - In case a chat message needs to be reported this is the available selection of reasons as enum

## Concepts

Every interaction with the chat is handled through the Mycrocast.Chat protocol implementation.

### Receiving chat messages

The first step you should do is create a class that conforms to the ChatDelegate protocol

```swift
public protocol ChatDelegate {
    /**
     A new chat message was received. Inspect the streamId of the message to determine for which chat this message
      was received for in case you have multiple chats joined
     - Parameter message: the new message
     */
    func onMessage(_ message: Message)
    /**
     A chat status update was received. This is when the streamer decides to either enable or disable the chat
     - Parameters:
       - streamId: the id of the stream to which this chatroom belongs to
       - status: the new status of the chat
     */
    func onChatStatusChanged(_ streamId: Int, status: ChatStatus)
}
```

The next step is to register this class as an observer for the chat

```swift
  Mycrocast.shared.chat.addObserver(self);
```

You are now set to receive updates for the chat

### Joining a chat room

Before you can interact with a specific chat room you first need to join the chat room. Before joining there is no information available if the chat is actually enabled by the streamer and if chat messages are present.

After joining the chat room the callback closure is called with the response of the chat room join. After you joined you start receiving updates for the chat.

```swift
    /**
      To be able to participate in the chat for a stream we first need to join it.
      After the chat was joined we receive the status of the chat room (enabled or disabled) and receive updates for new
      chat messages
     - Parameters:
       - streamId: the stream id where we want to join the chat for
       - callback: The callback that is executed when the chat was joined containing the chat messages, status and potential error
     */
    func joinChat(_ streamId: Int, callback: @escaping (_ messages: [Message], _ chatStatus: ChatStatus, _ error: MycrocastError?) -> ())
```

### Leaving a chat room

To leave a chatroom just call levateChat with the id of the stream that chat belongs to. Afterwards you will not receive any more updates in the observer for that chat room.

```swift
   /**
     Leave a chat and therefore automatically unsubscribing for receiving and updates on this specific chatroom
     When a stream ends the chat is automatically left
     - Parameter streamId: the id of the stream
     */
    func leaveChat(_ streamId: Int)
```



### Send a message

To send a chat message just call sendChatMessage with the id of the stream that chat belongs to and the actual message you want to send.

The message will only be send if you are joined in the chat and if the streamer did not disable the chatroom. A single message can only have 255 characters and everything larger will be cut off. You should limit the number of characters your user can enter.

```swift
    /**
        Send a new chat message. A message is can only be at maximum 255 characters long.If the message is longer,
         it will be the first 255 characters send
     - Parameters:
       - streamId: the id of the chatroom this message will be send to
       - message: the message to send either completely or the first 255 characters
       - callback: the callback executed when the request is done, the bool indicates if you where allowed to send or if you
        are blocked from the streamer
     */
    func sendChatMessage(_ streamId: Int, message: String, callback: @escaping (_ success: Bool, _ error: MycrocastError?) -> ())
```

### Reporting a chat message

You can provide your users with the functionality to report a chat message. Just call reportChatMessage with the message you want to report, a report reason and any additional information you want to provide. The more information we receive the better we can decide what our next steps are regarding that message.

```swift
    /**
     Report a chat message and therefore the user of that chat message
      The more information the reporter provides the better we can take action
     - Parameters:
       - message: the message that will be reported
       - reason: the report reason
       - additionalInformation: any additional information the user can provide at maximum 255 characters
       - callback: executed when the response is received from the server
     */
    func reportChatMessage(_ message: Message, reason: ReportReason, additionalInformation: String,
                           callback: @escaping (_ success: Bool, _ error: MycrocastError?) -> ())
```