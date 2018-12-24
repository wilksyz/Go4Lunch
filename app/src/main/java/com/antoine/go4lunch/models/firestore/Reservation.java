package com.antoine.go4lunch.models.firestore;

import android.support.annotation.Nullable;

public class Reservation {

    @Nullable
    private String mSelectedRestaurant;
    private String mRestaurantName;
    private String mCreatedDate;
    private User mUser;
    private String mID;

    public Reservation( ) {
    }

    public Reservation(String createdDate, User user, String ID){
        this.mCreatedDate = createdDate;
        this.mUser = user;
        this.mID = ID;
    }

    public Reservation(User mUser, String mID) {
        this.mUser = mUser;
        this.mID = mID;
    }

    // --- GETTERS ---

    @Nullable
    public String getmSelectedRestaurant() {
        return mSelectedRestaurant;
    }

    public String getmRestaurantName() {
        return mRestaurantName;
    }

    public String getmCreatedDate() {
        return mCreatedDate;
    }

    public User getmUser() {
        return this.mUser;
    }

    public String getmID() {
        return this.mID;
    }

    // --- SETTERS ---

    public void setmSelectedRestaurant(@Nullable String mSelectedRestaurant) {
        this.mSelectedRestaurant = mSelectedRestaurant;
    }

    public void setmRestaurantName(String mRestaurantName) {
        this.mRestaurantName = mRestaurantName;
    }

    public void setmCreatedDate(String mCreatedDate) {
        this.mCreatedDate = mCreatedDate;
    }

    public void setmUser(User mUser) {
        this.mUser = mUser;
    }

    public void setmID(String mID) {
        this.mID = mID;
    }
}
