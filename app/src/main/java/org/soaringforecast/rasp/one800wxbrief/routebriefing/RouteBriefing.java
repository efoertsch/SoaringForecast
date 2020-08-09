
package org.soaringforecast.rasp.one800wxbrief.routebriefing;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RouteBriefing {

    @SerializedName("returnStatus")
    @Expose
    public Boolean returnStatus;

    @SerializedName("returnMessage")
    @Expose
    public List<String> returnMessages = null;

    @SerializedName("returnCodedMessage")
    @Expose
    public List<ReturnCodedMessage> returnCodedMessage = null;

    @SerializedName("textualWeatherBriefing")
    @Expose
    public String textualWeatherBriefing;

    @SerializedName("simpleWeatherBriefing")
    @Expose
    public String simpleWeatherBriefing;

    @SerializedName("htmlweatherBriefing")
    @Expose
    public String htmlweatherBriefing;

    @SerializedName("ngbsummary")
    @Expose
    public String ngbsummary;

    @SerializedName("ngbweatherBriefing")
    @Expose
    public String ngbweatherBriefing;

    @SerializedName("ngbv2HtmlBriefing")
    @Expose
    public String ngbv2HtmlBriefing;

    @SerializedName("ngbv2PdfBriefing")
    @Expose
    public String ngbv2PdfBriefing;

}