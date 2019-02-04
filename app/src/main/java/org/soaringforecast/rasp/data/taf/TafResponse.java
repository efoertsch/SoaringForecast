package org.soaringforecast.rasp.data.taf;

import org.soaringforecast.rasp.data.common.AviationWeatherResponse;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "response", strict = false)
public class TafResponse extends AviationWeatherResponse {

    @Element(required = false)
    protected Data data;


    /**
     * Gets the value of the data property.
     * 
     * @return
     *     possible object is
     *     {@link Data }
     *     
     */
    public Data getData() {
        return data;
    }

    /**
     * Sets the value of the data property.
     * 
     * @param value
     *     allowed object is
     *     {@link Data }
     *     
     */
    public void setData(Data value) {
        this.data = value;
    }


}
