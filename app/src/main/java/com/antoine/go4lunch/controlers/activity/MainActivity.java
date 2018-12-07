package com.antoine.go4lunch.controlers.activity;

import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.controlers.fragment.MapFragment;
import com.antoine.go4lunch.controlers.fragment.RestaurantViewFragment;
import com.antoine.go4lunch.controlers.fragment.WorkmatesFragment;
import com.antoine.go4lunch.data.UserHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @BindView(R.id.navigation) BottomNavigationView mBottomNavigation;
    private DrawerLayout mDrawerLayout;
    private final FragmentManager mFragmentManager = getSupportFragmentManager();
    private final Fragment mMapFragment = new MapFragment();
    private final Fragment mRestaurantView = new RestaurantViewFragment();
    private final Fragment mWorkmatesView = new WorkmatesFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_navigation);
        ButterKnife.bind(this);
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
                                Intent intentSettings = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(intentSettings);
                                return true;
                            case R.id.your_lunch:
                                if (getCurrentUser() != null){
                                    UserHelper.getUser(getCurrentUser().getUid())
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> documentSnapshotTask) {
                                                    if (documentSnapshotTask.isSuccessful()) {
                                                        DocumentSnapshot document = documentSnapshotTask.getResult();
                                                        if (document.exists()) {
                                                            String myRestaurant = document.getString("myRestaurant");
                                                            if (myRestaurant != null){
                                                                Intent intentYourLunch = new Intent(MainActivity.this, InfoPageRestaurantActivity.class);
                                                                intentYourLunch.putExtra("placeId", myRestaurant);
                                                                startActivity(intentYourLunch);
                                                            }else{
                                                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.AlertDialogCustom));
                                                                builder.setTitle(getResources().getString(R.string.YOUR_LUNCH))
                                                                        .setMessage(getResources().getString(R.string.YOU_HAVE_NOT_CHOSEN_A_RESTAURANT_FOR_LUNCH_YET))
                                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                            }
                                                                        })
                                                                        .create()
                                                                        .show();
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
        mBottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        Log.e("TAG","Passage home");
                        mFragmentManager.beginTransaction().replace(R.id.fragment_layout,mMapFragment,"1").addToBackStack(null).commit();
                        return true;
                    case R.id.navigation_dashboard:
                        Log.e("TAG","Passage list view");
                        mFragmentManager.beginTransaction().replace(R.id.fragment_layout,mRestaurantView,"2").addToBackStack(null).commit();
                        return true;
                    case R.id.navigation_notifications:
                        Log.e("TAG","Passage workmates");
                        mFragmentManager.beginTransaction().replace(R.id.fragment_layout,mWorkmatesView,"3").addToBackStack(null).commit();
                        return true;
                }
                return false;
            }
        });
    }

    private void configureShowFragment(){
        mFragmentManager.beginTransaction().add(R.id.fragment_layout,mMapFragment, "1").commit();
        mFragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_layout);
                switch (f.getTag()){
                    case "1":

                        break;
                    case "2":

                        break;
                    case "3":

                        break;
                }
            }
        });
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

    private void checkIfUserConnected() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            // already signed in
            Intent intent = new Intent(this, StarterActivity.class);
            startActivity(intent);
        }
    }
}
