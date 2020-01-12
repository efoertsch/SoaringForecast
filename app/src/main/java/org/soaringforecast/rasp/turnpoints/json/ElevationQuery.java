package org.soaringforecast.rasp.turnpoints.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ElevationQuery {

    @SerializedName("x")
    @Expose
    private Double x;
    @SerializedName("y")
    @Expose
    private Double y;
    @SerializedName("Data_Source")
    @Expose
    private String data_Source;
    @SerializedName("Elevation")
    @Expose
    private Double elevation;
    @SerializedName("Units")
    @Expose
    private String units;

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public String getData_Source() {
        return data_Source;
    }

    public void setData_Source(String data_Source) {
        this.data_Source = data_Source;
    }

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

}
