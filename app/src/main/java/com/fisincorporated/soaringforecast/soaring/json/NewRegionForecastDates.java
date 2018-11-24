package com.fisincorporated.soaringforecast.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class NewRegionForecastDates {

        @SerializedName("regions")
        @Expose
        private List<Region> regions = null;

        public List<Region> getRegions() {
            return regions;
        }

        public void setRegions(List<Region> regions) {
            this.regions = regions;
        }

        @Override
        public String toString() {
            return new StringBuilder().append("regions").append(Arrays.toString(regions.toArray())).toString();
        }

    }
