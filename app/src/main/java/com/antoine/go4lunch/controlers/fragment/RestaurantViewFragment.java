package com.antoine.go4lunch.controlers.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.antoine.go4lunch.R;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantViewFragment extends BaseFragment {

    @BindView(R.id.restaurant_recycler_view) RecyclerView mRecyclerView;

    public RestaurantViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_restaurant_view, container, false);
    }

}
