package com.cts.program_training_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                if (jwtUtil.validateToken(token)) {
                    String email = jwtUtil.extractUsername(token);
                    // Extracting "role" which is "ADMIN" based on your payload
                    String role = jwtUtil.extractClaim(token, claims -> claims.get("role", String.class));

                    if (email != null && role != null) {
                        // CRITICAL FIX: Add ROLE_ prefix so .hasRole("ADMIN") works
                        String authority = "ROLE_" + role.toUpperCase();
                        
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        email, null,
                                        List.of(new SimpleGrantedAuthority(authority))
                                );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.info("SecurityContext updated for user: {} with authority: {}", email, authority);
                    }
                }
            } catch (Exception e) {
                log.error("JWT Authentication failed: {}", e.getMessage());
            }
        }

        // Always proceed with the filter chain
        filterChain.doFilter(request, response);
    }
}