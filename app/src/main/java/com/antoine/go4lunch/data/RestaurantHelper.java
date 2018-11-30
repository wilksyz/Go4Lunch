package com.antoine.go4lunch.data;

import com.antoine.go4lunch.models.Restaurant;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RestaurantHelper {

    private static final String COLLECTION_NAME = "restaurants";

    public static CollectionReference getRestaurantsCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    public static Task<Void> createRestaurant(String placeId){
        Restaurant restaurantToCreate = new Restaurant(placeId);
        return RestaurantHelper.getRestaurantsCollection().document(placeId).set(restaurantToCreate);
    }

    public static Task<DocumentSnapshot> getRestaurant(String placeId){
        return RestaurantHelper.getRestaurantsCollection().document(placeId).get();
    }

    public static Task<Void> updateRating(String placeId, int rating){
        return RestaurantHelper.getRestaurantsCollection().document(placeId).update("rating", rating);
    }

}
