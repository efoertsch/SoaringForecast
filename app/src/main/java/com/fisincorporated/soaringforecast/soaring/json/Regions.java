package com.fisincorporated.soaringforecast.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

/**
 * Contains a list of Region objects
 * Created from  call to http://www.soargbsc.com/rasp/current.json
 *
 */
public class Regions {

        @SerializedName("regions")
        @Expose
        private List<Region> regions = null;

        public List<Region> getRegions() {
            return regions;
        }

        public void setRegions(List<Region> regions) {
            this.regions = regions;
        }

        public Region getRegion(String regionName){
            if (regions == null) {
                return null;
            }
            for (Region region: regions) {
                if (regionName.equals(region.getName())){
                    return region;
                }
            }
            return null;

        }
        @Override
        public String toString() {
            return new StringBuilder().append("regions").append(Arrays.toString(regions.toArray())).toString();
        }

    }
