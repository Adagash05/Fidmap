//package com.amsal.fidmap.endUser;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.core.oidc.user.OidcUser;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//@RequiredArgsConstructor
//public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {
//
//    private final EndUserService endUserService;
//
//    @Override
//    public void onAuthenticationSuccess(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            Authentication authentication
//    ) throws IOException {
//
//        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
//
//        assert oidcUser != null;
//        EndUser endUser = endUserService.processGoogleUser(oidcUser);
//
//        System.out.println("Google login successful for EndUser: " + endUser.getId());
//
//        response.sendRedirect("http://localhost:5173/");
//    }
//}