package com.antoine.go4lunch.views;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.models.firestore.Reservation;
import com.bumptech.glide.RequestManager;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class WorkmatesAdapter extends FirestoreRecyclerAdapter<Reservation, WorkmatesViewHolder> {

    public interface Listener {
        void onDataChanged();
    }

    private final RequestManager mGlide;
    private Listener callback;
    private int mNumActivity;

    public WorkmatesAdapter(@NonNull FirestoreRecyclerOptions<Reservation> options, RequestManager glide, Listener callback, int numActivity) {
        super(options);
        this.mGlide = glide;
        this.callback = callback;
        this.mNumActivity = numActivity;
    }

    @Override
    public void onBindViewHolder(@NonNull WorkmatesViewHolder holder, int position, @NonNull Reservation model) {
        holder.updateWithUserList(model, this.mGlide, this.mNumActivity);
    }

    @NonNull
    @Override
    public WorkmatesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new WorkmatesViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_workmates_item, viewGroup, false));
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        this.callback.onDataChanged();
    }
}
