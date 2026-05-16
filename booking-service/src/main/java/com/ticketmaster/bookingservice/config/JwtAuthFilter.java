package com.ticketmaster.bookingservice.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        HttpServletRequest effectiveRequest = request;
        String auth = request.getHeader("Authorization");

        if (auth != null && auth.startsWith("Bearer ")) {
            try {
                String token = auth.substring(7);
                Claims claims = Jwts.parser()
                        .verifyWith(getKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String email = claims.getSubject();
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_CUSTOMER"),
                        new SimpleGrantedAuthority("ROLE_ORGANIZER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                );
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(email, null, authorities));

                Object claimUserId = claims.get("userId");
                if (claimUserId != null && !claimUserId.toString().isBlank()) {
                    String jwtUserId = claimUserId.toString();
                    String headerUserId = request.getHeader("X-User-Id");
                    if (headerUserId != null && !headerUserId.isBlank()
                            && !headerUserId.equals(jwtUserId)) {
                        log.warn("X-User-Id header ({}) does not match JWT userId ({}); using JWT",
                                headerUserId, jwtUserId);
                    }
                    effectiveRequest = new UserIdHeaderRequestWrapper(request, jwtUserId);
                }

            } catch (JwtException e) {
                log.warn("JWT invalid: {}", e.getMessage());
            }
        }
        chain.doFilter(effectiveRequest, response);
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    private static final class UserIdHeaderRequestWrapper extends HttpServletRequestWrapper {

        private final String userId;

        UserIdHeaderRequestWrapper(HttpServletRequest request, String userId) {
            super(request);
            this.userId = userId;
        }

        @Override
        public String getHeader(String name) {
            if ("X-User-Id".equalsIgnoreCase(name)) {
                return userId;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if ("X-User-Id".equalsIgnoreCase(name)) {
                String value = getHeader(name);
                return value != null
                        ? Collections.enumeration(List.of(value))
                        : Collections.emptyEnumeration();
            }
            return super.getHeaders(name);
        }
    }
}
