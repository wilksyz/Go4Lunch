package com.antoine.go4lunch.controlers.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.data.UserHelper;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

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
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() == null) {
                    // already signed in
                    startSignInActivity();
                }else{
                    Intent intent = new Intent(StarterActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkIfUserConnected();
    }

    private void checkIfUserConnected() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
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
                        .setLogo(R.mipmap.ic_google)
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
