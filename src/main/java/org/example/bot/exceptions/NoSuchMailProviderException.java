package org.example.bot.exceptions;

public class NoSuchMailProviderException extends RuntimeException {

    public NoSuchMailProviderException(String message) {
        super(message);
    }

    public NoSuchMailProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchMailProviderException(Throwable cause) {
        super(cause);
    }
}
