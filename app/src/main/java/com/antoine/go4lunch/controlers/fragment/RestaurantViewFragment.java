package com.antoine.go4lunch.controlers.fragment;


import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.controlers.activity.InfoPageRestaurantActivity;
import com.antoine.go4lunch.data.PlaceApiStream;
import com.antoine.go4lunch.models.ItemClickSupport;
import com.antoine.go4lunch.models.placeAPI.placeDetails.DetailsRestaurant;
import com.antoine.go4lunch.models.placeAPI.placeDetails.Result;
import com.antoine.go4lunch.views.RestaurantsViewAdapter;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantViewFragment extends BaseFragment {

    @BindView(R.id.restaurant_recycler_view) RecyclerView mRecyclerView;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 191;
    private static final String LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    protected FusedLocationProviderClient mFusedLocationProviderClient;
    CompositeDisposable disposable = new CompositeDisposable();
    Map<String,String> queryLocation = new HashMap<>();
    private String mLocation;
    private Location mLastKnownLocation;
    private List<DetailsRestaurant> mListOfRestaurant;
    private RestaurantsViewAdapter mRestaurantAdapter;

    public RestaurantViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_view, container, false);
        ButterKnife.bind(this, view);
        queryLocation.put("key", getString(R.string.google_maps_api));
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        checkPermissionBeforeRequest();
        configureOnClickRecyclerView();
        return view;
    }

    private boolean hasLocationPermissions() {
        return EasyPermissions.hasPermissions(getContext(), LOCATION);
    }

    @AfterPermissionGranted(PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
    private void checkPermissionBeforeRequest() {
        if (hasLocationPermissions()) {
            // Have permission, do the thing!
            getLastLocation();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.THIS_APP_NEEDS_YOUR_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastKnownLocation = task.getResult();
                            mLocation = (String.valueOf(mLastKnownLocation.getLatitude())+","+String.valueOf(mLastKnownLocation.getLongitude()));
                            queryLocation.put("location", mLocation);
                            executeHttpRequestListOfRestaurant();
                            configureRecyclerView();
                        } else {
                            Log.w("TAG", "getLastLocation:exception", task.getException());
                        }
                    }
                });
    }

    private void executeHttpRequestListOfRestaurant(){
        disposable.add(PlaceApiStream.streamFetchListRestaurants(queryLocation).subscribeWith(getDisposable()));
    }

    protected DisposableObserver<List<DetailsRestaurant>> getDisposable(){
        //manages so api request is OK or not OK
        return new DisposableObserver<List<DetailsRestaurant>>() {
            @Override
            public void onNext(List<DetailsRestaurant> listOfRestaurant) {
                updateAfterRequest(listOfRestaurant);
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

    private void updateAfterRequest(List<DetailsRestaurant> listOfRestaurant){
        mListOfRestaurant.addAll(listOfRestaurant);
        mRestaurantAdapter.notifyDataSetChanged();
    }

    private void configureRecyclerView(){
        this.mListOfRestaurant = new ArrayList<>();
        this.mRestaurantAdapter = new RestaurantsViewAdapter(Glide.with(this), this.mListOfRestaurant, this.mLocation);
        this.mRecyclerView.setAdapter(this.mRestaurantAdapter);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(mRecyclerView, R.layout.fragment_restaurant_view_item)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Result restaurant = mRestaurantAdapter.getRestaurant(position);
                        String placeId = restaurant.getPlaceId();
                        Intent intent = new Intent(getActivity(), InfoPageRestaurantActivity.class);
                        intent.putExtra("placeId", placeId);
                        startActivity(intent);
                    }
                });
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
