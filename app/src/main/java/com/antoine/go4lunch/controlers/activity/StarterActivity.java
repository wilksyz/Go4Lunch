package com.antoine.go4lunch.controlers.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.data.ReservationHelper;
import com.antoine.go4lunch.data.UserHelper;
import com.antoine.go4lunch.models.firestore.User;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StarterActivity extends BaseActivity {

    @BindView(R.id.sign_in_button) Button mSignInButton;
    @BindView(R.id.myCoordinatorLayout) CoordinatorLayout coordinatorLayout;
    protected static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        ButterKnife.bind(this);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignInActivity();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void startSignInActivity(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                new AuthUI.IdpConfig.FacebookBuilder().build(),
                                new AuthUI.IdpConfig.GoogleBuilder().build()))
                        .setIsSmartLockEnabled(false, true)
                        .setTheme(R.style.LoginTheme)
                        //.setLogo(R.mipmap.ic_chef)
                        .build(),
                RC_SIGN_IN);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            // Successfully signed in
            if (resultCode == RESULT_OK) {
                this.createUserInFirestore();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
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

    private void createUserInFirestore(){

        if (this.getCurrentUser() != null){

            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : null;
            String username = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();

            UserHelper.createUser(uid, username, urlPicture).addOnFailureListener(this.onFailureListener());

            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE);
            String date = df.format(new Date());
            User user = new User(uid, username, urlPicture);
            ReservationHelper.createReservation(uid, date, user).addOnFailureListener(this.onFailureListener());
        }
    }

    private OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
            }
        };
    }

    private void showSnackBar( String message){
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }
}
