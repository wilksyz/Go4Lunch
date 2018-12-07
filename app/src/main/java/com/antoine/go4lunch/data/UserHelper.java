package com.antoine.go4lunch.data;

import android.support.annotation.Nullable;

import com.antoine.go4lunch.models.firestore.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class UserHelper {

    private static final String COLLECTION_NAME = "users";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getUsersCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createUser(String uid, String username, String urlPicture,@Nullable String myRestaurant) {
        User userToCreate = new User(uid, username, urlPicture, myRestaurant);
        return UserHelper.getUsersCollection().document(uid).set(userToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getUser(String uid){
        return UserHelper.getUsersCollection().document(uid).get();
    }

    // --- UPDATE ---

    public static Task<Void> updateUsername(String username, String uid) {
        return UserHelper.getUsersCollection().document(uid).update("mUsername", username);
    }

    public static Task<Void> updateSelectedRestaurant(String selectedRestaurantId, String uid) {
        return UserHelper.getUsersCollection().document(uid).update("myRestaurant", selectedRestaurantId);
    }
    // --- DELETE ---

    public static Task<Void> deleteUser(String uid) {
        return UserHelper.getUsersCollection().document(uid).delete();
    }

    public static Query getAllUser(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }
}
