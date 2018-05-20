package com.example.user.sensor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created with love by Hari Nugroho on 20/05/2018 at 22.01.
 */
public class AlarmReceiver extends BroadcastReceiver {

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;

    @Override
    public void onReceive(Context context, Intent intent) {
        String massage = "";
        String title = intent.getStringExtra("title");
        Boolean content = intent.getBooleanExtra("content", true);
        String deviceId = intent.getStringExtra("device_id");

        saveStatusToDatabase(content, deviceId);

        if(content){
            massage = "Device is turn On";
        } else {
            massage = "Device is turn Off";
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(massage);

        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId = Integer.parseInt(deviceId.substring(9, 12));
        if(content){
            mNotificationId += 10000;
        } else {
            mNotificationId += 20000;
        }

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }


    private void saveStatusToDatabase(boolean status, String deviceId) {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mDatabaseReference.child("device").child(deviceId).child("status").setValue(status);
        if(status) {
            mDatabaseReference.child("device").child(deviceId).child("startTime").setValue(0);
        } else {
            mDatabaseReference.child("device").child(deviceId).child("finishTime").setValue(0);
        }
    }
}
