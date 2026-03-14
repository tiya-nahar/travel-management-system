package com.travel.filter;

import com.travel.servlet.AdminLoginServlet;
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

@WebFilter("/admin.html")
public class AdminAuthFilter extends HttpFilter implements Filter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpSession session = req.getSession(false);
        Object adminUser = session == null ? null : session.getAttribute(AdminLoginServlet.ADMIN_SESSION_KEY);

        if (adminUser == null) {
            String message = URLEncoder.encode("Please sign in as admin", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin-login.html?msg=" + message);
            return;
        }

        chain.doFilter(req, resp);
    }
}
