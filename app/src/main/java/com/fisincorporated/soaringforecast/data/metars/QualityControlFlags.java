package com.fisincorporated.soaringforecast.data.metars;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;


@Root(name = "quality_control_flags", strict = false)
public class QualityControlFlags {

    @Element(name = "corrected", required = false)
    private String corrected;

    @Element(name = "auto", required = false)
    private String auto;

    @Element(name = "auto_station", required = false)
    private String autoStation;

    @Element(name = "maintenance_indicator_on", required = false)
    private String maintenanceIndicatorOn;

    @Element(name = "no_signal", required = false)
    private String noSignal;

    @Element(name = "lightning_sensor_off", required = false)
    private String lightningSensorOff;

    @Element(name = "freezing_rain_sensor_off", required = false)
    private String freezingRainSensorOff;

    @Element(name = "present_weather_sensor_off", required = false)
    private String presentWeatherSensorOff;

    public String getAutoStation() {
        return this.autoStation;
    }

    public void setAutoStation(String value) {
        this.autoStation = value;
    }

    public String getMaintenanceIndicatorOn() {
        return this.maintenanceIndicatorOn;
    }

    public void setMaintenanceIndicatorOn(String value) {
        this.maintenanceIndicatorOn = value;
    }

    public String getNoSignal() {
        return noSignal;
    }

    public void setNoSignal(String noSignal) {
        this.noSignal = noSignal;
    }

    public String getLightningSensorOff() {
        return lightningSensorOff;
    }

    public void setLightningSensorOff(String lightningSensorOff) {
        this.lightningSensorOff = lightningSensorOff;
    }

    public String getFreezingRainSensorOff() {
        return freezingRainSensorOff;
    }

    public void setFreezingRainSensorOff(String freezingRainSensorOff) {
        this.freezingRainSensorOff = freezingRainSensorOff;
    }

    public String getPresentWeatherSensorOff() {
        return presentWeatherSensorOff;
    }

    public void setPresentWeatherSensorOff(String presentWeatherSensorOff) {
        this.presentWeatherSensorOff = presentWeatherSensorOff;
    }
}
