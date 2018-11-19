package com.antoine.go4lunch.data;

import com.antoine.go4lunch.models.placeAPI.placeDetails.DetailsRestaurants;
import com.antoine.go4lunch.models.placeAPI.placeSearch.ListOfRestaurant;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PlaceApiStream {

    public static Observable<ListOfRestaurant> streamFetchListRestaurants(String location){
        PlaceApiService placeApiService = PlaceApiService.retrofit.create(PlaceApiService.class);
        return placeApiService.getLocation(location)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    public static Observable<DetailsRestaurants> streamFetchDetailsPlace(String placeid){
        PlaceApiService placeApiService = PlaceApiService.retrofit.create(PlaceApiService.class);
        return placeApiService.getPlaceID(placeid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }
}
