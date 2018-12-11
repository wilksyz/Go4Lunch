package com.antoine.go4lunch.data.notifications;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NotificationsService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            // 1 - Get message sent by Firebase
            String message = remoteMessage.getNotification().getBody();
            //2 - Show message in console
            Log.e("TAG", message);
        }
    }
}
