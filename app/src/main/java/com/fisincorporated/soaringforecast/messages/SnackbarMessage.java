package com.fisincorporated.soaringforecast.messages;

public final class SnackbarMessage {

    private final String message;

    public SnackbarMessage(String message){
            this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
