package com.fisincorporated.soaringforecast.data.metars;

import com.fisincorporated.soaringforecast.data.common.AviationWeatherResponse;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * I think best way to get to simplexml pojos from xsd is to use jaxb and then edit annotations to
 * simplexml syntax. The problem with online xml to pojo converters (such as
 * rnevet.github.io/simple-xml-pojo-gen/ ) is that they aren't sophisticated enough to create
 * good pojos)
 */

@Root(name = "response", strict = false)
public class MetarResponse extends AviationWeatherResponse {

    @Element(name = "data", required = false)
    private Data data;

    @Attribute(name = "xsd", required = false)
    private String xsd;

    @Attribute(name = "xsi", required = false)
    private String xsi;

    @Attribute(name = "noNamespaceSchemaLocation", required = false)
    private String noNamespaceSchemaLocation;

    public Data getData() {
        return this.data;
    }

    public void setData(Data value) {
        this.data = value;
    }

    public String getXsd() {
        return this.xsd;
    }

    public void setXsd(String value) {
        this.xsd = value;
    }

    public String getXsi() {
        return this.xsi;
    }

    public void setXsi(String value) {
        this.xsi = value;
    }

    public String getNoNamespaceSchemaLocation() {
        return this.noNamespaceSchemaLocation;
    }

    public void setNoNamespaceSchemaLocation(String value) {
        this.noNamespaceSchemaLocation = value;
    }

}