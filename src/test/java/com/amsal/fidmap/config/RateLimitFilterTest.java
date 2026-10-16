package com.amsal.fidmap.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitFilterTest {

    @Test
    void authEndpointsAreGroupedAsAuth() {
        assertEquals("auth", RateLimitFilter.groupOf("POST", "/auth/log-in"));
        assertEquals("auth", RateLimitFilter.groupOf("POST", "/auth/forgot-password"));
        assertEquals("auth", RateLimitFilter.groupOf("POST", "/api/auth/reset-password"));
    }

    @Test
    void publicWriteEndpointsAreGrouped() {
        assertEquals("public-write", RateLimitFilter.groupOf("POST", "/feedback/board/abc"));
        assertEquals("public-write", RateLimitFilter.groupOf("POST", "/comment/123"));
        assertEquals("public-write", RateLimitFilter.groupOf("POST", "/vote/123"));
        assertEquals("public-write", RateLimitFilter.groupOf("POST", "/api/public/invitations/accept"));
    }

    @Test
    void readsAndUnlistedPathsAreNotLimited() {
        assertNull(RateLimitFilter.groupOf("GET", "/auth/log-in"));
        assertNull(RateLimitFilter.groupOf("GET", "/api/public/notifications/unsubscribe"));
        assertNull(RateLimitFilter.groupOf("POST", "/api/decisions/feedback/1/decide"));
    }

    @Test
    void blocksAfterLimitWithinWindow() {
        RateLimitFilter filter = new RateLimitFilter();
        long t = 1_000_000L;
        for (int i = 0; i < RateLimitFilter.AUTH_LIMIT; i++) {
            assertTrue(filter.allow("auth:1.1.1.1", RateLimitFilter.AUTH_LIMIT, t + i), "request " + i);
        }
        assertFalse(filter.allow("auth:1.1.1.1", RateLimitFilter.AUTH_LIMIT, t + 100));
    }

    @Test
    void limitIsPerKey() {
        RateLimitFilter filter = new RateLimitFilter();
        long t = 1_000_000L;
        for (int i = 0; i < RateLimitFilter.AUTH_LIMIT; i++) {
            filter.allow("auth:1.1.1.1", RateLimitFilter.AUTH_LIMIT, t);
        }
        assertFalse(filter.allow("auth:1.1.1.1", RateLimitFilter.AUTH_LIMIT, t));
        assertTrue(filter.allow("auth:2.2.2.2", RateLimitFilter.AUTH_LIMIT, t));
    }

    @Test
    void allowsAgainAfterWindowExpires() {
        RateLimitFilter filter = new RateLimitFilter();
        long t = 1_000_000L;
        for (int i = 0; i < RateLimitFilter.AUTH_LIMIT; i++) {
            filter.allow("auth:1.1.1.1", RateLimitFilter.AUTH_LIMIT, t);
        }
        assertFalse(filter.allow("auth:1.1.1.1", RateLimitFilter.AUTH_LIMIT, t + 1));
        assertTrue(filter.allow("auth:1.1.1.1", RateLimitFilter.AUTH_LIMIT, t + RateLimitFilter.WINDOW_MS + 1));
    }
}
