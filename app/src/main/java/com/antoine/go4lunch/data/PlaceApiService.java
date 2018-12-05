package com.antoine.go4lunch.data;

import com.antoine.go4lunch.models.placeAPI.placeDetails.DetailsRestaurant;
import com.antoine.go4lunch.models.placeAPI.placeSearch.ListOfRestaurant;


import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface PlaceApiService {

    @GET("nearbysearch/json?rankby=distance&type=restaurant")
    Observable<ListOfRestaurant> getLocation(@QueryMap Map<String, String> location);

    @GET("details/json?fields=name,rating,formatted_phone_number,formatted_address,geometry,type,photo,place_id,opening_hours,geometry,website")
    Observable<DetailsRestaurant> getPlaceID(@QueryMap Map<String, String> placeid);


    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();
}
