package com.njangi.gateway.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;

@Component
public class OtpValidationFilter implements Filter {

    private static final List<String> OTP_REQUIRED_PATHS = List.of(
        "/api/paiements/valider",
        "/api/cotisations/pot/verser"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        var request = (HttpServletRequest) req;
        var response = (HttpServletResponse) res;
        boolean requiresOtp = OTP_REQUIRED_PATHS.stream().anyMatch(request.getRequestURI()::startsWith);
        if (requiresOtp && request.getHeader("X-OTP-Token") == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "OTP validation required");
            return;
        }
        chain.doFilter(req, res);
    }
}
