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


    private static final String[] PUBLIC_URLS = {

            "/swagger-ui.html",
            "/swagger-ui/**",
            "/auth/**",
            "/oauth2/**",
            "/login/**",
            "/api/public/**",
            "/v3/api-docs/**",
            "/", "/home", "/index",
            "/webjars/**",
            "/error",
            "/favicon.ico",
            "/board/public/workspace/**",
            "/feedback/board/{boardId}",
            "/feedback/board/{boardId}/feedback/{feedbackId}",
            "/comment/{feedbackId}",
            "/vote/{feedbackId}",
            "/roadmap/{roadmapId}/items",
            "/workspaces/{workspaceId}/changelogs",
            "/workspace/slug/**",
            "/vote/**",

            "/api/webhooks/paddle/**",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",

//            "/auth/**",
//            "/board/**",
//            "/comment/**",
//            "/workspace/**",
//            "/roadmap/**",
//            "/workspaces",
//            "/feedback/board/**",
//            "/roadmap/item/**",


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

                                .requestMatchers(PUBLIC_URLS).permitAll()
                                .requestMatchers(OPTIONS, "/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/board/{id}", "/feedback/board/**",
                                        "/comment/**", "/roadmap/**", "/workspaces/*/changelogs",
                                        "/workspaces/changelog/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/feedback/board/{boardId}",
                                        "/comment/**", "/vote/**").permitAll()
                                .requestMatchers("/feedback/user/me").authenticated()


//                        .requestMatchers("/board/**").hasRole(Role.ADMIN.name())
//                        .requestMatchers(POST,"/board/**").hasAuthority(Permissions.ADMIN_CREATE.name())
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
