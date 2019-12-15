package org.soaringforecast.rasp.data.taf;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root(name = "TAF", strict = false)
public class TAF {

    @Element(name = "raw_text", required = false)
    protected String rawText;

    @Element(name = "station_id", required = false)
    protected String stationId;

    @Element(name = "issue_time", required = false)
    protected String issueTime;

    @Element(name = "bulletin_time", required = false)
    protected String bulletinTime;

    @Element(name = "valid_time_from", required = false)
    protected String validTimeFrom;

    @Element(name = "valid_time_to", required = false)
    protected String validTimeTo;

    @Element(name = "remarks", required = false)
    protected String remarks;

    @Element(name = "latitude", required = false)
    protected Float latitude;

    @Element(name = "longitude", required = false)
    protected Float longitude;

    @Element(name = "elevation_m", required = false)
    protected Float elevationM;

    @ElementList(name = "forecast", inline = true, required = false)
    protected List<Forecast> forecast;

    /**
     * Gets the value of the rawText property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRawText() {
        return rawText;
    }

    /**
     * Sets the value of the rawText property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRawText(String value) {
        this.rawText = value;
    }

    /**
     * Gets the value of the stationId property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStationId() {
        return stationId;
    }

    /**
     * Sets the value of the stationId property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStationId(String value) {
        this.stationId = value;
    }

    /**
     * Gets the value of the issueTime property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getIssueTime() {
        return issueTime;
    }

    /**
     * Sets the value of the issueTime property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setIssueTime(String value) {
        this.issueTime = value;
    }

    /**
     * Gets the value of the bulletinTime property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getBulletinTime() {
        return bulletinTime;
    }

    /**
     * Sets the value of the bulletinTime property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setBulletinTime(String value) {
        this.bulletinTime = value;
    }

    /**
     * Gets the value of the validTimeFrom property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getValidTimeFrom() {
        return validTimeFrom;
    }

    /**
     * Sets the value of the validTimeFrom property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setValidTimeFrom(String value) {
        this.validTimeFrom = value;
    }

    /**
     * Gets the value of the validTimeTo property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getValidTimeTo() {
        return validTimeTo;
    }

    /**
     * Sets the value of the validTimeTo property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setValidTimeTo(String value) {
        this.validTimeTo = value;
    }

    /**
     * Gets the value of the remarks property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * Sets the value of the remarks property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRemarks(String value) {
        this.remarks = value;
    }

    /**
     * Gets the value of the latitude property.
     *
     * @return possible object is
     * {@link Float }
     */
    public Float getLatitude() {
        return latitude;
    }

    /**
     * Sets the value of the latitude property.
     *
     * @param value allowed object is
     *              {@link Float }
     */
    public void setLatitude(Float value) {
        this.latitude = value;
    }

    /**
     * Gets the value of the longitude property.
     *
     * @return possible object is
     * {@link Float }
     */
    public Float getLongitude() {
        return longitude;
    }

    /**
     * Sets the value of the longitude property.
     *
     * @param value allowed object is
     *              {@link Float }
     */
    public void setLongitude(Float value) {
        this.longitude = value;
    }

    /**
     * Gets the value of the elevationM property.
     *
     * @return possible object is
     * {@link Float }
     */
    public Float getElevationM() {
        return elevationM;
    }

    /**
     * Sets the value of the elevationM property.
     *
     * @param value allowed object is
     *              {@link Float }
     */
    public void setElevationM(Float value) {
        this.elevationM = value;
    }

    /**
     * Gets the value of the forecast property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the forecast property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getForecast().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Forecast }
     */
    public List<Forecast> getForecast() {
        if (forecast == null) {
            forecast = new ArrayList<>();
        }
        return this.forecast;
    }

}
