package com.moneyme.config;

import com.moneyme.exception.UnauthorizedAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {}

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getCredentials() == null) {
            throw new UnauthorizedAccessException("Unauthenticated request. Please log in.");
        }

        Object credentials = auth.getCredentials();
        if (credentials instanceof Long) {
            return (Long) credentials;
        } else if (credentials instanceof Number) {
            return ((Number) credentials).longValue();
        } else {
            try {
                return Long.parseLong(credentials.toString());
            } catch (NumberFormatException e) {
                throw new UnauthorizedAccessException("Invalid user identity in token.");
            }
        }
    }
}
