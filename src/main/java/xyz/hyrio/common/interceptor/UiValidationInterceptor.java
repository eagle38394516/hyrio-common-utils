package xyz.hyrio.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.hyrio.common.exception.request.UiDeprecatedException;

public class UiValidationInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(UiValidationInterceptor.class);

    private final String requestHeaderKey;
    private final String buildVersion;

    public UiValidationInterceptor(String requestHeaderKey, String buildVersion) {
        this.requestHeaderKey = requestHeaderKey;
        this.buildVersion = buildVersion;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String buildVersionInHeader = request.getHeader(requestHeaderKey);
        if (!buildVersion.equals(buildVersionInHeader)) {
            log.warn("UI deprecated. buildVersion: {}, buildVersionInHeader: {}", buildVersion, buildVersionInHeader);
            throw new UiDeprecatedException("页面已过期，请刷新页面后重试");
        }
        return true;
    }
}
