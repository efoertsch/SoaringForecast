package com.fisincorporated.aviationweather.airport.codelist;



import android.os.Parcel;
import android.os.Parcelable;

public class AirportList implements Parcelable {

    private String airportList;

    public String getAirportList() {
        return airportList;
    }

    public void setAirportList(String airportList) {
        this.airportList = airportList;
    }


    public String getAirportListForMetars() {
        StringBuilder sb = new StringBuilder();
        if (airportList != null) {
            String[] airportArray  = airportList.trim().split(" ");
            for (int i = 0; i < airportArray.length; ++i){
                sb.append(airportArray[i] + (i < airportArray.length - 1 ? ", " : ""));
            }
        }
        return sb.toString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.airportList);
    }

    public AirportList() {
    }

    protected AirportList(Parcel in) {
        this.airportList = in.readString();
    }

    public static final Parcelable.Creator<AirportList> CREATOR = new Parcelable
            .Creator<AirportList>() {
        @Override
        public AirportList createFromParcel(Parcel source) {
            return new AirportList(source);
        }

        @Override
        public AirportList[] newArray(int size) {
            return new AirportList[size];
        }
    };
}
