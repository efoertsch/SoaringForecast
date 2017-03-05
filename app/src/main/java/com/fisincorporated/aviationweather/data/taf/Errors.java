package com.fisincorporated.aviationweather.data.taf;

import org.simpleframework.xml.Root;

@Root(name = "errors", strict = false)
public class Errors {

    protected String error;

    public String getError() {
        return error;
    }

    public void setError(String value) {
        this.error = value;
    }

}
