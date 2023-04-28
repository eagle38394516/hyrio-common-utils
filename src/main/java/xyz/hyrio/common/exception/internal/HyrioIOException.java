package xyz.hyrio.common.exception.internal;

public class HyrioIOException extends InternalException {
    public HyrioIOException() {
    }

    public HyrioIOException(String message) {
        super(message);
    }

    public HyrioIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public HyrioIOException(Throwable cause) {
        super(cause);
    }
}
