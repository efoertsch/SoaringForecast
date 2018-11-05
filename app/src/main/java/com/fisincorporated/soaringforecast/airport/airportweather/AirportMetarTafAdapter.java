package com.fisincorporated.soaringforecast.airport.airportweather;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.data.AirportMetarTaf;
import com.fisincorporated.soaringforecast.data.metars.Metar;
import com.fisincorporated.soaringforecast.data.taf.Forecast;
import com.fisincorporated.soaringforecast.data.taf.SkyCondition;
import com.fisincorporated.soaringforecast.data.taf.TAF;
import com.fisincorporated.soaringforecast.databinding.AirportWeatherBinding;
import com.fisincorporated.soaringforecast.databinding.SkyConditionBinding;
import com.fisincorporated.soaringforecast.databinding.TafForecastBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class AirportMetarTafAdapter extends RecyclerView.Adapter<AirportMetarTafAdapter.BindingHolder> {

    private List<AirportMetarTaf> airportMetarTafList = Collections.synchronizedList(new ObservableArrayList<AirportMetarTaf>());

    public WeatherMetarTafPreferences weatherMetarTafPreferences;

    @Inject
    public AppPreferences appPreferences;

    @Inject
    public AirportMetarTafAdapter() {
    }

    public AirportMetarTafAdapter setAirportMetarTafList(@NonNull List<AirportMetarTaf> selectedAirports) {
        synchronized (airportMetarTafList) {
            if (airportMetarTafList.size() == 0) {
                airportMetarTafList.addAll(selectedAirports);
            } else {
                // Make sure list of airports ordered (and w/ weather info) based on list passed in
                ArrayList<AirportMetarTaf> newOrderedList = new ArrayList<>();
                boolean airportAdded;
                for(AirportMetarTaf selectedAirport : selectedAirports) {
                    airportAdded = false;
                    for (AirportMetarTaf existingAirport : airportMetarTafList) {
                        if (selectedAirport.getIcaoId().equals(existingAirport.getIcaoId())) {
                            newOrderedList.add(existingAirport);
                            airportAdded = true;
                        }
                    }
                    if (!airportAdded) {
                        newOrderedList.add(selectedAirport);
                    }
                }
                airportMetarTafList.clear();
                airportMetarTafList.addAll(newOrderedList);
            }
        }
        return this;

    }

    public AirportMetarTafAdapter setWeatherMetarTafPreferences(@NonNull WeatherMetarTafPreferences weatherMetarTafPreferences) {
        this.weatherMetarTafPreferences = weatherMetarTafPreferences;
        return this;
    }

    //TODO DRY
    public void updateMetarList(@NonNull List<Metar> metarList) {
        if (metarList != null) {
            synchronized (airportMetarTafList) {
                boolean metarFound;
                for (Metar newMetar : metarList) {
                    metarFound = false;
                    for (int i = 0; i < airportMetarTafList.size(); ++i) {
                        AirportMetarTaf airportMetarTaf = airportMetarTafList.get(i);
                        if (airportMetarTaf.getIcaoId().equals(newMetar.getStationId())) {
                            airportMetarTaf.setElevationM(newMetar.getElevationM());
                            airportMetarTaf.setMetar(newMetar);
                            notifyItemChanged(i);
                            metarFound = true;
                            break;
                        }
                    }
                    if (!metarFound) {
                        AirportMetarTaf airportMetarTaf = new AirportMetarTaf();
                        airportMetarTaf.setMetar(newMetar);
                        airportMetarTaf.setIcaoId(newMetar.getStationId());
                        airportMetarTaf.setElevationM(newMetar.getElevationM());
                        airportMetarTafList.add(airportMetarTaf);
                        notifyItemInserted(airportMetarTafList.size() - 1);
                    }
                }
            }
        }
    }

    //TODO DRY
    public void updateTafList(List<TAF> tafs) {
        if (tafs != null && tafs.size() > 0) {
            synchronized (airportMetarTafList) {
                boolean tafFound;
                for (TAF newTaf : tafs) {
                    tafFound = false;
                    for (int i = 0; i < airportMetarTafList.size(); ++i) {
                        AirportMetarTaf airportMetarTaf = airportMetarTafList.get(i);
                        if (airportMetarTaf.getIcaoId().equals(newTaf.getStationId())) {
                            airportMetarTaf.setElevationM(newTaf.getElevationM());
                            airportMetarTaf.setTaf(newTaf);
                            notifyItemChanged(i);
                            tafFound = true;
                            break;
                        }
                    }
                    if (!tafFound) {
                        AirportMetarTaf airportMetarTaf = new AirportMetarTaf();
                        airportMetarTaf.setTaf(newTaf);
                        airportMetarTaf.setIcaoId(newTaf.getStationId());
                        airportMetarTaf.setElevationM(newTaf.getElevationM());
                        airportMetarTafList.add(airportMetarTaf);
                        notifyItemInserted(airportMetarTafList.size() - 1);
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
        holder.binding.setAirportMetarTaf(airportMetarTafList.get(position));
        holder.binding.setDisplayPrefs(weatherMetarTafPreferences);
        addTafForecasts(holder, airportMetarTafList.get(position).getTaf());
        holder.binding.executePendingBindings();
    }

    private void addTafForecasts(BindingHolder holder, TAF taf) {
        if (taf != null && taf.getForecast() != null & taf.getForecast().size() > 0) {
            LinearLayout layout = holder.binding.airportWeatherIncludeTaf.airportTafForecastLayout;
            LayoutInflater inflater = (LayoutInflater) layout.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            List<Forecast> forecastList = taf.getForecast();
            for (Forecast forecast : forecastList) {
                TafForecastBinding binding = DataBindingUtil.inflate(inflater, R.layout.taf_forecast, layout, false);
                binding.setForecast(forecast);
                binding.setDisplayPrefs(weatherMetarTafPreferences);
                binding.getRoot().setLayoutParams(new LinearLayoutCompat.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                layout.addView(binding.getRoot());
                addSkyConditionsToForecast(binding, forecast);
            }
        }
    }

    private void addSkyConditionsToForecast(TafForecastBinding tafForecastBinding, Forecast forecast) {
        if (forecast != null && forecast.getSkyCondition() != null & forecast.getSkyCondition().size() > 0) {
            LinearLayout layout = tafForecastBinding.airportTafCloudLayerLayout;
            LayoutInflater inflater = (LayoutInflater) layout.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            List<SkyCondition> skyConditionList = forecast.getSkyCondition();
            for (SkyCondition skyCondition : skyConditionList) {
                SkyConditionBinding binding = DataBindingUtil.inflate(inflater, R.layout.sky_condition, layout, false);
                binding.setSkyCondition(skyCondition);
                binding.setDisplayPrefs(weatherMetarTafPreferences);
                binding.getRoot().setLayoutParams(new LinearLayoutCompat.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                layout.addView(binding.getRoot());
            }
        }

    }

    @Override
    public int getItemCount() {
        return airportMetarTafList.size();
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
