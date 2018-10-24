package com.fisincorporated.soaringforecast.data.common;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "data_source", strict = false)
public class DataSource {

    @Attribute(name = "name", required = false)
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String value) {
        this.name = value;
    }

}
