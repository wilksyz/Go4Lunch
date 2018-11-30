package com.antoine.go4lunch.data;

import com.antoine.go4lunch.models.placeAPI.placeDetails.DetailsRestaurant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PlaceApiStream {

    public static Observable<List<DetailsRestaurant>> streamFetchListRestaurants(Map<String,String> location){
        PlaceApiService placeApiService = PlaceApiService.retrofit.create(PlaceApiService.class);
        return placeApiService.getLocation(location)
                .flatMap(apiResponse -> Observable.fromIterable(apiResponse.getResults()))
                .flatMap(restaurant -> streamFetchDetailsPlace(generateQueryMap(location.get("key"),restaurant.getPlaceId())))
                .toList()
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(30, TimeUnit.SECONDS);
    }

    public static Observable<DetailsRestaurant> streamFetchDetailsPlace(Map<String,String> placeId){
        PlaceApiService placeApiService = PlaceApiService.retrofit.create(PlaceApiService.class);
        return placeApiService.getPlaceID(placeId)
                .subscribeOn(Schedulers.io())
                .timeout(30, TimeUnit.SECONDS);
    }

    private static Map<String, String> generateQueryMap(String key, String placeId){
        Map<String,String> mapQuery = new HashMap<>();
        mapQuery.put("key", key);
        mapQuery.put("placeid", placeId);
        return mapQuery;
    }

}
