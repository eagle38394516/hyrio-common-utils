package xyz.hyrio.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.hyrio.common.exception.request.AuthorizationException;

import static xyz.hyrio.common.util.ObjectUtils.blank2NullOtherwiseStrip;
import static xyz.hyrio.common.util.ObjectUtils.requireHasTextElse;
import static xyz.hyrio.common.util.ServletUtils.isUriIn;

@Slf4j
public class AuthCheckInterceptor implements HandlerInterceptor {
    public static final String DEFAULT_TOKEN_HEADER_KEY = "token";

    public interface TokenValidator {
        void checkAndSaveUserInfo(String token) throws Exception;

        void clearStates();
    }

    @Getter @Setter private String[][] skipAuthCheckUris;
    @Getter @Setter private String tokenKey = DEFAULT_TOKEN_HEADER_KEY;

    private final TokenValidator tokenValidator;

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
