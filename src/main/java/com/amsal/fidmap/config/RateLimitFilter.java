package com.amsal.fidmap.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small in-memory sliding-window rate limiter for the abuse-prone public
 * endpoints (credential endpoints and anonymous writes).
 *
 * Limits are per client IP and per endpoint group. State is per application
 * instance; with several instances behind a load balancer the effective limit
 * is per instance (documented in SECURITY_AND_TENANCY_AUDIT.md).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    static final long WINDOW_MS = 60_000L;
    static final int AUTH_LIMIT = 10;
    static final int PUBLIC_WRITE_LIMIT = 40;

    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();
    private long lastPurge = System.currentTimeMillis();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String group = groupOf(request.getMethod(), request.getRequestURI());

        if (group == null) {
            chain.doFilter(request, response);
            return;
        }

        int limit = group.equals("auth") ? AUTH_LIMIT : PUBLIC_WRITE_LIMIT;

        if (!allow(group + ":" + request.getRemoteAddr(), limit, System.currentTimeMillis())) {

            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Too many requests. Please wait a minute and try again.\"}"
            );
            return;
        }

        chain.doFilter(request, response);
    }

    static String groupOf(String method, String path) {

        if (!"POST".equals(method)) {
            return null;
        }

        if (path.equals("/auth/log-in")
                || path.equals("/auth/sign-in")
                || path.equals("/auth/personal-register")
                || path.equals("/auth/forgot-password")
                || path.equals("/auth/reset-password")
                || path.equals("/api/auth/forgot-password")
                || path.equals("/api/auth/reset-password")) {
            return "auth";
        }

        if (path.startsWith("/feedback/board/")
                || path.startsWith("/comment/")
                || path.startsWith("/vote/")
                || path.startsWith("/api/public/")) {
            return "public-write";
        }

        return null;
    }

    boolean allow(String key, int limit, long now) {

        purgeIfDue(now);

        Deque<Long> window = hits.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (window) {

            while (!window.isEmpty() && now - window.peekFirst() > WINDOW_MS) {
                window.pollFirst();
            }

            if (window.size() >= limit) {
                return false;
            }

            window.addLast(now);

            return true;
        }
    }

    private synchronized void purgeIfDue(long now) {

        if (now - lastPurge < WINDOW_MS) {
            return;
        }

        lastPurge = now;

        hits.entrySet().removeIf(entry -> {
            Deque<Long> window = entry.getValue();
            synchronized (window) {
                return window.isEmpty() || now - window.peekLast() > WINDOW_MS;
            }
        });
    }
}
