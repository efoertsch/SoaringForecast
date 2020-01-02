package org.soaringforecast.rasp.turnpoint;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.turnpoint.cup.CupStyle;

import java.util.ArrayList;

public class CupStyleAdapter extends ArrayAdapter<CupStyle> implements View.OnClickListener {

    // View lookup cache
    private static class ViewHolder {
        TextView txtCupStyleDescription;

    }

    public CupStyleAdapter(ArrayList<CupStyle> cupStyles, Context context) {
        super(context, R.layout.cup_style_row, cupStyles);
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        CupStyle cupStyle = getItem(position);
                EventBus.getDefault().post(cupStyle);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCupStyleView(position, convertView, parent, false);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCupStyleView(position, convertView, parent, true);
    }

    @NonNull
    private View getCupStyleView(int position, View convertView, ViewGroup parent, boolean inDropDown) {
        // Get the data item for this position
        CupStyle cupStyle = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        CupStyleAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {
            viewHolder = new CupStyleAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.cup_style_row, parent, false);
            viewHolder.txtCupStyleDescription = convertView.findViewById(R.id.cup_style_description);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CupStyleAdapter.ViewHolder) convertView.getTag();
        }


        viewHolder.txtCupStyleDescription.setText(cupStyle.getDescription());

        viewHolder.txtCupStyleDescription.setTag(position);
        // Return the completed view to render on screen
        return convertView;
    }

}