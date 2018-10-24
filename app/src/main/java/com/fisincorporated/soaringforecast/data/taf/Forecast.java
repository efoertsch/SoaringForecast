package com.fisincorporated.soaringforecast.data.taf;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root(name = "forecast", strict = false)
public class Forecast {

    @Element(name = "fcst_time_from", required = false)
    protected String fcstTimeFrom;

    @Element(name = "fcst_time_to", required = false)
    protected String fcstTimeTo;

    @Element(name = "change_indicator", required = false)
    protected String changeIndicator;

    @Element(name = "time_becoming", required = false)
    protected String timeBecoming;

    @Element(name = "probability", required = false)
    protected Integer probability;

    @Element(name = "wind_dir_degrees", required = false)
    protected Short windDirDegrees;

    @Element(name = "wind_speed_kt", required = false)
    protected Integer windSpeedKt;

    @Element(name = "wind_gust_kt", required = false)
    protected Integer windGustKt;

    @Element(name = "wind_shear_hgt_ft_agl", required = false)
    protected Short windShearHgtFtAgl;

    @Element(name = "wind_shear_dir_degrees", required = false)
    protected Short windShearDirDegrees;

    @Element(name = "wind_shear_speed_kt", required = false)
    protected Integer windShearSpeedKt;

    @Element(name = "visibility_statute_mi", required = false)
    protected Float visibilityStatuteMi;

    @Element(name = "altim_in_hg", required = false)
    protected Float altimInHg;

    @Element(name = "vert_vis_ft", required = false)
    protected Short vertVisFt;

    @Element(name = "wx_string", required = false)
    protected String wxString;

    @Element(name = "not_decoded", required = false)
    protected String notDecoded;

    @ElementList(name = "sky_condition", inline = true, required = false)
    protected List<SkyCondition> skyCondition;

    @ElementList(name = "turbulence_condition", inline = true, required = false)
    protected List<TurbulenceCondition> turbulenceCondition;

    @ElementList(name = "icing_condition", inline = true, required = false)
    protected List<IcingCondition> icingCondition;

    @ElementList(name = "temperature", inline = true, required = false)
    protected List<Temperature> temperature;

    /**
     * Gets the value of the fcstTimeFrom property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFcstTimeFrom() {
        return fcstTimeFrom;
    }

    /**
     * Sets the value of the fcstTimeFrom property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFcstTimeFrom(String value) {
        this.fcstTimeFrom = value;
    }

    /**
     * Gets the value of the fcstTimeTo property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFcstTimeTo() {
        return fcstTimeTo;
    }

    /**
     * Sets the value of the fcstTimeTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFcstTimeTo(String value) {
        this.fcstTimeTo = value;
    }

    /**
     * Gets the value of the changeIndicator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChangeIndicator() {
        return changeIndicator;
    }

    /**
     * Sets the value of the changeIndicator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChangeIndicator(String value) {
        this.changeIndicator = value;
    }

    /**
     * Gets the value of the timeBecoming property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeBecoming() {
        return timeBecoming;
    }

    /**
     * Sets the value of the timeBecoming property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeBecoming(String value) {
        this.timeBecoming = value;
    }

    /**
     * Gets the value of the probability property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getProbability() {
        return probability;
    }

    /**
     * Sets the value of the probability property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setProbability(Integer value) {
        this.probability = value;
    }

    /**
     * Gets the value of the windDirDegrees property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getWindDirDegrees() {
        return windDirDegrees;
    }

    /**
     * Sets the value of the windDirDegrees property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setWindDirDegrees(Short value) {
        this.windDirDegrees = value;
    }

    /**
     * Gets the value of the windSpeedKt property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getWindSpeedKt() {
        return windSpeedKt;
    }

    /**
     * Sets the value of the windSpeedKt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setWindSpeedKt(Integer value) {
        this.windSpeedKt = value;
    }

    /**
     * Gets the value of the windGustKt property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getWindGustKt() {
        return windGustKt;
    }

    /**
     * Sets the value of the windGustKt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setWindGustKt(Integer value) {
        this.windGustKt = value;
    }

    /**
     * Gets the value of the windShearHgtFtAgl property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getWindShearHgtFtAgl() {
        return windShearHgtFtAgl;
    }

    /**
     * Sets the value of the windShearHgtFtAgl property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setWindShearHgtFtAgl(Short value) {
        this.windShearHgtFtAgl = value;
    }

    /**
     * Gets the value of the windShearDirDegrees property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getWindShearDirDegrees() {
        return windShearDirDegrees;
    }

    /**
     * Sets the value of the windShearDirDegrees property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setWindShearDirDegrees(Short value) {
        this.windShearDirDegrees = value;
    }

    /**
     * Gets the value of the windShearSpeedKt property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getWindShearSpeedKt() {
        return windShearSpeedKt;
    }

    /**
     * Sets the value of the windShearSpeedKt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setWindShearSpeedKt(Integer value) {
        this.windShearSpeedKt = value;
    }

    /**
     * Gets the value of the visibilityStatuteMi property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getVisibilityStatuteMi() {
        return visibilityStatuteMi;
    }

    /**
     * Sets the value of the visibilityStatuteMi property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setVisibilityStatuteMi(Float value) {
        this.visibilityStatuteMi = value;
    }

    /**
     * Gets the value of the altimInHg property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getAltimInHg() {
        return altimInHg;
    }

    /**
     * Sets the value of the altimInHg property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setAltimInHg(Float value) {
        this.altimInHg = value;
    }

    /**
     * Gets the value of the vertVisFt property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getVertVisFt() {
        return vertVisFt;
    }

    /**
     * Sets the value of the vertVisFt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setVertVisFt(Short value) {
        this.vertVisFt = value;
    }

    /**
     * Gets the value of the wxString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWxString() {
        return wxString;
    }

    /**
     * Sets the value of the wxString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWxString(String value) {
        this.wxString = value;
    }

    /**
     * Gets the value of the notDecoded property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotDecoded() {
        return notDecoded;
    }

    /**
     * Sets the value of the notDecoded property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotDecoded(String value) {
        this.notDecoded = value;
    }

    /**
     * Gets the value of the skyCondition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the skyCondition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSkyCondition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SkyCondition }
     * 
     * 
     */
    public List<SkyCondition> getSkyCondition() {
        if (skyCondition == null) {
            skyCondition = new ArrayList<SkyCondition>();
        }
        return this.skyCondition;
    }

    /**
     * Gets the value of the turbulenceCondition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the turbulenceCondition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTurbulenceCondition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TurbulenceCondition }
     * 
     * 
     */
    public List<TurbulenceCondition> getTurbulenceCondition() {
        if (turbulenceCondition == null) {
            turbulenceCondition = new ArrayList<TurbulenceCondition>();
        }
        return this.turbulenceCondition;
    }

    /**
     * Gets the value of the icingCondition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the icingCondition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIcingCondition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IcingCondition }
     * 
     * 
     */
    public List<IcingCondition> getIcingCondition() {
        if (icingCondition == null) {
            icingCondition = new ArrayList<IcingCondition>();
        }
        return this.icingCondition;
    }

    /**
     * Gets the value of the temperature property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the temperature property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTemperature().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Temperature }
     * 
     * 
     */
    public List<Temperature> getTemperature() {
        if (temperature == null) {
            temperature = new ArrayList<Temperature>();
        }
        return this.temperature;
    }

}
