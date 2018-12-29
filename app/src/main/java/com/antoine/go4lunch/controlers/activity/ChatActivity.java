package com.antoine.go4lunch.controlers.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.data.MessageHelper;
import com.antoine.go4lunch.models.firestore.Message;
import com.antoine.go4lunch.views.ChatAdapter;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends BaseActivity implements ChatAdapter.Listener{

    @BindView(R.id.recycler_view_chat) RecyclerView mRecyclerViewChat;
    @BindView(R.id.send_button_chat) ImageButton mSendButton;
    @BindView(R.id.editText_chat) EditText mEditTextMessage;
    private ChatAdapter mChatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        configureRecyclerView();

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mEditTextMessage.getText()) && FirebaseAuth.getInstance().getCurrentUser() != null){
                    MessageHelper.createMessage(mEditTextMessage.getText().toString(), getCurrentUser().getDisplayName()).addOnFailureListener(onFailureListener());
                    mEditTextMessage.setText("");
                }
            }
        });
    }

    private void configureRecyclerView(){
        this.mChatAdapter = new ChatAdapter(generateOptionsForAdapter(MessageHelper.getAllMessageForChatRoom()), Glide.with(this),this, this.getCurrentUser().getDisplayName());
        mChatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mRecyclerViewChat.smoothScrollToPosition(mChatAdapter.getItemCount()); // Scroll to bottom on new messages
            }
        });
        mRecyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewChat.setAdapter(this.mChatAdapter);
    }


    private FirestoreRecyclerOptions<Message> generateOptionsForAdapter(Query query){
        return new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .setLifecycleOwner(this)
                .build();
    }

    @Override
    public void onDataChanged() {

    }

    private OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
            }
        };
    }
}
