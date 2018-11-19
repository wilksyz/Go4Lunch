package com.antoine.go4lunch.controlers.fragment;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.antoine.go4lunch.data.PlaceApiStream;
import com.antoine.go4lunch.models.placeAPI.placeSearch.ListOfRestaurant;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class BaseFragment extends Fragment {

    Disposable disposable;
    Map<String,String> location = new HashMap<>();


    protected void executeHttpRequestWithRetrofit(String Plocation){
        disposable = PlaceApiStream.streamFetchListRestaurants(Plocation).subscribeWith(new DisposableObserver<ListOfRestaurant>() {
            @Override
            public void onNext(ListOfRestaurant listOfRestaurant) {
                Log.e("TAG","On successful: "+listOfRestaurant.getStatus());
            }

            @Override
            public void onError(Throwable e) {
                Log.e("TAG","On Error stream"+Log.getStackTraceString(e));
            }

            @Override
            public void onComplete() {
                Log.e("TAG","On Complete !!");
            }
        });
    }
}
