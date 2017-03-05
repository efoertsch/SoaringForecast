package com.fisincorporated.aviationweather.data.taf;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;


@Root(name = "request")
public class Request {

    @Attribute(name = "type")
    protected String type;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

}
