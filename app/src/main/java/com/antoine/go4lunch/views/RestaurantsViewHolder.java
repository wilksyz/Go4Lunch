package com.antoine.go4lunch.views;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.data.PlaceApiStream;
import com.antoine.go4lunch.data.RestaurantHelper;
import com.antoine.go4lunch.data.UserHelper;
import com.antoine.go4lunch.models.matrixAPI.DistanceMatrixRestaurant;
import com.antoine.go4lunch.models.placeAPI.placeDetails.DetailsRestaurant;
import com.bumptech.glide.RequestManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class RestaurantsViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.image_restaurant_view_imageView) ImageView mImageRestaurant;
    @BindView(R.id.distance_restaurant_textView) TextView mDistanceIntoRestaurant;
    @BindView(R.id.person_join_restaurant_textView) TextView mPersonHasSelectedRestaurant;
    @BindView(R.id.note_of_restaurant_ratingBar) RatingBar mNoteOfTheRestaurant;
    @BindView(R.id.name_restaurant_textView) TextView mNameOfRestaurant;
    @BindView(R.id.address_restaurant_textView) TextView mAddressOfRestaurant;
    @BindView(R.id.opening_restaurant_textView) TextView mOpeningRestaurant;
    private Map<String,String> queryLocation = new HashMap<>();
    private CompositeDisposable disposables = new CompositeDisposable();
    private long mRating;

    public RestaurantsViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        queryLocation.put("key", itemView.getContext().getString(R.string.google_maps_api));
    }

    public void updateWithRestaurantList(RequestManager glide, DetailsRestaurant restaurant, String location){
        //Log.e("TAG","Je suis "+restaurant.getResult().getName()+" et mon placeId est: "+restaurant.getResult().getPlaceId());
        mNoteOfTheRestaurant.setVisibility(View.INVISIBLE);
        if (restaurant.getResult().getPhotos() != null){
            String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=2976&photoreference="+restaurant.getResult().getPhotos().get(0).getPhotoReference()+"&key="+itemView.getContext().getString(R.string.google_maps_api);
            glide.load(url)
                    .into(mImageRestaurant);
        }
        mNameOfRestaurant.setText(restaurant.getResult().getName());
        mAddressOfRestaurant.setText(restaurant.getResult().getFormattedAddress());
        getOpening(restaurant);
        getDistanceOfRestaurant(location, restaurant);
        getNoteOfRestaurant(restaurant);
        getSelectedThisRestaurant(restaurant.getResult().getPlaceId());
    }

    private void getOpening(DetailsRestaurant restaurant){
        if (restaurant.getResult().getOpeningHours() != null){
            if (restaurant.getResult().getOpeningHours().getOpenNow()){
                Calendar calendar = Calendar.getInstance();
                Date date = new Date();
                calendar.setTime(date);
                int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                int i = 0;
                try {
                while (i < 6){
                        if (restaurant.getResult().getOpeningHours().getPeriods().get(i).getClose().getDay() == currentDayOfWeek){
                            String closeHours = restaurant.getResult().getOpeningHours().getPeriods().get(i).getClose().getTime();
                            String hour = closeHours.substring(0,2);
                            String minute = closeHours.substring(2,4);
                            closeHours = itemView.getContext().getString(R.string.OPEN_UNTIL)+" "+hour+"h"+minute;
                            mOpeningRestaurant.setText(closeHours);
                            mOpeningRestaurant.setTextColor(itemView.getResources().getColor(R.color.colorBlack));
                            i = 8;
                        }else{
                            mOpeningRestaurant.setText(itemView.getContext().getString(R.string.OPEN));
                            i++;
                        }
                    }
                    }catch(Exception e){
                        mOpeningRestaurant.setText(itemView.getContext().getString(R.string.OPEN));
                }
            }else if (!restaurant.getResult().getOpeningHours().getOpenNow()){
                mOpeningRestaurant.setText(itemView.getContext().getString(R.string.CLOSED));
                mOpeningRestaurant.setTextColor(itemView.getResources().getColor(R.color.colorPrimaryDark));
                mOpeningRestaurant.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }
    }

    private void getDistanceOfRestaurant(String location, DetailsRestaurant restaurant){
        queryLocation.put("origins", location);
        queryLocation.put("destinations","place_id:"+restaurant.getResult().getPlaceId());
        disposables.add(PlaceApiStream.streamFetchDistanceMatrix(queryLocation).subscribeWith(new DisposableObserver<DistanceMatrixRestaurant>() {
            @Override
            public void onNext(DistanceMatrixRestaurant distanceMatrixRestaurant) {
                mDistanceIntoRestaurant.setText(distanceMatrixRestaurant.getRows().get(0).getElements().get(0).getDistance().getText());
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

    private void getNoteOfRestaurant(DetailsRestaurant restaurant){
        RestaurantHelper.getRestaurantsCollection().document(restaurant.getResult().getPlaceId())
                .addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot,
                                        @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("TAG", "Listen failed.", e);
                            return;
                        }
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            mRating = documentSnapshot.getLong("rating");
                            if (mRating < 5 && mRating != 0){
                                mNoteOfTheRestaurant.setNumStars(1);
                                mNoteOfTheRestaurant.setRating(1);
                                mNoteOfTheRestaurant.setVisibility(View.VISIBLE);
                            }else if(mRating < 10 && mRating != 0){
                                mNoteOfTheRestaurant.setNumStars(2);
                                mNoteOfTheRestaurant.setRating(2);
                                mNoteOfTheRestaurant.setVisibility(View.VISIBLE);
                            }else if(mRating > 10){
                                mNoteOfTheRestaurant.setNumStars(3);
                                mNoteOfTheRestaurant.setRating(3);
                                mNoteOfTheRestaurant.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.d("TAG", "Current data: null");
                        }
                    }
                });
    }

    private void getSelectedThisRestaurant(String placeId){
        UserHelper.getUsersCollection()
                .whereEqualTo("myRestaurant", placeId)
                .addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("TAG", "Listen failed.", e);
                            return;
                        }

                        List<String> client = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get("username") != null) {
                                client.add(doc.getString("username"));
                            }
                        }
                        String numberClient = "("+client.size()+")";
                        mPersonHasSelectedRestaurant.setText(numberClient);
                    }
                });
    }
}
