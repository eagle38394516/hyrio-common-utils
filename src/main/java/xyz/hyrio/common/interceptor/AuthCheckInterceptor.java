package xyz.hyrio.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.hyrio.common.exception.request.AuthorizationException;

import static xyz.hyrio.common.util.ObjectUtils.blank2NullOtherwiseStrip;
import static xyz.hyrio.common.util.ObjectUtils.requireHasTextElse;
import static xyz.hyrio.common.util.ServletUtils.isUriIn;

public class AuthCheckInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AuthCheckInterceptor.class);

    public static final String DEFAULT_TOKEN_HEADER_KEY = "token";

    public interface TokenValidator {
        void checkAndSaveUserInfo(String token) throws Exception;

        void clearStates();
    }

    private String[][] skipAuthCheckUris;
    private String tokenKey = DEFAULT_TOKEN_HEADER_KEY;

    private final TokenValidator tokenValidator;

    public String[][] getSkipAuthCheckUris() {
        return skipAuthCheckUris;
    }

    public void setSkipAuthCheckUris(String[][] skipAuthCheckUris) {
        this.skipAuthCheckUris = skipAuthCheckUris;
    }

    public String getTokenKey() {
        return tokenKey;
    }

    public void setTokenKey(String tokenKey) {
        this.tokenKey = tokenKey;
    }

    public AuthCheckInterceptor(TokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestMethod = request.getMethod();
        String requestURI = request.getRequestURI();
        if (isUriIn(requestMethod, requestURI, skipAuthCheckUris)) return true;

        String token = requireHasTextElse(
                blank2NullOtherwiseStrip(request.getParameter(tokenKey)),
                blank2NullOtherwiseStrip(request.getHeader(tokenKey))
        );
        try {
            tokenValidator.checkAndSaveUserInfo(token);
            return true;
        } catch (Exception e) {
            throw new AuthorizationException("token is invalid (" + e.getMessage() + ")", e);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        tokenValidator.clearStates();
    }
}
