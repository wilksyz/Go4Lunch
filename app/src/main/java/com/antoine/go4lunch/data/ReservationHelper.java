package com.antoine.go4lunch.data;

import com.antoine.go4lunch.models.firestore.Reservation;
import com.antoine.go4lunch.models.firestore.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ReservationHelper {

    private static final String COLLECTION_NAME = "reservations";

    public static CollectionReference getReservationsCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    public static Task<Void> createReservation(String uId ,String date, User user){
        Reservation reservationToCreate = new Reservation(user, uId);
        return ReservationHelper.getReservationsCollection().document(uId).set(reservationToCreate);
    }

    public static Task<DocumentSnapshot> getReservation(String uId){
        return ReservationHelper.getReservationsCollection().document(uId).get();
    }

    public static Task<Void> updateRestaurantReservation(String restaurantId, String uId){
        return ReservationHelper.getReservationsCollection().document(uId).update("mSelectedRestaurant", restaurantId);
    }

    public static Task<Void> updateRestaurantName(String restaurantName, String uId){
        return ReservationHelper.getReservationsCollection().document(uId).update("mRestaurantName", restaurantName);
    }

    public static Task<Void> updateDate(String uId, String date){
        return ReservationHelper.getReservationsCollection().document(uId).update("mCreatedDate", date);
    }

    public static Query getAllUserSelectedThisRestaurant(String placeId, String date){
        return ReservationHelper.getReservationsCollection()
                .whereEqualTo("mSelectedRestaurant", placeId)
                .whereEqualTo("mCreatedDate", date);
    }

    public static Query getAllReservation(){
        return getReservationsCollection();
    }
}
