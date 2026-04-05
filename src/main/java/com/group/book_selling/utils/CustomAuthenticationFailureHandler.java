package com.group.book_selling.utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        String redirectUrl = "/login?error";

        // Try to preserve the submitted username so the login page can pre-fill
        // the resend-verification form when account is disabled.
        String username = request.getParameter("username");

        if (exception instanceof DisabledException) {
            if (username != null && !username.isBlank()) {
                redirectUrl = "/login?disabled&email=" + URLEncoder.encode(username, StandardCharsets.UTF_8.name());
            } else {
                redirectUrl = "/login?disabled";
            }
        } else if (exception instanceof LockedException) {
            redirectUrl = "/login?locked";
        }

        response.sendRedirect(request.getContextPath() + redirectUrl);
    }
}
