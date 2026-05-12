
package com.cts.api_gateway.filter;

import com.cts.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter
        extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final Environment env;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    public JwtAuthenticationFilter(Environment env) {
        super(Config.class);
        this.env = env;
    }

    public static class Config {
        private String allowedRoles;

        public String getAllowedRoles() {
            return allowedRoles;
        }
        public void setAllowedRoles(String allowedRoles) {
            this.allowedRoles = allowedRoles;
        }
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization Header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7).trim();
            if (token.isEmpty()) {
                return onError(exchange, "Empty token", HttpStatus.UNAUTHORIZED);
            }

            try {
                String secret = env.getProperty("jwt.secret");
                if (secret == null || secret.isBlank()) {
                    return onError(exchange, "JWT secret not configured", HttpStatus.INTERNAL_SERVER_ERROR);
                }

                Claims claims =  jwtUtil.extractAllClaims(token);
                System.out.println(claims);
                System.out.println("secret"+secret);





                String userRole = claims.get("role", String.class);

                if (config.getAllowedRoles() != null && !config.getAllowedRoles().isEmpty()) {

                    List<String> authorizedRoles = Arrays.stream(config.getAllowedRoles().split(","))
                            .map(r -> r.trim().toUpperCase())
                            .collect(Collectors.toList());

                    if (userRole == null ||
                            !authorizedRoles.contains(userRole.trim().toUpperCase())) {
                        return onError(
                                exchange,
                                "Forbidden: role '" + userRole + "' not allowed",
                                HttpStatus.FORBIDDEN
                        );
                    }
                }

                String userId = claims.get("userId") != null
                        ? claims.get("userId").toString()
                        : "";

                String email = claims.getSubject() != null
                        ? claims.getSubject()
                        : "";

                System.out.println(userId+" "+userRole+" "+email);


                HttpHeaders headers = new HttpHeaders();
                headers.putAll(request.getHeaders());

                headers.set("X-User-Id", userId);
                headers.set("X-User-Role", userRole != null ? userRole : "");
                headers.set("X-User-Email", email);

                ServerHttpRequest decoratedRequest = new ServerHttpRequestDecorator(request) {
                    @Override
                    public HttpHeaders getHeaders() {
                        return headers;
                    }
                };

                return chain.filter(exchange.mutate().request(decoratedRequest).build());




            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
                return onError(
                        exchange,
                        "Token validation failed",
                        HttpStatus.UNAUTHORIZED
                );
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String msg, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);


        exchange.getResponse().getHeaders().setContentType(org.springframework.http.MediaType.TEXT_PLAIN);

        byte[] bytes = msg.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        org.springframework.core.io.buffer.DataBuffer buffer =
                exchange.getResponse().bufferFactory().wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}