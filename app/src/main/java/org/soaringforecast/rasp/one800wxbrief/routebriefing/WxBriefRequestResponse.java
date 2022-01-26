package org.soaringforecast.rasp.one800wxbrief.routebriefing;

// Just indicates briefing request submitted successful and  briefing should come shortly via email
public class WxBriefRequestResponse {

    private String message;
    private boolean isErrorMsg = false;
    private Exception exception;


    public WxBriefRequestResponse(String message) {
        this(message, false, null);
    }

    public WxBriefRequestResponse(String message, boolean isErrorMsg) {
          this(message, isErrorMsg, null);
    }

    public WxBriefRequestResponse(String message, boolean isErrorMsg, Exception exception) {
        this.message = message;
        this.isErrorMsg = isErrorMsg;
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public boolean isErrorMsg() {
        return isErrorMsg;
    }

    public Exception getException() {
        return exception;
    }
}
