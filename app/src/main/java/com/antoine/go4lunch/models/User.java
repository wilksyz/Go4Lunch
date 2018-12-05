package com.antoine.go4lunch.models;

import android.support.annotation.Nullable;

public class User {

    private String uid;
    private String mUsername;
    private String mMyRestauraznt;
    @Nullable
    private String mUrlPicture;

    public User() { }

    public User(String uid, String username, String urlPicture) {
        this.uid = uid;
        this.mUsername = username;
        this.mUrlPicture = urlPicture;
        //this.mMyRestauraznt = null;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }

    public String getUsername() { return mUsername; }

    public String getUrlPicture() { return mUrlPicture; }

    public String getMyRestauraznt() {
        return mMyRestauraznt;
    }

    // --- SETTERS ---
    public void setUsername(String username) { this.mUsername = username; }

    public void setUid(String uid) { this.uid = uid; }

    public void setUrlPicture(String urlPicture) { this.mUrlPicture = urlPicture; }

    public void setMyRestauraznt(String mSelectedRestauraznt) {
        this.mMyRestauraznt = mSelectedRestauraznt;
    }
}
