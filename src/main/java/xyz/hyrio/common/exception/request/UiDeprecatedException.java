package xyz.hyrio.common.exception.request;

public class UiDeprecatedException extends RequestException {
    public UiDeprecatedException() {
    }

    public UiDeprecatedException(String message) {
        super(message);
    }

    public UiDeprecatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UiDeprecatedException(Throwable cause) {
        super(cause);
    }
}
