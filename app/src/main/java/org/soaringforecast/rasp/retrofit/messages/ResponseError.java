package org.soaringforecast.rasp.retrofit.messages;

public class ResponseError {

    private String responseError;

    public ResponseError(String responseError){
        this.responseError = responseError;
    }

    public String getResponseError() {
        return responseError;
    }
}
