package com.travel.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;

import com.travel.dao.TravelDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class UserLoginServlet extends HttpServlet {

    private final TravelDao travelDao = new TravelDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (ViewUtil.currentCustomer(req) != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        ViewUtil.setCommon(req, "login");
        req.setAttribute("mode", sanitizeMode(req.getParameter("mode")));
        req.setAttribute("message", escapeHtml(req.getParameter("msg")));
        req.setAttribute("next", sanitizeNext(req.getParameter("next")));
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = value(req, "email").toLowerCase();
        String password = req.getParameter("password");
        password = password == null ? "" : password;
        String next = sanitizeNext(req.getParameter("next"));

        if (email.isBlank() || password.isBlank()) {
            redirectToLogin(req, resp, "Email and password are required", next);
            return;
        }

        try {
            Map<String, Object> user = travelDao.authenticateCustomer(email, password);
            if (user == null) {
                redirectToLogin(req, resp, "Invalid user credentials", next);
                return;
            }

            HttpSession existingSession = req.getSession(false);
            if (existingSession != null) {
                existingSession.invalidate();
            }

            HttpSession session = req.getSession(true);
            session.setAttribute(SessionKeys.CUSTOMER_USER, user);

            String target = next.isBlank() ? "/dashboard?msg=" + encode("Welcome back, " + user.get("name")) : next;
            resp.sendRedirect(req.getContextPath() + target);
        } catch (SQLException e) {
            redirectToLogin(req, resp, "Login failed", next);
        }
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp, String message, String next)
            throws IOException {
        String target = req.getContextPath() + "/login?mode=login&msg=" + encode(message);
        if (!next.isBlank()) {
            target += "&next=" + encode(next);
        }
        resp.sendRedirect(target);
    }

    private String value(HttpServletRequest req, String paramName) {
        String value = req.getParameter(paramName);
        return value == null ? "" : value.trim();
    }

    private String sanitizeNext(String next) {
        if (next == null || next.isBlank() || !next.startsWith("/") || next.startsWith("//") || next.contains("\"") || next.contains("<")) {
            return "";
        }
        return next;
    }

    private String escapeHtml(String input) {
        if (input == null) return null;
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#x27;");
    }

    private String sanitizeMode(String mode) {
        return "register".equalsIgnoreCase(mode) ? "register" : "login";
    }

    private String encode(Object value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }
}
