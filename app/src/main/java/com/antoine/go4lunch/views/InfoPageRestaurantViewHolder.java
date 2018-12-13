package com.antoine.go4lunch.views;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.models.firestore.User;
import com.bumptech.glide.RequestManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InfoPageRestaurantViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.user_imageView) ImageView mUserProfileImage;
    @BindView(R.id.user_name_textview) TextView mProfileTextView;

    public InfoPageRestaurantViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateWithUserList(User user, RequestManager glide) {

    }
}
