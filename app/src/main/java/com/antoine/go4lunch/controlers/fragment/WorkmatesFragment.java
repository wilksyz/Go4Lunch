package com.antoine.go4lunch.controlers.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.antoine.go4lunch.R;
import com.antoine.go4lunch.data.UserHelper;
import com.antoine.go4lunch.models.firestore.User;
import com.antoine.go4lunch.views.WorkmatesAdapter;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class WorkmatesFragment extends Fragment implements WorkmatesAdapter.Listener{

    @BindView(R.id.workmates_recyclerview_fragment) RecyclerView mWorkmatesRecyclerView;
    public WorkmatesAdapter mWorkmatesAdapter;
    public WorkmatesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);
        ButterKnife.bind(this, view);
        configureRecyclerView();


        return view;
    }

    private void configureRecyclerView(){
        this.mWorkmatesAdapter = new WorkmatesAdapter(generateOptionsForAdapter(UserHelper.getAllUser()),Glide.with(this),this);
        mWorkmatesRecyclerView.setAdapter(this.mWorkmatesAdapter);
        mWorkmatesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private FirestoreRecyclerOptions<User> generateOptionsForAdapter(Query query){
        return new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .setLifecycleOwner(this)
                .build();
    }

    @Override
    public void onDataChanged() {

    }
}
