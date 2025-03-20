# Android example App - ListenerChatActivity

This activity is the implementation of an example chat with the mycrocast sdk.

The chat is visually represented by a recyclerview with the ChatViewHolder as the visual implementation for a single chat message.

On the bottom of the chat view you have an input element and a send button so that the user can participate in the chat.

Should the chat be disabled or is already disabled, the visual elements are hidden and an information is presented to the user.



## Workflow

The next section describes the workflows for the mycrocast sdk but leaves out any irrelevant parts (like the configuration of the recyclerview)

#### OnResume

When the view/activity is opened, we do the following

1. Check if the livestream this view belongs to was removed in the meantime as the app was for example in the background

2. Register ourselves for chat update to the class as the class conforms to the Chat.Observer interface

3. Register ourselves for stream updates as the class also conforms to LiveStreamContainer.Observer -> we want to leave the view in case our current stream stops

4. Check if we previously joined the chat, if so we get the internal messages again and display them, otherwise join the chat

   

   ````java
   @Override
       protected void onResume() {
           super.onResume();
   
           // first check if the stream was not removed during the time
           // the observers where not active (streamer could have stopped in the meantime
           Optional<LiveStream> optional = this.findLiveStream(this.liveStreamContainer);
           if (optional.isEmpty()) {
               this.finish();
               return;
           }
   
           // if we are not currently in the chat, we join
           if (this.chat.chatJoined(this.liveStream.getId())) {
               ChatRoomStatus status = this.chat.getChatRoomStatus(this.liveStream.getId());
               this.chatRoomStatusChange(status);
   
           } else {
               // we joined previously therefore we just update the view
               // with potential missed messages or chatroom state updates
               this.chat.joinChatRoom(this.liveStream.getId());
               Optional<List<MycrocastChatMessage>> messages = this.chat.getCurrentMessage(this.liveStream.getId());
               if (messages.isPresent()) {
                   this.chatMessageAdapter.setChatMessages(messages.get());
                   this.chatMessageAdapter.notifyDataSetChanged();
               }
           }
		   
		   // register observers again
           this.chat.addObserver(this);
           this.liveStreamContainer.addObserver(this);
       }
   ````

   #### OnPause

   OnPause we unregister again for the above 

   ````java
       @Override
       protected void onPause() {
           super.onPause();
   
           // clean up observers, we do not get updates in this view anymore
           this.chat.removeObserver(this);
           this.liveStreamContainer.removeObserver(this);
       }
   ````

   #### OnChatRoomJoined

   Required function from the Chat.Observer interface. This function is called when we successfully joined a chatroom

   We just check the chat status get the messages and update the corresponding views accordingly

   

   ````java
       /**
        * We received a callback on the process of joining a chatroom
        * First we need to check if this callback is for our current chatroom
        * Afterwards we can process the received chat messages and chat room status
        * @param streamId - the id of the stream you joined
        * @param status   - the current status of the chatroom
        * @param messages - the chat messages already present in the chat
        */
       @Override
       public void onChatRoomJoined(long streamId, ChatRoomStatus status, List<MycrocastChatMessage> messages) {
           if (this.liveStream.getId() == streamId) {
               this.runOnUiThread(() -> {
                   this.chatRoomStatusChange(status);
                   this.chatMessageAdapter.setChatMessages(messages);
                   this.chatMessageAdapter.notifyDataSetChanged();
               });
           }
       }
   
   ````

   

   #### OnChatMessage

   This is a function that is required to be implemented to conform to the Chat.Observer interface.

   Here we receive a new chat message and just update the view.

   

   ````java
       /**
        * We received a new chat message, this very naive implementation
        * just provides the adapter with the fresh list and lets redraw everything in case
        * the chat message belongs to our current live stream
        * @param message - the new message
        */
       @Override
       public void onChatMessage(MycrocastChatMessage message) {
           if (message.getStreamId() == this.liveStream.getId()) {
               Optional<List<MycrocastChatMessage>> messages = this.chat.getCurrentMessage(this.liveStream.getId());
               if (messages.isPresent()) {
                   this.runOnUiThread(() -> {
                       this.chatMessageAdapter.setChatMessages(messages.get());
                       this.chatMessageAdapter.notifyDataSetChanged();
                   });
               }
           }
       }
   ````

   

   #### OnChatRoomChanged

   Required function to implement to follow the Chat.Observer interface, here we receive updates to the chat status, the chat was either enabled or disabled

   ````java
      /**
        * We received an update for a chatroom, first we need to decide if it is belonging to our
        * current chat afterwards we update the view
        * @param streamId - the if of the stream to which this change belongs
        * @param status   - the new status of the chat room
        */
       @Override
       public void onChatRoomChanged(long streamId, ChatRoomStatus status) {
           if (this.liveStream.getId() == streamId) {
               this.runOnUiThread(() -> {
                   this.chatRoomStatusChange(status);
               });
           }
       }
   ````

   #### onChatMessageSendClicked

   Function that is executed when the send button was hit. This will send a new chat message to the chat if the input was not empty

   ````java
       /**
        * We hit the send button to send a new message
        * This is only possible when actually something is written
        */
       private void onChatMessageSendClicked() {
           String message = this.messageInput.getText().toString();
           if (message == null || message.isEmpty()) {
               return;
           }
   
           this.chat.sendChatMessage(this.liveStream.getId(), message);
           this.messageInput.setText("");
           this.messageInput.setFocusable(false);
           this.messageInput.setFocusable(true);
       }
   ````

   

