package com.user.driven.operations.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration class for setting up HTTP security filters.
 * <p>
 * This configuration:
 * <ul>
 * <li>Disables CSRF protection</li>
 * <li>Allows unauthenticated access to specific endpoints like /api/**,
 * /swagger-ui/**, /api-docs/**, and /h2-console/**</li>
 * <li>Requires authentication for all other requests</li>
 * <li>Disables frame options to allow access to the H2 console</li>
 * </ul>
 * </p>
 * 
 * @Author Jatin Raheja
 */
//@Configuration
//@EnableWebSecurity
public class SecurityConfig {

//	@Bean
//	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//		http.csrf(csrf -> csrf.disable())
//				.authorizeHttpRequests(authz -> authz.requestMatchers("/api/**").permitAll()
//						.requestMatchers("/swagger-ui/**").permitAll().requestMatchers("/api-docs/**").permitAll()
//						.requestMatchers("/h2-console/**").permitAll().anyRequest().authenticated())
//				.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
//		return http.build();
//	}
}