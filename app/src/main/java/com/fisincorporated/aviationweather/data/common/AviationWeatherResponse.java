package com.fisincorporated.aviationweather.data.common;


import com.fisincorporated.aviationweather.data.taf.Warnings;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public class AviationWeatherResponse {

    @Element(name = "request_index", required = false)
    private int requestIndex;

    @Element(name = "data_source")
    private DataSource dataSource;

    @Element(name = "request")
    private Request request;

    @Element(required = true)
    protected Errors errors;

    @Element(name = "warnings", required = false)
    private Warnings warnings;

    @Element(name = "time_taken_ms", required = false)
    private int timeTakenMs;

    @Attribute(name = "version", required = false)
    private String version;


    /**
     * Gets the value of the requestIndex property.
     *
     */
    public int getRequestIndex() {
        return requestIndex;
    }

    /**
     * Sets the value of the requestIndex property.
     *
     */
    public void setRequestIndex(int value) {
        this.requestIndex = value;
    }

    /**
     * Gets the value of the dataSource property.
     *
     * @return
     *     possible object is
     *     {@link DataSource }
     *
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Sets the value of the dataSource property.
     *
     * @param value
     *     allowed object is
     *     {@link DataSource }
     *
     */
    public void setDataSource(DataSource value) {
        this.dataSource = value;
    }

    /**
     * Gets the value of the request property.
     *
     * @return
     *     possible object is
     *     {@link Request }
     *
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Sets the value of the request property.
     *
     * @param value
     *     allowed object is
     *     {@link Request }
     *
     */
    public void setRequest(Request value) {
        this.request = value;
    }

    /**
     * Gets the value of the errors property.
     *
     * @return
     *     possible object is
     *     {@link Errors }
     *
     */
    public Errors getErrors() {
        return errors;
    }

    /**
     * Sets the value of the errors property.
     *
     * @param value
     *     allowed object is
     *     {@link Errors }
     *
     */
    public void setErrors(Errors value) {
        this.errors = value;
    }

    /**
     * Gets the value of the warnings property.
     *
     * @return
     *     possible object is
     *     {@link Warnings }
     *
     */
    public Warnings getWarnings() {
        return warnings;
    }

    /**
     * Sets the value of the warnings property.
     *
     * @param value
     *     allowed object is
     *     {@link Warnings }
     *
     */
    public void setWarnings(Warnings value) {
        this.warnings = value;
    }

    /**
     * Gets the value of the timeTakenMs property.
     *
     */
    public int getTimeTakenMs() {
        return timeTakenMs;
    }

    /**
     * Sets the value of the timeTakenMs property.
     *
     */
    public void setTimeTakenMs(int value) {
        this.timeTakenMs = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getVersion() {
        if (version == null) {
            return "1.2";
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setVersion(String value) {
        this.version = value;
    }

}
