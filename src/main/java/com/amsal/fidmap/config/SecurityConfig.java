package com.amsal.fidmap.config;


//import com.amsal.fidmap.endUser.GoogleOAuth2SuccessHandler;

import com.amsal.fidmap.jwt.CustomAuthenticationEntryPoint;
import com.amsal.fidmap.jwt.JwtAuthenticationFilter;
import com.amsal.fidmap.user.Permissions;
import com.amsal.fidmap.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfigurationSource;


import static org.springframework.http.HttpMethod.*;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {


    /**
     * Endpoints reachable without a token (any HTTP method).
     * Everything else requires authentication; tenant authorization is then
     * enforced in the services via WorkspaceAuthorizationService.
     */
    private static final String[] PUBLIC_URLS = {

            "/auth/**",
            "/oauth2/**",
            "/login/**",
            "/api/public/**",

            "/", "/home", "/index",
            "/error",
            "/favicon.ico",

            "/api/webhooks/polar/**",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",

            // Polar redirects the browser here after checkout (no Authorization header).
            "/api/billing/success",
            "/billing/success/**",
            "/api/partner/auth/accept-invitation",
    };

    /** Public READ-ONLY endpoints (portal, public roadmap, public changelog). */
    private static final String[] PUBLIC_GET_URLS = {
            "/board/public/workspace/**",
            "/board/{id}",
            "/feedback/board/{boardId}",
            "/feedback/board/{boardId}/page",
            "/feedback/board/{boardId}/feedback/{feedbackId}",
            "/comment/{feedbackId}",
            "/vote/board/{boardId}",
            "/roadmap/**",
            "/workspaces/*/changelogs",
            "/workspaces/changelog/**",
            "/workspace/slug/**",
    };

    /** Public WRITE endpoints used by anonymous portal visitors (name + email). */
    private static final String[] PUBLIC_POST_URLS = {
            "/feedback/board/{boardId}",
            "/comment/{feedbackId}",
            "/vote/{feedbackId}",
    };

    private static final String[] OWNER_READ_URLS = {

    };
    private static final String[] OWNER_CREATE_URLS = {


    };

    private static final String[] ADMIN_CREATE_URLS = {

    };


    private static final String[] ADMIN_READ_URLS = {

    };


    private static final String[] ADMIN_DELETE_URLS = {


    };


    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;
    private final CorsConfig corsConfig;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
//    private final GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {


        http
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth

                                .requestMatchers(OPTIONS, "/**").permitAll()
                                .requestMatchers(PUBLIC_URLS).permitAll()
                                .requestMatchers(HttpMethod.GET, PUBLIC_GET_URLS).permitAll()
                                .requestMatchers(HttpMethod.POST, PUBLIC_POST_URLS).permitAll()

                                // API documentation is not public in production.
                                .requestMatchers(
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/webjars/**"
                                ).hasRole(Role.SUPER_ADMIN.name())

                                .requestMatchers(
                                        "/api/partner/referrals/**"
                                ).hasRole("PARTNER")

                                .requestMatchers(
                                        "/api/admin/**"
                                )
                                .hasRole(Role.SUPER_ADMIN.name())

                                .anyRequest().authenticated()
                )

                .authenticationProvider((authenticationProvider))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout ->
                        logout.addLogoutHandler(logoutHandler)
                                .logoutUrl("/api/v1/auth/logout")
                                .logoutSuccessHandler((request, response, authentication) ->
                                        SecurityContextHolder.clearContext()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)


                );
//                .oauth2Login(oauth -> oauth
//                        .successHandler(
//                                googleOAuth2SuccessHandler
//                        )
//                );


        return http.build();

    }
}
