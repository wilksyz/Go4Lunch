package com.antoine.go4lunch.data;

import com.antoine.go4lunch.models.placeAPI.placeDetails.DetailsRestaurants;
import com.antoine.go4lunch.models.placeAPI.placeSearch.ListOfRestaurant;


import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlaceApiService {

    @GET("nearbysearch/json?rankby=distance&type=restaurant&key=AIzaSyDaCuQJlWAqP-VZ53_e-mDXRZZMNmaM9mk")
    Observable<ListOfRestaurant> getLocation(@Query("location") String location);

    @GET("details/json?fields=name,rating,formatted_phone_number,formatted_address,geometry,type,photo,place_id,opening_hours&key=AIzaSyDaCuQJlWAqP-VZ53_e-mDXRZZMNmaM9mk")
    Observable<DetailsRestaurants> getPlaceID(@Query("placeid") String placeid);


    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();
}
