package com.cts.user_service.security;

import io.jsonwebtoken.Claims;
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

    // This ensures the filter is completely bypassed for public/internal endpoints
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.equals("/users/login") ||
                path.equals("/users/register") ||
                path.startsWith("/users/internal/");
    }

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
                    String role = jwtUtil.extractClaim(token, claims -> claims.get("role", String.class));

                    if (email != null && role != null) {
                        String authority = "ROLE_" + role.toUpperCase();
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        email, null,
                                        List.of(new SimpleGrantedAuthority(authority))
                                );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception e) {
                log.error("JWT validation error in UserService: {}", e.getMessage());
                // Optional: If token is malformed, you might want to return 401 here
            }
        }
        filterChain.doFilter(request, response);
    }
}