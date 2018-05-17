package com.example.user.sensor;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.example.user.sensor.model.Device;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

/**
 * Created by USER on 11/26/2017.
 */

public class ReaderActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    public static final int REQUEST_CAMERA = 1;
    public ZXingScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission()) {
                requestPermission();
            }
        }

    }

    /**
     * checking for camera permission
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
     * @param requestCode request code
     * @param permission permission
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
                                                    requestPermission(new String[]{CAMERA}, REQUEST_CAMERA);}
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

    private int session = 0;
    private void showDialogInputName(final String idDevice){
        AlertDialog.Builder builder = new AlertDialog.Builder(ReaderActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        View viewDialog = inflater.inflate(R.layout.dialog_name, null);
        final EditText editTextName = viewDialog.findViewById(R.id.edit_text_name);
        builder.setTitle(idDevice);
        builder.setView(viewDialog)
                .setPositiveButton(R.string.dialog_submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String deviceName = editTextName.getText().toString().trim();
                        saveToDatabase(idDevice, deviceName);
                        showDialogInputName(String.valueOf(Long.valueOf(idDevice) + 1));
                        session++;
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

    private void saveToDatabase(String idDevice, String deviceName){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        FirebaseDatabase.getInstance().getReference("user").child(firebaseUser.getUid()).push().setValue(idDevice);
        FirebaseDatabase.getInstance().getReference("device").child(idDevice).child("name").setValue(deviceName);
        if(session>2) {
            finish();
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

    private void showDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_manage_device, null);
        final EditText editTextDeviceName = dialogView.findViewById(R.id.input_device_name);
        final EditText editTextDeviceId = dialogView.findViewById(R.id.input_device_id);
        dialogBuilder.setPositiveButton(getText(R.string.dialog_submit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String deviceName = editTextDeviceName.getText().toString().trim();
                        String deviceID = editTextDeviceId.getText().toString().trim();

                        saveToDatabase(deviceID, deviceName);
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
}
