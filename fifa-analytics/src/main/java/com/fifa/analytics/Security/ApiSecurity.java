package com.fifa.analytics.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class ApiSecurity extends OncePerRequestFilter {

    private static final Set<String> VALID_API_KEYS = Set.of(
            "fifa-central-api-key-0001",
            "user-fifa-api-key-abc456"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null || !VALID_API_KEYS.contains(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or missing API key");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
