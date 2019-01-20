package com.fisincorporated.soaringforecast.soaring.forecast;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.soaring.json.Forecast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class ForecastTypeAdapter extends ArrayAdapter<Forecast> implements View.OnClickListener {

    private ArrayList<Forecast> forecasts;
    Context context;

    // View lookup cache
    private static class ViewHolder {
        TextView txtForecastNameDisplay;
        ImageView ivForecastInfo;
    }

    public ForecastTypeAdapter(ArrayList<Forecast> forecasts, Context context) {
        super(context, R.layout.forecast_row, forecasts);
        this.forecasts = forecasts;
        this.context = context;

    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        Object object = getItem(position);
        Forecast forecast = (Forecast) object;
        switch (v.getId()) {
            case R.id.soaring_forecast_info:
                EventBus.getDefault().post(forecast);
                break;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getForecastView(position, convertView, parent, false);

    }

    @NonNull
    private View getForecastView(int position, View convertView, ViewGroup parent, boolean inDropDown) {
        // Get the data item for this position
        Forecast forecast = (Forecast) getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.forecast_row, parent, false);
            viewHolder.txtForecastNameDisplay = convertView.findViewById(R.id.soaring_forecast_type);
            viewHolder.ivForecastInfo = convertView.findViewById(R.id.soaring_forecast_info);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.ivForecastInfo.setVisibility(inDropDown ? View.INVISIBLE : View.VISIBLE);
        viewHolder.txtForecastNameDisplay.setText(forecast.getForecastNameDisplay());
        viewHolder.ivForecastInfo.setOnClickListener(this);
        viewHolder.ivForecastInfo.setTag(position);
        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getForecastView(position, convertView, parent, true);
    }
}