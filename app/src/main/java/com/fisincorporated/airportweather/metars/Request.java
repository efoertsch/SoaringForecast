package com.fisincorporated.airportweather.metars;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "request")
public class Request {

    @Attribute(name = "type", required = false)
    private String type;

    public String getType() {
        return this.type;
    }

    public void setType(String value) {
        this.type = value;
    }

}
