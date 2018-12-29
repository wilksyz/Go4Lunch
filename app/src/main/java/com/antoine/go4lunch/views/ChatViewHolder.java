package com.antoine.go4lunch.views;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.models.firestore.Message;
import com.bumptech.glide.RequestManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.textView_date_viewHolder) TextView mDateSend;
    @BindView(R.id.textView_message_viewHolder) TextView mTextMessage;
    @BindView(R.id.textView_username_viewHolder) TextView mUsernameSender;

    public ChatViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateInterfaceWithMessage(Message message, RequestManager glide, String username){

        if (message.getmDateMessage() != null){
            this.mDateSend.setText(this.convertDateToHour(message.getmDateMessage()));
        }

        this.mTextMessage.setText(message.getmMessage());
        this.mUsernameSender.setText(message.getmUsernameSender());
        if (message.getmUsernameSender().equals(username)){
            this.mTextMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        }
    }

    private String convertDateToHour(Date date){
        DateFormat dfTime = new SimpleDateFormat("HH:mm", Locale.FRANCE);
        return dfTime.format(date);
    }
}
