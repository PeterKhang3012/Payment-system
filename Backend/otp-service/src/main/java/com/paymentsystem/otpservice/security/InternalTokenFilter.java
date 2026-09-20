package com.paymentsystem.otpservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Kiểm tra header X-Internal-Service-Token cho mọi /api/** request.
 * Bỏ qua các path public: /swagger-ui/**, /api-docs/**, /v3/api-docs/**
 */
@Component
@RequiredArgsConstructor
public class InternalTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(InternalTokenFilter.class);

    @Value("${internal.service-token}")
    private String validToken;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String receivedToken = request.getHeader("X-Internal-Service-Token");
        if (validToken.equals(receivedToken)) {
            log.debug("token hop le: {}", path);
            filterChain.doFilter(request, response);
        } else {
            log.warn("token khong hop le: path={} token={}", path, receivedToken != null ? "[sai]" : "[thieu]");
            writeUnauthorized(response, path);
        }
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/swagger-ui")
                || path.startsWith("/api-docs")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars");
    }

    private void writeUnauthorized(HttpServletResponse response, String path) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        body.put("status", 401);
        body.put("error", "Unauthorized");
        body.put("message", "Header 'X-Internal-Service-Token' khong hop le hoac bi thieu.");
        body.put("path", path);

        objectMapper.writeValue(response.getWriter(), body);
    }
}
