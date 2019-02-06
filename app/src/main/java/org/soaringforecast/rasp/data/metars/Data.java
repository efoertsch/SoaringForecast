package org.soaringforecast.rasp.data.metars;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "data", strict = false)
public class Data {

    @ElementList(name = "METAR", inline = true, required = false)
    private List<Metar> metars;

    @Attribute(name = "num_results", required = false)
    private Integer numResults;

    public List<Metar> getMetars() {
        return metars;
    }

    public void setMetars(List<Metar> metars) {
        this.metars = metars;
    }

    public Integer getNumResults() {
        return this.numResults;
    }

    public void setNumResults(Integer value) {
        this.numResults = value;
    }

}
