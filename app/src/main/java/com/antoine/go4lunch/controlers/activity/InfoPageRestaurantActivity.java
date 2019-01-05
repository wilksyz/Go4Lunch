package com.antoine.go4lunch.controlers.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.data.PlaceApiStream;
import com.antoine.go4lunch.data.ReservationHelper;
import com.antoine.go4lunch.models.firestore.Reservation;
import com.antoine.go4lunch.models.placeAPI.placeDetails.DetailsRestaurant;
import com.antoine.go4lunch.models.placeAPI.placeDetails.Result;
import com.antoine.go4lunch.views.WorkmatesAdapter;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class InfoPageRestaurantActivity extends AppCompatActivity implements WorkmatesAdapter.Listener{

    private String mPlaceId;
    private float mRating;
    private Result mDetailsRestaurant;
    private String uIdUser;
    public WorkmatesAdapter mWorkmatesAdapter;
    private String mDayDate;
    private Reservation mUserReservation;
    private String mDateReservation;
    private boolean mStatutLike;
    Map<String,String> queryLocation = new HashMap<>();
    CompositeDisposable disposable = new CompositeDisposable();
    @BindView(R.id.name_of_restaurant_textView) TextView mNameOfRestaurantTextView;
    @BindView(R.id.restaurant_imageView) ImageView mImageRestaurant;
    @BindView(R.id.adress_textView) TextView MadressTextView;
    @BindView(R.id.ratingBar) RatingBar mRatingBar;
    @BindView(R.id.webButton) Button mWebButton;
    @BindView(R.id.likeButton) ToggleButton mLikeButton;
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
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE);
        mDayDate = df.format(new Date());
        mStatutLike = getSharedPreferences("Like", MODE_PRIVATE).getBoolean(mPlaceId, false);
        mLikeButton.setChecked(mStatutLike);
        executeHttpRequestListOfRestaurant();
        executeRequestFirestore(mPlaceId);
        configureRecyclerView();
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
        mLikeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor saveSettings = getSharedPreferences("Like", MODE_PRIVATE).edit();
                if (isChecked) {
                    // The toggle is enabled
                    saveSettings.putBoolean(mPlaceId, true);
                    saveSettings.apply();
                } else {
                    // The toggle is disabled
                    saveSettings.putBoolean(mPlaceId, false);
                    saveSettings.apply();
                }
            }
        });
        mRestaurantSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDayDate.equals(mDateReservation) && mPlaceId.equals(mUserReservation.getmSelectedRestaurant())){
                    ReservationHelper.updateRestaurantReservation(null, uIdUser).addOnFailureListener(onFailureListener());
                    mRestaurantSelect.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                    mUserReservation.setmSelectedRestaurant(null);
                }else{
                    ReservationHelper.updateRestaurantReservation(mPlaceId, uIdUser).addOnFailureListener(onFailureListener());
                    ReservationHelper.updateRestaurantName(mDetailsRestaurant.getName(), uIdUser).addOnFailureListener(onFailureListener());
                    ReservationHelper.updateDate(uIdUser, mDayDate).addOnFailureListener(onFailureListener());
                    mRestaurantSelect.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorValidation)));
                    mDateReservation = mDayDate;
                    mUserReservation.setmSelectedRestaurant(mPlaceId);
                    mUserReservation.setmRestaurantName(mDetailsRestaurant.getName());
                }
            }
        });
    }

    private void executeHttpRequestListOfRestaurant(){
        disposable.add(PlaceApiStream.streamFetchDetailsPlace(queryLocation).subscribeWith(getDisposable()));
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
            mRating = mDetailsRestaurant.getRating();
            mRating = (mRating/5)*3;
            mRatingBar.setRating(mRating);
        }
    }

    public void executeRequestFirestore(String placeId){
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            uIdUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
            ReservationHelper.getReservation(uIdUser)
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> documentSnapshotTask) {
                            if (documentSnapshotTask.isSuccessful()){
                                DocumentSnapshot document = documentSnapshotTask.getResult();
                                if (document.exists()){
                                    mUserReservation = document.toObject(Reservation.class);
                                    if (mUserReservation != null){
                                        mDateReservation = mUserReservation.getmCreatedDate();
                                        String selectedRestaurant = mUserReservation.getmSelectedRestaurant();
                                        if (mDayDate.equals(mDateReservation) && mPlaceId.equals(selectedRestaurant)){
                                            mRestaurantSelect.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorValidation)));
                                        }else{
                                            mRestaurantSelect.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                                        }
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

    private void configureRecyclerView(){
        this.mWorkmatesAdapter = new WorkmatesAdapter(generateOptionsForAdapter(ReservationHelper.getAllUserSelectedThisRestaurant(mPlaceId,mDayDate)),Glide.with(this),this, 2);
        mWorkmatersRecyclerView.setAdapter(this.mWorkmatesAdapter);
        mWorkmatersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private FirestoreRecyclerOptions<Reservation> generateOptionsForAdapter(Query query){
        return new FirestoreRecyclerOptions.Builder<Reservation>()
                .setQuery(query, Reservation.class)
                .setLifecycleOwner(this)
                .build();
    }

    @Override
    public void onDataChanged() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.disposeWhenDestroy();
    }

    private void disposeWhenDestroy(){
        if (this.disposable != null && !this.disposable.isDisposed()) this.disposable.dispose();
    }
}
