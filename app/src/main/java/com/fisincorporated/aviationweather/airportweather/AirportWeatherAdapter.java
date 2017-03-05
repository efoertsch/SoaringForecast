package com.fisincorporated.aviationweather.airportweather;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.data.metars.Metar;
import com.fisincorporated.aviationweather.data.taf.TAF;
import com.fisincorporated.aviationweather.databinding.AirportMetarTafBinding;

import java.util.List;

import javax.inject.Inject;

public class AirportWeatherAdapter extends RecyclerView.Adapter<AirportWeatherAdapter.BindingHolder> {


    public ObservableList<TAF> currentTafs = new ObservableArrayList<>();

    public ObservableList<Metar> currentMetars = new ObservableArrayList<>();

    public WeatherDisplayPreferences weatherDisplayPreferences;

    @Inject
    public AppPreferences appPreferences;

    @Inject
    public AirportWeatherAdapter() {}

    public AirportWeatherAdapter setMetarList(@NonNull List<Metar> metarList) {
        currentMetars.addAll(metarList);
        return this;
    }

    public AirportWeatherAdapter setWeatherDisplayPreferences(@NonNull WeatherDisplayPreferences weatherDisplayPreferences) {
        this.weatherDisplayPreferences = weatherDisplayPreferences;
        return this;
    }

    public void updateMetarList(@NonNull List<Metar> metarList){
        // update observable list
        boolean metarAdded;
        for (Metar newMetar : metarList) {
            metarAdded = false;
            for (int i = 0 ; i < currentMetars.size(); ++i ) {
                if (currentMetars.get(i).getStationId().equals(newMetar.getStationId())){
                    currentMetars.remove(i);
                    currentMetars.add(i, newMetar);
                    metarAdded = true;
                    notifyItemChanged(i);
                    break;
                }
            }
            if (!metarAdded) {
                currentMetars.add(newMetar);
                notifyItemInserted(currentMetars.size() - 1);
            }
        }
    }

    public void updateTafList(List<TAF> tafs) {
        // update observable list
        boolean tafAdded;
        for (TAF newTaf : tafs) {
            tafAdded = false;
            for (int i = 0 ; i < currentTafs.size(); ++i ) {
                if (currentTafs.get(i).getStationId().equals(newTaf.getStationId())){
                    currentTafs.remove(i);
                    currentTafs.add(i, newTaf);
                    tafAdded = true;
                    notifyItemChanged(i);
                    break;
                }
            }
            if (!tafAdded) {
                currentTafs.add(newTaf);
                notifyItemInserted(currentTafs.size() - 1);
            }
        }
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AirportMetarTafBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent
                .getContext()), BindingHolder.LAYOUT_RESOURCE, parent, false);
        return new BindingHolder(binding);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        holder.binding.setMetar(currentMetars.get(position));
        holder.binding.setDisplayPrefs(weatherDisplayPreferences);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return currentMetars.size();
    }

    public static class BindingHolder extends RecyclerView.ViewHolder {
        protected static final int LAYOUT_RESOURCE = R.layout.airport_metar_taf;

        private AirportMetarTafBinding binding;

        public BindingHolder(AirportMetarTafBinding bindingView) {
            super(bindingView.getRoot());
            binding = bindingView;
        }

    }

}
