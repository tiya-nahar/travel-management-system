package com.travel.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Map;

import com.travel.dao.TravelDao;
import com.travel.service.AdminPackageManagementService;
import com.travel.service.AdminUserManagementService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin")
public class AdminDashboardServlet extends HttpServlet {

    private final TravelDao travelDao = new TravelDao();
    private final AdminPackageManagementService packageManagementService = new AdminPackageManagementService(travelDao);
    private final AdminUserManagementService userManagementService = new AdminUserManagementService(travelDao);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("message", req.getParameter("msg"));

        try {
            String userSearch = value(req, "userSearch");
            String packageCategory = value(req, "packageCategory");
            String bookingStatus = value(req, "bookingStatus");

            req.setAttribute("userSearch", userSearch);
            req.setAttribute("packageCategory", packageCategory);
            req.setAttribute("bookingStatus", bookingStatus);

            req.setAttribute("stats", travelDao.fetchDashboardStats());
            req.setAttribute("recentBookings", travelDao.fetchRecentBookings(5));
            req.setAttribute("monthlyBookings", travelDao.fetchMonthlyBookingStats(6));
            req.setAttribute("packages", travelDao.fetchPackagesByCategoryForAdmin(packageCategory));
            req.setAttribute("destinations", travelDao.fetchDestinationsAdmin());
            req.setAttribute("experiences", travelDao.fetchExperiencesAdmin());
            Date bookingStartDate = parseSqlDate(req.getParameter("bookingStartDate"));
            Date bookingEndDate = parseSqlDate(req.getParameter("bookingEndDate"));
            Integer bookingUserId = parseOptionalInt(req.getParameter("bookingUserId"));
            Integer bookingPackageId = parseOptionalInt(req.getParameter("bookingPackageId"));

            req.setAttribute("bookingStartDate", req.getParameter("bookingStartDate"));
            req.setAttribute("bookingEndDate", req.getParameter("bookingEndDate"));
            req.setAttribute("bookingUserId", bookingUserId);
            req.setAttribute("bookingPackageId", bookingPackageId);
            req.setAttribute("bookingUsers", userManagementService.getAllUsers());
            req.setAttribute("bookingPackages", travelDao.fetchPackageOptions());
            req.setAttribute("bookings", travelDao.fetchBookingsFiltered(bookingStartDate, bookingEndDate, bookingUserId, bookingPackageId, bookingStatus));

                Date paymentStartDate = parseSqlDate(req.getParameter("paymentStartDate"));
                Date paymentEndDate = parseSqlDate(req.getParameter("paymentEndDate"));
                Integer paymentUserId = parseOptionalInt(req.getParameter("paymentUserId"));
                Integer paymentPackageId = parseOptionalInt(req.getParameter("paymentPackageId"));
                String paymentStatus = value(req, "paymentStatus");

                req.setAttribute("paymentStartDate", req.getParameter("paymentStartDate"));
                req.setAttribute("paymentEndDate", req.getParameter("paymentEndDate"));
                req.setAttribute("paymentUserId", paymentUserId);
                req.setAttribute("paymentPackageId", paymentPackageId);
                req.setAttribute("paymentStatus", paymentStatus);
                req.setAttribute("payments", travelDao.fetchAdminPaymentsFiltered(
                    paymentStartDate,
                    paymentEndDate,
                    paymentUserId,
                    paymentPackageId,
                    paymentStatus));
            req.setAttribute("users", userManagementService.getAllUsers(userSearch));
            req.setAttribute("memories", travelDao.fetchMemoriesAdmin());
            req.setAttribute("budgetRules", travelDao.fetchBudgetRulesAdmin());
            req.setAttribute("tripTags", travelDao.fetchTripTagsAdmin());
            req.setAttribute("pricingRules", travelDao.fetchPricingRulesAdmin());
            req.setAttribute("destinationInfo", travelDao.fetchDestinationInfoAdmin());

            int invoiceBookingId = parseInt(req.getParameter("invoiceBookingId"));
            if (invoiceBookingId > 0) {
                Map<String, Object> invoiceData = travelDao.generateInvoicePreview(invoiceBookingId);
                if (invoiceData != null) {
                    req.setAttribute("invoiceData", invoiceData);
                }
            }

            int editPackageId = parseInt(req.getParameter("editPackageId"));
            if (editPackageId > 0) {
                Map<String, Object> editingPackage = packageManagementService.getPackageById(editPackageId);
                if (editingPackage != null) {
                    req.setAttribute("editingPackage", editingPackage);
                }
            }

            int historyUserId = parseInt(req.getParameter("historyUserId"));
            if (historyUserId > 0) {
                req.setAttribute("historyUserId", historyUserId);
                req.setAttribute("userBookingHistory", userManagementService.getUserBookingHistory(historyUserId));
            }

            req.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Unable to load admin dashboard", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = value(req, "action");

        try {
            if ("seed-demo-data".equals(action)) {
                handleSeedData(req, resp);
                return;
            }

            if (isCatalogAction(action)) {
                handleCatalogAction(req, resp, action);
                return;
            }

            if (isUserAction(action)) {
                handleUserAction(req, resp, action);
                return;
            }

            if (isBookingAction(action)) {
                handleBookingAction(req, resp, action);
                return;
            }

            if (isPaymentAction(action)) {
                handlePaymentAction(req, resp, action);
                return;
            }

            redirectWithMessage(req, resp, "Unsupported action", "overview");
        } catch (SQLException e) {
            redirectWithMessage(req, resp, "Operation failed", "overview");
        }
    }

    private void handleUserAction(HttpServletRequest req, HttpServletResponse resp, String action) throws IOException, SQLException {
        int userId = parseInt(req.getParameter("userId"));
        if (userId <= 0) {
            redirectWithMessage(req, resp, "Invalid user selection", "users");
            return;
        }

        boolean changed;
        switch (action) {
            case "activate" -> {
                changed = userManagementService.activateUser(userId);
                redirectWithMessage(req, resp, changed
                        ? "User activated successfully"
                        : "Could not activate selected user", "users");
            }
            case "deactivate" -> {
                changed = userManagementService.deactivateUser(userId);
                redirectWithMessage(req, resp, changed
                        ? "User deactivated successfully"
                        : "Could not deactivate selected user", "users");
            }
            case "delete" -> {
                changed = userManagementService.deleteUser(userId);
                redirectWithMessage(req, resp, changed
                        ? "User deleted successfully"
                        : "Could not delete selected user", "users");
            }
            case "history" -> resp.sendRedirect(req.getContextPath() + "/admin?historyUserId=" + userId + "#users");
            default -> redirectWithMessage(req, resp, "Unsupported user action", "users");
        }
    }

    private void handleBookingAction(HttpServletRequest req, HttpServletResponse resp, String action) throws IOException, SQLException {
        int bookingId = parseInt(req.getParameter("bookingId"));
        if (bookingId <= 0) {
            redirectWithMessage(req, resp, "Invalid booking selected", "bookings");
            return;
        }

        boolean changed;
        switch (action) {
            case "booking-approve" -> {
                changed = travelDao.updateBookingStatusForAdmin(bookingId, "Confirmed");
                redirectWithMessage(req, resp, changed ? "Booking approved" : "Booking not updated", "bookings");
            }
            case "booking-reject" -> {
                changed = travelDao.updateBookingStatusForAdmin(bookingId, "Cancelled");
                redirectWithMessage(req, resp, changed ? "Booking rejected" : "Booking not updated", "bookings");
            }
            case "booking-status" -> {
                String newStatus = value(req, "newStatus");
                changed = travelDao.updateBookingStatusForAdmin(bookingId, newStatus);
                redirectWithMessage(req, resp, changed ? "Booking status updated" : "Booking not updated", "bookings");
            }
            case "booking-invoice" -> resp.sendRedirect(req.getContextPath() + "/admin?invoiceBookingId=" + bookingId + "#bookings");
            default -> redirectWithMessage(req, resp, "Unsupported booking action", "bookings");
        }
    }

    private boolean isUserAction(String action) {
        return "activate".equals(action)
                || "deactivate".equals(action)
                || "delete".equals(action)
                || "history".equals(action);
    }

    private boolean isBookingAction(String action) {
        return "booking-approve".equals(action)
                || "booking-reject".equals(action)
                || "booking-status".equals(action)
                || "booking-invoice".equals(action);
    }

    private void handlePaymentAction(HttpServletRequest req, HttpServletResponse resp, String action) throws IOException, SQLException {
        switch (action) {
            case "payment-refund" -> {
                int paymentId = parseInt(req.getParameter("paymentId"));
                BigDecimal refundAmount = parseDecimal(req.getParameter("refundAmount"));
                String reason = value(req, "refundReason");
                if (paymentId <= 0) {
                    redirectWithMessage(req, resp, "Invalid payment selected", "payments");
                    return;
                }
                boolean refunded = travelDao.processRefundForAdmin(paymentId, refundAmount, reason);
                redirectWithMessage(req, resp,
                        refunded ? "Refund processed successfully" : "Refund not allowed for selected payment",
                        "payments");
            }
            case "payment-simulate" -> {
                int bookingId = parseInt(req.getParameter("bookingId"));
                if (bookingId <= 0) {
                    redirectWithMessage(req, resp, "Enter a valid booking id", "payments");
                    return;
                }
                String paymentMethod = value(req, "paymentMethod");
                Map<String, Object> simulated = travelDao.simulatePaymentGatewayForAdmin(bookingId, paymentMethod);
                redirectWithMessage(req, resp,
                        simulated == null ? "Booking not found for simulation" : "Gateway payment simulated",
                        "payments");
            }
            default -> redirectWithMessage(req, resp, "Unsupported payment action", "payments");
        }
    }

    private boolean isPaymentAction(String action) {
        return "payment-refund".equals(action) || "payment-simulate".equals(action);
    }

    private boolean isCatalogAction(String action) {
        return "add-destination".equals(action)
                || "add-experience".equals(action)
                || "add-memory".equals(action)
                || "add-budget-rule".equals(action)
                || "add-trip-tag".equals(action)
                || "add-pricing-rule".equals(action)
                || "add-destination-info".equals(action);
    }

    private void handleSeedData(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        int added = travelDao.seedAdminDemoData();
        redirectWithMessage(req, resp,
                added > 0 ? "Demo data added successfully" : "Demo data already available",
                "overview");
    }

    private void handleCatalogAction(HttpServletRequest req, HttpServletResponse resp, String action) throws IOException, SQLException {
        switch (action) {
            case "add-destination" -> {
                String city = value(req, "city");
                String country = value(req, "country");
                String description = value(req, "description");
                String imageUrl = value(req, "imageUrl");
                if (city.isBlank() || country.isBlank()) {
                    redirectWithMessage(req, resp, "City and country are required", "destinations");
                    return;
                }
                travelDao.addDestinationForAdmin(city, country, description, imageUrl);
                redirectWithMessage(req, resp, "Destination added", "destinations");
            }
            case "add-experience" -> {
                int destinationId = parseInt(req.getParameter("destinationId"));
                String title = value(req, "title");
                String type = value(req, "type");
                BigDecimal price = parseDecimal(req.getParameter("price"));
                int durationHours = parseInt(req.getParameter("durationHours"));
                String description = value(req, "description");
                if (destinationId <= 0 || title.isBlank()) {
                    redirectWithMessage(req, resp, "Valid destination and title required", "experiences");
                    return;
                }
                travelDao.addExperienceForAdmin(destinationId, title, type, price, durationHours, description);
                redirectWithMessage(req, resp, "Experience added", "experiences");
            }
            case "add-memory" -> {
                int userId = parseInt(req.getParameter("userId"));
                Integer destinationId = parseOptionalInt(req.getParameter("destinationId"));
                String caption = value(req, "caption");
                String imageUrl = value(req, "imageUrl");
                if (userId <= 0 || caption.isBlank()) {
                    redirectWithMessage(req, resp, "Valid user and caption required", "memories");
                    return;
                }
                travelDao.addMemoryForAdmin(userId, destinationId, imageUrl, caption, "Pending");
                redirectWithMessage(req, resp, "Memory added", "memories");
            }
            case "add-budget-rule" -> {
                BigDecimal minBudget = parseDecimal(req.getParameter("minBudget"));
                BigDecimal maxBudget = parseDecimal(req.getParameter("maxBudget"));
                int minDays = parseInt(req.getParameter("minDays"));
                int maxDays = parseInt(req.getParameter("maxDays"));
                String recommendation = value(req, "recommendation");
                if (minBudget == null || maxBudget == null || minDays <= 0 || maxDays <= 0 || recommendation.isBlank()) {
                    redirectWithMessage(req, resp, "Fill all budget rule fields", "budget");
                    return;
                }
                travelDao.addBudgetRuleForAdmin(minBudget, maxBudget, minDays, maxDays, recommendation);
                redirectWithMessage(req, resp, "Budget rule added", "budget");
            }
            case "add-trip-tag" -> {
                int packageId = parseInt(req.getParameter("packageId"));
                String tag = value(req, "tag");
                if (packageId <= 0 || tag.isBlank()) {
                    redirectWithMessage(req, resp, "Valid package and tag required", "trip-tags");
                    return;
                }
                travelDao.addTripTagForAdmin(packageId, tag);
                redirectWithMessage(req, resp, "Trip tag added", "trip-tags");
            }
            case "add-pricing-rule" -> {
                String label = value(req, "label");
                BigDecimal basePrice = parseDecimal(req.getParameter("basePrice"));
                BigDecimal perPerson = parseDecimal(req.getParameter("perPerson"));
                BigDecimal durationMultiplier = parseDecimal(req.getParameter("durationMultiplier"));
                if (label.isBlank() || basePrice == null || perPerson == null || durationMultiplier == null) {
                    redirectWithMessage(req, resp, "Fill all pricing rule fields", "pricing");
                    return;
                }
                travelDao.addPricingRuleForAdmin(label, basePrice, perPerson, durationMultiplier);
                redirectWithMessage(req, resp, "Pricing rule added", "pricing");
            }
            case "add-destination-info" -> {
                int destinationId = parseInt(req.getParameter("destinationId"));
                String bestSeason = value(req, "bestSeason");
                String climate = value(req, "climate");
                String highlights = value(req, "highlights");
                if (destinationId <= 0) {
                    redirectWithMessage(req, resp, "Select destination", "weather");
                    return;
                }
                travelDao.addDestinationInfoForAdmin(destinationId, bestSeason, climate, highlights);
                redirectWithMessage(req, resp, "Destination info added", "weather");
            }
            default -> redirectWithMessage(req, resp, "Unsupported catalog action", "overview");
        }
    }

    private void redirectWithMessage(HttpServletRequest req, HttpServletResponse resp, String message, String anchor) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/admin?msg=" + URLEncoder.encode(message, StandardCharsets.UTF_8) + "#" + anchor);
    }

    private String value(HttpServletRequest req, String paramName) {
        String value = req.getParameter(paramName);
        return value == null ? "" : value.trim();
    }

    private int parseInt(String raw) {
        if (raw == null || raw.isBlank()) {
            return -1;
        }

        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private Integer parseOptionalInt(String raw) {
        int parsed = parseInt(raw);
        return parsed > 0 ? parsed : null;
    }

    private Date parseSqlDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return Date.valueOf(raw.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private BigDecimal parseDecimal(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
