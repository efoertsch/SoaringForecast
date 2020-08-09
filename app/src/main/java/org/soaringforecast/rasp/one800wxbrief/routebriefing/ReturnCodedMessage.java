package org.soaringforecast.rasp.one800wxbrief.routebriefing;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReturnCodedMessage {

    @SerializedName("code")
    @Expose
    public String code;
    @SerializedName("message")
    @Expose
    public String message;

}
