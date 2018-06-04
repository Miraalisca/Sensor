package com.example.user.sensor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created with love by Hari Nugroho on 04/06/2018 at 11.10.
 */
public class AlarmReceiver  extends BroadcastReceiver {
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        Boolean content = intent.getBooleanExtra("content", true);
        String deviceId = intent.getStringExtra("device_id");
        saveStatusToDatabase(content, deviceId);
        createNotification(context, title, deviceId, content);
    }

    private void createNotification(Context context, String title, String deviceId, Boolean content) {
        String massage;
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
    }
}
