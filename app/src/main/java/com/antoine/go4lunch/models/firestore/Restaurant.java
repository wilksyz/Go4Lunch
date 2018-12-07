package com.antoine.go4lunch.models.firestore;

public class Restaurant {

    private String mPlaceId;
    private long mRating;

    public Restaurant() {
    }

    public Restaurant(String placeId){
        this.mPlaceId = placeId;
    }

    // --- GETTERS ---

    public String getPlaceId() {
        return mPlaceId;
    }

    public long getRating() {
        return mRating;
    }

    // --- SETTERS ---

    public void setRating(long rating) {
        this.mRating = mRating + rating;
    }

}
