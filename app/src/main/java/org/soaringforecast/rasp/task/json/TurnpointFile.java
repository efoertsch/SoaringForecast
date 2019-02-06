package org.soaringforecast.rasp.task.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class TurnpointFile {

    @SerializedName("state")
    @Expose
    private String state;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("filename")
    @Expose
    private String filename;
    @SerializedName("date")
    @Expose
    private String date;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRelativeUrl(){
        return new StringBuilder().append(getLocation()).append("/").append(getFilename()).toString();
    }

}