package com.example.user.sensor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.user.sensor.adapter.DeviceListAdapter;
import com.example.user.sensor.model.Device;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private ListView mListViewDevice;
    private TextView mTextViewDataIsEmpty;

    private FirebaseUser mCurrentUser;
    private DeviceListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mListViewDevice = findViewById(R.id.list_view_device);
        mTextViewDataIsEmpty = findViewById(R.id.text_view_empty_view);

        mListViewDevice.setEmptyView(mTextViewDataIsEmpty);

        final ArrayList<Device> devices = readDeviceData();

        mAdapter = new DeviceListAdapter(this, devices);
        mListViewDevice.setAdapter(mAdapter);

        mListViewDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra(DetailActivity.ARGS_DEVICE_NAME, devices.get(i).getDeviceName());
                intent.putExtra(DetailActivity.ARGS_DEVICE_ID, devices.get(i).getDeviceId());
                intent.putExtra(DetailActivity.ARGS_PUSH_ID, devices.get(i).getPushKey());
                startActivity(intent);
            }
        });
    }

    public void goToReader(View view) {
        Intent intent = new Intent(MainActivity.this, ReaderActivity.class);
        startActivity(intent);
    }

    private ArrayList<Device> readDeviceData() {
        final ArrayList<Device> devicesData = new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        final DatabaseReference myRef = database.getReference();
        myRef.keepSynced(true);
        myRef.child("user").child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                devicesData.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    final String deviceId = userSnapshot.getValue(String.class);
                    final String pushKey = userSnapshot.getKey();

                    final Device device = new Device(pushKey, deviceId);
                    devicesData.add(device);

                    myRef.child("device").child(deviceId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                String deviceName = dataSnapshot.child("name").getValue(String.class);
                                boolean status = dataSnapshot.child("status").getValue(boolean.class);

                                device.setDeviceName(deviceName);
                                device.setStatus(status);
                            } catch (NullPointerException e){
                                myRef.child("device").child(deviceId).child("status").setValue(false);
                                device.setStatus(false);
                            }
                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
        return devicesData;
    }
}
