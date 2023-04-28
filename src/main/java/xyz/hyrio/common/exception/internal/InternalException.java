package xyz.hyrio.common.exception.internal;

import xyz.hyrio.common.exception.HyrioException;

public class InternalException extends HyrioException {
    public InternalException() {
    }

    public InternalException(String message) {
        super(message);
    }

    public InternalException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalException(Throwable cause) {
        super(cause);
    }
}
