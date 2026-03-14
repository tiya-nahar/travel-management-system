package com.travel.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebFilter(urlPatterns = {"/bookings", "/bookings/create", "/payments", "/reviews/add"})
public class UserAuthFilter extends HttpFilter implements Filter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpSession session = req.getSession(false);
        Object currentUser = session == null ? null : session.getAttribute(SessionKeys.CUSTOMER_USER);
        if (currentUser != null) {
            chain.doFilter(req, resp);
            return;
        }

        String message = URLEncoder.encode("Please sign in to continue", StandardCharsets.UTF_8);
        String next = URLEncoder.encode(req.getRequestURI().substring(req.getContextPath().length()), StandardCharsets.UTF_8);
        resp.sendRedirect(req.getContextPath() + "/login?msg=" + message + "&next=" + next);
    }
}
