package com.antoine.go4lunch.controlers.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.data.PlaceApiStream;
import com.antoine.go4lunch.data.RestaurantHelper;
import com.antoine.go4lunch.data.UserHelper;
import com.antoine.go4lunch.models.placeAPI.placeDetails.DetailsRestaurant;
import com.antoine.go4lunch.models.placeAPI.placeDetails.Result;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class InfoPageRestaurantActivity extends AppCompatActivity {

    private String mPlaceId;
    private long mRating;
    private Result mDetailsRestaurant;
    private String uIdUser;
    private String mMyRestaurant;
    Map<String,String> queryLocation = new HashMap<>();
    CompositeDisposable disposables = new CompositeDisposable();
    @BindView(R.id.name_of_restaurant_textView) TextView mNameOfRestaurantTextView;
    @BindView(R.id.restaurant_imageView) ImageView mImageRestaurant;
    @BindView(R.id.adress_textView) TextView MadressTextView;
    @BindView(R.id.ratingBar) RatingBar mRatingBar;
    @BindView(R.id.webButton) Button mWebButton;
    @BindView(R.id.likeButton) Button mLikeButton;
    @BindView(R.id.call_button) Button mCallButton;
    @BindView(R.id.floatingActionButton) FloatingActionButton mRestaurantSelect;
    @BindView(R.id.workmaters_recycler_view) RecyclerView mWorkmatersRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_page);
        ButterKnife.bind(this);
        mPlaceId = getIntent().getStringExtra("placeId");
        queryLocation.put("key", getString(R.string.google_maps_api));
        queryLocation.put("placeid", mPlaceId);
        executeHttpRequestListOfRestaurant();
        executeRequestFirestore(mPlaceId);
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDetailsRestaurant.getFormattedPhoneNumber() != null){
                    String tel = "tel:"+mDetailsRestaurant.getFormattedPhoneNumber();
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(tel));
                    startActivity(intent);
                }else{
                    Toast.makeText(InfoPageRestaurantActivity.this,"No number phone available",Toast.LENGTH_LONG).show();
                }
            }
        });
        mWebButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDetailsRestaurant.getWebsite() != null){
                    String url = mDetailsRestaurant.getWebsite();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse(url));
                    startActivity(browserIntent);
                }else{
                    Toast.makeText(InfoPageRestaurantActivity.this,getString(R.string.NO_WEBSITE_AVAILABLE),Toast.LENGTH_LONG).show();
                }
            }
        });
        mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRating++;
                RestaurantHelper.updateRating(mPlaceId,mRating).addOnFailureListener(onFailureListener());
            }
        });
        mRestaurantSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMyRestaurant == null){
                    UserHelper.updateSelectedRestaurant(mPlaceId, uIdUser).addOnFailureListener(onFailureListener());
                    mRestaurantSelect.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorValidation)));
                    mMyRestaurant = mPlaceId;
                }else if (mMyRestaurant.equals(mPlaceId)){
                    UserHelper.updateSelectedRestaurant(null, uIdUser).addOnFailureListener(onFailureListener());
                    mRestaurantSelect.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                    mMyRestaurant = null;
                }else{
                    UserHelper.updateSelectedRestaurant(mPlaceId, uIdUser).addOnFailureListener(onFailureListener());
                    mRestaurantSelect.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorValidation)));
                    mMyRestaurant = mPlaceId;
                }
            }
        });
    }

    private void executeHttpRequestListOfRestaurant(){
        disposables.add(PlaceApiStream.streamFetchDetailsPlace(queryLocation).subscribeWith(getDisposable()));
    }

    protected DisposableObserver<DetailsRestaurant> getDisposable(){
        //manages so api request is OK or not OK
        return new DisposableObserver<DetailsRestaurant>() {
            @Override
            public void onNext(DetailsRestaurant listOfRestaurant) {
                updatingInterfaceUser(listOfRestaurant.getResult());
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

    public void updatingInterfaceUser(Result detailsRestaurant){
        mDetailsRestaurant = detailsRestaurant;
        if (mDetailsRestaurant != null){
            mNameOfRestaurantTextView.setText(mDetailsRestaurant.getName());
            MadressTextView.setText(mDetailsRestaurant.getFormattedAddress());
            if (mDetailsRestaurant.getPhotos() != null){
                String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=2976&photoreference="+mDetailsRestaurant.getPhotos().get(0).getPhotoReference()+"&key="+getString(R.string.google_maps_api);
                Glide.with(this)
                        .load(url)
                        .into(mImageRestaurant);
            }
        }
    }

    public void executeRequestFirestore(String placeId){
        RestaurantHelper.getRestaurantsCollection().document(placeId)
                .addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("TAG", "Listen failed.", e);
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            mRating = documentSnapshot.getLong("rating");
                            if (mRating < 5 && mRating != 0){
                                mRatingBar.setNumStars(1);
                                mRatingBar.setRating(1);
                            }else if(mRating < 10 && mRating != 0){
                                mRatingBar.setNumStars(2);
                                mRatingBar.setRating(2);
                            }else if(mRating > 10){
                                mRatingBar.setNumStars(3);
                                mRatingBar.setRating(3);
                            }else{
                                mRatingBar.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            Log.d("TAG", "Current data: null");
                        }
                    }
                });
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            uIdUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
            UserHelper.getUser(uIdUser)
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> documentSnapshotTask) {
                            if (documentSnapshotTask.isSuccessful()){
                                DocumentSnapshot document = documentSnapshotTask.getResult();
                                if (document.exists()){
                                    mMyRestaurant = document.getString("myRestaurant");
                                    Log.e("TAG","restaurant: "+mMyRestaurant);
                                    if (mMyRestaurant != null && mMyRestaurant.equals(mPlaceId)){
                                        mRestaurantSelect.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorValidation)));
                                    }else{
                                        mRestaurantSelect.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                                    }
                                }
                            }
                        }
                    });
        }

    }

    private OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
            }
        };
    }
}
