package com.fisincorporated.aviationweather.airportweather;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.data.AirportWeather;
import com.fisincorporated.aviationweather.data.metars.Metar;
import com.fisincorporated.aviationweather.data.taf.TAF;
import com.fisincorporated.aviationweather.databinding.AirportWeatherBinding;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class AirportWeatherAdapter extends RecyclerView.Adapter<AirportWeatherAdapter.BindingHolder> {

    private List<AirportWeather> airportWeatherList = Collections.synchronizedList(new ObservableArrayList<AirportWeather>());

    public WeatherDisplayPreferences weatherDisplayPreferences;

    @Inject
    public AppPreferences appPreferences;

    @Inject
    public AirportWeatherAdapter() {
    }

    public AirportWeatherAdapter setAirportWeatherList(@NonNull List<AirportWeather> airportWeatherList) {
        airportWeatherList.addAll(airportWeatherList);
        return this;
    }

    public AirportWeatherAdapter setWeatherDisplayPreferences(@NonNull WeatherDisplayPreferences weatherDisplayPreferences) {
        this.weatherDisplayPreferences = weatherDisplayPreferences;
        return this;
    }

    //TODO DRY
    public void updateMetarList(@NonNull List<Metar> metarList) {
        if (metarList != null) {
            synchronized (airportWeatherList) {
                boolean metarFound;
                for (Metar newMetar : metarList) {
                    metarFound = false;
                    for (int i = 0; i < airportWeatherList.size(); ++i) {
                        AirportWeather airportWeather = airportWeatherList.get(i);
                        if (airportWeather.getIcaoId().equals(newMetar.getStationId())) {
                            airportWeather.setElevationM(newMetar.getElevationM());
                            airportWeather.setMetar(newMetar);
                            notifyItemChanged(i);
                            metarFound = true;
                            break;
                        }
                    }
                    if (!metarFound) {
                        AirportWeather airportWeather = new AirportWeather();
                        airportWeather.setMetar(newMetar);
                        airportWeather.setIcaoId(newMetar.getStationId());
                        airportWeather.setElevationM(newMetar.getElevationM());
                        airportWeatherList.add(airportWeather);
                        notifyItemInserted(airportWeatherList.size() - 1);
                    }
                }
            }
        }
    }

    //TODO DRY
    public void updateTafList(List<TAF> tafs) {
        if (tafs != null) {
            synchronized (airportWeatherList) {
                boolean tafFound;
                for (TAF newTaf : tafs) {
                    tafFound = false;
                    for (int i = 0; i < airportWeatherList.size(); ++i) {
                        AirportWeather airportWeather = airportWeatherList.get(i);
                        if (airportWeather.getIcaoId().equals(newTaf.getStationId())) {
                            airportWeather.setElevationM(newTaf.getElevationM());
                            airportWeather.setTaf(newTaf);
                            notifyItemChanged(i);
                            tafFound = true;
                            break;
                        }
                    }
                    if (!tafFound) {
                        AirportWeather airportWeather = new AirportWeather();
                        airportWeather.setTaf(newTaf);
                        airportWeather.setIcaoId(newTaf.getStationId());
                        airportWeather.setElevationM(newTaf.getElevationM());
                        airportWeatherList.add(airportWeather);
                        notifyItemInserted(airportWeatherList.size() - 1);
                    }
                }
            }
        }
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AirportWeatherBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent
                .getContext()), BindingHolder.LAYOUT_RESOURCE, parent, false);
        return new BindingHolder(binding);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        holder.binding.setAirportWeather(airportWeatherList.get(position));
        holder.binding.setDisplayPrefs(weatherDisplayPreferences);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return airportWeatherList.size();
    }

    public static class BindingHolder extends RecyclerView.ViewHolder {
        protected static final int LAYOUT_RESOURCE = R.layout.airport_weather;

        private AirportWeatherBinding binding;

        public BindingHolder(AirportWeatherBinding bindingView) {
            super(bindingView.getRoot());
            binding = bindingView;
        }

    }

}
