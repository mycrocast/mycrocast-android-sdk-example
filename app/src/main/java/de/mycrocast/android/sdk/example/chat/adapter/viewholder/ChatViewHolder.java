package de.mycrocast.android.sdk.example.chat.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.mycrocast.android.sdk.chat.data.ChatMessageSender;
import de.mycrocast.android.sdk.example.R;
import de.mycrocast.android.sdk.chat.data.ChatMessage;


/**
 * A sample implementation for a chat element within the chat functionality
 * Selecting a chat message that is not from yourself will open a context menu providing the
 * functionality to report a user/chat message
 */
public class ChatViewHolder extends RecyclerView.ViewHolder {

    private final TextView leftHeader;
    private final TextView rightHeader;

    private final TextView chatMessageView;
    private final View leftBorder;
    private final View rightBorder;


    public ChatViewHolder(View itemView) {
        super(itemView);

        this.leftHeader = itemView.findViewById(R.id.chatHeaderLeft);
        this.rightHeader = itemView.findViewById(R.id.chatHeaderRight);
        this.chatMessageView = itemView.findViewById(R.id.chatMessageText);
        this.leftBorder = itemView.findViewById(R.id.chatBorderLeft);
        this.rightBorder = itemView.findViewById(R.id.chatBorderRight);
    }

    public void initialize(ChatMessage chatMessage) {
        this.chatMessageView.setText(chatMessage.getMessage());

        String userName = chatMessage.getSenderName();

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(chatMessage.getCreationTime());
        String sendTime = format.format(date);

        boolean messageFromCurrentUser = chatMessage.getMessageSource() == ChatMessageSender.MYSELF;
        if (messageFromCurrentUser) {
            this.leftHeader.setText(sendTime);
            this.rightHeader.setText(userName);
        } else {
            this.leftHeader.setText(userName);
            this.rightHeader.setText(sendTime);
        }

        this.leftBorder.setVisibility(messageFromCurrentUser ? View.VISIBLE : View.GONE);
        this.rightBorder.setVisibility(messageFromCurrentUser ? View.GONE : View.VISIBLE);
    }


    public void setClickListener(View.OnClickListener onClickListener) {
        this.itemView.setOnClickListener(onClickListener);
    }
}

