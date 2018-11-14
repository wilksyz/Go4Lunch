package com.antoine.go4lunch.controlers.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.antoine.go4lunch.Data.UserHelper;
import com.antoine.go4lunch.R;
import com.antoine.go4lunch.controlers.fragment.MapFragment;
import com.antoine.go4lunch.controlers.fragment.RestaurantViewFragment;
import com.antoine.go4lunch.controlers.fragment.WorkmatesFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;

import butterknife.BindView;

public class MainActivity extends BaseActivity {

    @BindView(R.id.myCoordinatorLayout) CoordinatorLayout coordinatorLayout;
    private DrawerLayout mDrawerLayout;
    private final FragmentManager mFragmentManager = getSupportFragmentManager();
    private final Fragment mMapFragment = new MapFragment();
    private final Fragment mRestaurantView = new RestaurantViewFragment();
    private final Fragment mWorkmatesView = new WorkmatesFragment();
    private Fragment active = mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_navigation);
        this.configureBottomNavigationView();
        this.checkIfUserConnected();
        this.configureToolBar();
        this.configureNavigationDrawer();
        this.configureShowFragment();

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
                                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.your_lunch:

                                return true;
                        }
                        return true;
                    }
                });
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void configureBottomNavigationView(){
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        mFragmentManager.beginTransaction().hide(active).show(mMapFragment).commit();
                        active = mMapFragment;
                        return true;
                    case R.id.navigation_dashboard:
                        mFragmentManager.beginTransaction().hide(active).show(mRestaurantView).commit();
                        active = mRestaurantView;
                        return true;
                    case R.id.navigation_notifications:
                        mFragmentManager.beginTransaction().hide(active).show(mWorkmatesView).commit();
                        active = mWorkmatesView;
                        return true;
                }
                return false;
            }
        });
    }

    private void configureShowFragment(){
        mFragmentManager.beginTransaction().add(R.id.fragment_layout, mWorkmatesView, "3").hide(mWorkmatesView).commit();
        mFragmentManager.beginTransaction().add(R.id.fragment_layout, mRestaurantView, "2").hide(mRestaurantView).commit();
        mFragmentManager.beginTransaction().add(R.id.fragment_layout,mMapFragment, "1").commit();
    }

    private void createUserInFirestore(){

        if (this.getCurrentUser() != null){

            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : null;
            String username = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();

            UserHelper.createUser(uid, username, urlPicture).addOnFailureListener(this.onFailureListener());
        }
    }

    private void showSnackBar( String message){
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                this.createUserInFirestore();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackBar(getString(R.string.sign_in_cancelled));
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackBar(getString(R.string.no_internet_connection));
                    return;
                }

                showSnackBar(getString(R.string.unknown_error));
                Log.e("TAG", "Sign-in error: ", response.getError());
            }
        }
    }

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }



}