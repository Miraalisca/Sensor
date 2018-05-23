package com.example.user.sensor;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.user.sensor.chart.MyMarkerView;
import com.example.user.sensor.model.DeviceHistory;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DetailActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    public static final int ELECTRICITY_PRICES = 1100;
    public static final String ARGS_DEVICE_NAME = "device name";
    public static final String ARGS_DEVICE_ID = "device id";
    public static final String ARGS_PUSH_ID = "push id";
    private static final String TAG = DetailActivity.class.getSimpleName();

    private Switch mSwitchStatus;
    private TextView mTextViewDeviceStatus;
    private LineChart mChart;
    private Button mButtonManageTimer;
    private Spinner mSpinnerHour;
    private Spinner mSpinnerMinute;
    private Spinner mSpinnerScond;
    private TextView mMessageView;
    private TextView mMessageView2;
    private TextView mTimeLineView;
    private TextView mPrectionPriceView;
    private TextView mNoDataMessage;
    private ImageView mCloseButton;
    private ImageView mCloseButton2;
    private CardView mMassageLayout;
    private CardView mMassageLayout2;
    private EditText mEditTextStartDate;
    private EditText mEditTextStartTime;
    private EditText mEditTextFinishDate;
    private EditText mEditTextFinishTime;
    private LinearLayout mTimePicker;
    private LinearLayout mDurationPicker;
    private RadioGroup mRadioGroup;

    private String mDeviceName;
    private String mDeviceStatus;
    private String mDeviceId;
    private String mPushKey;
    private float mAverageKwH;
    private Boolean mIsUseDuration = false;
    private Boolean mIsInSetup = false;

    FirebaseDatabase mFirebaseDatabase;
    private FirebaseUser mCurrentUser;
    DatabaseReference mDatabaseReference;

    private ArrayList<DeviceHistory> recordKWH;
    private int mHour = 0, mMinute = 0, mScond = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(ARGS_DEVICE_NAME);
        mDeviceId = intent.getStringExtra(ARGS_DEVICE_ID);
        mPushKey = intent.getStringExtra(ARGS_PUSH_ID);

        getSupportActionBar().setTitle(mDeviceName);
        mSwitchStatus = findViewById(R.id.switch_status);
        mButtonManageTimer = findViewById(R.id.button_manage_timer);
        mNoDataMessage = findViewById(R.id.no_data_massage);
        mTextViewDeviceStatus = findViewById(R.id.text_view_device_status);
        mTextViewDeviceStatus.setText(mDeviceName);
        mTimeLineView = findViewById(R.id.timeline);
        mPrectionPriceView = findViewById(R.id.prediction_price);
        mCloseButton = findViewById(R.id.button_close);
        mCloseButton2 = findViewById(R.id.button_close_2);
        mMassageLayout = findViewById(R.id.layout_massage);
        mMassageLayout2 = findViewById(R.id.layout_massage_2);
        mMessageView = findViewById(R.id.view_massage);
        mMessageView2 = findViewById(R.id.view_massage_2);
        mChart = findViewById(R.id.line_chart);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        recordKWH = new ArrayList<>();

        mButtonManageTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsInSetup) {
                    resetAllTimer();
                } else {
                    showDialogSetupTimer();
                }
            }
        });

        readStatus();
        setStatus();
        readStartAndFinishTimer();
        defineChart();
        readKWH();
    }

    private void resetAllTimer() {

    }

    private void readStatus() {
        mDatabaseReference.child("device").child(mDeviceId).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    boolean status = dataSnapshot.getValue(Boolean.class);
                    mSwitchStatus.setChecked(status);
                    if(status)
                        mTextViewDeviceStatus.setText(R.string.title_device_status_on);
                    else
                        mTextViewDeviceStatus.setText(R.string.title_device_status_off);
                } catch (NullPointerException e) {
                    Log.e(TAG, "onDataChange: " + e.getMessage());
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
                if (dataSnapshot.exists()) {
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
        mDatabaseReference.child("device").child(mDeviceId).child("massage2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mMassageLayout2.setVisibility(View.VISIBLE);
                    mMessageView2.setText("You Have Save " + String.valueOf(makeSavingResult()));
                } else {
                    mMassageLayout2.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseReference.child("device").child(mDeviceId).child("massage").removeValue();
                mMassageLayout.setVisibility(View.GONE);
            }
        });
        mCloseButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseReference.child("device").child(mDeviceId).child("massage2").removeValue();
                mMassageLayout2.setVisibility(View.GONE);
            }
        });
    }

    private void setStatus() {
        mSwitchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean status) {
                saveStatusToDatabase(status);
                if(status)
                    mTextViewDeviceStatus.setText(R.string.title_device_status_on);
                else
                    mTextViewDeviceStatus.setText(R.string.title_device_status_off);
            }
        });
    }

    private void saveStatusToDatabase(boolean status) {
        mDatabaseReference.child("device").child(mDeviceId).child("status").setValue(status);
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
                mChart.clearValues();
                int i = 0;
                float tempKwh = 0;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if (userSnapshot.child("ampere").getValue(Integer.class) != null &&
                            userSnapshot.child("voltage").getValue(Integer.class) != null &&
                            userSnapshot.child("start").getValue(Integer.class) != null &&
                            userSnapshot.child("duration").getValue(Integer.class) != null) {
                        int ampere = userSnapshot.child("ampere").getValue(Integer.class);
                        int voltage = userSnapshot.child("voltage").getValue(Integer.class);
                        int duration = userSnapshot.child("duration").getValue(Integer.class);
                        long time = userSnapshot.child("start").getValue(Long.class);
                        float kwh = ((ampere * voltage * duration) / 1000) / 3600;
//                        float kwh = ampere * voltage * duration;
                        Log.i(TAG, "onDataChange: " + ampere + "A, " + voltage + "V, " + duration + "S, " + (ampere * voltage * duration) + "watt");
                        addEntry(time, kwh);

                        DeviceHistory deviceHistory = new DeviceHistory(ampere, duration, time, voltage);
                        recordKWH.add(deviceHistory);
                        tempKwh += kwh;
                        i++;
                    }
                }

                if(dataSnapshot.getChildrenCount() == 0){
                    mNoDataMessage.setVisibility(View.VISIBLE);
                    mChart.setVisibility(View.GONE);
                } else {
                    mNoDataMessage.setVisibility(View.GONE);
                    mChart.setVisibility(View.VISIBLE);
                }

                setMassage();
                mAverageKwH = tempKwh/(i+1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

    private void readStartAndFinishTimer(){
        mDatabaseReference.child("device").child(mDeviceId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("startTime").exists() && dataSnapshot.child("finishTime").exists()){
                    if(dataSnapshot.child("startTime").getValue(Long.class)!=0 && dataSnapshot.child("finishTime").getValue(Long.class)!=0) {
                        mTimeLineView.setVisibility(View.VISIBLE);
                        mPrectionPriceView.setVisibility(View.VISIBLE);

                        long startTime = dataSnapshot.child("startTime").getValue(Long.class);
                        long finishTime = dataSnapshot.child("finishTime").getValue(Long.class);
                        mTimeLineView.setText(getTime(startTime) + " - " + getTime(finishTime));
                        mPrectionPriceView.setText("Rp. " + String.valueOf(setupPrediction(finishTime - startTime)));
                    } else {
                        mPrectionPriceView.setVisibility(View.GONE);
                        mTimeLineView.setVisibility(View.GONE);
                    }
                } else {
                    mPrectionPriceView.setVisibility(View.GONE);
                    mTimeLineView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showDialogEdit() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_name, null);
        final EditText editTextDeviceName = dialogView.findViewById(R.id.edit_text_name);
        editTextDeviceName.setText(mDeviceName);
        dialogBuilder.setPositiveButton(getText(R.string.dialog_edit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String deviceName = editTextDeviceName.getText().toString().trim();
                mDatabaseReference.child("device").child(mDeviceId).child("name").setValue(deviceName);
                mTextViewDeviceStatus.setText(deviceName);
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

    private void showDialogSetupTimer() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_timer, null);
        mEditTextStartDate = dialogView.findViewById(R.id.edit_text_start_date);
        mEditTextStartTime = dialogView.findViewById(R.id.edit_text_start_time);
        mEditTextFinishDate = dialogView.findViewById(R.id.edit_text_finish_date);
        mEditTextFinishTime = dialogView.findViewById(R.id.edit_text_finish_time);
        mSpinnerHour = dialogView.findViewById(R.id.spinner_hour);
        mSpinnerMinute = dialogView.findViewById(R.id.spinner_minute);
        mSpinnerScond = dialogView.findViewById(R.id.spinner_second);
        mRadioGroup = dialogView.findViewById(R.id.radio_group);
        mTimePicker = dialogView.findViewById(R.id.time_picker);
        mDurationPicker = dialogView.findViewById(R.id.duration_picker);
        setupTimePickerListener();
        dialogBuilder.setPositiveButton(getText(R.string.dialog_start), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long startTime = getUnixTimeStamp(mEditTextStartDate.getText().toString() + ", " + mEditTextStartTime.getText().toString());
                long finishTime;
                if(mIsUseDuration) {
                    finishTime = startTime + mHour*3600 + mMinute*60 + mScond;
                } else {
                    finishTime = getUnixTimeStamp(mEditTextFinishDate.getText().toString() + ", " + mEditTextFinishTime.getText().toString());
                }

                long duration = startTime - finishTime;
                saveTimeSetupToDatabase(startTime, finishTime);
                startAlarm(startTime);
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

    private void saveTimeSetupToDatabase(long startTime, long finishTime) {
        mDatabaseReference.child("device").child(mDeviceId).child("startTime").setValue(startTime/1000);
        mDatabaseReference.child("device").child(mDeviceId).child("finishTime").setValue(finishTime/1000);
    }

    private void setupTimePickerListener() {
        final String[] arrayHour = {
                "00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
                "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "20", "21", "22", "23"
        };

        final String[] arrayMinuteAndScond = {
                "00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
                "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
                "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
                "40", "41", "42", "43", "44", "45", "46", "47", "48", "49",
                "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"
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
        mEditTextStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker(mEditTextStartDate);
            }
        });
        mEditTextStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePicker(mEditTextStartTime);
            }
        });
        mEditTextFinishDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker(mEditTextFinishDate);
            }
        });
        mEditTextFinishTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePicker(mEditTextFinishTime);
            }
        });

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_setup_with_duration: {
                        mDurationPicker.setVisibility(View.VISIBLE);
                        mTimePicker.setVisibility(View.GONE);
                        mIsUseDuration = true;
                        break;
                    }
                    case R.id.radio_setup_with_timer: {
                        mDurationPicker.setVisibility(View.GONE);
                        mTimePicker.setVisibility(View.VISIBLE);
                        mIsUseDuration = false;
                        break;
                    }
                }
            }
        });
    }

    private void datePicker(final EditText editText) {
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        Resources res = getResources();
        final String mBulan[] = res.getStringArray(R.array.mounth); //change with your mounth

        DatePickerDialog mDatePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                editText.setText(selectedday + " " + mBulan[selectedmonth] + " " + selectedyear);
            }
        }, mYear, mMonth, mDay);
        mDatePicker.show();
    }

    private void timePicker(final EditText editText) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        editText.setText(hourOfDay + ":" + minute);
                    }
                }, hour, minute, false);
        timePickerDialog.show();
    }

    private float makeSavingResult(){
        int last = recordKWH.size()-1;
        float curentKwh = recordKWH.get(last).getAmpere()*recordKWH.get(last).getVoltage()*recordKWH.get(last).getDuration();
        float lastKwh = recordKWH.get(last-1).getAmpere()*recordKWH.get(last-1).getVoltage()*recordKWH.get(last-1).getDuration();
        float defferencesKwH = lastKwh - curentKwh * ELECTRICITY_PRICES / 3600;
        return defferencesKwH;
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
                showDialogEdit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startAlarm(long startTime) {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent startIntent = new Intent(DetailActivity.this, AlarmReceiver.class);
        startIntent.putExtra("title", mDeviceName);
        startIntent.putExtra("content", true);
        startIntent.putExtra("device_id", mDeviceId);
        PendingIntent startPendingIntent = PendingIntent.getBroadcast(DetailActivity.this, 1, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        manager.set(AlarmManager.RTC_WAKEUP, startTime, startPendingIntent);
    }

    private long getUnixTimeStamp(String time) {
        Log.i(TAG, "getUnixTimeStamp: " + time);
        try {
            Date date = null;
            DateFormat formatter = new SimpleDateFormat("dd MMMMM yyyy, HH:mm");
            date = formatter.parse(time);
            Log.i(TAG, "getUnixTimeStamp: " + date.getTime());
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String getTime(long unixTimeStamp){
        Date date = new Date(unixTimeStamp*1000);
        String dateInText = new SimpleDateFormat("dd MMM yyyy, HH:mm").format(date);
        return dateInText;
    }

    private float setupPrediction(long duration){
        Log.i(TAG, "setupPrediction: " + mAverageKwH + " | " + duration + " | " + (float)1100/3600);
        float price = (mAverageKwH * duration /3600) * ELECTRICITY_PRICES;
        return price;
    }

    private void defineChart(){
        mChart.setOnChartValueSelectedListener(this);

        // enable description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv); // Set the marker to the chart

        LineData data = new LineData();

        // add empty data
        mChart.setData(data);
//        mChart.setVisibleXRange(1526688000, 1526903268);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);


//        IAxisValueFormatter xAxisFormatter = new DateAxisValueFormatter(1526688000);
        XAxis xl = mChart.getXAxis();
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
//        xl.setValueFormatter(xAxisFormatter);
        xl.setGranularityEnabled(true);
        xl.setGranularity(1f);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "kWh");
        set.setDrawIcons(false);

        int color1 = ResourcesCompat.getColor(getResources(), R.color.colorChart1, null);
        set.enableDashedHighlightLine(10f, 5f, 0f);
        set.setColor(color1);
        set.setCircleColor(color1);
        set.setLineWidth(1f);
        set.setCircleRadius(3f);
        set.setDrawCircleHole(false);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
//            mSetKWH.setDrawFilled(true);
        set.setFormLineWidth(1f);
        set.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        set.setFormSize(15.f);
        set.setFillColor(color1);
        return set;
    }

    private void addEntry(long time, float value) {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

//            data.addEntry(new Entry(time-1526688000, value), 0);
            data.addEntry(new Entry(set.getEntryCount(), value), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

}
