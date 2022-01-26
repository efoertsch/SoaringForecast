package org.soaringforecast.rasp.common.messages;

public class ProgramError {
    private String error;
    private  Exception  exception;
    public ProgramError(String error){
        this.error = error;
    }
    public ProgramError(Exception exception){
        this.exception = exception;
    }
}
