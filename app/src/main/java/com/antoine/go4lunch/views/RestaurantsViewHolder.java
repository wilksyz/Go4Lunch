package com.antoine.go4lunch.views;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.models.placeAPI.placeDetails.DetailsRestaurant;
import com.bumptech.glide.RequestManager;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantsViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.image_restaurant_view_imageView) ImageView mImageRestaurant;
    @BindView(R.id.distance_restaurant_textView) TextView mDistanceIntoRestaurant;
    @BindView(R.id.person_join_restaurant_textView) TextView mPersonHasSelectedRestaurant;
    @BindView(R.id.note_of_restaurant_ratingBar) RatingBar mNoteOfTheRestaurant;
    @BindView(R.id.name_restaurant_textView) TextView mNameOfRestaurant;
    @BindView(R.id.address_restaurant_textView) TextView mAddressOfRestaurant;
    @BindView(R.id.opening_restaurant_textView) TextView mOpeningRestaurant;
    private Map<String,String> queryLocation = new HashMap<>();
    private float mRating;

    public RestaurantsViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        queryLocation.put("key", itemView.getContext().getString(R.string.google_maps_api));
    }

    public void updateWithRestaurantList(RequestManager glide, DetailsRestaurant restaurant){
        String mUrlImage = getUrlImage(restaurant);
        glide.load(mUrlImage).into(mImageRestaurant);
        mNameOfRestaurant.setText(restaurant.getResult().getName());
        mAddressOfRestaurant.setText(restaurant.getResult().getFormattedAddress());
        getOpening(restaurant);
        getRatingOfRestaurant(restaurant);
        getSelectedThisRestaurant(restaurant);
        getDistanceOfRestaurant(restaurant);
    }

    private String getUrlImage(DetailsRestaurant restaurant){
        String url = null;
        if (restaurant.getResult().getPhotos() != null) {
            url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=2976&photoreference=" + restaurant.getResult().getPhotos().get(0).getPhotoReference() + "&key=" + itemView.getContext().getString(R.string.google_maps_api);
        }
        return url;
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
            }else{
                mOpeningRestaurant.setText(itemView.getContext().getString(R.string.NO_TIME_AVAILABLE));
            }
        }
    }

    private void getRatingOfRestaurant(DetailsRestaurant restaurant){
        mRating = restaurant.getResult().getRating();
        mRating = (mRating/5)*3;
        mNoteOfTheRestaurant.setRating(mRating);
    }

    private void getSelectedThisRestaurant(DetailsRestaurant restaurant){
        if (restaurant.getResult().getReservation() != null){
            mPersonHasSelectedRestaurant.setText(restaurant.getResult().getReservation());
        }
    }

    private void getDistanceOfRestaurant(DetailsRestaurant restaurant){
        if (restaurant.getResult().getDistance() != null){
            mDistanceIntoRestaurant.setText(restaurant.getResult().getDistance());
        }
    }
}
