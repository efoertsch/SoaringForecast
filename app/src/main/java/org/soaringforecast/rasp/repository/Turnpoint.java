package org.soaringforecast.rasp.repository;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import org.soaringforecast.rasp.utils.CSVUtils;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

//SeeYou cup file format
//Title,Code,Country,Latitude,Longitude,Elevation,Style,Direction,Length,Frequency,Description
//"Sterling","3B3",US,4225.500N,07147.470W,459ft,5,160,3086ft,122.900,"Home Field, Finish Point, Turn Point, 3B3, RW width: 40, CTAF: 122.9, Fuel: 100LL"

@Entity(indices = {@Index(value = {"title", "code"}, unique = true), @Index("code")})
public class Turnpoint {

    private static final String AIRPORT_DETAILS = "%1$s  %2$s\n%3$s\nLat: %4$f Long:%5$f\nElev: %6$s \nDirection: %7$s Length:%8$s\nFreq: %9$s\n%10$s";
    private static final String NON_AIRPORT_DETAILS = "%1$s  %2$s\n%3$s\nLat: %4$f Long:%5$f\nElev: %6$s \n%7$s ";
    private static final DecimalFormat latitudeFormat = new DecimalFormat("0000.000");
    private static final DecimalFormat longitudeFormat = new DecimalFormat("00000.000");

    @NonNull
    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String title = "";

    @NonNull
    private String code = "";

    private String country;

    private float latitudeDeg;

    private float longitudeDeg;

    private String elevation;

    private String style;

    private String direction;

    private String length;

    private String frequency;

    private String description;

    public Turnpoint() {
    }

    public static Turnpoint createTurnpointFromCSVDetail(String turnpointDetail) {
        List<String> turnpointDetails = CSVUtils.parseLine(turnpointDetail);

        Turnpoint turnpoint = new Turnpoint();

        try {
            turnpoint.title = turnpointDetails.get(0);
            turnpoint.code = turnpointDetails.get(1);
            turnpoint.country = turnpointDetails.get(2);
            turnpoint.latitudeDeg = convertToLat(turnpointDetails.get(3));
            turnpoint.longitudeDeg = convertToLong(turnpointDetails.get(4));

            turnpoint.elevation = turnpointDetails.get(5);
            turnpoint.style = turnpointDetails.get(6);
            turnpoint.direction = turnpointDetails.get(7);
            turnpoint.length = turnpointDetails.get(8);
            turnpoint.frequency = turnpointDetails.get(9);
            turnpoint.description = turnpointDetails.get(10);

        } catch (Exception e) {
            turnpoint = null;
        }
        return turnpoint;

    }

    public void setId(@NonNull long id) {
        this.id = id;
    }

    @NonNull
    public long getId() {
        return id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getCode() {
        return code;
    }

    public void setCode(@NonNull String code) {
        this.code = code;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public String getElevation() {
        return elevation;
    }

    public void setElevation(String elevation) {
        this.elevation = elevation;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public void updateTurnpoint(Turnpoint turnpointUpdate) {
        country = turnpointUpdate.getCountry();
        latitudeDeg = turnpointUpdate.getLatitudeDeg();
        longitudeDeg = turnpointUpdate.getLongitudeDeg();
        elevation = turnpointUpdate.getElevation();
        style = turnpointUpdate.getStyle();
        direction = turnpointUpdate.getDirection();
        length = turnpointUpdate.getLength();
        frequency = turnpointUpdate.getFrequency();
        description = turnpointUpdate.getDescription();

    }

    /**
     * @param latitudeString is a field of length 9 (1 based), where 1-2 characters are degrees
     *                       , 3-4 characters are minutes, 5 decimal point
     *                       , 6-8 characters are decimal minutes
     *                       and 9th character is either N or S
     *                       eg 4225.500N
     * @return latitude converted to decimal degrees
     * @throws Exception
     */
    public static float convertToLat(String latitudeString) throws Exception {
        if (latitudeString.length() != 9
                || !(latitudeString.endsWith("N") || latitudeString.endsWith("S"))) {
            throw new Exception();
        }

        return (Float.parseFloat(latitudeString.substring(0, 2))
                + (Float.parseFloat(latitudeString.substring(2, 4)) / 60)
                + (Float.parseFloat(latitudeString.substring(4, 8)) / 60))
                * (latitudeString.substring(8).equals("N") ? 1f : -1f);
    }

    /**
     * @param longitudeString is a field of length 10 (1 based), where
     *                        1-3 characters are degrees
     *                        4-5 characters are minutes,
     *                        6 decimal point
     *                        7-9 characters are decimal minutes
     *                        10th character is either E or W.
     *                        eg 07147.470W
     * @return longitude converted to decimal degrees
     * @throws Exception
     */
    public static float convertToLong(String longitudeString) throws Exception {
        if (longitudeString.length() != 10
                || !(longitudeString.endsWith("E") || longitudeString.endsWith("W"))) {
            throw new Exception();
        }
        return (Float.parseFloat(longitudeString.substring(0, 3))
                + (Float.parseFloat(longitudeString.substring(3, 5)) / 60)
                + (Float.parseFloat(longitudeString.substring(5, 9)) / 60))
                * (longitudeString.endsWith("E") ? 1f : -1f);

    }

    public String getStyleName() {
        return getStyleName(style);
    }

    public static String getStyleName(String style) {
        switch (style) {
            case "0":
                return "Unknown";
            case "1":
                return "Waypoint";
            case "2":
                return "Airfield with grass surface runway";
            case "3":
                return "Outlanding";
            case "4":
                return "Gliding airfield";
            case "5":
                return "Airfield with solid surface runway";
            case "6":
                return "Mountain Pass";
            case "7":
                return "Mountain Top";
            case "8":
                return "Transmitter Mast";
            case "9":
                return "VOR";
            case "10":
                return "NDB";
            case "11":
                return "Cooling Tower";
            case "12":
                return "Dam";
            case "13":
                return "Tunnel";
            case "14":
                return "Bridge";
            case "15":
                return "Power Plant";
            case "16":
                return "Castle";
            case "17":
                return "Intersection";
            default:
                return "Unknown";
        }
    }

    public String getFormattedTurnpointDetails() {
        String turnpointDetails;
        switch (style) {
            case "2":
            case "4":
            case "5":
                turnpointDetails = String.format(Locale.getDefault(), AIRPORT_DETAILS
                        , getTitle(), getCode()
                        , getStyleName()
                        , getLatitudeDeg(), getLongitudeDeg()
                        , getElevation()
                        , getDirection(), getLength()
                        , getFrequency()
                        , getDescription());
                break;
            default:
                turnpointDetails = String.format(Locale.getDefault(), NON_AIRPORT_DETAILS
                        , getTitle(), getCode()
                        , getStyleName()
                        , getLatitudeDeg(), getLongitudeDeg()
                        , getElevation()
                        , getDescription());
        }
        return turnpointDetails;
    }

    public boolean isGrassOrGliderAirport(){
        return (style.equals("2") || style.equals("4"));
    }

    public boolean isHardSurfaceAirport(){
        return style.equals("5");
    }

    public String getLatitudeInCupFormat() {
        return getLatitudeInCupFormat(latitudeDeg);
    }

    public static String getLatitudeInCupFormat(float lat) {
        int degrees =  (int) lat ;
        float minutes =  (lat - degrees) * 60 ;
        return  latitudeFormat.format(Math.abs(degrees * 100  + minutes))+ (degrees >= 0 ? 'N' : 'S');
    }


    public String getLongitudeInCupFormat() {
        return getLongitudeInCupFormat(longitudeDeg);
    }

    public static String getLongitudeInCupFormat(float lng) {
        int degrees =  (int) lng;
        float minutes =  (lng - degrees) * 60 ;
        return  longitudeFormat.format(Math.abs(degrees * 100  + minutes))+ (degrees >= 0 ? 'E' : 'W');

    }
}
