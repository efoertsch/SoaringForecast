package com.fisincorporated.aviationweather.soaring.json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseDate {

    private static final DateFormat yyyymmddFormat = new SimpleDateFormat("yyyy-MM-dd" );
    private static final DateFormat formattedDateFormat = new SimpleDateFormat(("EEE, MMM dd"));

    protected int index;
    protected String formattedDate;    //"Friday March 30"
    protected String yyyymmddDate;     //"2018-03-30"

    public BaseDate() {
        index = 0;
        Date date = new Date();
        formattedDate = formattedDateFormat.format(date);
        yyyymmddDate = yyyymmddFormat.format(date);
    }

    public BaseDate(int index, String formattedDate, String yyyymmddDate) {
       setBaseDate(index,formattedDate,yyyymmddDate);
    }

    public void setBaseDate(int index, String formattedDate, String yyyymmddDate){
        this.index = index;
        this.formattedDate = formattedDate;
        this.yyyymmddDate = yyyymmddDate;
    }

    public int getIndex() {
        return index;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public String getYyyymmddDate() {
        return yyyymmddDate;
    }

    public void setYyyymmddDate(String yyyymmddDate) {
        this.yyyymmddDate = yyyymmddDate;
    }

    // For date display
    @Override
    public String toString() {
        return formattedDate;
    }

}
