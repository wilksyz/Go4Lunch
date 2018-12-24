package com.antoine.go4lunch.models.firestore;

import android.support.annotation.Nullable;

public class User {

    private String uid;
    private String mUsername;
    @Nullable
    private String mUrlPicture;

    public User() { }

    public User(String uid, String username, String urlPicture) {
        this.uid = uid;
        this.mUsername = username;
        this.mUrlPicture = urlPicture;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }

    public String getUsername() { return mUsername; }

    public String getUrlPicture() { return mUrlPicture; }

    // --- SETTERS ---
    public void setUsername(String username) { this.mUsername = username; }

    public void setUid(String uid) { this.uid = uid; }

    public void setUrlPicture(String urlPicture) { this.mUrlPicture = urlPicture; }
}
