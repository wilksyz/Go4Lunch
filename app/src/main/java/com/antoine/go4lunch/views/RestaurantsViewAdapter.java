package com.antoine.go4lunch.views;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.models.placeAPI.placeDetails.DetailsRestaurant;
import com.antoine.go4lunch.models.placeAPI.placeDetails.Result;
import com.bumptech.glide.RequestManager;

import java.util.List;

public class RestaurantsViewAdapter extends RecyclerView.Adapter<RestaurantsViewHolder> {

    private final RequestManager mGlide;
    private List<DetailsRestaurant> mListOfRestaurants;

    public RestaurantsViewAdapter(RequestManager glide, List<DetailsRestaurant> listOfRestaurants) {
        this.mGlide = glide;
        this.mListOfRestaurants = listOfRestaurants;
    }

    @NonNull
    @Override
    public RestaurantsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new RestaurantsViewHolder(LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.fragment_restaurant_view_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantsViewHolder restaurantsViewHolder, int i) {
        restaurantsViewHolder.updateWithRestaurantList(mGlide, this.mListOfRestaurants.get(i));
    }

    @Override
    public int getItemCount() {
        return this.mListOfRestaurants.size();
    }

    public Result getRestaurant(int position){
        return mListOfRestaurants.get(position).getResult();
    }
}
