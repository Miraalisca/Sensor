package com.example.user.sensor.notification;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by USER on 1/16/2018.
 */

public class FirebaseInstance extends FirebaseInstanceIdService {
    private static final String REG_TOKEN =  "REG_TOKEN";

    @Override
    /**
     * Get updated InstanceID token
     */
    public void onTokenRefresh() {
        String recent_token = FirebaseInstanceId.getInstance().getToken();
        Log.i("Token", "onTokenRefresh: " + recent_token);
        PrefManager prefManager = new PrefManager(FirebaseInstance.this);
        prefManager.setToken(recent_token);
    }
}

