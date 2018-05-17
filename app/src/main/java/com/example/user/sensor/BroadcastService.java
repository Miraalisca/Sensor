package com.example.user.sensor;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

/**
 * Created with love by Hari Nugroho on 04/05/2018 at 20.32.
 */
public class BroadcastService  extends Service {

    private final static String TAG = "BroadcastService";

    public static final String ARG_ID = "ARG_ID";
    public static final String ARG_TIMER = "ARG_TIMER";

    public static final String COUNTDOWN_BR = "com.example.user.sensor.";
    private String deviceId = "";
    private long milisInFuture = 0;

    CountDownTimer cdt = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        cdt.cancel();
        Log.i(TAG, "Timer cancelled");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        deviceId = intent.getStringExtra(ARG_ID);
        milisInFuture = intent.getLongExtra(ARG_TIMER, 0);
        Log.i(TAG, "onStartCommand: " + milisInFuture + " | " + deviceId);

        Log.i(TAG, "Starting milisInFuture...");
        final Intent bi = new Intent(COUNTDOWN_BR + deviceId);

        Log.i(TAG, "onCreate: " + milisInFuture + " | " + deviceId);
        cdt = new CountDownTimer(milisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, "onTick: " + deviceId + ": " + millisUntilFinished/1000);
                bi.putExtra("countdown", millisUntilFinished);
                sendBroadcast(bi);
            }

            @Override
            public void onFinish() {
                Log.i(TAG, "Timer finished");
            }
        };

        cdt.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
