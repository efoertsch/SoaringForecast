package org.soaringforecast.rasp.messages;

public class CallFailure {

    private String callFailure;

    public CallFailure(String callFailure){
        this.callFailure = callFailure;
    }

    public String getcallFailure() {
        return callFailure;
    }
}
