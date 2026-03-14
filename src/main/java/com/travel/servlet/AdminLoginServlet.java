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

@WebServlet("/admin-login")
public class AdminLoginServlet extends HttpServlet {

    public static final String ADMIN_SESSION_KEY = SessionKeys.ADMIN_USER;

    private final TravelDao travelDao = new TravelDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(SessionKeys.ADMIN_USER) != null) {
            redirect(resp, req.getContextPath() + "/admin");
            return;
        }

        req.setAttribute("message", escapeHtml(req.getParameter("msg")));
        req.getRequestDispatcher("/WEB-INF/views/admin-login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = value(req, "email").toLowerCase();
        String password = req.getParameter("password");
        password = password == null ? "" : password;

        if (email.isBlank() || password.isBlank()) {
            redirectToLogin(req, resp, "Admin email and password are required");
            return;
        }

        try {
            Map<String, Object> admin = travelDao.authenticateAdmin(email, password);
            if (admin == null) {
                redirectToLogin(req, resp, "Invalid admin credentials");
                return;
            }

            HttpSession existingSession = req.getSession(false);
            if (existingSession != null) {
                existingSession.invalidate();
            }

            HttpSession session = req.getSession(true);
            session.setAttribute(SessionKeys.ADMIN_USER, admin);

            String name = String.valueOf(admin.getOrDefault("name", "Admin"));
            redirect(resp, req.getContextPath() + "/admin?msg="
                    + encode("Welcome back, " + name));
        } catch (SQLException e) {
            redirectToLogin(req, resp, "Admin login failed");
        }
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
        redirect(resp, req.getContextPath() + "/admin-login?msg=" + encode(message));
    }

    private void redirect(HttpServletResponse resp, String location) throws IOException {
        resp.sendRedirect(location);
    }

    private String value(HttpServletRequest req, String paramName) {
        String value = req.getParameter(paramName);
        return value == null ? "" : value.trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String escapeHtml(String input) {
        if (input == null) return null;
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#x27;");
    }
}
