package com.antoine.go4lunch.data;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MessageHelper {

    private static final String COLLECTION_NAME = "messages";

    // --- GET ---

    public static Query getAllMessageForChatRoom(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
                .orderBy("mDateMessage")
                .limit(50);
    }
}
