package com.travel.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Map;

import com.travel.dao.TravelDao;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/users/add")
public class AddUserServlet extends HttpServlet {

    private final TravelDao travelDao = new TravelDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = value(req, "name");
        String email = value(req, "email").toLowerCase();
        String phone = value(req, "phone");
        String password = value(req, "password");
        boolean autoLogin = Boolean.parseBoolean(value(req, "autoLogin"));
        String next = sanitizeNext(value(req, "next"));

        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            redirectWithMessage(req, resp, "All user fields are required", next, "register");
            return;
        }

        try {
            travelDao.addCustomer(name, email, phone, password);
            if (autoLogin) {
                Map<String, Object> user = travelDao.authenticateCustomer(email, password);
                if (user != null) {
                    HttpSession existingSession = req.getSession(false);
                    if (existingSession != null) {
                        existingSession.invalidate();
                    }

                    HttpSession session = req.getSession(true);
                    session.setAttribute(SessionKeys.CUSTOMER_USER, user);
                    String target = next.isBlank() ? "/dashboard?msg=" + encode("Account created successfully") : next;
                    resp.sendRedirect(req.getContextPath() + target);
                    return;
                }
            }

            redirectWithMessage(req, resp, "User added successfully", next, "login");
        } catch (SQLException e) {
            redirectWithMessage(req, resp, userMessage(e), next, "register");
        }
    }

    private void redirectWithMessage(
            HttpServletRequest req,
            HttpServletResponse resp,
            String message,
            String next,
            String mode)
            throws IOException {
        String target = req.getContextPath() + "/login?mode=" + encode(mode) + "&msg=" + encode(message);
        if (!next.isBlank()) {
            target += "&next=" + encode(next);
        }
        resp.sendRedirect(target);
    }

    private String value(HttpServletRequest req, String paramName) {
        String value = req.getParameter(paramName);
        return value == null ? "" : value.trim();
    }

    private String userMessage(SQLException e) {
        if (e instanceof SQLIntegrityConstraintViolationException || "23000".equals(e.getSQLState())) {
            return "Email already exists";
        }
        return "User creation failed";
    }

    private String sanitizeNext(String next) {
        if (next == null || next.isBlank() || !next.startsWith("/") || next.startsWith("//") || next.contains("\"") || next.contains("<")) {
            return "";
        }
        return next;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
