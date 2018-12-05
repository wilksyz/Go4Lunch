package com.antoine.go4lunch.controlers.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.controlers.fragment.MapFragment;
import com.antoine.go4lunch.controlers.fragment.RestaurantViewFragment;
import com.antoine.go4lunch.controlers.fragment.WorkmatesFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;

public class MainActivity extends BaseActivity {


    private DrawerLayout mDrawerLayout;
    private final FragmentManager mFragmentManager = getSupportFragmentManager();
    private final Fragment mMapFragment = new MapFragment();
    private final Fragment mRestaurantView = new RestaurantViewFragment();
    private final Fragment mWorkmatesView = new WorkmatesFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_navigation);
        this.configureBottomNavigationView();
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

    private Boolean isCurrentUserLogged(){
        return (this.getCurrentUser() != null); }

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
                        mFragmentManager.beginTransaction().replace(R.id.fragment_layout,mMapFragment).addToBackStack(null).commit();
                        return true;
                    case R.id.navigation_dashboard:
                        mFragmentManager.beginTransaction().replace(R.id.fragment_layout,mRestaurantView).addToBackStack(null).commit();
                        return true;
                    case R.id.navigation_notifications:
                        mFragmentManager.beginTransaction().replace(R.id.fragment_layout,mWorkmatesView).addToBackStack(null).commit();
                        return true;
                }
                return false;
            }
        });
    }

    private void configureShowFragment(){
        mFragmentManager.beginTransaction().add(R.id.fragment_layout,mMapFragment, "1").commit();
    }

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }
}
