package com.example.user.sensor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.user.sensor.adapter.DeviceListAdapter;
import com.example.user.sensor.model.Device;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listViewDevice;
    private TextView textViewDataIsEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewDevice = findViewById(R.id.list_view_device);
        textViewDataIsEmpty = findViewById(R.id.text_view_empty_view);

        listViewDevice.setEmptyView(textViewDataIsEmpty);

        final ArrayList<Device> devices = new ArrayList<>();
        devices.add(new Device("Lampu Belajar", "2611201700001", true));
        devices.add(new Device("Lampu Teras", "2611201700002", false));
        devices.add(new Device("Cas Laptop", "2611201700003", false));
        devices.add(new Device("Ruang Tengah", "2611201700004", true));

        DeviceListAdapter adapter = new DeviceListAdapter(this, devices);
        listViewDevice.setAdapter(adapter);

        listViewDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra(DetailActivity.ARGS_DEVICE_NAME, devices.get(i).getDeviceName());
                intent.putExtra(DetailActivity.ARGS_DEVICE_ID, devices.get(i).getDeviceId());
                startActivity(intent);
            }
        });
    }

    public void goToReader(View view) {
        Intent intent = new Intent(MainActivity.this, ReaderActivity.class);
        startActivity(intent);
    }


}
