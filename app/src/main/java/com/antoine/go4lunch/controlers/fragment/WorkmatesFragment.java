package com.antoine.go4lunch.controlers.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.controlers.activity.InfoPageRestaurantActivity;
import com.antoine.go4lunch.data.ReservationHelper;
import com.antoine.go4lunch.models.ItemClickSupport;
import com.antoine.go4lunch.models.firestore.Reservation;
import com.antoine.go4lunch.views.WorkmatesAdapter;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

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
        configureOnClickRecyclerView();

        return view;
    }

    private void configureRecyclerView(){
        this.mWorkmatesAdapter = new WorkmatesAdapter(generateOptionsForAdapter(ReservationHelper.getAllReservation()),Glide.with(this),this, 1);
        mWorkmatesRecyclerView.setAdapter(this.mWorkmatesAdapter);
        mWorkmatesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private FirestoreRecyclerOptions<Reservation> generateOptionsForAdapter(Query query){
        return new FirestoreRecyclerOptions.Builder<Reservation>()
                .setQuery(query, Reservation.class)
                .setLifecycleOwner(this)
                .build();
    }

    private void configureOnClickRecyclerView(){
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE);
        String date = df.format(new Date());
        ItemClickSupport.addTo(mWorkmatesRecyclerView, R.layout.fragment_workmates_item)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Reservation user = mWorkmatesAdapter.getItem(position);
                        if (date.equals(user.getmCreatedDate()) && user.getmSelectedRestaurant() != null){
                            String placeId = user.getmSelectedRestaurant();
                            Intent intent = new Intent(getActivity(), InfoPageRestaurantActivity.class);
                            intent.putExtra("placeId", placeId);
                            startActivity(intent);
                        }else{
                            Toast.makeText(getContext(), R.string.YOU_HAVE_NOT_CHOSEN_A_RESTAURANT_FOR_LUNCH_YET, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onDataChanged() {

    }
}
