package com.fisincorporated.soaringforecast.data.taf;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "temperature")
public class Temperature {

    @Element(name = "valid_time", required = true)
    protected String validTime;
    @Element(name = "sfc_temp_c")
    protected Float sfcTempC;
    @Element(name = "max_temp_c")
    protected String maxTempC;
    @Element(name = "min_temp_c")
    protected String minTempC;

    /**
     * Gets the value of the validTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValidTime() {
        return validTime;
    }

    /**
     * Sets the value of the validTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValidTime(String value) {
        this.validTime = value;
    }

    /**
     * Gets the value of the sfcTempC property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getSfcTempC() {
        return sfcTempC;
    }

    /**
     * Sets the value of the sfcTempC property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setSfcTempC(Float value) {
        this.sfcTempC = value;
    }

    /**
     * Gets the value of the maxTempC property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxTempC() {
        return maxTempC;
    }

    /**
     * Sets the value of the maxTempC property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxTempC(String value) {
        this.maxTempC = value;
    }

    /**
     * Gets the value of the minTempC property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinTempC() {
        return minTempC;
    }

    /**
     * Sets the value of the minTempC property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinTempC(String value) {
        this.minTempC = value;
    }

}
