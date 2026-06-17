package com.user.driven.operations.app.security;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.user.driven.operations.app.core.model.AppUser;
import com.user.driven.operations.app.core.repository.AppUserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Authentication filter that validates API keys provided in the X-API-Key header.
 * <p>
 * When a valid API key is present and no authentication already exists in the security context,
 * this filter looks up the associated user and populates the security context with their
 * details and roles.
 * </p>
 * <p>
 * The filter skips authentication endpoints (/api/v1/auth/) since those handle their own
 * authentication flow.
 * </p>
 *
 * @author Jatin Raheja
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);

    private static final String API_KEY_HEADER = "X-API-Key";

    private final AppUserRepository userRepository;

    public ApiKeyAuthenticationFilter(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Only process if no authentication already exists AND header is present
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String apiKey = request.getHeader(API_KEY_HEADER);

            if (apiKey != null && !apiKey.isBlank()) {
                userRepository.findByApiKey(apiKey)
                    .filter(AppUser::isEnabled)
                    .filter(user -> !user.isLocked())
                    .ifPresent(user -> {
                        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
                        var authentication = new UsernamePasswordAuthenticationToken(
                            user.getEmail(), null, authorities
                        );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // Set userId in MDC for log correlation
                        MDC.put("userId", String.valueOf(user.getId()));

                        log.debug("Authenticated user '{}' via API key", user.getEmail());
                    });
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Don't process for auth endpoints
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/");
    }
}
