package com.fisincorporated.soaringforecast.data.taf;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "turbulence_condition")
public class TurbulenceCondition {

    @Attribute(name = "turbulence_intensity")
    protected String turbulenceIntensity;
    @Attribute(name = "turbulence_min_alt_ft_agl")
    protected Integer turbulenceMinAltFtAgl;
    @Attribute(name = "turbulence_max_alt_ft_agl")
    protected Integer turbulenceMaxAltFtAgl;

    /**
     * Gets the value of the turbulenceIntensity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTurbulenceIntensity() {
        return turbulenceIntensity;
    }

    /**
     * Sets the value of the turbulenceIntensity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTurbulenceIntensity(String value) {
        this.turbulenceIntensity = value;
    }

    /**
     * Gets the value of the turbulenceMinAltFtAgl property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTurbulenceMinAltFtAgl() {
        return turbulenceMinAltFtAgl;
    }

    /**
     * Sets the value of the turbulenceMinAltFtAgl property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTurbulenceMinAltFtAgl(Integer value) {
        this.turbulenceMinAltFtAgl = value;
    }

    /**
     * Gets the value of the turbulenceMaxAltFtAgl property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTurbulenceMaxAltFtAgl() {
        return turbulenceMaxAltFtAgl;
    }

    /**
     * Sets the value of the turbulenceMaxAltFtAgl property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTurbulenceMaxAltFtAgl(Integer value) {
        this.turbulenceMaxAltFtAgl = value;
    }

}
