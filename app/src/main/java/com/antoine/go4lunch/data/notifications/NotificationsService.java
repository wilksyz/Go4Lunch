package com.antoine.go4lunch.data.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.controlers.activity.MainActivity;
import com.antoine.go4lunch.data.PlaceApiStream;
import com.antoine.go4lunch.data.ReservationHelper;
import com.antoine.go4lunch.models.firestore.Reservation;
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

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

import static android.content.Context.MODE_PRIVATE;

public class NotificationsService  extends BroadcastReceiver {

    private final int NOTIFICATION_ID = 2007;
    private final String NOTIFICATION_TAG = "Go4lunch";
    private Map<String,String> queryLocation = new HashMap<>();
    private CompositeDisposable disposable = new CompositeDisposable();
    private String mNameRestaurant;
    private String mAddressRestaurant;
    private String mWorkmatesJoinsForLunch;
    private String mMyRestaurant;
    private boolean mStatusNotification;
    private static final String STATUS_NOTIFICATION = "status notification";
    private String mDate;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("TAG", "NotificationService");

        this.mContext = context;
        Log.e("TAG","Notification");
        queryLocation.put("key", context.getString(R.string.google_maps_api));
        mStatusNotification = context.getSharedPreferences("Notification", MODE_PRIVATE).getBoolean(STATUS_NOTIFICATION, true);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE);
        mDate = df.format(new Date());
        if (mStatusNotification){
            getSettingsNotifications();
        }

    }

    private void getSettingsNotifications(){
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            ReservationHelper.getReservation(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> documentSnapshotTask) {
                            if (documentSnapshotTask.isSuccessful()) {
                                DocumentSnapshot document = documentSnapshotTask.getResult();
                                if (document.exists()) {
                                    mMyRestaurant = document.getString("mSelectedRestaurant");
                                    String dateDatabase = document.getString("mCreatedDate");
                                    if (mMyRestaurant != null && mDate.equals(dateDatabase)){
                                        mNameRestaurant = mContext.getString(R.string.YOUR_LUNCH) + " " + document.getString("mRestaurantName");
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
        ReservationHelper.getReservationsCollection()
                .whereEqualTo("mSelectedRestaurant", placeId)
                .whereEqualTo("mCreatedDate", mDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        executeHttpRequestRestaurant();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Reservation reservation = document.toObject(Reservation.class);
                                if (mWorkmatesJoinsForLunch == null){
                                    mWorkmatesJoinsForLunch =mContext.getString(R.string.JOIN) + " " + reservation.getmUser().getUsername();
                                }else{
                                    mWorkmatesJoinsForLunch = mWorkmatesJoinsForLunch +", "+ reservation.getmUser().getUsername();
                                }
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void executeHttpRequestRestaurant(){

        disposable.add(PlaceApiStream.streamFetchDetailsPlace(queryLocation).subscribeWith(new DisposableObserver<DetailsRestaurant>() {
            @Override
            public void onNext(DetailsRestaurant detailsRestaurant) {
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
        if (this.disposable != null && !this.disposable.isDisposed()) this.disposable.dispose();

        // 1 - Create an Intent that will be shown when user will click on the Notification
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        // 2 - Create a Style for the Notification
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(mNameRestaurant);
        inboxStyle.addLine(mAddressRestaurant);
        inboxStyle.addLine(mWorkmatesJoinsForLunch);

        // 3 - Create a Channel (Android 8)
        String channelId = mContext.getString(R.string.default_notification_channel_id);

        // 4 - Build a Notification object
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mContext, channelId)
                        .setSmallIcon(R.drawable.ic_restaurant_menu)
                        .setContentTitle(mNameRestaurant)
                        .setContentText(mAddressRestaurant)
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent)
                        .setStyle(inboxStyle);

        // 5 - Add the Notification to the Notification Manager and show it.
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // 6 - Support Version >= Android 8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Go4lunch Application";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        // 7 - Show notification
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
    }


}
