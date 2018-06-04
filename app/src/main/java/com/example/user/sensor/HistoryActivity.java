package com.example.user.sensor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.user.sensor.adapter.HistoryAdapter;
import com.example.user.sensor.model.DeviceHistory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class HistoryActivity extends AppCompatActivity {

    public static final String ARG_DEVICE_ID = "DEVICE_ID";

    private HistoryAdapter mHistoryAdapter;
    private RecyclerView mListHistory;

    FirebaseDatabase mFirebaseDatabase;
    private FirebaseUser mCurrentUser;
    DatabaseReference mDatabaseReference;

    private ArrayList<DeviceHistory> recordKWH;
    private String mDeviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mListHistory = findViewById(R.id.list_history);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        mDeviceId = intent.getStringExtra(ARG_DEVICE_ID);

        recordKWH = new ArrayList<>();
        mListHistory.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mListHistory.setLayoutManager(linearLayoutManager);

        // specify an adapter (see also next example)
        mHistoryAdapter = new HistoryAdapter(recordKWH, this);
        mListHistory.setAdapter(mHistoryAdapter);
        readKWH();
    }

    // 0 usage, 1 daily, 2 monthly
    private void readKWH() {
        mDatabaseReference.keepSynced(true);
        mDatabaseReference.child("device").child(mDeviceId).child("value").orderByChild("start").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                recordKWH.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    DeviceHistory deviceHistory = snapshot.getValue(DeviceHistory.class);
                    recordKWH.add(deviceHistory);
                }
                Collections.reverse(recordKWH);
                mHistoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
