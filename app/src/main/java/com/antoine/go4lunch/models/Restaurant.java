package com.antoine.go4lunch.models;

public class Restaurant {

    private String mPlaceId;
    private int mRating;

    public Restaurant() {
    }

    public Restaurant(String placeId){
        this.mPlaceId = placeId;
    }

    // --- GETTERS ---

    public String getPlaceId() {
        return mPlaceId;
    }

    public int getRating() {
        return mRating;
    }

    // --- SETTERS ---

    public void setRating(int rating) {
        this.mRating = mRating + rating;
    }

}
