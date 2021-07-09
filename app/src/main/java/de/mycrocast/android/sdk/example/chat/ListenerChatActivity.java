package de.mycrocast.android.sdk.example.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.mycrocast.android.sdk.chat.Chat;
import de.mycrocast.android.sdk.chat.data.ChatMessage;
import de.mycrocast.android.sdk.chat.data.ChatMessageSender;
import de.mycrocast.android.sdk.chat.data.ChatRoomStatus;
import de.mycrocast.android.sdk.core.Mycrocast;
import de.mycrocast.android.sdk.core.util.Optional;
import de.mycrocast.android.sdk.example.R;
import de.mycrocast.android.sdk.example.chat.adapter.ChatAdapter;
import de.mycrocast.android.sdk.example.utility.VerticalItemSpacing;
import de.mycrocast.android.sdk.example.utility.Constants;
import de.mycrocast.android.sdk.live.container.LiveStreamContainer;
import de.mycrocast.android.sdk.live.data.LiveStream;

/**
 * Example activity displaying the chat for a current live stream
 * <p>
 * This example uses the Chat interface as well as the LiveStreamContainer interface of the mycrocast sdk
 */
public class ListenerChatActivity extends AppCompatActivity implements Chat.Observer, LiveStreamContainer.Observer {

    /**
     * Creates an intent for starting the ListenerChatActivity for the given LiveStream
     *
     * @param context
     * @param liveStream The LiveStream from which we want to display the chat
     * @return Intent for starting a ListenerChatActivity
     */
    public static Intent newInstance(Context context, LiveStream liveStream) {
        long streamId = liveStream == null ? Constants.INVALID_ID : liveStream.getId();

        Intent result = new Intent(context, ListenerChatActivity.class);
        result.putExtra(Constants.LIVE_STREAM_ID_KEY, streamId);
        return result;
    }

    private final LiveStreamContainer liveStreamContainer = Mycrocast.getLiveStreamContainer();
    private final Chat chat = Mycrocast.getChat();

    private long liveStreamId;

    private TextView inActiveChatText;
    private ChatAdapter chatMessageAdapter;
    private EditText messageInput;
    private RecyclerView recyclerView;
    private Button sendButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_listener_chat);

        // add back button to toolbar
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // get id of the livestream from bundle
        this.liveStreamId = this.getIntent().getLongExtra(Constants.LIVE_STREAM_ID_KEY, Constants.INVALID_ID);

        // checking if the current live stream still exists or the provided id is
        // valid at all, otherwise we navigate back
        Optional<LiveStream> optional = this.liveStreamContainer.find(this.liveStreamId);
        if (optional.isEmpty()) {
            this.finish();
            return;
        }

        this.inActiveChatText = this.findViewById(R.id.chat_room_deactivated);
        this.recyclerView = this.findViewById(R.id.chatRecyclerView);
        this.messageInput = this.findViewById(R.id.chatMessageTextInput);
        this.sendButton = this.findViewById(R.id.sendChatMessage);
        this.sendButton.setOnClickListener(v -> this.onChatMessageSendClicked());

        this.chatMessageAdapter = new ChatAdapter(this::onChatMessageClicked);

        this.recyclerView.setAdapter(this.chatMessageAdapter);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        this.recyclerView.addItemDecoration(new VerticalItemSpacing(5));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // clean up observers, we do not get updates in this view anymore
        this.chat.removeObserver(this);
        this.liveStreamContainer.removeObserver(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // first check if the stream was not removed during the time
        // the observers where not active (streamer could have stopped in the meantime
        Optional<LiveStream> optional = this.liveStreamContainer.find(this.liveStreamId);
        if (optional.isEmpty()) {
            this.finish();
            return;
        }

        // if we are not currently in the chat, we join
        if (this.chat.chatJoined(this.liveStreamId)) {
            ChatRoomStatus status = this.chat.getChatRoomStatus(this.liveStreamId);
            this.chatRoomStatusChange(status);
        } else {
            // we joined previously therefore we just update the view
            // with potential missed messages or chatroom state updates
            this.chat.joinChatRoom(this.liveStreamId);
            Optional<List<ChatMessage>> messages = this.chat.getCurrentMessage(this.liveStreamId);
            if (messages.isPresent()) {
                this.chatMessageAdapter.setChatMessages(messages.get());
                this.chatMessageAdapter.notifyDataSetChanged();
            }
        }

        // register observers again
        this.chat.addObserver(this);
        this.liveStreamContainer.addObserver(this);
    }

    /**
     * A chat message was clicked we could react in some way
     *
     * @param chatMessage the clicked message
     */
    private void onChatMessageClicked(ChatMessage chatMessage) {
        if (chatMessage.getMessageSource() == ChatMessageSender.MYSELF) {
            return;
        }

        // we could implement reporting a user/chat message here
        // see -> chat.reportChatMessage();
    }

    /**
     * We hit the send button to send a new message
     * This is only possible when actually something is written
     */
    private void onChatMessageSendClicked() {
        String message = this.messageInput.getText().toString();
        if (message.isEmpty()) {
            return;
        }

        this.chat.sendChatMessage(this.liveStreamId, message);
        this.messageInput.setText("");
        this.messageInput.setFocusable(false);
        this.messageInput.setFocusable(true);
    }

    /**
     * We received a new chat message, this very naive implementation
     * just provides the adapter with the fresh list and lets redraw everything in case
     * the chat message belongs to our current live stream
     *
     * @param message - the new message
     */
    @Override
    public void onChatMessageReceived(ChatMessage message) {
        if (message.getStreamId() == this.liveStreamId) {
            Optional<List<ChatMessage>> messages = this.chat.getCurrentMessage(this.liveStreamId);
            if (messages.isPresent()) {
                this.runOnUiThread(() -> {
                    this.chatMessageAdapter.setChatMessages(messages.get());
                    this.chatMessageAdapter.notifyDataSetChanged();
                });
            }
        }
    }

    /**
     * We received an update for a chatroom, first we need to decide if it is belonging to our
     * current chat afterwards we update the view
     *
     * @param streamId - the if of the stream to which this change belongs
     * @param status   - the new status of the chat room
     */
    @Override
    public void onChatRoomChanged(long streamId, ChatRoomStatus status) {
        if (this.liveStreamId == streamId) {
            this.runOnUiThread(() -> this.chatRoomStatusChange(status));
        }
    }

    private void chatRoomStatusChange(ChatRoomStatus status) {
        if (status == ChatRoomStatus.DISABLED) {
            this.recyclerView.setVisibility(View.GONE);
            this.sendButton.setVisibility(View.GONE);
            this.messageInput.setVisibility(View.GONE);

            this.inActiveChatText.setVisibility(View.VISIBLE);
        } else {
            this.recyclerView.setVisibility(View.VISIBLE);
            this.sendButton.setVisibility(View.VISIBLE);
            this.messageInput.setVisibility(View.VISIBLE);

            this.inActiveChatText.setVisibility(View.GONE);
        }
    }

    /**
     * We received a callback on the process of joining a chatroom
     * First we need to check if this callback is for our current chatroom
     * Afterwards we can process the received chat messages and chat room status
     *
     * @param streamId - the id of the stream you joined
     * @param status   - the current status of the chatroom
     * @param messages - the chat messages already present in the chat
     */
    @Override
    public void onChatRoomJoined(long streamId, ChatRoomStatus status, List<ChatMessage> messages) {
        if (this.liveStreamId == streamId) {
            this.runOnUiThread(() -> {
                this.chatRoomStatusChange(status);
                this.chatMessageAdapter.setChatMessages(messages);
                this.chatMessageAdapter.notifyDataSetChanged();
            });
        }
    }

    /**
     * A livestream was added.
     * <p>
     * Not of interest here but required from the interface
     *
     * @param liveStream - the new livestream
     */
    @Override
    public void onLiveStreamAdded(LiveStream liveStream) {
    }

    /**
     * A livestream was changed.
     * <p>
     * Not of interest here but required from the interface
     *
     * @param liveStream - the changed livestream
     */
    @Override
    public void onLiveStreamUpdated(LiveStream liveStream) {
    }

    /**
     * A livestream has ended.
     * <p>
     * We need to check if the ended livestream is the livestream of our currently displayed chat.
     * If yes, we need to close this activity.
     *
     * @param liveStream - the ended livestream
     */
    @Override
    public void onLiveStreamRemoved(LiveStream liveStream) {
        if (this.liveStreamId == liveStream.getId()) {
            this.finish();
        }
    }

    /**
     * The list of current livestreams has changed.
     * <p>
     * We need to check if the livestream of our currently displayed chat is still in this list of all currently active livestreams.
     * If it is not, it went offline, so we need to close this view.
     * If yes, we can update all our views.
     */
    @Override
    public void onLiveStreamListChanged() {
        Optional<LiveStream> optional = this.liveStreamContainer.find(this.liveStreamId);
        if (optional.isEmpty()) {
            this.finish();
        }

        this.onLiveStreamUpdated(optional.get());
    }
}
