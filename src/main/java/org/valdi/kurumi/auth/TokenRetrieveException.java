package org.valdi.kurumi.auth;

public class TokenRetrieveException extends Exception {
    public TokenRetrieveException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenRetrieveException(String message) {
        super(message);
    }
}
