package com.example.user.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.DashPathEffect;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.example.user.sensor.chart.MyMarkerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity implements OnChartGestureListener, OnChartValueSelectedListener {

    public static final String ARGS_DEVICE_NAME = "device name";
    public static final String ARGS_DEVICE_ID = "device id";
    public static final String ARGS_PUSH_ID = "push id";
    private static final String TAG = DetailActivity.class.getSimpleName();

    private Switch mSwitchStatus;
    private TextView mTextViewDeviceName;
    private LineChart mChart;
    private Button mButtonStartTimer;
    private Spinner mSpinnerHour;
    private Spinner mSpinnerMinute;
    private Spinner mSpinnerScond;
    private TextView mCountDownView;
    private TextView mMessageView;
    private ImageView mCloseButton;
    private CardView mMassageLayout;
    private LinearLayout mTimePicker;

    private String mDeviceName;
    private String mDeviceId;
    private String mPushKey;

    FirebaseDatabase mFirebaseDatabase;
    private FirebaseUser mCurrentUser;
    DatabaseReference mDatabaseReference;


    ArrayList<Entry> mValuesKWH;
    LineDataSet mSetKWH;

    private int mHour = 0, mMinute = 0, mScond = 0;
    private boolean isCountDownFinish = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(ARGS_DEVICE_NAME);
        mDeviceId = intent.getStringExtra(ARGS_DEVICE_ID);
        mPushKey = intent.getStringExtra(ARGS_PUSH_ID);

        mSwitchStatus = findViewById(R.id.switch_status);
        mButtonStartTimer = findViewById(R.id.button_start_timer);
        mSpinnerHour = findViewById(R.id.spinner_hour);
        mSpinnerMinute = findViewById(R.id.spinner_minute);
        mSpinnerScond = findViewById(R.id.spinner_second);
        mCountDownView = findViewById(R.id.countdown_timer);
        mTextViewDeviceName = findViewById(R.id.text_view_device_name);
        mTimePicker = findViewById(R.id.time_picker);
        mTextViewDeviceName.setText(mDeviceName);
        mCloseButton = findViewById(R.id.button_close);
        mMassageLayout = findViewById(R.id.layout_massage);
        mMessageView = findViewById(R.id.view_massage);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mValuesKWH = new ArrayList<>();

        mCountDownView.setVisibility(View.GONE);

        setupSpinner();

        mButtonStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCountDownFinish) {
                    startTimer();
                } else {
                    setupUICountDownStop();
                    saveStatusToDatabase(false);
                    stopService(new Intent(DetailActivity.this, BroadcastService.class));
                }
            }
        });

        readStatus();
        setStatus();
        setMassage();
        devineDevice();
    }

    private void readStatus() {
        mDatabaseReference.child("device").child(mDeviceId).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    boolean status = dataSnapshot.getValue(Boolean.class);
                    mSwitchStatus.setChecked(status);
                } catch (NullPointerException e){

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setMassage() {
        mDatabaseReference.child("device").child(mDeviceId).child("massage").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    mMassageLayout.setVisibility(View.VISIBLE);
                    mMessageView.setText(dataSnapshot.getValue(String.class));
                } else {
                    mMassageLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMassageLayout.setVisibility(View.GONE);
            }
        });
    }

    private void setStatus() {
        mSwitchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean status) {
                saveStatusToDatabase(status);
                if (!status && !isCountDownFinish) {
                    setupUICountDownStop();
                    saveStatusToDatabase(false);
                }
            }
        });
    }

    private void saveStatusToDatabase(boolean status) {
        mDatabaseReference.child("device").child(mDeviceId).child("status").setValue(status);
    }

    private void setupSpinner() {
        final String[] arrayHour = {
                "00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
                "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "20", "21", "22", "23", "24"
        };

        final String[] arrayMinuteAndScond = {
                "00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
                "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
                "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
                "40", "41", "42", "43", "44", "45", "46", "47", "48", "49",
                "50", "51", "52", "53", "54", "55", "56", "57", "58", "59",
                "60"
        };

        ArrayAdapter<String> adapterHour = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, arrayHour);
        adapterHour.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerHour.setAdapter(adapterHour);

        ArrayAdapter<String> adapterMinuteAndScond = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, arrayMinuteAndScond);
        adapterMinuteAndScond.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerMinute.setAdapter(adapterMinuteAndScond);
        mSpinnerScond.setAdapter(adapterMinuteAndScond);


        mSpinnerHour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mHour = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mSpinnerMinute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mMinute = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mSpinnerScond.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mScond = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void startTimer() {
        long milisInFuture = ((mHour * 60 * 60) + (mMinute * 60) + mScond) * 1000;
        if(milisInFuture!=0){
            setupUICountDownRun();
            saveStatusToDatabase(true);

            Intent serviceIntent = new Intent(this, BroadcastService.class);
            serviceIntent.putExtra(BroadcastService.ARG_ID, mPushKey);
            serviceIntent.putExtra(BroadcastService.ARG_TIMER, milisInFuture);
            startService(serviceIntent);
            Log.i(TAG, "Started service");
        }
    }

    private void setupUICountDownRun() {
        mTimePicker.setVisibility(View.GONE);
        mCountDownView.setVisibility(View.VISIBLE);
        mButtonStartTimer.setText("Stop");
        isCountDownFinish = false;
    }

    private void setupUICountDownStop() {
        mTimePicker.setVisibility(View.VISIBLE);
        mCountDownView.setVisibility(View.GONE);
        mButtonStartTimer.setText("Start");
        isCountDownFinish = true;
    }

    private void devineDevice() {
        mChart = findViewById(R.id.line_chart);
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);

        mChart.getDescription().setEnabled(false);

        mChart.setTouchEnabled(true);

        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(true);

        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv); // Set the marker to the chart

        // x-axis limit line
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);

        YAxis leftAxis = mChart.getAxisLeft();

        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);

        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getAxisRight().setEnabled(false);
        readKWH();

        mChart.animateX(2500);
        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    private void readKWH() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        final DatabaseReference myRef = database.getReference();
        myRef.keepSynced(true);
        myRef.child("device").child(mDeviceId).child("value").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mValuesKWH.clear();
                int i = 0;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if(userSnapshot.child("ampere").getValue(Integer.class)!= null &&
                            userSnapshot.child("voltage").getValue(Integer.class) != null &&
                            userSnapshot.child("duration").getValue(Integer.class) != null) {
                        int ampere = userSnapshot.child("ampere").getValue(Integer.class);
                        int voltage = userSnapshot.child("voltage").getValue(Integer.class);
                        int duration = userSnapshot.child("duration").getValue(Integer.class);
//                        float kwh = ((ampere * voltage * duration) / 1000) / 60;
                        float kwh = ampere * voltage * duration;
                        Log.i(TAG, "onDataChange: " + ampere + "A, " + voltage + "V, " + duration + "S, " + (ampere*voltage*duration) + "watt");
                        final Entry entry = new Entry(i, kwh);
                        mValuesKWH.add(entry);
                        i++;
                    }
                }
                setData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

    private void setData() {
        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            mSetKWH = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            mSetKWH.setValues(mValuesKWH);

            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            mSetKWH = new LineDataSet(mValuesKWH, "watt");

            mSetKWH.setDrawIcons(false);

            int color1 = ResourcesCompat.getColor(getResources(), R.color.colorChart1, null);
            mSetKWH.enableDashedHighlightLine(10f, 5f, 0f);
            mSetKWH.setColor(color1);
            mSetKWH.setCircleColor(color1);
            mSetKWH.setLineWidth(1f);
            mSetKWH.setCircleRadius(3f);
            mSetKWH.setDrawCircleHole(false);
            mSetKWH.setValueTextSize(9f);
            mSetKWH.setDrawValues(false);
//            mSetKWH.setDrawFilled(true);
            mSetKWH.setFormLineWidth(1f);
            mSetKWH.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            mSetKWH.setFormSize(15.f);
            mSetKWH.setFillColor(color1);


            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(mSetKWH);

            LineData data = new LineData(dataSets);
            mChart.setData(data);
        }
    }

    private void showDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_manage_device, null);
        final EditText editTextDeviceName = dialogView.findViewById(R.id.input_device_name);
        EditText editTextDeviceId = dialogView.findViewById(R.id.input_device_id);
        editTextDeviceName.setText(mDeviceName);
        editTextDeviceId.setText(mDeviceId);
        editTextDeviceId.setFocusable(false);
        editTextDeviceId.setClickable(false );
        dialogBuilder.setPositiveButton(getText(R.string.dialog_edit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String deviceName = editTextDeviceName.getText().toString().trim();
                        mDatabaseReference.child("device").child(mDeviceId).child("name").setValue(deviceName);
                        mTextViewDeviceName.setText(deviceName);
                    }
                })
                .setNegativeButton(getText(R.string.dialog_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabaseReference.child("device").child(mDeviceId).removeValue();
                        mDatabaseReference.child("user").child(mCurrentUser.getUid()).child(mPushKey).removeValue();
                        finish();
                    }
                })
                .setNeutralButton(getText(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateGUI(intent); // or whatever method used to update your GUI fields
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(br, new IntentFilter(BroadcastService.COUNTDOWN_BR + mPushKey));
        Log.i(TAG, "Registered broacast receiver");
    }

    @Override
    public void onPause() {
        super.onPause();
//        unregisterReceiver(br);
//        Log.i(TAG, "Unregistered broacast receiver");
    }

    @Override
    public void onStop() {
        try {
            unregisterReceiver(br);
        } catch (Exception e) {
            // Receiver was probably already stopped in onPause()
        }
        super.onStop();
    }
    @Override
    public void onDestroy() {
//        stopService(new Intent(this, BroadcastService.class));
//        Log.i(TAG, "Stopped service");
        super.onDestroy();
    }

    private void updateGUI(Intent intent) {
        if (intent.getExtras() != null) {
            setupUICountDownRun();
            long millisUntilFinished = intent.getLongExtra("countdown", 0);
            mCountDownView.setText(String.valueOf(millisUntilFinished/1000));
            if(millisUntilFinished/1000 == 1){
                setupUICountDownStop();
                saveStatusToDatabase(false);
            }
        } else {
            setupUICountDownStop();
            saveStatusToDatabase(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_edit:
                showDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            mChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("LOWHIGH", "low: " + mChart.getLowestVisibleX() + ", high: " + mChart.getHighestVisibleX());
        Log.i("MIN MAX", "xmin: " + mChart.getXChartMin() + ", xmax: " + mChart.getXChartMax() + ", ymin: " + mChart.getYChartMin() + ", ymax: " + mChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }
}
