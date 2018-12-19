package com.fisincorporated.soaringforecast.windy;

import com.fisincorporated.soaringforecast.satellite.data.SatelliteCode;

public class WindyLayer {

    private static final String COMMA_DELIMITER = ",";

    private int id;
    private String code;
    private String name;
    private boolean byAltitude;

    public WindyLayer(String codeCommaName) {
        String[] values = codeCommaName.split(COMMA_DELIMITER);
        id =  values.length > 0  ? Integer.parseInt(values[0]) : 0;
        code = values.length > 1 ? values[1].trim() : "";
        name = values.length > 2 ? values[2].trim() : "";
        byAltitude = values.length > 3 && ((values[3].trim().equals("1")));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isByAltitude() {
        return byAltitude;
    }

    public void setByAltitude(boolean byAltitude) {
        this.byAltitude = byAltitude;
    }

    //to display this as a string in spinner
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SatelliteCode) {
            WindyLayer c = (WindyLayer) obj;
            return c.getCode().equals(code) && c.getName().equals(name);
        }

        return false;
    }

    // For storing selected layer in SharedPreferences
    public String toStore(){
        return id
                + COMMA_DELIMITER + code.trim()
                + COMMA_DELIMITER + name.trim()
                + COMMA_DELIMITER + (byAltitude ? "1" :"0");
    }
}
