package com.fisincorporated.aviationweather.airports;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.fisincorporated.aviationweather.utils.CSVUtils;

import java.util.List;

@Entity(indices = {@Index("name"),@Index("state"),@Index("municipality")})
public class Airport {

    @PrimaryKey
    private String ident;

    private String name;

    private float latitudeDeg;

    private float longitudeDeg;

    private int elevationFt;

    private String state;

    private String municipality;


//"id","ident","type","name","latitude_deg","longitude_deg","elevation_ft","continent","iso_country","iso_region","municipality","scheduled_service","gps_code","iata_code","local_code","home_link","wikipedia_link","keywords"
//3756,"KORH","medium_airport","Worcester Regional Airport",42.26729965209961,-71.87570190429688,1009,"NA","US","US-MA","Worcester","no","KORH","ORH","ORH",,"http://en.wikipedia.org/wiki/Worcester-Metrowest-Boston_Airport"

    private Airport(){}

    public static Airport createAirport(String airportDetail) {
        // assumes example format and field position at above
        List<String> airportDetails = CSVUtils.parseLine(airportDetail);
        Airport airport = new Airport();
        try {
            airport.ident = airportDetails.get(1);
            airport.name = airportDetails.get(3);
            airport.latitudeDeg = Float.parseFloat(airportDetails.get(4));
            airport.longitudeDeg = Float.parseFloat(airportDetails.get(5));
            airport.elevationFt = Integer.parseInt(airportDetails.get(6));
            airport.state = airportDetails.get(9).substring(3);
            airport.municipality = airportDetails.get(10);
        } catch (Exception nfe) {
            airport = null;
        }
        return airport;
    }


    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

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


