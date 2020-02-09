package org.soaringforecast.rasp.repository;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import org.soaringforecast.rasp.utils.CSVUtils;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

//SeeYou cup file format
//Title,Code,Country,Latitude,Longitude,Elevation,Style,Direction,Length,Frequency,Description
//"Sterling","3B3",US,4225.500N,07147.470W,459ft,5,160,3086ft,122.900,"Home Field, Finish Point, Turn Point, 3B3, RW width: 40, CTAF: 122.9, Fuel: 100LL"

@Entity(indices = {@Index(value = {"title", "code"}, unique = true), @Index("code")})
public class Turnpoint implements Cloneable, Parcelable {

    private static final String AIRPORT_DETAILS = "%1$s  %2$s\n%3$s\nLat: %4$f Long:%5$f\nElev: %6$s \nDirection: %7$s Length:%8$s\nFreq: %9$s\n%10$s";
    private static final String NON_AIRPORT_DETAILS = "%1$s  %2$s\n%3$s\nLat: %4$f Long:%5$f\nElev: %6$s \n%7$s ";
    private static final DecimalFormat latitudeFormat = new DecimalFormat("0000.000");
    private static final DecimalFormat longitudeFormat = new DecimalFormat("00000.000");
    private static final String QUOTE = "\"";
    private static final String COMMA = ",";
    @NonNull
    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String title = "";

    @NonNull
    private String code = "";

    private String country = "";

    private float latitudeDeg = 0;

    private float longitudeDeg = 0;

    private String elevation = "";

    private String style = "0";

    private String direction = "";

    private String length = "";

    private String frequency = "";

    private String description = "";

    public Turnpoint() {
    }

    public static Turnpoint newInstance(Turnpoint turnpoint) {
        Turnpoint newTurnpoint = new Turnpoint();
        newTurnpoint.id = turnpoint.id;
        newTurnpoint.title = turnpoint.title;
        newTurnpoint.code = turnpoint.code;
        newTurnpoint.country = turnpoint.country;
        newTurnpoint.latitudeDeg = turnpoint.latitudeDeg;
        newTurnpoint.longitudeDeg = turnpoint.longitudeDeg;
        newTurnpoint.elevation = turnpoint.elevation;
        newTurnpoint.style = turnpoint.style;
        newTurnpoint.direction = turnpoint.direction;
        newTurnpoint.length = turnpoint.length;
        newTurnpoint.frequency = turnpoint.frequency;
        newTurnpoint.description = turnpoint.description;
        return newTurnpoint;
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

    public void setLatitudeDeg(double latitudeDeg) {
        this.latitudeDeg = (float) latitudeDeg;
    }

    public float getLongitudeDeg() {
        return longitudeDeg;
    }

    public void setLongitudeDeg(float longitudeDeg) {
        this.longitudeDeg = longitudeDeg;
    }

    public void setLongitudeDeg(double longitudeDeg) {
        this.longitudeDeg = (float) longitudeDeg;
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
                + (Float.parseFloat(longitudeString.substring(3, 5)) / 60.0f)
                + (Float.parseFloat(longitudeString.substring(5, 9)) / 60.0f))
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

    public boolean isLandable() {
        return style != null && style.matches("[2345]");

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

    public boolean isGrassOrGliderAirport() {
        return (style.equals("2") || style.equals("4"));
    }

    public boolean isHardSurfaceAirport() {
        return style.equals("5");
    }

    public String getLatitudeInCupFormat() {
        return getLatitudeInCupFormat(latitudeDeg);
    }

    public static String getLatitudeInCupFormat(float lat) {
        float latitude = Math.abs(lat);
        int degrees = (int) latitude;
        // This convoluted expression is needed for a couple cases where conversion of
        // cup string -> float -> cup string format was off by .001
        float minutes = Float.valueOf(longitudeFormat.format((latitude - degrees) * 60f));
        return latitudeFormat.format(Math.abs(degrees * 100 + minutes)) + (lat >= 0 ? 'N' : 'S');
    }


    public String getLongitudeInCupFormat() {
        return getLongitudeInCupFormat(longitudeDeg);
    }


    public static String getLongitudeInCupFormat(float lng) {
        float longitude = Math.abs(lng);
        int degrees = (int) longitude;
        // This convoluted expression is needed for a couple cases where conversion of
        // cup string -> float -> cup string format was off by .001
        float minutes = Float.valueOf(longitudeFormat.format((longitude - degrees) * 60f));
        return longitudeFormat.format((degrees * 100f) + minutes) + (lng >= 0 ? 'E' : 'W');
    }

    public LatLng getLatLng() {
        return new LatLng(latitudeDeg, longitudeDeg);
    }

    public String getCupFormattedRecord() {
        StringBuffer sb = new StringBuffer();
        sb.append(QUOTE).append(title).append(QUOTE).append(COMMA);
        sb.append(QUOTE).append(code).append(QUOTE).append(COMMA);
        sb.append(country).append(COMMA);
        sb.append(getLatitudeInCupFormat()).append(COMMA);
        sb.append(getLongitudeInCupFormat()).append(COMMA);
        sb.append(elevation).append(COMMA);
        sb.append(style).append(COMMA);
        sb.append(direction).append(COMMA);
        sb.append(length).append(COMMA);
        sb.append(frequency).append(COMMA);
        if (!description.isEmpty()) {
            sb.append(QUOTE).append(description).append(QUOTE);
        }
        return sb.toString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeString(this.code);
        dest.writeString(this.country);
        dest.writeFloat(this.latitudeDeg);
        dest.writeFloat(this.longitudeDeg);
        dest.writeString(this.elevation);
        dest.writeString(this.style);
        dest.writeString(this.direction);
        dest.writeString(this.length);
        dest.writeString(this.frequency);
        dest.writeString(this.description);
    }

    protected Turnpoint(Parcel in) {
        this.id = in.readLong();
        this.title = in.readString();
        this.code = in.readString();
        this.country = in.readString();
        this.latitudeDeg = in.readFloat();
        this.longitudeDeg = in.readFloat();
        this.elevation = in.readString();
        this.style = in.readString();
        this.direction = in.readString();
        this.length = in.readString();
        this.frequency = in.readString();
        this.description = in.readString();
    }

    public static final Parcelable.Creator<Turnpoint> CREATOR = new Parcelable.Creator<Turnpoint>() {
        @Override
        public Turnpoint createFromParcel(Parcel source) {
            return new Turnpoint(source);
        }

        @Override
        public Turnpoint[] newArray(int size) {
            return new Turnpoint[size];
        }
    };

}
