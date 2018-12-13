package com.antoine.go4lunch.views;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.data.PlaceApiStream;
import com.antoine.go4lunch.models.firestore.User;
import com.antoine.go4lunch.models.placeAPI.placeDetails.DetailsRestaurant;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.HashMap;
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

    protected void updateWithUserList(User user, RequestManager glide, int numActivity){
        if (user.getUrlPicture() != null){
            glide.load(user.getUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(mUserProfileImage);
        }
        if (numActivity == 1){
            mProfileTextView.setEnabled(false);
            mProfileTextView.setText(configureTextView(user));
        }else if (numActivity == 2){
            String text = user.getUsername()+" "+itemView.getContext().getString(R.string.IS_JOINING);
            mProfileTextView.setText(text);
        }

    }

    private String configureTextView(User user){
        mUsername = user.getUsername();
        String idRestaurant;
        if (user.getMyRestaurant() != null){
            idRestaurant = user.getMyRestaurant();
            queryLocation.put("placeid", idRestaurant);
            executeHttpRequestListOfRestaurant();
            mProfileTextView.setEnabled(true);
            return mUsername;
        }else{
            String text = mUsername+" "+ itemView.getContext().getString(R.string.HASNT_DECIDED_YET);
            return text;
        }
    }

    private void executeHttpRequestListOfRestaurant(){
        disposables.add(PlaceApiStream.streamFetchDetailsPlace(queryLocation).subscribeWith(getDisposable()));
    }

    private DisposableObserver<DetailsRestaurant> getDisposable(){
        //manages so api request is OK or not OK
        return new DisposableObserver<DetailsRestaurant>() {
            @Override
            public void onNext(DetailsRestaurant listOfRestaurant) {
                String text = mUsername+" "+itemView.getContext().getString(R.string.IS_EATING_TO)+listOfRestaurant.getResult().getName()+")";
                mProfileTextView.setText(text);
            }
            @Override
            public void onError(Throwable e) {
                Log.e("TAG","On Error",e);
            }

            @Override
            public void onComplete() {
                Log.i("TAG","On Complete !!");
            }
        };
    }
}
