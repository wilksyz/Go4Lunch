package com.antoine.go4lunch.views;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.data.PlaceApiStream;
import com.antoine.go4lunch.models.firestore.Reservation;
import com.antoine.go4lunch.models.firestore.User;
import com.antoine.go4lunch.models.placeAPI.placeDetails.DetailsRestaurant;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class WorkmatesViewHolder extends RecyclerView.ViewHolder{

    @BindView(R.id.user_imageView) ImageView mUserProfileImage;
    @BindView(R.id.user_name_textview) TextView mProfileTextView;
    private String mUsername;
    private Map<String,String> queryLocation = new HashMap<>();
    private CompositeDisposable disposables = new CompositeDisposable();

    protected WorkmatesViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        queryLocation.put("key", itemView.getContext().getString(R.string.google_maps_api));
    }

    protected void updateWithUserList(Reservation user, RequestManager glide, int numActivity){
        if (user.getmUser() != null){
            if (user.getmUser().getUrlPicture() != null){
                glide.load(user.getmUser().getUrlPicture())
                        .apply(RequestOptions.circleCropTransform())
                        .into(mUserProfileImage);
            }

            if (numActivity == 1){
                DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE);
                String date = df.format(new Date());
                if (date.equals(user.getmCreatedDate()) && user.getmSelectedRestaurant() != null){
                    mProfileTextView.setEnabled(true);
                    mProfileTextView.setText(configureTextView(user, 1));
                }else{
                    mProfileTextView.setEnabled(false);
                    mProfileTextView.setText(configureTextView(user, 2));
                }
            }else if (numActivity == 2){
                String text = user.getmUser().getUsername()+" "+itemView.getContext().getString(R.string.IS_JOINING);
                mProfileTextView.setText(text);
            }
        }
    }

    private String configureTextView(Reservation user, int indicateur){
        if (indicateur == 1){
            mUsername = user.getmUser().getUsername()+" "+itemView.getContext().getString(R.string.IS_EATING_TO)+user.getmRestaurantName()+")";
        }else if (indicateur == 2){
            mUsername = user.getmUser().getUsername()+" "+itemView.getContext().getString(R.string.HASNT_DECIDED_YET);
        }
        return mUsername;
    }
}
