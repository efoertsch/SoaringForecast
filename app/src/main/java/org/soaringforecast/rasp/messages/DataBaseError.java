package org.soaringforecast.rasp.messages;

public class DataBaseError {

    private String errorMsg;
    private Throwable throwable;
    public DataBaseError(String errorMsg, Throwable throwable) {
        this.errorMsg = errorMsg;
        this.throwable = throwable;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }


}
