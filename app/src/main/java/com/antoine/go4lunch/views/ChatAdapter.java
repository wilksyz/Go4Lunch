package com.antoine.go4lunch.views;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.models.firestore.Message;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ChatAdapter extends FirestoreRecyclerAdapter<Message, ChatViewHolder> {

    public interface Listener {
        void onDataChanged();
    }


    private ChatAdapter.Listener callback;
    private String mUsername;

    public ChatAdapter(@NonNull FirestoreRecyclerOptions<Message> options, ChatAdapter.Listener callback, String username) {
        super(options);
        this.callback = callback;
        this.mUsername = username;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull Message model) {
        holder.updateInterfaceWithMessage(model, mUsername);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ChatViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.activity_chat_item, viewGroup, false));
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        this.callback.onDataChanged();
    }
}
