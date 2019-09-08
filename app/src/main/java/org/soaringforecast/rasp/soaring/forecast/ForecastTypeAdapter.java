package org.soaringforecast.rasp.soaring.forecast;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.soaring.json.Forecast;
import org.soaringforecast.rasp.utils.BitmapImageUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class ForecastTypeAdapter extends ArrayAdapter<Forecast> implements View.OnClickListener {

    // View lookup cache
    private static class ViewHolder {
        TextView txtForecastNameDisplay;
        ImageView ivForecastInfo;
    }

    public ForecastTypeAdapter(ArrayList<Forecast> forecasts, Context context) {
        super(context, R.layout.forecast_row, forecasts);

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

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = getForecastView(position, convertView, parent, true);
        //view.setLayoutParams(linearLayoutParams);
        return view;
    }

    @NonNull
    private View getForecastView(int position, View convertView, ViewGroup parent, boolean inDropDown) {
        // Get the data item for this position
        Forecast forecast = getItem(position);
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

        BitmapImageUtils.getForecastDrawable(forecast.getForecastCategory(), viewHolder.ivForecastInfo);
        if (!inDropDown) {
            viewHolder.ivForecastInfo.setOnClickListener(this);
        }
        viewHolder.txtForecastNameDisplay.setText(forecast.getForecastNameDisplay());

        viewHolder.ivForecastInfo.setTag(position);
        // Return the completed view to render on screen
        return convertView;
    }

}