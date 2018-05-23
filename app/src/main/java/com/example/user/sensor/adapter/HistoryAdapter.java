package com.example.user.sensor.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.user.sensor.R;
import com.example.user.sensor.model.DeviceHistory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created with love by Hari Nugroho on 24/05/2018 at 00.26.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>{

    ArrayList<DeviceHistory> deviceHistories;
    Context context;

    public HistoryAdapter(ArrayList<DeviceHistory> deviceHistories, Context context) {
        this.deviceHistories = deviceHistories;
        this.context = context;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        HistoryViewHolder viewHolder = new HistoryViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        DeviceHistory deviceHistory = deviceHistories.get(position);

        float kwh = deviceHistory.getAmpere() * deviceHistory.getVoltage() * deviceHistory.getDuration() / 1000 / 3600;
        float price = 1100 * kwh;

        holder.startDate.setText(getDate(deviceHistory.getStart()));
        holder.endDate.setText(getDate(deviceHistory.getStart()+deviceHistory.getDuration()));
        holder.startTime.setText(getTime(deviceHistory.getStart()));
        holder.endTime.setText(getTime(deviceHistory.getStart()+deviceHistory.getDuration()));
        holder.kwh.setText(kwhConverter(kwh) + "kWh");
        holder.price.setText(currencyConverter(price));

    }

    @Override
    public int getItemCount() {
        return deviceHistories.size();
    }

    private String kwhConverter(float value){
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return  decimalFormat.format(value);
    }

    private String currencyConverter(float value){
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        return  decimalFormat.format(value);
    }

    private String getTime(long unixTimeStamp){
        Date date = new Date(unixTimeStamp*1000);
        String dateInText = new SimpleDateFormat("HH:mm").format(date);
        return dateInText;
    }

    private String getDate(long unixTimeStamp){
        Date date = new Date(unixTimeStamp*1000);
        String dateInText = new SimpleDateFormat("MMM dd, yyyy").format(date);
        return dateInText;
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView startDate;
        TextView endDate;
        TextView startTime;
        TextView endTime;
        TextView price;
        TextView kwh;

        public HistoryViewHolder(View view) {
            super(view);
            startDate = view.findViewById(R.id.start_date);
            endDate = view.findViewById(R.id.end_date);
            startTime = view.findViewById(R.id.start_time);
            endTime = view.findViewById(R.id.end_time);
            price = view.findViewById(R.id.price);
            kwh = view.findViewById(R.id.kwh);
        }
    }
}
