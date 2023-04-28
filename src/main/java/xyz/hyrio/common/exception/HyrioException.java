package xyz.hyrio.common.exception;

public class HyrioException extends RuntimeException {
    public HyrioException() {
    }

    public HyrioException(String message) {
        super(message);
    }

    public HyrioException(String message, Throwable cause) {
        super(message, cause);
    }

    public HyrioException(Throwable cause) {
        super(cause);
    }
}
