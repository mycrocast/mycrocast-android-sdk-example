package de.mycrocast.android.sdk.example.chat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.mycrocast.android.sdk.chat.data.ChatMessage;
import de.mycrocast.android.sdk.example.R;
import de.mycrocast.android.sdk.example.chat.adapter.viewholder.ChatViewHolder;
import de.mycrocast.android.sdk.example.utility.ClickObserver;

public class ChatAdapter extends RecyclerView.Adapter<ChatViewHolder> {

    private final List<ChatMessage> messages;
    private final ClickObserver<ChatMessage> messageClickObserver;

    public ChatAdapter(ClickObserver<ChatMessage> messageClickObserver) {
        this.messages = new ArrayList<>();
        this.messageClickObserver = messageClickObserver;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatmessage, parent, false);
        return new ChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = this.messages.get(position);
        if (message == null) {
            return;
        }

        holder.initialize(message);
        holder.setClickListener(view -> this.messageClickObserver.onClicked(message));
    }

    @Override
    public int getItemCount() {
        return this.messages.size();
    }

    public void setChatMessages(List<ChatMessage> messages) {
        this.messages.clear();
        this.messages.addAll(messages);
    }
}
