package com.antoine.go4lunch.controlers.activity;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public abstract class BaseActivity extends AppCompatActivity {

    protected static final int SIGN_OUT_TASK = 10;
    protected static final int DELETE_USER_TASK = 20;

    @Nullable
    protected FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser(); }

    protected OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin){
                    case SIGN_OUT_TASK:
                        recreate();
                        break;
                    case DELETE_USER_TASK:
                        recreate();
                        break;
                    default:
                        break;
                }
            }
        };
    }
}
