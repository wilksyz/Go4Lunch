package com.antoine.go4lunch.controlers.activity;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.antoine.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.notification_switch) Switch mNotificationSwitchButton;
    private boolean mStatusNotification;
    private static final String STATUS_NOTIFICATION = "status notification";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        mStatusNotification = getSharedPreferences("Notification", MODE_PRIVATE).getBoolean(STATUS_NOTIFICATION, true);
        mNotificationSwitchButton.setChecked(mStatusNotification);

        mNotificationSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor saveSettings = getSharedPreferences("Notification", MODE_PRIVATE).edit();
                if (isChecked) {
                    // The toggle is enabled
                    mStatusNotification = true;
                    saveSettings.putBoolean(STATUS_NOTIFICATION, mStatusNotification);
                    saveSettings.apply();
                }else {
                    mStatusNotification = false;
                    saveSettings.putBoolean(STATUS_NOTIFICATION, mStatusNotification);
                    saveSettings.apply();
                }
            }
        });
    }
}
