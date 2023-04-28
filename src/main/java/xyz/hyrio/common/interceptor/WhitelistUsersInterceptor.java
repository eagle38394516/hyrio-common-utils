package xyz.hyrio.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.hyrio.common.exception.request.AuthorizationException;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
public class WhitelistUsersInterceptor implements HandlerInterceptor {
    private final List<String> whitelistUsernames;
    private final Supplier<String> currentUsernameSupplier;

    public WhitelistUsersInterceptor(List<String> whitelistUsernames, Supplier<String> currentUsernameSupplier) {
        this.whitelistUsernames = whitelistUsernames;
        this.currentUsernameSupplier = currentUsernameSupplier;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!whitelistUsernames.contains(currentUsernameSupplier.get())){
            throw new AuthorizationException("当前用户不在白名单中");
        }
        return true;
    }
}
