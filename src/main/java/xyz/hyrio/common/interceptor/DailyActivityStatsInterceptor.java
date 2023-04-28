package xyz.hyrio.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import static xyz.hyrio.common.interceptor.ShowRequestInterceptor.getRequestIpAddress;

public class DailyActivityStatsInterceptor implements HandlerInterceptor {
    @FunctionalInterface
    public interface AccessRecorder {
        void recordAccess(String ipAddress);
    }

    private final AccessRecorder accessRecorder;

    public DailyActivityStatsInterceptor(AccessRecorder accessRecorder) {
        this.accessRecorder = accessRecorder;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        accessRecorder.recordAccess(getRequestIpAddress());
        return true;
    }
}
