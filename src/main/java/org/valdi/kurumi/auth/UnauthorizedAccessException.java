package org.valdi.kurumi.auth;

public class UnauthorizedAccessException extends RuntimeException {

    @SuppressWarnings("SameParameterValue")
    public UnauthorizedAccessException(final String message){
        super(message);
    }

}