package org.soaringforecast.rasp.common.messages;

public class CrashReport {
    private String crashErrorMsg;
    private Exception crashException;

    public CrashReport(String crashErrorMsg, Exception crashException) {
        this.crashErrorMsg = crashErrorMsg;
        this.crashException = crashException;
    }

    public String getCrashErrorMsg() {
        return crashErrorMsg;
    }

    public Exception getCrashException() {
        return crashException;
    }
}
