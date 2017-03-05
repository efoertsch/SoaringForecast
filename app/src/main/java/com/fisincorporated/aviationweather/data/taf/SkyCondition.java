package com.fisincorporated.aviationweather.data.taf;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "sky_condition", strict = false)
public class SkyCondition {

    @Attribute(name = "sky_cover", required = false)
    protected String skyCover;
    @Attribute(name = "cloud_base_ft_agl", required = false)
    protected Integer cloudBaseFtAgl;
    @Attribute(name = "cloud_type", required = false)
    protected String cloudType;

    /**
     * Gets the value of the skyCover property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSkyCover() {
        return skyCover;
    }

    /**
     * Sets the value of the skyCover property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSkyCover(String value) {
        this.skyCover = value;
    }

    /**
     * Gets the value of the cloudBaseFtAgl property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCloudBaseFtAgl() {
        return cloudBaseFtAgl;
    }

    /**
     * Sets the value of the cloudBaseFtAgl property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCloudBaseFtAgl(Integer value) {
        this.cloudBaseFtAgl = value;
    }

    /**
     * Gets the value of the cloudType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCloudType() {
        return cloudType;
    }

    /**
     * Sets the value of the cloudType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCloudType(String value) {
        this.cloudType = value;
    }

}
