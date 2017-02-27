package com.fisincorporated.airportweather.metars;

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
public class MetarResponse {

    @Element(name = "request_index", required = false)
    private int requestIndex;

    @Element(name = "data_source")
    private DataSource dataSource;

    @Element(name = "request")
    private Request request;

    @Element(name = "errors", required = false)
    private String errors;

    @Element(name = "warnings", required = false)
    private String warnings;

    @Element(name = "time_taken_ms", required = false)
    private int timeTakenMs;

    @Element(name = "data", required = false)
    private Data data;

    @Attribute(name = "xsd", required = false)
    private String xsd;

    @Attribute(name = "xsi", required = false)
    private String xsi;

    @Attribute(name = "version", required = false)
    private String version;

    @Attribute(name = "noNamespaceSchemaLocation", required = false)
    private String noNamespaceSchemaLocation;

    public int getRequestIndex() {
        return this.requestIndex;
    }

    public void setRequestIndex(int value) {
        this.requestIndex = value;
    }


    public DataSource getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(DataSource value) {
        this.dataSource = value;
    }


    public Request getRequest() {
        return this.request;
    }

    public void setRequest(Request value) {
        this.request = value;
    }


    public String getErrors() {
        return this.errors;
    }

    public void setErrors(String value) {
        this.errors = value;
    }


    public String getWarnings() {
        return this.warnings;
    }

    public void setWarnings(String value) {
        this.warnings = value;
    }


    public int getTimeTakenMs() {
        return this.timeTakenMs;
    }

    public void setTimeTakenMs(int value) {
        this.timeTakenMs = value;
    }


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


    public String getVersion() {
        return this.version;
    }

    public void setVersion(String value) {
        this.version = value;
    }


    public String getNoNamespaceSchemaLocation() {
        return this.noNamespaceSchemaLocation;
    }

    public void setNoNamespaceSchemaLocation(String value) {
        this.noNamespaceSchemaLocation = value;
    }

}