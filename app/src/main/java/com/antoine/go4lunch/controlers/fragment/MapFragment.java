package com.antoine.go4lunch.controlers.fragment;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.controlers.activity.InfoPageRestaurantActivity;
import com.antoine.go4lunch.data.PlaceApiStream;
import com.antoine.go4lunch.data.RestaurantHelper;
import com.antoine.go4lunch.models.placeAPI.placeDetails.DetailsRestaurant;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends BaseFragment implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    @BindView(R.id.mapView)
    MapView mMapView;
    private boolean mLocationPermissionGranted;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1918;
    protected static final int DEFAULT_ZOOM = 15;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private CameraPosition mCameraPosition;
    CompositeDisposable disposables = new CompositeDisposable();
    Map<String,String> queryLocation = new HashMap<>();
    protected FusedLocationProviderClient mFusedLocationProviderClient;
    protected Location mLastKnownLocation;
    protected String mLocation;
    protected GoogleMap mGoogleMap;
    protected List<DetailsRestaurant> mListOfRestaurant;
    protected LatLng mCurentLocation;

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        queryLocation.put("key", getString(R.string.google_maps_api));



        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        this.updateLocationUI();

    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getLastLocation();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getLastLocation();
                }
            }
        }
        this.updateLocationUI();
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
                            if (mGoogleMap != null){
                                centerCameraOnLocation();
                                executeHttpRequestListOfRestaurant();
                            }
                        } else {
                            Log.w("TAG", "getLastLocation:exception", task.getException());
                        }
                    }
                });
    }

    private void centerCameraOnLocation() {
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mLastKnownLocation.getLatitude(),
                        mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
    }

    private void updateLocationUI() {
        if (mGoogleMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mGoogleMap.setMyLocationEnabled(false);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void executeHttpRequestListOfRestaurant(){
        disposables.add(PlaceApiStream.streamFetchListRestaurants(queryLocation).subscribeWith(getDisposable()));
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
        mListOfRestaurant = listOfRestaurant;
        if (mGoogleMap != null){
            addMarkerMap();
        }
        createRestaurantInFirestore();
    }

    private void addMarkerMap(){
        int size = mListOfRestaurant.size();
        for (int i = 0; i<size; i++){
            mCurentLocation = new LatLng(mListOfRestaurant.get(i).getResult().getGeometry().getLocation().getLat(), mListOfRestaurant.get(i).getResult().getGeometry().getLocation().getLng());
            Marker mSydney = mGoogleMap.addMarker(new MarkerOptions()
                    .position(mCurentLocation)
                    .title(mListOfRestaurant.get(i).getResult().getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            mSydney.setTag(i);
        }
        mGoogleMap.setOnMarkerClickListener(this);
    }

    private void createRestaurantInFirestore(){
        int size = mListOfRestaurant.size();
        for (int i = 0; i<size; i++){
            int finalI = i;
            DocumentReference db = RestaurantHelper.getRestaurantsCollection().document(mListOfRestaurant.get(i).getResult().getPlaceId());
            db.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> documentSnapshotTask) {
                            if (documentSnapshotTask.isSuccessful()) {
                                DocumentSnapshot document = documentSnapshotTask.getResult();
                                if ( document.exists()) {
                                    Log.d("TAG", "The document already exists");
                                } else {
                                    String placeId = mListOfRestaurant.get(finalI).getResult().getPlaceId();
                                    RestaurantHelper.createRestaurant(placeId).addOnFailureListener(onFailureListener());
                                }
                            } else {
                                Log.d("TAG", "get failed with ", documentSnapshotTask.getException());
                            }
                        }
                    });
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Integer clickCount = (Integer) marker.getTag();
        Log.e("TAG", "clic marker"+marker.getTag());
        Intent intent = new Intent(this.getActivity(), InfoPageRestaurantActivity.class);
        intent.putExtra("placeId", mListOfRestaurant.get(clickCount).getResult().getPlaceId());
        startActivity(intent);
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.getLocationPermission();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        this.disposeWhenDestroy();
    }

    private void disposeWhenDestroy(){
        if (this.disposables != null && !this.disposables.isDisposed()) this.disposables.dispose();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mGoogleMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mGoogleMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
