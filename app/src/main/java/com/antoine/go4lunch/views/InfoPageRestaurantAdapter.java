package com.antoine.go4lunch.views;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.models.firestore.User;
import com.bumptech.glide.RequestManager;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class InfoPageRestaurantAdapter extends FirestoreRecyclerAdapter<User, InfoPageRestaurantViewHolder> {

    public interface Listener {
        void onDataChanged();
    }

    private final RequestManager mGlide;
    private Listener callback;


    public InfoPageRestaurantAdapter(@NonNull FirestoreRecyclerOptions<User> options, RequestManager mGlide, Listener callback) {
        super(options);
        this.mGlide = mGlide;
        this.callback = callback;
    }

    @Override
    protected void onBindViewHolder(@NonNull InfoPageRestaurantViewHolder holder, int position, @NonNull User model) {
        holder.updateWithUserList(model, this.mGlide);
    }

    @NonNull
    @Override
    public InfoPageRestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new InfoPageRestaurantViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_workmates_item, viewGroup, false));
    }
}
