package com.antoine.go4lunch.controlers.fragment;
import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.antoine.go4lunch.R;
import com.google.android.gms.tasks.OnFailureListener;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;


public abstract class BaseFragment extends Fragment {

    protected OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
            }
        };
    }






}
