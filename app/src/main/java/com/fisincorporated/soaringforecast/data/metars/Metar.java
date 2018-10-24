package com.fisincorporated.soaringforecast.data.metars;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;


@Root(name = "METAR", strict = false)
public class Metar {

    @Element(name = "raw_text", required = false)
    private String rawText;

    @Element(name = "station_id", required = false)
    private String stationId;

    @Element(name = "observation_time", required = false)
    private String observationTime;

    @Element(name = "latitude", required = false)
    private Float latitude;

    @Element(name = "longitude", required = false)
    private Float longitude;

    @Element(name = "temp_c", required = false)
    private Float tempC;

    @Element(name = "dewpoint_c", required = false)
    private Float dewpointC;

    @Element(name = "wind_dir_degrees", required = false)
    private Integer windDirDegrees;

    @Element(name = "wind_speed_kt", required = false)
    private Integer windSpeedKt;

    @Element(name = "wind_gust_kt", required = false)
    private Integer windGustKt;

    @Element(name = "visibility_statute_mi", required = false)
    private Float visibilityStatuteMi;

    @Element(name = "altim_in_hg", required = false)
    private Float altimInHg;

    @Element(name = "sea_level_pressure_mb", required = false)
    private Float seaLevelPressureMb;

    @Element(name = "quality_control_flags", required = false)
    private QualityControlFlags qualityControlFlags;

    @Element(name = "wx_string", required = false)
    private String wxString;

    @ElementList(name = "sky_condition", required = false, inline = true)
    private List<SkyCondition> skyConditions;

    @Element(name = "flight_category", required = false)
    private String flightCategory;

    @Element(name = "three_hr_pressure_tendency_mb", required = false)
    private Float threeHrPressureTendencyMb;

    @Element(name = "maxT_c", required = false)
    private Float maxTC;

    @Element(name = "minT_c", required = false)
    private Float minTC;

    @Element(name = "maxT24hr_c", required = false)
    private Float maxT24HrC;

    @Element(name = "minT24hr_c", required = false)
    private Float minT24HrC;

    @Element(name = "precip_in", required = false)
    private Float precipIn;

    @Element(name = "pcp3hr_in", required = false)
    private Float pcp3HrIn;

    @Element(name = "pcp6hr_in", required = false)
    private Float pcp6HrIn;

    @Element(name = "pcp24hr_in", required = false)
    private Float pcp24HrIn;

    @Element(name = "snow_in", required = false)
    private Float snowIn;

    @Element(name = "vert_vis_ft", required = false)
    private Integer vertVisFt;

    @Element(name = "metar_type", required = false)
    private String metarType;

    @Element(name = "elevation_m", required = false)
    private Float elevationM;

    public String getRawText() {
        return this.rawText;
    }

    public void setRawText(String value) {
        this.rawText = value;
    }


    public String getStationId() {
        return this.stationId;
    }

    public void setStationId(String value) {
        this.stationId = value;
    }


    public String getObservationTime() {
        return this.observationTime;
    }

    public void setObservationTime(String value) {
        this.observationTime = value;
    }


    public Float getLatitude() {
        return this.latitude;
    }

    public void setLatitude(Float value) {
        this.latitude = value;
    }


    public Float getLongitude() {
        return this.longitude;
    }

    public void setLongitude(Float value) {
        this.longitude = value;
    }


    public Float getTempC() {
        return this.tempC;
    }

    public void setTempC(Float value) {
        this.tempC = value;
    }


    public Float getDewpointC() {
        return this.dewpointC;
    }

    public void setDewpointC(Float value) {
        this.dewpointC = value;
    }


    public Integer getWindDirDegrees() {
        return this.windDirDegrees;
    }

    public void setWindDirDegrees(int value) {
        this.windDirDegrees = value;
    }


    public Integer getWindSpeedKt() {
        return this.windSpeedKt;
    }

    public void setWindSpeedKt(int value) {
        this.windSpeedKt = value;
    }


    public Float getVisibilityStatuteMi() {
        return this.visibilityStatuteMi;
    }

    public void setVisibilityStatuteMi(Float value) {
        this.visibilityStatuteMi = value;
    }


    public Float getAltimInHg() {
        return this.altimInHg;
    }

    public void setAltimInHg(Float value) {
        this.altimInHg = value;
    }


    public Float getSeaLevelPressureMb() {
        return this.seaLevelPressureMb;
    }

    public void setSeaLevelPressureMb(Float value) {
        this.seaLevelPressureMb = value;
    }


    public QualityControlFlags getQualityControlFlags() {
        return this.qualityControlFlags;
    }

    public void setQualityControlFlags(QualityControlFlags value) {
        this.qualityControlFlags = value;
    }


    public String getWxString() {
        return this.wxString;
    }

    public void setWxString(String value) {
        this.wxString = value;
    }

    public List<SkyCondition> getSkyConditions() {
        return skyConditions;
    }

    public void setSkyConditions(List<SkyCondition> skyConditions) {
        this.skyConditions = skyConditions;
    }

    public String getFlightCategory() {
        return this.flightCategory;
    }

    public void setFlightCategory(String value) {
        this.flightCategory = value;
    }


    public Float getPrecipIn() {
        return this.precipIn;
    }

    public void setPrecipIn(Float value) {
        this.precipIn = value;
    }


    public String getMetarType() {
        return this.metarType;
    }

    public void setMetarType(String value) {
        this.metarType = value;
    }


    public Float getElevationM() {
        return this.elevationM;
    }

    public void setElevationM(Float value) {
        this.elevationM = value;
    }

    public Integer getWindGustKt() {
        return windGustKt;
    }

    public void setWindGustKt(Integer windGustKt) {
        this.windGustKt = windGustKt;
    }

    public Float getThreeHrPressureTendencyMb() {
        return threeHrPressureTendencyMb;
    }

    public void setThreeHrPressureTendencyMb(Float threeHrPressureTendencyMb) {
        this.threeHrPressureTendencyMb = threeHrPressureTendencyMb;
    }

    public Float getMaxTC() {
        return maxTC;
    }

    public void setMaxTC(Float maxTC) {
        this.maxTC = maxTC;
    }

    public Float getMinTC() {
        return minTC;
    }

    public void setMinTC(Float minTC) {
        this.minTC = minTC;
    }

    public Float getMaxT24HrC() {
        return maxT24HrC;
    }

    public void setMaxT24HrC(Float maxT24HrC) {
        this.maxT24HrC = maxT24HrC;
    }

    public Float getMinT24HrC() {
        return minT24HrC;
    }

    public void setMinT24HrC(Float minT24HrC) {
        this.minT24HrC = minT24HrC;
    }

    public Float getPcp3HrIn() {
        return pcp3HrIn;
    }

    public void setPcp3HrIn(Float pcp3HrIn) {
        this.pcp3HrIn = pcp3HrIn;
    }

    public Float getPcp6HrIn() {
        return pcp6HrIn;
    }

    public void setPcp6HrIn(Float pcp6HrIn) {
        this.pcp6HrIn = pcp6HrIn;
    }

    public Float getPcp24HrIn() {
        return pcp24HrIn;
    }

    public void setPcp24HrIn(Float pcp24HrIn) {
        this.pcp24HrIn = pcp24HrIn;
    }

    public Float getSnowIn() {
        return snowIn;
    }

    public void setSnowIn(Float snowIn) {
        this.snowIn = snowIn;
    }

    public Integer getVertVisFt() {
        return vertVisFt;
    }

    public void setVertVisFt(Integer vertVisFt) {
        this.vertVisFt = vertVisFt;
    }

    //TODO refactor with TAF SkyCondition logic
    public String getSkyConditionsListString() {
        StringBuilder sb = new StringBuilder();
        if (skyConditions != null) {
            for (SkyCondition skyCondition : skyConditions) {
                sb.append((skyCondition.getCloudBaseFtAgl() != null ? skyCondition.getCloudBaseFtAgl() + " " : "")
                        + (skyCondition.getSkyCover() != null ? skyCondition.getSkyCover() : "")
                        + "\n");
            }
        }
        if (sb.length() > 1) {
            return sb.delete(sb.length() - 1, sb.length()).toString();
        }
        return sb.toString();
    }

}
