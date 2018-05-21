package com.example.user.sensor;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created with love by Hari Nugroho on 20/05/2018 at 22.01.
 */
public class AlarmReceiver extends BroadcastReceiver {

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        String massage = "";
        final String title = intent.getStringExtra("title");
        Boolean content = intent.getBooleanExtra("content", true);
        final String deviceId = intent.getStringExtra("device_id");
        this.context = context;

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

        if(content){
            mDatabaseReference.child("device").child(deviceId).child("finishTime").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    setupFinishAlarm(dataSnapshot.getValue(Long.class), deviceId, title);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    private void saveStatusToDatabase(boolean status, String deviceId) {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mDatabaseReference.child("device").child(deviceId).child("status").setValue(status);
        if(!status) {
            mDatabaseReference.child("device").child(deviceId).child("startTime").removeValue();
            mDatabaseReference.child("device").child(deviceId).child("finishTime").removeValue();
        }
    }

    private void setupFinishAlarm(long finishTime, String deviceId, String deviceName){
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent startIntent = new Intent(context, AlarmReceiver.class);
        startIntent.putExtra("title", deviceName);
        startIntent.putExtra("content", false);
        startIntent.putExtra("device_id", deviceId);
        PendingIntent startPendingIntent = PendingIntent.getBroadcast(context, 1, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        manager.set(AlarmManager.RTC_WAKEUP, finishTime, startPendingIntent);
    }
}
