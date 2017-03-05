package com.fisincorporated.aviationweather.data.taf;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root(name = "data", strict = false)
public class Data {

    @ElementList(name = "TAF", inline = true)
    protected List<TAF> taf;
    @Attribute(name = "num_results", required = false)
    protected Integer numResults;

    public List<TAF> getTAFs() {
        if (taf == null) {
            taf = new ArrayList<TAF>();
        }
        return this.taf;
    }

    public Integer getNumResults() {
        return numResults;
    }

    public void setNumResults(Integer value) {
        this.numResults = value;
    }

}
