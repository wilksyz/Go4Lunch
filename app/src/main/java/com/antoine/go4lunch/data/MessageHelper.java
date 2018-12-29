package com.antoine.go4lunch.data;

import com.antoine.go4lunch.models.firestore.Message;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MessageHelper {

    private static final String COLLECTION_NAME = "messages";

    public static Task<DocumentReference> createMessage(String textMessage, String userSender){
        Message message = new Message(textMessage, userSender);
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
                .add(message);
    }

    public static Query getAllMessageForChatRoom(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
                .orderBy("mDateMessage")
                .limit(50);
    }
}
