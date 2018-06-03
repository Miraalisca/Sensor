package com.example.user.sensor;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.sensor.model.Device;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

/**
 * Created by USER on 11/26/2017.
 */

public class ReaderActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    ArrayList<String> allDevicesId;
    ArrayList<String> allDevicesName;
    ArrayList<String> userDevicesId;
    public static final int REQUEST_CAMERA = 1;
    public ZXingScannerView scannerView;

    FirebaseDatabase mFirebaseDatabase;
    FirebaseUser mCurrentUser;
    DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        userDevicesId = new ArrayList<>();
        allDevicesId = new ArrayList<>();
        allDevicesName = new ArrayList<>();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        readDevice();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission()) {
                requestPermission();
            }
        }
    }

    /**
     * checking for camera permission
     *
     * @return is camera granted
     */
    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(ReaderActivity.this, CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * request permission
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    /**
     * result of request permission
     *
     * @param requestCode  request code
     * @param permission   permission
     * @param grantResults grant result
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String permission[], @NonNull int grantResults[]) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (!cameraAccepted) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                displayAlertMessage(
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermission(new String[]{CAMERA}, REQUEST_CAMERA);
                                                }
                                            }

                                            private void requestPermission(String[] strings, int requestCamera) {
                                            }
                                        });
                                return;
                            }
                        }
                    }

                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                scannerView = new ZXingScannerView(this);
                setContentView(scannerView);
            }
            scannerView.setResultHandler(this);
            scannerView.startCamera();
        } else {
            requestPermission();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
    }

    /**
     * showing alert message
     *
     * @param listener listen for action click
     */
    public void displayAlertMessage(DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(ReaderActivity.this)
                .setMessage("you need to allow access for both permission")
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void handleResult(Result result) {
        final String idDevice = result.getText();
        showDialogInputName(idDevice);
    }

    private void showDialogInputName(final String idDevice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ReaderActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        View viewDialog = inflater.inflate(R.layout.dialog_add_4_name, null);
        final TextView textView1 = viewDialog.findViewById(R.id.view_id_1);
        final TextView textView2 = viewDialog.findViewById(R.id.view_id_2);
        final TextView textView3 = viewDialog.findViewById(R.id.view_id_3);
        final TextView textView4 = viewDialog.findViewById(R.id.view_id_4);

        final EditText editTextName1 = viewDialog.findViewById(R.id.edit_text_name_1);
        final EditText editTextName2 = viewDialog.findViewById(R.id.edit_text_name_2);
        final EditText editTextName3 = viewDialog.findViewById(R.id.edit_text_name_3);
        final EditText editTextName4 = viewDialog.findViewById(R.id.edit_text_name_4);

        final TextInputLayout textInputLayout1 = viewDialog.findViewById(R.id.input_layout_device_name_1);
        final TextInputLayout textInputLayout2 = viewDialog.findViewById(R.id.input_layout_device_name_2);
        final TextInputLayout textInputLayout3 = viewDialog.findViewById(R.id.input_layout_device_name_3);
        final TextInputLayout textInputLayout4 = viewDialog.findViewById(R.id.input_layout_device_name_4);

        final String idDevice2 = String.valueOf(Long.valueOf(idDevice) + 1);
        final String idDevice3 = String.valueOf(Long.valueOf(idDevice) + 2);
        final String idDevice4 = String.valueOf(Long.valueOf(idDevice) + 3);

        textView1.setText(idDevice);
        textView2.setText(idDevice2);
        textView3.setText(idDevice3);
        textView4.setText(idDevice4);

        builder.setView(viewDialog)
                .setPositiveButton(R.string.dialog_submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String deviceName1 = editTextName1.getText().toString().trim();
                        String deviceName2 = editTextName2.getText().toString().trim();
                        String deviceName3 = editTextName3.getText().toString().trim();
                        String deviceName4 = editTextName4.getText().toString().trim();
                        saveToDatabase(idDevice, deviceName1, textInputLayout1);
                        saveToDatabase(idDevice2, deviceName2, textInputLayout2);
                        saveToDatabase(idDevice3, deviceName3, textInputLayout3);
                        saveToDatabase(idDevice4, deviceName4, textInputLayout4);
                        finish();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create();
        builder.show();
    }

    private void saveToDatabase(String idDevice, String deviceName, TextInputLayout textInputLayout) {
        if(allDevicesId.contains(idDevice)) {
            if(!userDevicesId.contains(idDevice)) {
                mDatabaseReference.child("user").child(mCurrentUser.getUid()).push().setValue(idDevice);
            }
            mDatabaseReference.child("device").child(idDevice).child("name").setValue(deviceName);
        } else {
            textInputLayout.setError("Please check again your id");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.reader_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_add:
                showDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_manage_device, null);
        final EditText editTextDeviceName = dialogView.findViewById(R.id.input_device_name);
        final EditText editTextDeviceId = dialogView.findViewById(R.id.input_device_id);
        final TextInputLayout textInputLayout = dialogView.findViewById(R.id.input_layout_device_id);
        dialogBuilder.setPositiveButton(getText(R.string.dialog_submit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String deviceName = editTextDeviceName.getText().toString().trim();
                String deviceID = editTextDeviceId.getText().toString().trim();

                saveToDatabase(deviceID, deviceName, textInputLayout);
                finish();
            }
        })
                .setNegativeButton(getText(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void readDevice() {

        mDatabaseReference.child("device").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allDevicesId.clear();
                allDevicesName.clear();
                for (DataSnapshot device : dataSnapshot.getChildren()) {
                    allDevicesId.add(device.getKey());
                    allDevicesName.add(device.child("name").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child("user").child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userDevicesId.clear();
                for (DataSnapshot device : dataSnapshot.getChildren()) {
                    userDevicesId.add(device.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
