package com.amsal.fidmap.security;

import com.amsal.fidmap.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {

    }


    public static Authentication getAuthentication() {

        return SecurityContextHolder.getContext().getAuthentication();
    }

    /* get the username/email */


    /**
     * Like getCurrentUserId() but returns null for anonymous / unauthenticated
     * requests instead of throwing. Use on endpoints that are public but expose
     * more to signed-in staff.
     */
    public static UUID getCurrentUserIdOrNull() {

        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return user.getId();
        }

        return null;
    }

    public static UUID getCurrentUserId() {

        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("user is not authenticated");

        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return user.getId();
        }

        throw new RuntimeException("Unexpected principal type: " + principal);


    }


}

