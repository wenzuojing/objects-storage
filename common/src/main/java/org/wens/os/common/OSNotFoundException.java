package org.wens.os.common;

/**
 * @author wens
 */
public class OSNotFoundException extends RuntimeException {

    public OSNotFoundException() {
    }

    public OSNotFoundException(String message) {
        super(message);
    }

    public OSNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public OSNotFoundException(Throwable cause) {
        super(cause);
    }

    public OSNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
