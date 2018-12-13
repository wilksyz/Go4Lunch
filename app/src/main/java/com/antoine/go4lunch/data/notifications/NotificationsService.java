package com.antoine.go4lunch.data.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.controlers.activity.InfoPageRestaurantActivity;
import com.antoine.go4lunch.controlers.activity.MainActivity;
import com.antoine.go4lunch.data.PlaceApiStream;
import com.antoine.go4lunch.data.UserHelper;
import com.antoine.go4lunch.models.matrixAPI.DistanceMatrixRestaurant;
import com.antoine.go4lunch.models.placeAPI.placeDetails.DetailsRestaurant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class NotificationsService extends FirebaseMessagingService {

    private final int NOTIFICATION_ID = 007;
    private final String NOTIFICATION_TAG = "GO4LUNCH";
    private boolean mStatusNotification;
    private static final String STATUS_NOTIFICATION = "status notification";
    private Map<String,String> queryLocation = new HashMap<>();
    private CompositeDisposable disposables = new CompositeDisposable();
    private String mNameRestaurant;
    private String mAddressRestaurant;
    private String mWorkmatesJoinsForLunch;
    private String mMyRestaurant;
    ListenerRegistration registration;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            String message = remoteMessage.getNotification().getBody();
            mStatusNotification = getSharedPreferences("Notification", MODE_PRIVATE).getBoolean(STATUS_NOTIFICATION, false);
            queryLocation.put("key", getString(R.string.google_maps_api));

            if (message.equals("I have choice a restaurant") && mStatusNotification){
                getSettingsNotifications();
            }


        }
    }

    private void getSettingsNotifications(){
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            UserHelper.getUser(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> documentSnapshotTask) {
                            if (documentSnapshotTask.isSuccessful()) {
                                DocumentSnapshot document = documentSnapshotTask.getResult();
                                if (document.exists()) {
                                    mMyRestaurant = document.getString("myRestaurant");
                                    if (mMyRestaurant != null){
                                        queryLocation.put("placeid", mMyRestaurant);
                                        getSelectedThisRestaurant(mMyRestaurant);
                                    }

                                } else {
                                    Log.d("TAG", "No such document");
                                }
                            } else {
                                Log.d("TAG", "get failed with ", documentSnapshotTask.getException());
                            }
                        }
                    });
        }
    }

    private void getSelectedThisRestaurant(String placeId){
        Query query = UserHelper.getUsersCollection();
        registration = query.whereEqualTo("myRestaurant", placeId)
                .addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("TAG", "Listen failed.", e);
                            return;
                        }
                        executeHttpRequestRestaurant();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get("username") != null) {
                                if (mWorkmatesJoinsForLunch == null){
                                    mWorkmatesJoinsForLunch = doc.getString("username");
                                }else{
                                    mWorkmatesJoinsForLunch = mWorkmatesJoinsForLunch + doc.getString("username");
                                }
                            }
                        }
                    }
                });
    }

    private void executeHttpRequestRestaurant(){

        disposables.add(PlaceApiStream.streamFetchDetailsPlace(queryLocation).subscribeWith(new DisposableObserver<DetailsRestaurant>() {
            @Override
            public void onNext(DetailsRestaurant detailsRestaurant) {
                mNameRestaurant =getString(R.string.YOUR_LUNCH) + " " + detailsRestaurant.getResult().getName();
                mAddressRestaurant = detailsRestaurant.getResult().getFormattedAddress();
                sendNotification();
            }
            @Override
            public void onError(Throwable e) {
                Log.e("TAG","On Error",e);
            }

            @Override
            public void onComplete() {
                Log.i("TAG","On Complete !!");
            }
        }));
    }

    private void sendNotification() {
        registration.remove();
        // 1 - Create an Intent that will be shown when user will click on the Notification
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        // 2 - Create a Style for the Notification
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(mNameRestaurant);
        inboxStyle.addLine(mAddressRestaurant);
        inboxStyle.addLine(mWorkmatesJoinsForLunch);

        // 3 - Create a Channel (Android 8)
        String channelId = getString(R.string.default_notification_channel_id);

        // 4 - Build a Notification object
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_restaurant_menu)
                        .setContentTitle(mNameRestaurant)
                        .setContentText(mAddressRestaurant)
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent)
                        .setStyle(inboxStyle);

        // 5 - Add the Notification to the Notification Manager and show it.
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 6 - Support Version >= Android 8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Message provenant de Firebase";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        // 7 - Show notification
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
    }
}
