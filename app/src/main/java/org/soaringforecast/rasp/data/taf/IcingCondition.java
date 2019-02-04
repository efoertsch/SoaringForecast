package org.soaringforecast.rasp.data.taf;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "icing_condition", strict = false)
public class IcingCondition {

    @Attribute(name = "icing_intensity", required = false)
    protected String icingIntensity;
    @Attribute(name = "icing_min_alt_ft_agl", required = false)
    protected Integer icingMinAltFtAgl;
    @Attribute(name = "icing_max_alt_ft_agl", required = false)
    protected Integer icingMaxAltFtAgl;

    /**
     * Gets the value of the icingIntensity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIcingIntensity() {
        return icingIntensity;
    }

    /**
     * Sets the value of the icingIntensity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIcingIntensity(String value) {
        this.icingIntensity = value;
    }

    /**
     * Gets the value of the icingMinAltFtAgl property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getIcingMinAltFtAgl() {
        return icingMinAltFtAgl;
    }

    /**
     * Sets the value of the icingMinAltFtAgl property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setIcingMinAltFtAgl(Integer value) {
        this.icingMinAltFtAgl = value;
    }

    /**
     * Gets the value of the icingMaxAltFtAgl property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getIcingMaxAltFtAgl() {
        return icingMaxAltFtAgl;
    }

    /**
     * Sets the value of the icingMaxAltFtAgl property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setIcingMaxAltFtAgl(Integer value) {
        this.icingMaxAltFtAgl = value;
    }

}
