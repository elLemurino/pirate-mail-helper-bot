package org.example.bot.exceptions;

public class StoreMessagingException extends RuntimeException {

    public StoreMessagingException(String message) {
        super(message);
    }

    public StoreMessagingException(String message, Throwable cause) {
        super(message, cause);
    }

    public StoreMessagingException(Throwable cause) {
        super(cause);
    }
}
