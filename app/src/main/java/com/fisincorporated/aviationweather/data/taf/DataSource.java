package com.fisincorporated.aviationweather.data.taf;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;


@Root(name = "data_source", strict = false)
public class DataSource {

    @Attribute(name = "name", required = false)
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

}
