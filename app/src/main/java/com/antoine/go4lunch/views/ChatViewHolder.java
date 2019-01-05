package com.antoine.go4lunch.views;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.TextView;

import com.antoine.go4lunch.R;
import com.antoine.go4lunch.models.firestore.Message;

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
    private ConstraintSet mConstraintSet = new ConstraintSet();
    private ConstraintLayout mConstraintLayout;
    private String mDayDate;
    private DateFormat mDateFormat;

    public ChatViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mConstraintLayout = itemView.findViewById(R.id.constraint_chat);
        mConstraintSet.clone(mConstraintLayout);
        mDateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE);
        mDayDate = mDateFormat.format(new Date());
    }

    public void updateInterfaceWithMessage(Message message, String username){

        if (message.getmDateMessage() != null){
            this.mDateSend.setText(this.checkingDateOfTheMessage(message.getmDateMessage()));
        }

        this.mTextMessage.setText(message.getmMessage());
        this.mUsernameSender.setText(message.getmUsernameSender());
        if (message.getmUsernameSender().equals(username)){
            mConstraintSet.setHorizontalBias(mTextMessage.getId(), 1.0f);
            TransitionManager.beginDelayedTransition(mConstraintLayout);
            mConstraintSet.applyTo(mConstraintLayout);
        }
    }

    public String checkingDateOfTheMessage(Date date){
        String dateFormatted = mDateFormat.format(date);
        if (mDayDate.equals(dateFormatted)){
            DateFormat dfTime = new SimpleDateFormat("HH:mm", Locale.FRANCE);
            return dfTime.format(date);
        }else {
            return dateFormatted;
        }
    }
}
