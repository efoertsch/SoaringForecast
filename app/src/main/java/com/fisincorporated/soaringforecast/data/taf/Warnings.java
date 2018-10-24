package com.fisincorporated.soaringforecast.data.taf;

import org.simpleframework.xml.Root;

@Root(name = "warnings")
public class Warnings {

    protected String warning;

    /**
     * Gets the value of the warning property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWarning() {
        return warning;
    }

    /**
     * Sets the value of the warning property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWarning(String value) {
        this.warning = value;
    }

}
