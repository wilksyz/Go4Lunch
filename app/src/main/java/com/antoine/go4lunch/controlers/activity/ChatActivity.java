package com.antoine.go4lunch.controlers.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageButton;

import com.antoine.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.recycler_view_chat) RecyclerView mRecyclerViewChat;
    @BindView(R.id.send_button_chat) ImageButton mSendButton;
    @BindView(R.id.editText_chat) EditText mEditTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
    }

}
