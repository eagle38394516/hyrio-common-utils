package xyz.hyrio.common.util;

import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import xyz.hyrio.common.exception.internal.InternalException;
import xyz.hyrio.common.exception.request.*;
import xyz.hyrio.common.pojo.vo.CommonVo;

import static org.springframework.util.StringUtils.hasText;

public final class ExceptionHandlerUtils {
    private ExceptionHandlerUtils() {
    }

    private static final ThreadLocal<Exception> exceptionTl = new ThreadLocal<>();
    private static final ThreadLocal<HttpStatus> statusTl = new ThreadLocal<>();

    public static HttpStatus getCodeByException(Exception e) {
        if (e instanceof UiDeprecatedException) return HttpStatus.UPGRADE_REQUIRED;
        if (e instanceof RateLimitException) return HttpStatus.TOO_MANY_REQUESTS;
        if (e instanceof InternalException) return HttpStatus.INTERNAL_SERVER_ERROR;
        if (e instanceof NotFoundException) return HttpStatus.NOT_FOUND;
        if (e instanceof AuthorizationException) return HttpStatus.UNAUTHORIZED;
        if (e instanceof RequestException) return HttpStatus.BAD_REQUEST;
        if (e instanceof NestedRuntimeException) return HttpStatus.BAD_REQUEST;
        if (e instanceof RuntimeException) return HttpStatus.INTERNAL_SERVER_ERROR;
        return HttpStatus.BAD_REQUEST;
    }

    public static ResponseEntity<?> getResponseEntity(Exception e) {
        return getResponseEntity(getCodeByException(e), e);
    }

    public static ResponseEntity<?> getResponseEntity(HttpStatus status, Exception e) {
        exceptionTl.set(e);
        statusTl.set(status);
        String message = e.getMessage();
        return ResponseEntity.status(status).body(CommonVo.of(status.value(), hasText(message) ? message : e.getClass().getName()));
    }

    public static Exception getException() {
        return exceptionTl.get();
    }

    public static HttpStatus getStatus() {
        return statusTl.get();
    }

    public static void clearStates() {
        exceptionTl.remove();
        statusTl.remove();
    }
}
