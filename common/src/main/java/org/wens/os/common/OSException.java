package org.wens.os.common;

/**
 * @author wens
 */
public class OSException extends RuntimeException {

    public OSException() {
    }

    public OSException(String message) {
        super(message);
    }

    public OSException(String message, Throwable cause) {
        super(message, cause);
    }

    public OSException(Throwable cause) {
        super(cause);
    }

    public OSException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
