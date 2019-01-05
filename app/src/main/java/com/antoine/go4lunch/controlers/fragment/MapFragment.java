package com.antoine.go4lunch.controlers.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.controlers.activity.InfoPageRestaurantActivity;
import com.antoine.go4lunch.data.PlaceApiStream;
import com.antoine.go4lunch.data.ReservationHelper;
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
public class MapFragment extends Fragment implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    @BindView(R.id.mapView) MapView mMapView;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 191;
    private static final String LATITUDE_LOCATION = "latitude location";
    private static final String LONGITUDE_LOCATION = "longitude location";
    private static final String LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    protected static final int DEFAULT_ZOOM = 18;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private CameraPosition mCameraPosition;
    CompositeDisposable disposable = new CompositeDisposable();
    Map<String,String> queryLocation = new HashMap<>();
    protected FusedLocationProviderClient mFusedLocationProviderClient;
    protected Location mLastKnownLocation;
    protected String mLocation;
    protected GoogleMap mGoogleMap;
    protected List<DetailsRestaurant> mListOfRestaurant;
    protected LatLng mCurentLocation;
    private Bundle msavedInstanceState;
    private String mDayDate;
    private Marker mMarker;
    private Marker[] mMarkerList = new Marker[20];

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, view);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE);
        mDayDate = df.format(new Date());
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        msavedInstanceState = savedInstanceState;
        createMap(savedInstanceState);
        queryLocation.put("key", getString(R.string.google_maps_api));
        checkPermissionAndLoadTheMap();
        return view;
    }

    private void createMap(Bundle savedInstanceState){
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        this.updateLocationUI();
    }

    private boolean hasLocationPermissions() {
        return EasyPermissions.hasPermissions(getContext(), LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
    private void checkPermissionAndLoadTheMap() {
        if (hasLocationPermissions()) {
            // Have permission, do the thing!
            createMap(msavedInstanceState);
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
            if (hasLocationPermissions()) {
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mGoogleMap.setMyLocationEnabled(false);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
            }
        } catch (SecurityException e)  {
            Log.e("Exception: ", e.getMessage());
        }
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
        mListOfRestaurant = listOfRestaurant;
        if (mGoogleMap != null){
            checkReservationOfTheRestaurant();
        }
    }

    private void checkReservationOfTheRestaurant(){
        int size = mListOfRestaurant.size();
        for (int i = 0; i<size; i++){
            int finalI = i;
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
                            int numberClient = client.size();
                            addMarkerMap(numberClient, finalI);
                        }
                    });
        }
    }

    private void addMarkerMap(int numberClient, int i){
        if (mMarkerList[i] != null){
            mMarkerList[i].remove();
        }

        boolean notReserved = numberClient == 0;
            mCurentLocation = new LatLng(mListOfRestaurant.get(i).getResult().getGeometry().getLocation().getLat(), mListOfRestaurant.get(i).getResult().getGeometry().getLocation().getLng());
            mMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(mCurentLocation)
                    .title(mListOfRestaurant.get(i).getResult().getName())
                    .icon(BitmapDescriptorFactory.fromResource(notReserved ? R.mipmap.ic_marker_not_selected : R.mipmap.ic_marker_selected)));
        mMarker.setTag(i);
        mMarkerList[i] = mMarker;
        mGoogleMap.setOnMarkerClickListener(this);
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
        if (this.disposable != null && !this.disposable.isDisposed()) this.disposable.dispose();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mGoogleMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mGoogleMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    public void updateUiAfterSearchWithSearchBar(String placeId){
        queryLocation.put("placeid", placeId);
        disposable.add(PlaceApiStream.streamFetchDetailsPlace(queryLocation).subscribeWith(new DisposableObserver<DetailsRestaurant>() {
            @Override
            public void onNext(DetailsRestaurant restaurant) {
                mListOfRestaurant.clear();
                mListOfRestaurant.add(restaurant);
                mGoogleMap.clear();
                checkReservationOfTheRestaurant();
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

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
