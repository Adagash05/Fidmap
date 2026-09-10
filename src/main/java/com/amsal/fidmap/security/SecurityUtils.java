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

