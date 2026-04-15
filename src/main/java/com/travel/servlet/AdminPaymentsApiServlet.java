package com.travel.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.travel.dao.TravelDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/api/payments/*")
public class AdminPaymentsApiServlet extends HttpServlet {

    private final TravelDao travelDao = new TravelDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Date startDate = parseSqlDate(req.getParameter("startDate"));
        Date endDate = parseSqlDate(req.getParameter("endDate"));
        Integer userId = parseInt(req.getParameter("userId"));
        Integer packageId = parseInt(req.getParameter("packageId"));
        String status = value(req, "status");

        try {
            List<Map<String, Object>> rows = travelDao.fetchAdminPaymentsFiltered(
                    startDate,
                    endDate,
                    userId,
                    packageId,
                    status);
            writeJson(resp, 200, toJson(rows));
        } catch (SQLException e) {
            writeJson(resp, 500, "{\"success\":false,\"message\":\"Unable to load payments\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String path = req.getPathInfo() == null ? "" : req.getPathInfo();

        try {
            switch (path) {
                case "/refund" -> {
                    Integer paymentIdValue = parseInt(req.getParameter("paymentId"));
                    int paymentId = paymentIdValue == null ? -1 : paymentIdValue;
                    BigDecimal refundAmount = parseDecimal(req.getParameter("refundAmount"));
                    String reason = value(req, "reason");
                    boolean refunded = travelDao.processRefundForAdmin(paymentId, refundAmount, reason);
                    if (!refunded) {
                        writeJson(resp, 400, "{\"success\":false,\"message\":\"Refund not allowed\"}");
                        return;
                    }
                    writeJson(resp, 200, "{\"success\":true,\"message\":\"Refund processed\"}");
                }
                case "/simulate" -> {
                    Integer bookingIdValue = parseInt(req.getParameter("bookingId"));
                    int bookingId = bookingIdValue == null ? -1 : bookingIdValue;
                    String paymentMethod = value(req, "paymentMethod");
                    Map<String, Object> simulated = travelDao.simulatePaymentGatewayForAdmin(bookingId, paymentMethod);
                    if (simulated == null) {
                        writeJson(resp, 404, "{\"success\":false,\"message\":\"Booking not found\"}");
                        return;
                    }
                    writeJson(resp, 200, "{\"success\":true,\"data\":" + toJsonObject(simulated) + "}");
                }
                default -> writeJson(resp, 404, "{\"success\":false,\"message\":\"Unknown endpoint\"}");
            }
        } catch (SQLException e) {
            writeJson(resp, 500, "{\"success\":false,\"message\":\"Payment operation failed\"}");
        }
    }

    private void writeJson(HttpServletResponse resp, int statusCode, String json) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json);
    }

    private String toJson(List<Map<String, Object>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(toJsonObject(rows.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonObject(Map<String, Object> row) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        int index = 0;
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (index++ > 0) {
                sb.append(',');
            }
            sb.append('"').append(escape(entry.getKey())).append('"').append(':');
            sb.append(toJsonValue(entry.getValue()));
        }
        sb.append('}');
        return sb.toString();
    }

    private String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Date date) {
            return "\"" + date + "\"";
        }
        if (value instanceof Timestamp ts) {
            return "\"" + ts.toLocalDateTime() + "\"";
        }
        return "\"" + escape(String.valueOf(value)) + "\"";
    }

    private String escape(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String value(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private Integer parseInt(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ignored) {
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

    private Date parseSqlDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Date.valueOf(LocalDate.parse(raw.trim()));
        } catch (Exception ignored) {
            return null;
        }
    }
}
