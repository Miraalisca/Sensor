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
import android.view.MotionEvent;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DetailActivity extends AppCompatActivity implements OnChartGestureListener, OnChartValueSelectedListener {

    public static final String ARGS_DEVICE_NAME = "device name";
    public static final String ARGS_DEVICE_ID = "device id";
    public static final String ARGS_PUSH_ID = "push id";
    private static final String TAG = DetailActivity.class.getSimpleName();

    private Switch mSwitchStatus;
    private TextView mTextViewDeviceName;
    private LineChart mChart;
    private Button mButtonManageTimer;
    private Spinner mSpinnerHour;
    private Spinner mSpinnerMinute;
    private Spinner mSpinnerScond;
    private TextView mMessageView;
    private TextView mStartTimeView;
    private TextView mFinishTimeView;
    private ImageView mCloseButton;
    private CardView mMassageLayout;
    private EditText mEditTextStartDate;
    private EditText mEditTextStartTime;
    private EditText mEditTextFinishDate;
    private EditText mEditTextFinishTime;
    private LinearLayout mTimePicker;
    private LinearLayout mDurationPicker;
    private RadioGroup mRadioGroup;

    private String mDeviceName;
    private String mDeviceId;
    private String mPushKey;
    private Boolean mIsUseDuration = false;
    private Boolean mIsInSetup = false;

    FirebaseDatabase mFirebaseDatabase;
    private FirebaseUser mCurrentUser;
    DatabaseReference mDatabaseReference;

    ArrayList<Entry> mValuesKWH;
    LineDataSet mSetKWH;

    private int mHour = 0, mMinute = 0, mScond = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(ARGS_DEVICE_NAME);
        mDeviceId = intent.getStringExtra(ARGS_DEVICE_ID);
        mPushKey = intent.getStringExtra(ARGS_PUSH_ID);

        mSwitchStatus = findViewById(R.id.switch_status);
        mButtonManageTimer = findViewById(R.id.button_manage_timer);
        mTextViewDeviceName = findViewById(R.id.text_view_device_name);
        mTextViewDeviceName.setText(mDeviceName);
        mStartTimeView = findViewById(R.id.start_time);
        mFinishTimeView = findViewById(R.id.finish_time);
        mCloseButton = findViewById(R.id.button_close);
        mMassageLayout = findViewById(R.id.layout_massage);
        mMessageView = findViewById(R.id.view_massage);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mValuesKWH = new ArrayList<>();

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
        setMassage();
        defineChart();
        readStartAndFinishTimer();
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
            }
        });
    }

    private void saveStatusToDatabase(boolean status) {
        mDatabaseReference.child("device").child(mDeviceId).child("status").setValue(status);
    }

    private void defineChart() {
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
        leftAxis.setAxisMinimum(0);

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
                    if (userSnapshot.child("ampere").getValue(Integer.class) != null &&
                            userSnapshot.child("voltage").getValue(Integer.class) != null &&
                            userSnapshot.child("duration").getValue(Integer.class) != null) {
                        int ampere = userSnapshot.child("ampere").getValue(Integer.class);
                        int voltage = userSnapshot.child("voltage").getValue(Integer.class);
                        int duration = userSnapshot.child("duration").getValue(Integer.class);
//                        float kwh = ((ampere * voltage * duration) / 1000) / 60;
                        float kwh = ampere * voltage * duration;
                        Log.i(TAG, "onDataChange: " + ampere + "A, " + voltage + "V, " + duration + "S, " + (ampere * voltage * duration) + "watt");
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

    private void readStartAndFinishTimer(){
        mDatabaseReference.child("device").child("130520180002").child("startTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.getValue(Long.class)!=0) {
                        mStartTimeView.setVisibility(View.VISIBLE);
                        mStartTimeView.setText(getTime(dataSnapshot.getValue(Long.class)));
                    } else {
                        mStartTimeView.setVisibility(View.GONE);
                    }
                } else {
                    mStartTimeView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDatabaseReference.child("device").child("130520180002").child("finishTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.getValue(Long.class)!=0) {
                        mFinishTimeView.setVisibility(View.VISIBLE);
                        mFinishTimeView.setText(getTime(dataSnapshot.getValue(Long.class)));
                    } else {
                        mFinishTimeView.setVisibility(View.GONE);
                    }
                } else {
                    mFinishTimeView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
            mSetKWH = new LineDataSet(mValuesKWH, "KWH");

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

    @Override
    public void onPause() {
        super.onPause();
//        unregisterReceiver(br);
//        Log.i(TAG, "Unregistered broacast receiver");
    }

    @Override
    public void onDestroy() {
//        stopService(new Intent(this, BroadcastService.class));
//        Log.i(TAG, "Stopped service");
        super.onDestroy();
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
}
