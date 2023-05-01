package xyz.hyrio.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.hyrio.common.util.ExceptionHandlerUtils;

import java.text.DecimalFormat;
import java.util.Optional;

import static xyz.hyrio.common.util.ServletUtils.*;

public class ShowRequestInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(ShowRequestInterceptor.class);

    private final boolean logException;

    public ShowRequestInterceptor() {
        this(false);
    }

    public ShowRequestInterceptor(boolean logException) {
        this.logException = logException;
    }

    private String[][] skipLogRecordUris;

    public String[][] getSkipLogRecordUris() {
        return skipLogRecordUris;
    }

    public void setSkipLogRecordUris(String[][] skipLogRecordUris) {
        this.skipLogRecordUris = skipLogRecordUris;
    }

    // For example, if you want to log the current user the request, you can override this method.
    public String getLogExtraField() {
        return null;
    }

    private static final ThreadLocal<Long> startTimeTl = new ThreadLocal<>();
    private static final ThreadLocal<String> requestIpAddressTl = new ThreadLocal<>();
    private static final DecimalFormat df = new DecimalFormat("#,###");

    public static String getRequestIpAddress() {
        return requestIpAddressTl.get();
    }

    public void clearStates() {
        ExceptionHandlerUtils.clearStates();
        startTimeTl.remove();
        requestIpAddressTl.remove();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        startTimeTl.set(System.currentTimeMillis());
        requestIpAddressTl.set(getIpAddressFromRequest(request));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            String requestMethod = request.getMethod();
            String requestURI = request.getRequestURI();

            boolean recordLog = !isUriIn(requestMethod, requestURI, skipLogRecordUris);
            Exception e = Optional.ofNullable(ExceptionHandlerUtils.getException()).orElse(ex);
            boolean hasException = e != null;
            HttpStatus status = Optional.ofNullable(ExceptionHandlerUtils.getStatus()).orElse(
                    Optional.ofNullable(e)
                            .map(ExceptionHandlerUtils::getCodeByException)
                            .orElse(HttpStatus.OK)
            );
            boolean is5xxServerError = status.is5xxServerError();

            if (hasException || recordLog) {
                String ipAddress = requestIpAddressTl.get();
                String logExtraField = Optional.ofNullable(getLogExtraField()).map(String::strip).filter(s -> !s.isEmpty()).map(s -> " " + s).orElse("");
                String requestUriAndQueryString = getRequestUriAndQueryString(request).toString();
                String timeSpent = df.format(System.currentTimeMillis() - startTimeTl.get());
                if (hasException) {
                    boolean showExceptionStackTrace = logException || is5xxServerError;
                    Object[] args = new Object[showExceptionStackTrace ? 8 : 7];
                    args[0] = ipAddress;
                    args[1] = logExtraField;
                    args[2] = requestMethod;
                    args[3] = requestUriAndQueryString;
                    args[4] = timeSpent;
                    args[5] = status;
                    args[6] = e.getMessage();
                    if (showExceptionStackTrace) {
                        args[7] = e;
                    }
                    log.warn("({}){} [{}] {} <{} ms> -> code: {}, message: {}", args);
                } else {
                    log.info("({}){} [{}] {} <{} ms>",
                            ipAddress,
                            logExtraField,
                            requestMethod,
                            requestUriAndQueryString,
                            timeSpent
                    );
                }
            }
        } finally {
            clearStates();
        }
    }
}
