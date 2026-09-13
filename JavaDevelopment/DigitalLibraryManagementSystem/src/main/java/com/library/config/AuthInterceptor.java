package com.library.config;

import com.library.model.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Minimal session-based authentication/authorisation guard.
 * On login, AuthController stores "memberId" and "role" in the HttpSession.
 */
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_MEMBER_ID = "memberId";
    public static final String SESSION_ROLE = "role";
    public static final String SESSION_NAME = "displayName";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        String path = request.getRequestURI();

        Object memberId = session == null ? null : session.getAttribute(SESSION_MEMBER_ID);
        Object role = session == null ? null : session.getAttribute(SESSION_ROLE);

        boolean isAdminPath = path.startsWith("/admin");
        boolean isUserPath = path.startsWith("/user");

        if (memberId == null) {
            response.sendRedirect("/login");
            return false;
        }
        if (isAdminPath && role != Role.ADMIN) {
            response.sendRedirect("/user/catalogue");
            return false;
        }
        if (isUserPath && role != Role.USER && role != Role.ADMIN) {
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }
}
