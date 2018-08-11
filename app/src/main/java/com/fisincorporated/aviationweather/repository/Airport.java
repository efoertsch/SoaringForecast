package com.fisincorporated.aviationweather.repository;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.fisincorporated.aviationweather.utils.CSVUtils;

import java.util.List;

@Entity(indices = {@Index("name"),@Index(value={"state","name"}),@Index("municipality")})
public class Airport {

    @PrimaryKey
    @NonNull
    private String ident = "";

    private String type;

    private String name;

    private float latitudeDeg;

    private float longitudeDeg;

    private int elevationFt;

    private String state;

    private String municipality;


//"id","ident","type","name","latitude_deg","longitude_deg","elevation_ft","continent","iso_country","iso_region","municipality","scheduled_service","gps_code","iata_code","local_code","home_link","wikipedia_link","keywords"
//3756,"KORH","medium_airport","Worcester Regional Airport",42.26729965209961,-71.87570190429688,1009,"NA","US","US-MA","Worcester","no","KORH","ORH","ORH",,"http://en.wikipedia.org/wiki/Worcester-Metrowest-Boston_Airport"


    public Airport(){}

    public static Airport getNewAirport(){
        return new Airport();
    }

    public static Airport createAirportFromCSVDetail(String airportDetail) {
        // assumes example format and field position at above
        // Strip off quote at start and end of airportDetail

        List<String> airportDetails = CSVUtils.parseLine(airportDetail);
        Airport airport = new Airport();
        try {
            airport.ident = airportDetails.get(1);
            airport.type = airportDetails.get(2);
            airport.name = airportDetails.get(3);
            if (!airportDetails.get(4).isEmpty()) {
                airport.latitudeDeg = Float.parseFloat(airportDetails.get(4));
            }
            if (!airportDetails.get(5).isEmpty()){
                airport.longitudeDeg = Float.parseFloat(airportDetails.get(5));
        }
            if (!airportDetails.get(6).isEmpty()) {
                airport.elevationFt = Integer.parseInt(airportDetails.get(6));
            }
            airport.state = airportDetails.get(9).substring(3);
            airport.municipality = airportDetails.get(10);
        } catch (Exception nfe) {
            airport = null;
        }
        return airport;
    }

    @NonNull
    public String getIdent() {
        return ident;
    }

    public void setIdent(@NonNull String ident) {
        this.ident = ident;
    }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getLatitudeDeg() {
        return latitudeDeg;
    }

    public void setLatitudeDeg(float latitudeDeg) {
        this.latitudeDeg = latitudeDeg;
    }

    public float getLongitudeDeg() {
        return longitudeDeg;
    }

    public void setLongitudeDeg(float longitudeDeg) {
        this.longitudeDeg = longitudeDeg;
    }

    public int getElevationFt() {
        return elevationFt;
    }

    public void setElevationFt(int elevationFt) {
        this.elevationFt = elevationFt;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

}


