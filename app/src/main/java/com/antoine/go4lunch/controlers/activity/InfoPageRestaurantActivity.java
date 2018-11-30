package com.antoine.go4lunch.controlers.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.controlers.fragment.BaseFragment;
import com.antoine.go4lunch.data.PlaceApiStream;
import com.antoine.go4lunch.data.RestaurantHelper;
import com.antoine.go4lunch.models.Restaurant;
import com.antoine.go4lunch.models.placeAPI.placeDetails.DetailsRestaurant;
import com.antoine.go4lunch.models.placeAPI.placeDetails.Result;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class InfoPageRestaurantActivity extends AppCompatActivity {

    private String mPlaceId;
    Map<String,String> queryLocation = new HashMap<>();
    CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_page);

        mPlaceId = getIntent().getStringExtra("placeId");
        queryLocation.put("key", getString(R.string.google_maps_api));
        queryLocation.put("placeid", mPlaceId);
        executeHttpRequestListOfRestaurant();
        executeRequestFirestore(mPlaceId);
        Log.e("TAG", "PlaceId: "+mPlaceId);
        //RestaurantHelper.updateRating(mPlaceId,2).addOnFailureListener(this.onFailureListener());
    }

    private void executeHttpRequestListOfRestaurant(){
        disposables.add(PlaceApiStream.streamFetchDetailsPlace(queryLocation).subscribeWith(getDisposable()));
    }

    protected DisposableObserver<DetailsRestaurant> getDisposable(){
        //manages so api request is OK or not OK
        return new DisposableObserver<DetailsRestaurant>() {
            @Override
            public void onNext(DetailsRestaurant listOfRestaurant) {

            }
            @Override
            public void onError(Throwable e) {
                Log.e("TAG","On Error",e);
            }

            @Override
            public void onComplete() {
                Log.i("TAG","On Complete !!");
            }
        };
    }

    public void executeRequestFirestore(String placeId){
        RestaurantHelper.getRestaurant(placeId)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> documentSnapshotTask) {
                        if (documentSnapshotTask.isSuccessful()) {
                            DocumentSnapshot document = documentSnapshotTask.getResult();
                            if (document.exists()) {
                                long rating = document.getLong("rating");
                            } else {
                                Log.d("TAG", "No such document");
                            }
                        } else {
                            Log.d("TAG", "get failed with ", documentSnapshotTask.getException());
                        }
                    }
                });





    }

    protected OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
            }
        };
    }

}
