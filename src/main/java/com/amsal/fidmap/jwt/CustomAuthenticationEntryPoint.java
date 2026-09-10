package com.amsal.fidmap.jwt;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        System.out.println(
                "AUTH ENTRY POINT HIT: " +
                        request.getMethod() +
                        " " +
                        request.getRequestURI()
        );

        System.out.println(
                "AUTH EXCEPTION: " + authException.getMessage()
        );

        response.getWriter().write("""
            {
                "success": "false",
                "message": "sorry you are not allowed access this resource,your credentials are invalid,please try again later",
                "data": {
                "error": "Token is either Invalid or missing"
                }
            }
        """);
    }
}
