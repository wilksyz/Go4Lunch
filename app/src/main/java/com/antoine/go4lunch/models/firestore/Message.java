package com.antoine.go4lunch.models.firestore;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Message {

    private Date mDateMessage;
    private String mMessage;
    private String mUsernameSender;

    public Message() {
    }

    public Message(String mMessage, String mUsernameSender) {
        this.mMessage = mMessage;
        this.mUsernameSender = mUsernameSender;
    }

    // --- GETTERS ---

    @ServerTimestamp public Date getmDateMessage() {
        return mDateMessage;
    }

    public String getmMessage() {
        return mMessage;
    }

    public String getmUsernameSender() {
        return mUsernameSender;
    }

    // --- SETTERS ---

    public void setmDateMessage(Date mDateMessage) {
        this.mDateMessage = mDateMessage;
    }

    public void setmMessage(String mMessage) {
        this.mMessage = mMessage;
    }

    public void setmUsernameSender(String mUsernameSender) {
        this.mUsernameSender = mUsernameSender;
    }
}
