package com.example.user.sensor.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.user.sensor.R;
import com.example.user.sensor.model.Device;
import com.example.user.sensor.model.History;

import java.util.ArrayList;

/**
 * Created by MY NOTEBOOK on 20/05/2018.
 */

public class HistoryListAdapter extends ArrayAdapter<History> {
    private Context context;

    public HistoryListAdapter(@NonNull Context context, ArrayList<History> objects) {
        super(context, 0,objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_history, parent, false);

        final History current = getItem(position);


        TextView percentage = view.findViewById(R.id.percentage);
        percentage.setText(current.getPercentage());

        TextView startDate = view.findViewById(R.id.start_date);
        startDate.setText(current.getStartDate());

        TextView endDate = view.findViewById(R.id.end_date);
        endDate.setText(current.getEndDate());

        TextView startTime = view.findViewById(R.id.start_time);
        startTime.setText(current.getStartTime());

        TextView endTime = view.findViewById(R.id.end_time);
        endTime.setText(current.getEndTime());

        TextView savings = view.findViewById(R.id.cost_savings);
        savings.setText(current.getSavings());

        return view;
    }
}
