package com.fisincorporated.soaringforecast.windy;

import com.fisincorporated.soaringforecast.satellite.data.SatelliteCode;

public class WindyAltitude {

    private static final String COMMA_DELIMITER = ",";

    private int id;
    private String metric;
    private String imperial;
    private String windyCode;

    public WindyAltitude (String codeCommaName) {
        String[] values = codeCommaName.split(COMMA_DELIMITER);
        id =  values.length > 0  ? Integer.parseInt(values[0]) : 0;
        metric = values.length > 1 ? values[1].trim() : "";
        imperial = values.length > 2 ? values[2].trim() : "";
        windyCode = values.length > 3 ? values[3].trim() : "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getImperial() {
        return imperial;
    }

    public void setImperial(String imperial) {
        this.imperial = imperial;
    }

    public String getWindyCode() {
        return windyCode;
    }

    public void setWindyCode(String windyCode) {
        this.windyCode = windyCode;
    }

    //to display this as a string in spinner
    @Override
    public String toString() {
        return imperial;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SatelliteCode) {
            WindyAltitude  c = (WindyAltitude ) obj;
            return c.getMetric().equals(metric) && c.getImperial().equals(imperial);
        }

        return false;
    }

    // For storing selected altitude in SharedPreferences
    public String toStore(){
        return id
                + COMMA_DELIMITER + metric.trim()
                + COMMA_DELIMITER + imperial.trim()
                + COMMA_DELIMITER + windyCode.trim();
    }
}
