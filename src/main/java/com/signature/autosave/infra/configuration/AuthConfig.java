package com.signature.autosave.infra.configuration;

import com.signature.autosave.infra.filter.JWTAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AuthConfig {
    private final JWTAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/webhooks/mercadopago").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/email/content/create", "/email/content/delete/", "/email/content", "/email/content/").hasAnyRole("EDITOR", "ADMIN", "REVIEWER")
                        .requestMatchers("/email/campaign/create", "/email/campaign/delete/", "/email/campaign", "/email/campaign/").hasAnyRole("EDITOR", "ADMIN", "REVIEWER")
                        .requestMatchers("/email/campaign/review/**").hasAnyRole("REVIEWER")
                        .requestMatchers(HttpMethod.PUT, "/users/update/role/").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/payload", "payload/").hasAnyRole("BILLING_MANAGER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
