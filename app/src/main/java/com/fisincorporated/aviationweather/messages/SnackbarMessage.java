package com.fisincorporated.aviationweather.messages;

public class SnackbarMessage {

    private final String message;

    public SnackbarMessage(String message){
            this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
