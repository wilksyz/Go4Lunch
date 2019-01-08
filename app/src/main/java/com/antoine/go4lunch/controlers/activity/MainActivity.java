package com.antoine.go4lunch.controlers.activity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.controlers.fragment.MapFragment;
import com.antoine.go4lunch.controlers.fragment.RestaurantViewFragment;
import com.antoine.go4lunch.controlers.fragment.WorkmatesFragment;
import com.antoine.go4lunch.data.ReservationHelper;
import com.antoine.go4lunch.data.notifications.NotificationsService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.maps.android.SphericalUtil;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @BindView(R.id.navigation) BottomNavigationView mBottomNavigation;
    private DrawerLayout mDrawerLayout;
    private final FragmentManager mFragmentManager = getSupportFragmentManager();
    private final MapFragment mMapFragment = new MapFragment();
    private final RestaurantViewFragment mRestaurantView = new RestaurantViewFragment();
    private final WorkmatesFragment mWorkmatesView = new WorkmatesFragment();
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1914;
    private static final String LATITUDE_LOCATION = "latitude location";
    private static final String LONGITUDE_LOCATION = "longitude location";
    private String mDayDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_navigation);
        ButterKnife.bind(this);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE);
        mDayDate = df.format(new Date());
        this.setTitle(R.string.android_label);
        this.configureBottomNavigationView();
        this.configureToolBar();
        this.configureNavigationDrawer();
        this.configureShowFragment();
        this.setAlarm();
    }

    private void setAlarm(){
        AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intents = new Intent(this, NotificationsService.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intents, 0);


        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, getTimeInMillisForAlarm(), AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    public static long getTimeInMillisForAlarm(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        long timeInMillis =calendar.getTimeInMillis();
        if (calendar.before(Calendar.getInstance())){
            timeInMillis = timeInMillis + AlarmManager.INTERVAL_DAY;
        }
        return timeInMillis;
    }

    private void configureNavigationDrawer(){
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View navHeaderView = navigationView.inflateHeaderView(R.layout.nav_header);
        TextView headerUserName = navHeaderView.findViewById(R.id.text_user_name);
        TextView headerMailUser = navHeaderView.findViewById(R.id.text_mail_user);
        ImageView mImageProfile = navHeaderView.findViewById(R.id.image_profile);
        updateWithUserInformation(headerUserName, headerMailUser, mImageProfile);

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();
                        switch (menuItem.getItemId()) {
                            case R.id.logout:
                                signOutUserFromFirebase();
                                return true;
                            case R.id.settings:
                                Intent intentSettings = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(intentSettings);
                                return true;
                            case R.id.chat:
                                Intent intentChat = new Intent(MainActivity.this, ChatActivity.class);
                                startActivity(intentChat);
                                return true;
                            case R.id.your_lunch:
                                if (getCurrentUser() != null){
                                    getYourLunch();
                                }
                                return true;
                        }
                        return true;
                    }
                });
    }

    private void getYourLunch(){
        ReservationHelper.getReservation(getCurrentUser().getUid())
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> documentSnapshotTask) {
                        if (documentSnapshotTask.isSuccessful()) {
                            DocumentSnapshot document = documentSnapshotTask.getResult();
                            if (document.exists()) {
                                String myRestaurant = document.getString("mSelectedRestaurant");
                                String dateDatabase = document.getString("mCreatedDate");
                                if (myRestaurant != null && mDayDate.equals(dateDatabase)){
                                    Intent intentYourLunch = new Intent(MainActivity.this, InfoPageRestaurantActivity.class);
                                    intentYourLunch.putExtra("placeId", myRestaurant);
                                    startActivity(intentYourLunch);
                                }else{
                                    displayAlertDialog();
                                }
                            } else {
                                Log.d("TAG", "No such document");
                            }
                        } else {
                            Log.d("TAG", "get failed with ", documentSnapshotTask.getException());
                        }
                    }
                });
    }

    private void displayAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.AlertDialogCustom));
        builder.setTitle(getResources().getString(R.string.YOUR_LUNCH))
                .setMessage(getResources().getString(R.string.YOU_HAVE_NOT_CHOSEN_A_RESTAURANT_FOR_LUNCH_YET))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                })
                .create()
                .show();
    }

    private void updateWithUserInformation(TextView headerUserName, TextView headerMailUser, ImageView mImageProfile) {
        if (this.getCurrentUser() != null){

            if (this.getCurrentUser().getPhotoUrl() != null){
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(mImageProfile);
            }
            String email = TextUtils.isEmpty(this.getCurrentUser().getEmail()) ? getString(R.string.info_no_email_found) : this.getCurrentUser().getEmail();
            String username = TextUtils.isEmpty(this.getCurrentUser().getDisplayName()) ? getString(R.string.info_no_username_found) : this.getCurrentUser().getDisplayName();

            //Update views with data
            headerUserName.setText(username);
            headerMailUser.setText(email);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    private void configureToolBar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
    }

    public static LatLngBounds toBounds(LatLng center) {
        double distanceFromCenterToCorner = 2000 * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.app_bar_search:
                Double latitude = Double.parseDouble(Objects.requireNonNull(getSharedPreferences("Location", MODE_PRIVATE).getString(LATITUDE_LOCATION, "48.858128"))) ;
                Double longitude = Double.parseDouble(Objects.requireNonNull(getSharedPreferences("Location", MODE_PRIVATE).getString(LONGITUDE_LOCATION, "2.294288"))) ;
                LatLng location = new LatLng(latitude, longitude);
                try {
                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                            .setCountry("FR")
                            .build();

                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setFilter(typeFilter)
                            .setBoundsBias(toBounds(location))
                            .build(this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                if (mMapFragment != null && mMapFragment.isVisible()){
                    mMapFragment.updateUiAfterSearchWithSearchBar(place.getId());
                }else if (mRestaurantView != null && mRestaurantView.isVisible()){
                    mRestaurantView.updateRecyclerView(place.getId());
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("TAG", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void configureBottomNavigationView(){
        mBottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        mFragmentManager.beginTransaction().replace(R.id.fragment_layout,mMapFragment,"1").addToBackStack("1").commit();
                        return true;
                    case R.id.navigation_dashboard:
                        mFragmentManager.beginTransaction().replace(R.id.fragment_layout,mRestaurantView,"2").addToBackStack("2").commit();
                        return true;
                    case R.id.navigation_notifications:
                        mFragmentManager.beginTransaction().replace(R.id.fragment_layout,mWorkmatesView,"3").addToBackStack("3").commit();
                        return true;
                }
                return false;
            }
        });
    }

    private void configureShowFragment(){
        mFragmentManager.beginTransaction().add(R.id.fragment_layout,mMapFragment, "1").addToBackStack("1").commit();
    }

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkIfUserConnected();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        this.checkIfUserConnected();
    }

    private void checkIfUserConnected() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            // already signed in
            Intent intent = new Intent(this, StarterActivity.class);
            startActivity(intent);
        }
    }

}
