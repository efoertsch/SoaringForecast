package org.soaringforecast.rasp.one800wxbrief.routebriefing;

// Just indicates briefing request submitted successful and  briefing should come shortly via email
public class Email1800WxBriefRequestResponse {

    private String errorMessage;

    public Email1800WxBriefRequestResponse() {
    }

    public Email1800WxBriefRequestResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
