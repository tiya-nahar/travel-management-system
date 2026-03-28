package com.travel.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.travel.dao.TravelDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/profile")
public class UserProfileServlet extends HttpServlet {

    private final TravelDao travelDao = new TravelDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = ViewUtil.currentCustomerId(req);
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        ViewUtil.setCommon(req, "profile");
        req.setAttribute("message", req.getParameter("msg"));
        req.setAttribute("error", req.getParameter("err"));

        try {
            List<Map<String, Object>> bookings = travelDao.fetchBookingsForUser(userId);
            List<Map<String, Object>> payments = travelDao.fetchPaymentsForUser(userId);
            List<Map<String, Object>> reviews = travelDao.fetchReviewsForUser(userId);

            req.setAttribute("bookings", bookings);
            req.setAttribute("payments", payments);
            req.setAttribute("reviews", reviews);
            req.setAttribute("recentBookings", bookings.stream().limit(3).toList());

            long completedBookings = bookings.stream()
                    .map(row -> String.valueOf(row.get("status")))
                    .filter(status -> "completed".equalsIgnoreCase(status))
                    .count();

            BigDecimal totalSpent = payments.stream()
                    .filter(row -> "paid".equalsIgnoreCase(String.valueOf(row.get("paymentStatus"))))
                    .map(row -> (BigDecimal) row.get("amount"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double averageRating = reviews.isEmpty()
                    ? 0.0
                    : reviews.stream()
                    .map(row -> (Number) row.get("rating"))
                    .mapToInt(Number::intValue)
                    .average()
                    .orElse(0.0);

            req.setAttribute("totalBookings", bookings.size());
            req.setAttribute("completedBookings", completedBookings);
            req.setAttribute("totalSpent", totalSpent);
            req.setAttribute("averageRating", averageRating);

            req.getRequestDispatcher("/WEB-INF/views/user-profile.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Unable to load user profile", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer userId = ViewUtil.currentCustomerId(req);
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = value(req, "action");
        try {
            if ("updateProfile".equals(action)) {
                updateProfile(req, userId);
                refreshSessionUser(req, value(req, "name"), value(req, "email"), value(req, "phone"));
                redirectSuccess(req, resp, "Profile updated successfully.");
                return;
            }

            if ("changePassword".equals(action)) {
                changePassword(req, userId);
                redirectSuccess(req, resp, "Password changed successfully.");
                return;
            }

            redirectError(req, resp, "Unsupported profile action.");
        } catch (IllegalArgumentException e) {
            redirectError(req, resp, e.getMessage());
        } catch (SQLException e) {
            redirectError(req, resp, "Unable to save profile details.");
        }
    }

    private void updateProfile(HttpServletRequest req, int userId) throws SQLException {
        String name = value(req, "name");
        String email = value(req, "email");
        String phone = value(req, "phone");

        if (name.length() < 2) {
            throw new IllegalArgumentException("Name must be at least 2 characters.");
        }

        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }

        if (!phone.isBlank() && !phone.matches("^[0-9+\\- ]{10,15}$")) {
            throw new IllegalArgumentException("Phone number must be 10 to 15 digits.");
        }

        travelDao.updateCustomerProfile(userId, name, email, phone);
    }

    private void changePassword(HttpServletRequest req, int userId) throws SQLException {
        String currentPassword = value(req, "currentPassword");
        String newPassword = value(req, "newPassword");
        String confirmPassword = value(req, "confirmPassword");

        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            throw new IllegalArgumentException("All password fields are required.");
        }

        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirm password must match.");
        }

        boolean changed = travelDao.changeCustomerPassword(userId, currentPassword, newPassword);
        if (!changed) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
    }

    @SuppressWarnings("unchecked")
    private void refreshSessionUser(HttpServletRequest req, String name, String email, String phone) {
        Object current = req.getSession().getAttribute(SessionKeys.CUSTOMER_USER);
        if (!(current instanceof Map<?, ?> userMap)) {
            return;
        }

        Map<String, Object> user = (Map<String, Object>) userMap;
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);
    }

    private void redirectSuccess(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/profile?msg=" + encode(message));
    }

    private void redirectError(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/profile?err=" + encode(message));
    }

    private String value(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}