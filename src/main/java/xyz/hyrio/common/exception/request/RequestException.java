package xyz.hyrio.common.exception.request;

import xyz.hyrio.common.exception.HyrioException;

public class RequestException extends HyrioException {
    public RequestException() {
    }

    public RequestException(String message) {
        super(message);
    }

    public RequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestException(Throwable cause) {
        super(cause);
    }
}
