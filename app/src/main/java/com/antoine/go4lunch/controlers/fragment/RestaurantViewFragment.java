package com.antoine.go4lunch.controlers.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.antoine.go4lunch.data.ReservationHelper;
import com.antoine.go4lunch.models.ItemClickSupport;
import com.antoine.go4lunch.models.matrixAPI.DistanceMatrixRestaurant;
import com.antoine.go4lunch.models.placeAPI.placeDetails.DetailsRestaurant;
import com.antoine.go4lunch.models.placeAPI.placeDetails.Result;
import com.antoine.go4lunch.views.RestaurantsViewAdapter;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantViewFragment extends BaseFragment {

    @BindView(R.id.restaurant_recycler_view) RecyclerView mRecyclerView;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 191;
    private static final String LATITUDE_LOCATION = "latitude location";
    private static final String LONGITUDE_LOCATION = "longitude location";
    private static final String LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    protected FusedLocationProviderClient mFusedLocationProviderClient;
    CompositeDisposable disposable = new CompositeDisposable();
    Map<String,String> queryLocation = new HashMap<>();
    private String mLocation;
    private Location mLastKnownLocation;
    private List<DetailsRestaurant> mListOfRestaurant;
    private RestaurantsViewAdapter mRestaurantAdapter;
    private String mDayDate;
    private int mNumberRequestMatrix = 0;
    private  int mNumberRequestFirestore = 0;

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
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE);
        mDayDate = df.format(new Date());
        checkPermissionBeforeRequest();
        configureRecyclerView();
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
                            SharedPreferences.Editor saveSettings = getActivity().getSharedPreferences("Location", MODE_PRIVATE).edit();
                            saveSettings.putString(LATITUDE_LOCATION, String.valueOf(mLastKnownLocation.getLatitude()));
                            saveSettings.putString(LONGITUDE_LOCATION, String.valueOf(mLastKnownLocation.getLongitude()));
                            saveSettings.apply();
                            executeHttpRequestListOfRestaurant();
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
        getDistanceAndReservationForRestaurant();
        mRestaurantAdapter.notifyDataSetChanged();
    }

    private void getDistanceAndReservationForRestaurant(){
        int i;
        for (i = 0; i < mListOfRestaurant.size(); i++){
            int finalI = i;
            queryLocation.put("origins", mLocation);
            queryLocation.put("destinations","place_id:"+mListOfRestaurant.get(i).getResult().getPlaceId());
            disposable.add(PlaceApiStream.streamFetchDistanceMatrix(queryLocation).subscribeWith(new DisposableObserver<DistanceMatrixRestaurant>() {
                @Override
                public void onNext(DistanceMatrixRestaurant distanceMatrixRestaurant) {
                    setDistanceToList(finalI, distanceMatrixRestaurant);
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

            ReservationHelper.getReservationsCollection()
                    .whereEqualTo("mSelectedRestaurant", mListOfRestaurant.get(i).getResult().getPlaceId())
                    .whereEqualTo("mCreatedDate", mDayDate)
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
                                if (doc.get("mSelectedRestaurant") != null) {
                                    client.add(doc.getString("mSelectedRestaurant"));
                                }
                            }
                            setTheNumberOfReservation(finalI, client);
                        }
                    });
        }
    }

    private void setDistanceToList(int i, DistanceMatrixRestaurant distanceMatrixRestaurant){
        mNumberRequestMatrix ++;
        mListOfRestaurant.get(i).getResult().setDistance(distanceMatrixRestaurant.getRows().get(0).getElements().get(0).getDistance().getText());
        if (mNumberRequestMatrix == mListOfRestaurant.size()){
            mRestaurantAdapter.notifyDataSetChanged();
            mNumberRequestMatrix = 0;
        }
    }

    private void setTheNumberOfReservation(int i,List<String> client){
        mNumberRequestFirestore ++;
        String numberClient = "("+client.size()+")";
        mListOfRestaurant.get(i).getResult().setReservation(numberClient);
        if (mNumberRequestFirestore == mListOfRestaurant.size()){
            mRestaurantAdapter.notifyDataSetChanged();
            mNumberRequestFirestore = 0;
        }
    }

    private void configureRecyclerView(){
        this.mListOfRestaurant = new ArrayList<>();
        this.mRestaurantAdapter = new RestaurantsViewAdapter(Glide.with(this), this.mListOfRestaurant);
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

    public void updateRecyclerView(String placeId){
        queryLocation.put("placeid", placeId);
        disposable.add(PlaceApiStream.streamFetchDetailsPlace(queryLocation).subscribeWith(new DisposableObserver<DetailsRestaurant>() {
            @Override
            public void onNext(DetailsRestaurant restaurant) {
                mListOfRestaurant.clear();
                mListOfRestaurant.add(restaurant);
                getDistanceAndReservationForRestaurant();
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
}
