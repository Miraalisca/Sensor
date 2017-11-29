package com.example.user.sensor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    public static final String ARGS_DEVICE_NAME = "device name";
    public static final String ARGS_DEVICE_ID = "device id";

    private TextView textViewDeviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        String deviceName = intent.getStringExtra(ARGS_DEVICE_NAME);

        textViewDeviceName = findViewById(R.id.text_view_device_name);
        textViewDeviceName.setText(deviceName);
    }
}
