# Chat

This document describes everything you need to know to get started with the Chat in the mycrocast android sdk.

Check the example app to see the usage if the chat in action. // TODO link to chat example 

## Relevant classes

- [Chat](documentation/de/mycrocast/android/sdk/chat/Chat.html) -> Interface for handling everything related to using the chat
- [Chat.Observer](documentation/de/mycrocast/android/sdk/chat/Chat.Observer.html) -> Interface to be implemented to receive events happening for the chat (need to be registered to the observer)
- [ChatMessage](documentation/de/mycrocast/android/sdk/chat/data/ChatMessage.html) -> Representation of a single chat message
- [ChatRoomStatus](documentation/de/mycrocast/android/sdk/chat/data/ChatRoomStatus.html) -> The current status of the chat room (the streamer can enable or disable the chat)
- [ChatMessageSender](documentation/de/mycrocast/android/sdk/chat/data/ChatMessageSender.html) -> Enum value representing who send the message
- [ReportReason](documentation/de/mycrocast/android/sdk/utility/ReportReason.html) -> Supported Reasons for a report of a chat message

## Concepts

Every interaction with the chat is handled through the Mycrocast.Chat interface implementation.

### Receiving chat messages

The first step you should do is create a class that implements the Chat.Observer interface to be able to receive updates for the chat rooms.

```java
interface Observer {
        /**
         * A new chat message was send by somebody
         *
         * @param message - the new message
         */
        void onChatMessage(MycrocastChatMessage message);

        /**
         * An update for the chatroom belonging to the streamId has happened.
         * This indicates a status change, the streamer could have enabled or disabled the chat
         *
         * @param streamId - the if of the stream to which this change belongs
         * @param status   - the new status of the chat room
         */
        void onChatRoomChanged(long streamId, ChatRoomStatus status);

        /**
         * Function called after successfully joined a chat room.
         *
         * @param streamId - the id of the stream you joined
         * @param status   - the current status of the chatroom
         * @param messages - the chat messages already present in the chat
         */
        void onChatRoomJoined(long streamId, ChatRoomStatus status, List<MycrocastChatMessage> messages);
    }
```

The next step is to register this class as an observer for the chat

```java
  Mycrocast.getChat().addObserver(this);
```

You are now set to receive updates for the chat

### Joining a chat room

Before you can interact with a specific chat room you first need to join the chat room. Before joining there is no information available if the chat is actually enabled by the streamer and if chat messages are present.

After joining the chat room onChatRoomJoined of the observer is called, you can now participate in the chat (if it is enabled). Additionally you now receive updates for this chat room as long as you stay joined (not calling leaveChatRoom). Updates include new chat message and changes made from the streamer to the current state of the chatroom.

```java
    /**
     * Join the chat if a chat is present for the provided stream
     * Implement the Observer interface to receive the OnChatRoomJoined callback as soon as the
     * chat was joined.
     * Only after joining a chat the status can be retrieved and updates for the chatroom are received
     *
     * @param streamId true if the streamId belongs to an existing stream otherwise false
     */
    boolean joinChatRoom(long streamId);
```

### Leaving a chat room

To leave a chatroom just call leaveChatRoom with the id of the stream that chat belongs to. Afterwards you will not receive any more updates in the observer for that chat room.

```java
    /**
     * Leave a chatroom, which results in no further updates for the current chat room
     *
     * @param streamId - the stream id of the stream from where to leave the chat
     */
    void leaveChatRoom(long streamId);
```



### Send a message

To send a chat message just call sendChatMessage with the id of the stream that chat belongs to and the actual message you want to send.

The message will only be send if you are joined in the chat and if the streamer did not disable the chatroom. A single message can only have 255 characters and everything larger will be cut off. You should limit the number of characters your user can enter.

```java
/**
 * Send a chat message with the provided content to the chatroom of the provided id
 * The message can be at maximum 255 characters. Larger messages are cut off
 * The message is only send if the chatroom is active and the chat room is currently joined
 *
 * @param streamId - the id of the stream
 * @param message  - the message to send
 * @return true if the chatroom is active and joined, false otherwise
 */
boolean sendChatMessage(long streamId, String message);
```

### Reporting a chat message

You can provide your users with the functionality to report a chat message. Just call reportChatMessage with the message you want to report, a report reason and any additional information you want to provide. The more information we receive the better we can decide what our next steps are regarding that message.

```java
    /**
     * Send a report of a chat message to our system.
     * This does not automatically removes a chatter from the chat
     *
     * @param chatMessage           - the chat message that lead to the report
     * @param reportReason          - the reason of the reporter as what is wrong with the message
     * @param additionalInformation - any additional information the reporter wants to provide
     */
    void reportChatMessage(MycrocastChatMessage chatMessage, ReportReason reportReason, String additionalInformation);
```

