package com.travel.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.travel.db.DBConnection;

public class TravelDao {

    public Map<String, Object> fetchDashboardStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DBConnection.getConnection();
             Statement statement = conn.createStatement()) {

            stats.put("users", singleInt(statement, "SELECT COUNT(*) FROM users"));
            stats.put("packages", singleInt(statement, "SELECT COUNT(*) FROM packages"));
            stats.put("bookings", singleInt(statement, "SELECT COUNT(*) FROM bookings"));
            stats.put("destinations", singleInt(statement, "SELECT COUNT(*) FROM destinations"));
            stats.put("pendingPayments", singleInt(statement,
                    "SELECT COUNT(*) FROM payments WHERE payment_status='Pending'"));
            stats.put("reviews", singleInt(statement, "SELECT COUNT(*) FROM reviews"));
            stats.put("revenue", singleDecimal(statement,
                    "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE payment_status='Paid'"));
        }
        return stats;
    }

    public List<Map<String, Object>> fetchTopPackages(int limit) throws SQLException {
        String sql = """
              SELECT p.package_id, p.title, p.price, p.duration_days, p.main_image,
                  p.category, p.available_slots, p.discount_percent, p.is_available,
                  (p.price * (1 - (COALESCE(p.discount_percent, 0) / 100))) AS final_price,
                       d.city, d.country, h.name AS hotel_name, h.rating AS hotel_rating
                FROM packages p
                JOIN destinations d ON d.destination_id = p.destination_id
                LEFT JOIN package_details pd ON pd.package_id = p.package_id
                LEFT JOIN hotels h ON h.hotel_id = pd.hotel_id
                ORDER BY p.created_at DESC, p.package_id DESC
                LIMIT ?
                """;

        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ensurePackageManagementColumns(conn);
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(packageFromResult(rs));
                }
            }
        }
        return list;
    }

    public List<Map<String, Object>> fetchPackages(String search, String city, Integer maxDuration, String sort)
            throws SQLException {
        StringBuilder sql = new StringBuilder("""
              SELECT p.package_id, p.title, p.price, p.duration_days, p.main_image,
                  p.category, p.available_slots, p.discount_percent, p.is_available,
                  (p.price * (1 - (COALESCE(p.discount_percent, 0) / 100))) AS final_price,
                       d.city, d.country, h.name AS hotel_name, h.rating AS hotel_rating
                FROM packages p
                JOIN destinations d ON d.destination_id = p.destination_id
                LEFT JOIN package_details pd ON pd.package_id = p.package_id
                LEFT JOIN hotels h ON h.hotel_id = pd.hotel_id
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(p.title) LIKE ? OR LOWER(d.city) LIKE ?)");
            String like = "%" + search.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }

        if (city != null && !city.isBlank()) {
            sql.append(" AND d.city = ?");
            params.add(city);
        }

        if (maxDuration != null) {
            sql.append(" AND p.duration_days <= ?");
            params.add(maxDuration);
        }

        sql.append(orderBy(sort));

        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ensurePackageManagementColumns(conn);

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(packageFromResult(rs));
                }
            }
        }

        return list;
    }

    public List<String> fetchCities() throws SQLException {
        List<String> cities = new ArrayList<>();
        String sql = "SELECT city FROM destinations ORDER BY city";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cities.add(rs.getString("city"));
            }
        }
        return cities;
    }

    public List<Map<String, Object>> fetchCustomers() throws SQLException {
        List<Map<String, Object>> users = new ArrayList<>();
        String sql = "SELECT user_id, name FROM users WHERE role='Customer' ORDER BY name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("userId", rs.getInt("user_id"));
                row.put("name", rs.getString("name"));
                users.add(row);
            }
        }

        return users;
    }

    public List<Map<String, Object>> fetchPackageOptions() throws SQLException {
        List<Map<String, Object>> packages = new ArrayList<>();
        String sql = "SELECT package_id, title FROM packages ORDER BY title";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ensurePackageManagementColumns(conn);
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("packageId", rs.getInt("package_id"));
                row.put("title", rs.getString("title"));
                packages.add(row);
            }
        }

        return packages;
    }

    public Map<String, Object> authenticateAdmin(String email, String password) throws SQLException {
        String sql = """
                SELECT admin_id, name, email, role
                FROM admin_users
                WHERE LOWER(email) = ? AND password = ?
                LIMIT 1
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase(Locale.ROOT));
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Map<String, Object> admin = new LinkedHashMap<>();
                admin.put("adminId", rs.getInt("admin_id"));
                admin.put("name", rs.getString("name"));
                admin.put("email", rs.getString("email"));
                admin.put("role", rs.getString("role"));
                return admin;
            }
        }
    }

    public Map<String, Object> authenticateCustomer(String email, String password) throws SQLException {
        String sql = """
                SELECT user_id, name, email, role
                FROM users
            WHERE LOWER(email) = ? AND password = ? AND role = 'Customer' AND is_active = 1
                LIMIT 1
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ensureUserStatusColumn(conn);
            ps.setString(1, email.trim().toLowerCase(Locale.ROOT));
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Map<String, Object> user = new LinkedHashMap<>();
                user.put("userId", rs.getInt("user_id"));
                user.put("name", rs.getString("name"));
                user.put("email", rs.getString("email"));
                user.put("role", rs.getString("role"));
                return user;
            }
        }
    }

    public void addCustomer(String name, String email, String phone, String password) throws SQLException {
        String sql = """
                INSERT INTO users (name, email, password, phone, role, profile_image)
                VALUES (?, ?, ?, ?, 'Customer', ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, email.trim().toLowerCase(Locale.ROOT));
            ps.setString(3, password.trim());
            ps.setString(4, phone.trim());
            ps.setString(5, "https://picsum.photos/seed/" + profileSeed(email) + "/300/300");
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> fetchRecentBookings(int limit) throws SQLException {
        String sql = """
                SELECT b.booking_id, u.name AS user_name, p.title AS package_title,
                  b.booking_date, b.travel_date, b.number_of_people,
                                    b.traveler_name, b.contact_phone, b.special_request, b.status,
                                    COALESCE((
                                            SELECT GROUP_CONCAT(CONCAT(bls.service_type, ': ', bls.provider_name)
                                                                                    ORDER BY bls.linked_service_id SEPARATOR ', ')
                                            FROM booking_linked_services bls
                                            WHERE bls.booking_id = b.booking_id
                                    ), '') AS linked_services
                FROM bookings b
                JOIN users u ON u.user_id = b.user_id
                JOIN packages p ON p.package_id = b.package_id
                ORDER BY b.booking_id DESC
                LIMIT ?
                """;

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(bookingFromResult(rs));
                }
            }
        }
        return rows;
    }

    public List<Map<String, Object>> fetchMonthlyBookingStats(int months) throws SQLException {
        String sql = """
              SELECT DATE_FORMAT(booking_date, '%Y-%m') AS month_key,
                  DATE_FORMAT(booking_date, '%b %Y') AS month_label,
                       COUNT(*) AS booking_count
                FROM bookings
                WHERE booking_date IS NOT NULL
                  AND booking_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH)
              GROUP BY DATE_FORMAT(booking_date, '%Y-%m'), DATE_FORMAT(booking_date, '%b %Y')
              ORDER BY month_key
                """;

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, months));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("monthLabel", rs.getString("month_label"));
                    row.put("bookingCount", rs.getInt("booking_count"));
                    rows.add(row);
                }
            }
        }

        return rows;
    }

    public void createBookingWithPayment(int userId, int packageId, Date travelDate, int people,
                                         String travelerName, String contactPhone, String specialRequest,
                                         String status, String paymentMethod) throws SQLException {
        String packageSql = "SELECT (price * (1 - (COALESCE(discount_percent, 0) / 100))) AS final_price FROM packages WHERE package_id = ?";
        String bookingSql = """
                INSERT INTO bookings (
                    user_id, package_id, booking_date, travel_date, number_of_people,
                    traveler_name, contact_phone, special_request, status
                )
                VALUES (?, ?, CURDATE(), ?, ?, ?, ?, ?, ?)
                """;
        String paymentSql = """
                INSERT INTO payments (booking_id, amount, payment_method, payment_status, payment_date)
                VALUES (?, ?, ?, ?, NOW())
                """;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ensureBookingDetailColumns(conn);
                ensureLinkedServicesTable(conn);
                ensurePackageManagementColumns(conn);

                BigDecimal price;
                try (PreparedStatement ps = conn.prepareStatement(packageSql)) {
                    ps.setInt(1, packageId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Package not found");
                        }
                        price = rs.getBigDecimal("final_price");
                    }
                }

                int bookingId;
                try (PreparedStatement ps = conn.prepareStatement(bookingSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, packageId);
                    ps.setDate(3, travelDate);
                    ps.setInt(4, people);
                    ps.setString(5, travelerName);
                    ps.setString(6, contactPhone);
                    ps.setString(7, specialRequest);
                    ps.setString(8, status);
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Booking ID generation failed");
                        }
                        bookingId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(paymentSql)) {
                    ps.setInt(1, bookingId);
                    ps.setBigDecimal(2, price.multiply(BigDecimal.valueOf(people)));
                    ps.setString(3, paymentMethod);
                    ps.setString(4, "Confirmed".equalsIgnoreCase(status) ? "Paid" : "Pending");
                    ps.executeUpdate();
                }

                linkServicesForBooking(conn, bookingId, packageId, people);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Map<String, Object>> fetchBookings() throws SQLException {
        String sql = """
                SELECT b.booking_id, u.name AS user_name, p.title AS package_title,
                  b.booking_date, b.travel_date, b.number_of_people,
                                    b.traveler_name, b.contact_phone, b.special_request, b.status,
                                    COALESCE((
                                            SELECT GROUP_CONCAT(CONCAT(bls.service_type, ': ', bls.provider_name)
                                                                                    ORDER BY bls.linked_service_id SEPARATOR ', ')
                                            FROM booking_linked_services bls
                                            WHERE bls.booking_id = b.booking_id
                                    ), '') AS linked_services
                FROM bookings b
                JOIN users u ON u.user_id = b.user_id
                JOIN packages p ON p.package_id = b.package_id
                ORDER BY b.booking_id DESC
                """;

        return fetchBookings(sql, null);
    }

    public List<Map<String, Object>> fetchBookingsFiltered(Date startDate, Date endDate, Integer userId, Integer packageId, String status)
            throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT b.booking_id, u.name AS user_name, p.title AS package_title,
                       b.booking_date, b.travel_date, b.number_of_people,
                       b.traveler_name, b.contact_phone, b.special_request, b.status,
                       COALESCE((
                               SELECT GROUP_CONCAT(CONCAT(bls.service_type, ': ', bls.provider_name)
                                                   ORDER BY bls.linked_service_id SEPARATOR ', ')
                               FROM booking_linked_services bls
                               WHERE bls.booking_id = b.booking_id
                       ), '') AS linked_services
                FROM bookings b
                JOIN users u ON u.user_id = b.user_id
                JOIN packages p ON p.package_id = b.package_id
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (startDate != null) {
            sql.append(" AND b.booking_date >= ?");
            params.add(startDate);
        }
        if (endDate != null) {
            sql.append(" AND b.booking_date <= ?");
            params.add(endDate);
        }
        if (userId != null) {
            sql.append(" AND b.user_id = ?");
            params.add(userId);
        }
        if (packageId != null) {
            sql.append(" AND b.package_id = ?");
            params.add(packageId);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND LOWER(b.status) = ?");
            params.add(status.trim().toLowerCase(Locale.ROOT));
        }

        sql.append(" ORDER BY b.booking_id DESC");

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(bookingFromResult(rs));
                }
            }
        }

        return rows;
    }

    public List<Map<String, Object>> fetchPackagesByCategoryForAdmin(String category) throws SQLException {
        StringBuilder sql = new StringBuilder("""
              SELECT p.package_id, p.title, p.price, p.duration_days, p.main_image,
                  p.category, p.available_slots, p.discount_percent, p.is_available,
                  (p.price * (1 - (COALESCE(p.discount_percent, 0) / 100))) AS final_price,
                       d.city, d.country, h.name AS hotel_name, h.rating AS hotel_rating
                FROM packages p
                JOIN destinations d ON d.destination_id = p.destination_id
                LEFT JOIN package_details pd ON pd.package_id = p.package_id
                LEFT JOIN hotels h ON h.hotel_id = pd.hotel_id
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();
        if (category != null && !category.isBlank()) {
            sql.append(" AND LOWER(p.category) = ?");
            params.add(category.trim().toLowerCase(Locale.ROOT));
        }
        sql.append(" ORDER BY p.package_id DESC");

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ensurePackageManagementColumns(conn);
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(packageFromResult(rs));
                }
            }
        }

        return rows;
    }

    public boolean updateBookingStatusForAdmin(int bookingId, String status) throws SQLException {
        if (status == null) {
            return false;
        }

        String normalized;
        String lower = status.trim().toLowerCase(Locale.ROOT);
        switch (lower) {
            case "pending" -> normalized = "Pending";
            case "confirmed" -> normalized = "Confirmed";
            case "cancelled" -> normalized = "Cancelled";
            default -> {
                return false;
            }
        }

        String updateBookingSql = "UPDATE bookings SET status = ? WHERE booking_id = ?";
        String updatePaymentSql = "UPDATE payments SET payment_status = ? WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int affected;
                try (PreparedStatement ps = conn.prepareStatement(updateBookingSql)) {
                    ps.setString(1, normalized);
                    ps.setInt(2, bookingId);
                    affected = ps.executeUpdate();
                }

                if (affected == 0) {
                    conn.rollback();
                    return false;
                }

                String paymentStatus = "Confirmed".equals(normalized) ? "Paid"
                        : "Cancelled".equals(normalized) ? "Failed"
                        : "Pending";

                try (PreparedStatement ps = conn.prepareStatement(updatePaymentSql)) {
                    ps.setString(1, paymentStatus);
                    ps.setInt(2, bookingId);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Map<String, Object> generateInvoicePreview(int bookingId) throws SQLException {
        String sql = """
                SELECT b.booking_id, b.booking_date, b.travel_date, b.number_of_people, b.status,
                       u.name AS user_name, u.email,
                       p.title AS package_title,
                       p.price AS base_price,
                       COALESCE(p.discount_percent, 0) AS discount_percent,
                       COALESCE(pay.amount,
                                (p.price * (1 - (COALESCE(p.discount_percent, 0) / 100)) * b.number_of_people)
                       ) AS amount,
                       COALESCE(pay.payment_status, 'Pending') AS payment_status
                FROM bookings b
                JOIN users u ON u.user_id = b.user_id
                JOIN packages p ON p.package_id = b.package_id
                LEFT JOIN payments pay ON pay.booking_id = b.booking_id
                WHERE b.booking_id = ?
                LIMIT 1
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ensurePackageManagementColumns(conn);
            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Map<String, Object> invoice = new LinkedHashMap<>();
                invoice.put("invoiceNumber", "INV-" + bookingId + "-" + rs.getDate("booking_date").toString().replace("-", ""));
                invoice.put("bookingId", rs.getInt("booking_id"));
                invoice.put("bookingDate", rs.getDate("booking_date"));
                invoice.put("travelDate", rs.getDate("travel_date"));
                invoice.put("numberOfPeople", rs.getInt("number_of_people"));
                invoice.put("status", rs.getString("status"));
                invoice.put("userName", rs.getString("user_name"));
                invoice.put("userEmail", rs.getString("email"));
                invoice.put("packageTitle", rs.getString("package_title"));
                invoice.put("basePrice", rs.getBigDecimal("base_price"));
                invoice.put("discountPercent", rs.getBigDecimal("discount_percent"));
                invoice.put("amount", rs.getBigDecimal("amount"));
                invoice.put("paymentStatus", rs.getString("payment_status"));
                return invoice;
            }
        }
    }

    public List<Map<String, Object>> fetchBookingsForUser(int userId) throws SQLException {
        String sql = """
                SELECT b.booking_id, u.name AS user_name, p.title AS package_title,
                  b.booking_date, b.travel_date, b.number_of_people,
                                    b.traveler_name, b.contact_phone, b.special_request, b.status,
                                    COALESCE((
                                            SELECT GROUP_CONCAT(CONCAT(bls.service_type, ': ', bls.provider_name)
                                                                                    ORDER BY bls.linked_service_id SEPARATOR ', ')
                                            FROM booking_linked_services bls
                                            WHERE bls.booking_id = b.booking_id
                                    ), '') AS linked_services
                FROM bookings b
                JOIN users u ON u.user_id = b.user_id
                JOIN packages p ON p.package_id = b.package_id
                WHERE b.user_id = ?
                ORDER BY b.booking_id DESC
                """;

        return fetchBookings(sql, userId);
    }

    public List<Map<String, Object>> fetchPayments() throws SQLException {
        String sql = """
            SELECT payment_id, booking_id, amount, payment_method, payment_status, payment_date,
                   gateway_transaction_id, refund_status, refund_amount, refund_reason, refunded_at
                FROM payments
                ORDER BY payment_id DESC
                """;

        return fetchPayments(sql, null);
    }

    public List<Map<String, Object>> fetchPaymentsForUser(int userId) throws SQLException {
        String sql = """
                SELECT pay.payment_id, pay.booking_id, pay.amount, pay.payment_method,
                       pay.payment_status, pay.payment_date,
                       pay.gateway_transaction_id, pay.refund_status, pay.refund_amount,
                       pay.refund_reason, pay.refunded_at
                FROM payments pay
                JOIN bookings b ON b.booking_id = pay.booking_id
                WHERE b.user_id = ?
                ORDER BY pay.payment_id DESC
                """;

        return fetchPayments(sql, userId);
    }

    public List<Map<String, Object>> fetchAdminPaymentsFiltered(Date startDate, Date endDate,
                                                                 Integer userId, Integer packageId,
                                                                 String paymentStatus) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT pay.payment_id, pay.booking_id, pay.amount, pay.payment_method,
                       pay.payment_status, pay.payment_date,
                       pay.gateway_transaction_id, pay.refund_status, pay.refund_amount,
                       pay.refund_reason, pay.refunded_at,
                       u.name AS user_name, p.title AS package_title
                FROM payments pay
                JOIN bookings b ON b.booking_id = pay.booking_id
                JOIN users u ON u.user_id = b.user_id
                JOIN packages p ON p.package_id = b.package_id
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();
        if (startDate != null) {
            sql.append(" AND DATE(pay.payment_date) >= ?");
            params.add(startDate);
        }
        if (endDate != null) {
            sql.append(" AND DATE(pay.payment_date) <= ?");
            params.add(endDate);
        }
        if (userId != null) {
            sql.append(" AND b.user_id = ?");
            params.add(userId);
        }
        if (packageId != null) {
            sql.append(" AND b.package_id = ?");
            params.add(packageId);
        }
        if (paymentStatus != null && !paymentStatus.isBlank()) {
            sql.append(" AND LOWER(pay.payment_status) = ?");
            params.add(paymentStatus.trim().toLowerCase(Locale.ROOT));
        }

        sql.append(" ORDER BY pay.payment_id DESC");

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ensurePaymentManagementColumns(conn);
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = paymentFromResult(rs);
                    row.put("userName", rs.getString("user_name"));
                    row.put("packageTitle", rs.getString("package_title"));
                    rows.add(row);
                }
            }
        }

        return rows;
    }

    public boolean processRefundForAdmin(int paymentId, BigDecimal refundAmount, String reason) throws SQLException {
        String readSql = "SELECT amount, payment_status FROM payments WHERE payment_id = ?";
        String updateSql = """
                UPDATE payments
                SET payment_status = 'Refunded',
                    refund_status = 'Completed',
                    refund_amount = ?,
                    refund_reason = ?,
                    refunded_at = NOW()
                WHERE payment_id = ?
                """;

        try (Connection conn = DBConnection.getConnection()) {
            ensurePaymentManagementColumns(conn);

            BigDecimal originalAmount;
            String currentStatus;
            try (PreparedStatement ps = conn.prepareStatement(readSql)) {
                ps.setInt(1, paymentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    originalAmount = rs.getBigDecimal("amount");
                    currentStatus = rs.getString("payment_status");
                }
            }

            if (currentStatus == null || !"paid".equalsIgnoreCase(currentStatus)) {
                return false;
            }

            BigDecimal finalRefund = refundAmount == null ? originalAmount : refundAmount;
            if (finalRefund.compareTo(BigDecimal.ZERO) < 0 || finalRefund.compareTo(originalAmount) > 0) {
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setBigDecimal(1, finalRefund);
                ps.setString(2, reason == null || reason.isBlank() ? "Admin initiated refund" : reason.trim());
                ps.setInt(3, paymentId);
                return ps.executeUpdate() > 0;
            }
        }
    }

    public Map<String, Object> simulatePaymentGatewayForAdmin(int bookingId, String paymentMethod) throws SQLException {
        String readBookingSql = """
                SELECT b.booking_id, b.number_of_people,
                       (p.price * (1 - (COALESCE(p.discount_percent, 0) / 100))) AS unit_price
                FROM bookings b
                JOIN packages p ON p.package_id = b.package_id
                WHERE b.booking_id = ?
                LIMIT 1
                """;
        String existingPaymentSql = "SELECT payment_id FROM payments WHERE booking_id = ? LIMIT 1";
        String insertSql = """
                INSERT INTO payments (
                    booking_id, amount, payment_method, payment_status, payment_date,
                    gateway_transaction_id, refund_status
                )
                VALUES (?, ?, ?, 'Paid', NOW(), ?, 'NotRequested')
                """;
        String updateSql = """
                UPDATE payments
                SET amount = ?,
                    payment_method = ?,
                    payment_status = 'Paid',
                    payment_date = NOW(),
                    gateway_transaction_id = ?,
                    refund_status = 'NotRequested',
                    refund_amount = NULL,
                    refund_reason = NULL,
                    refunded_at = NULL
                WHERE payment_id = ?
                """;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ensurePackageManagementColumns(conn);
                ensurePaymentManagementColumns(conn);

                BigDecimal amount;
                try (PreparedStatement ps = conn.prepareStatement(readBookingSql)) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return null;
                        }
                        BigDecimal unitPrice = rs.getBigDecimal("unit_price");
                        int people = rs.getInt("number_of_people");
                        amount = unitPrice.multiply(BigDecimal.valueOf(people));
                    }
                }

                Integer paymentId = null;
                try (PreparedStatement ps = conn.prepareStatement(existingPaymentSql)) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            paymentId = rs.getInt("payment_id");
                        }
                    }
                }

                String gatewayTxn = "SIM-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase(Locale.ROOT);
                String method = paymentMethod == null || paymentMethod.isBlank() ? "Gateway-Sim" : paymentMethod.trim();

                if (paymentId == null) {
                    try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, bookingId);
                        ps.setBigDecimal(2, amount);
                        ps.setString(3, method);
                        ps.setString(4, gatewayTxn);
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                paymentId = keys.getInt(1);
                            }
                        }
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                        ps.setBigDecimal(1, amount);
                        ps.setString(2, method);
                        ps.setString(3, gatewayTxn);
                        ps.setInt(4, paymentId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("paymentId", paymentId);
                result.put("bookingId", bookingId);
                result.put("amount", amount);
                result.put("paymentMethod", method);
                result.put("paymentStatus", "Paid");
                result.put("gatewayTransactionId", gatewayTxn);
                result.put("simulatedAt", new Timestamp(System.currentTimeMillis()));
                return result;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Map<String, Object>> fetchReviews() throws SQLException {
        String sql = """
                SELECT r.review_id, u.name AS user_name, p.title AS package_title,
                       r.rating, r.comment, r.created_at
                FROM reviews r
                JOIN users u ON u.user_id = r.user_id
                JOIN packages p ON p.package_id = r.package_id
                ORDER BY r.review_id DESC
                """;

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("reviewId", rs.getInt("review_id"));
                row.put("userName", rs.getString("user_name"));
                row.put("packageTitle", rs.getString("package_title"));
                row.put("rating", rs.getInt("rating"));
                row.put("comment", rs.getString("comment"));
                row.put("createdAt", rs.getTimestamp("created_at"));
                rows.add(row);
            }
        }
        return rows;
    }

    public List<Map<String, Object>> fetchReviewsForUser(int userId) throws SQLException {
        String sql = """
                SELECT r.review_id, u.name AS user_name, p.title AS package_title,
                       r.rating, r.comment, r.created_at
                FROM reviews r
                JOIN users u ON u.user_id = r.user_id
                JOIN packages p ON p.package_id = r.package_id
                WHERE r.user_id = ?
                ORDER BY r.review_id DESC
                """;

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(reviewFromResult(rs));
                }
            }
        }
        return rows;
    }

    public void updateCustomerProfile(int userId, String name, String email, String phone) throws SQLException {
        String sql = """
                UPDATE users
                SET name = ?, email = ?, phone = ?
                WHERE user_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, email.trim().toLowerCase(Locale.ROOT));
            ps.setString(3, phone == null || phone.isBlank() ? null : phone.trim());
            ps.setInt(4, userId);
            ps.executeUpdate();
        }
    }

    public boolean changeCustomerPassword(int userId, String currentPassword, String newPassword) throws SQLException {
        String readSql = "SELECT password FROM users WHERE user_id = ?";
        String updateSql = "UPDATE users SET password = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement readPs = conn.prepareStatement(readSql)) {
            readPs.setInt(1, userId);

            try (ResultSet rs = readPs.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                String savedPassword = rs.getString("password");
                if (savedPassword == null || !savedPassword.equals(currentPassword)) {
                    return false;
                }
            }

            try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                updatePs.setString(1, newPassword);
                updatePs.setInt(2, userId);
                updatePs.executeUpdate();
            }
        }

        return true;
    }

    public List<Map<String, Object>> fetchLatestReviews(int limit) throws SQLException {
        String sql = """
                SELECT r.review_id, u.name AS user_name, p.title AS package_title,
                       r.rating, r.comment, r.created_at
                FROM reviews r
                JOIN users u ON u.user_id = r.user_id
                JOIN packages p ON p.package_id = r.package_id
                ORDER BY r.review_id DESC
                LIMIT ?
                """;

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(reviewFromResult(rs));
                }
            }
        }
        return rows;
    }

    public void addReview(int userId, int packageId, int rating, String comment) throws SQLException {
        String sql = "INSERT INTO reviews (user_id, package_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, packageId);
            ps.setInt(3, rating);
            ps.setString(4, comment);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> fetchUsersHub() throws SQLException {
        return fetchSimpleRows("SELECT name, role FROM users ORDER BY user_id", "name", "role");
    }

    public List<Map<String, Object>> fetchDestinationsHub() throws SQLException {
        return fetchSimpleRows(
                "SELECT city, country, image_url FROM destinations ORDER BY destination_id",
                "city", "country", "image_url");
    }

    public List<Map<String, Object>> fetchHotelsHub() throws SQLException {
        return fetchSimpleRows("SELECT name, city, rating FROM hotels ORDER BY hotel_id", "name", "city", "rating");
    }

    public List<Map<String, Object>> fetchTransportHub() throws SQLException {
        return fetchSimpleRows("SELECT type, provider, seat_capacity FROM transport ORDER BY transport_id",
                "type", "provider", "seat_capacity");
    }

    public List<Map<String, Object>> fetchAdminUsersList() throws SQLException {
        return fetchAdminUsersList(null);
    }

    public List<Map<String, Object>> fetchAdminUsersList(String search) throws SQLException {
        String sql = """
                SELECT user_id, name, email, phone, role, is_active, created_at
                FROM users
                WHERE (? IS NULL OR ? = '' OR LOWER(name) LIKE ? OR LOWER(email) LIKE ?)
                ORDER BY user_id DESC
                """;

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ensureUserStatusColumn(conn);

            String term = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
            String like = "%" + term + "%";
            ps.setString(1, term);
            ps.setString(2, term);
            ps.setString(3, like);
            ps.setString(4, like);

            try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("userId", rs.getInt("user_id"));
                row.put("name", rs.getString("name"));
                row.put("email", rs.getString("email"));
                row.put("phone", rs.getString("phone"));
                row.put("role", rs.getString("role"));
                boolean isActive = rs.getBoolean("is_active");
                row.put("isActive", isActive);
                row.put("statusLabel", isActive ? "Active" : "Inactive");
                row.put("createdAt", rs.getTimestamp("created_at"));
                rows.add(row);
            }
            }
        }
        return rows;
    }

    public void addPackageForAdmin(int destinationId, String title, String description, BigDecimal price,
                                   int durationDays, int maxPeople, String mainImage,
                                   String category, int availableSlots, BigDecimal discountPercent) throws SQLException {
        String sql = """
                INSERT INTO packages (
                    destination_id, title, description, price, duration_days,
                    max_people, main_image, category, available_slots, discount_percent, is_available
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ensurePackageManagementColumns(conn);
            ps.setInt(1, destinationId);
            ps.setString(2, title);
            ps.setString(3, description);
            ps.setBigDecimal(4, price);
            ps.setInt(5, durationDays);
            ps.setInt(6, maxPeople);
            ps.setString(7, mainImage);
            ps.setString(8, category == null || category.isBlank() ? "family" : category.trim().toLowerCase(Locale.ROOT));
            ps.setInt(9, Math.max(0, availableSlots));
            ps.setBigDecimal(10, discountPercent == null ? BigDecimal.ZERO : discountPercent.max(BigDecimal.ZERO));
            ps.setBoolean(11, availableSlots > 0);
            ps.executeUpdate();
        }
    }

    public boolean updatePackageForAdmin(int packageId, int destinationId, String title, String description,
                                         BigDecimal price, int durationDays, int maxPeople, String mainImage,
                                         String category, int availableSlots, BigDecimal discountPercent) throws SQLException {
        String sql = """
                UPDATE packages
                SET destination_id = ?,
                    title = ?,
                    description = ?,
                    price = ?,
                    duration_days = ?,
                    max_people = ?,
                    main_image = ?,
                    category = ?,
                    available_slots = ?,
                    discount_percent = ?,
                    is_available = ?
                WHERE package_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ensurePackageManagementColumns(conn);
            ps.setInt(1, destinationId);
            ps.setString(2, title);
            ps.setString(3, description);
            ps.setBigDecimal(4, price);
            ps.setInt(5, durationDays);
            ps.setInt(6, maxPeople);
            ps.setString(7, mainImage);
            ps.setString(8, category == null || category.isBlank() ? "family" : category.trim().toLowerCase(Locale.ROOT));
            ps.setInt(9, Math.max(0, availableSlots));
            ps.setBigDecimal(10, discountPercent == null ? BigDecimal.ZERO : discountPercent.max(BigDecimal.ZERO));
            ps.setBoolean(11, availableSlots > 0);
            ps.setInt(12, packageId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletePackageForAdmin(int packageId) throws SQLException {
        String countBookingsSql = "SELECT COUNT(*) FROM bookings WHERE package_id = ?";
        String deletePackageDetailsSql = "DELETE FROM package_details WHERE package_id = ?";
        String deletePackageImagesSql = "DELETE FROM package_images WHERE package_id = ?";
        String deleteTripTagsSql = "DELETE FROM trip_tags WHERE package_id = ?";
        String deleteReviewsSql = "DELETE FROM reviews WHERE package_id = ?";
        String deletePackageSql = "DELETE FROM packages WHERE package_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int bookingCount;
                try (PreparedStatement ps = conn.prepareStatement(countBookingsSql)) {
                    ps.setInt(1, packageId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        bookingCount = rs.getInt(1);
                    }
                }

                if (bookingCount > 0) {
                    conn.rollback();
                    return false;
                }

                runDelete(conn, deletePackageDetailsSql, packageId);
                runDelete(conn, deletePackageImagesSql, packageId);
                runDelete(conn, deleteTripTagsSql, packageId);
                runDelete(conn, deleteReviewsSql, packageId);
                int affected = runDelete(conn, deletePackageSql, packageId);
                conn.commit();
                return affected > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Map<String, Object> fetchPackageByIdForAdmin(int packageId) throws SQLException {
        String sql = """
                SELECT p.package_id, p.title, p.description, p.price, p.duration_days, p.max_people, p.main_image,
                       p.destination_id, p.category, p.available_slots, p.discount_percent, p.is_available,
                       d.city, d.country,
                       h.name AS hotel_name, h.rating AS hotel_rating,
                       (p.price * (1 - (COALESCE(p.discount_percent, 0) / 100))) AS final_price
                FROM packages p
                JOIN destinations d ON d.destination_id = p.destination_id
                LEFT JOIN package_details pd ON pd.package_id = p.package_id
                LEFT JOIN hotels h ON h.hotel_id = pd.hotel_id
                WHERE p.package_id = ?
                LIMIT 1
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ensurePackageManagementColumns(conn);
            ps.setInt(1, packageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> row = packageFromResult(rs);
                row.put("description", rs.getString("description"));
                row.put("destinationId", rs.getInt("destination_id"));
                row.put("maxPeople", rs.getInt("max_people"));
                return row;
            }
        }
    }

    public boolean setUserActiveStatus(int userId, boolean active) throws SQLException {
        String sql = """
                UPDATE users
                SET is_active = ?
                WHERE user_id = ? AND role = 'Customer'
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ensureUserStatusColumn(conn);
            ps.setBoolean(1, active);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteUserAndRelatedData(int userId) throws SQLException {
        String deletePaymentsSql = """
                DELETE p FROM payments p
                JOIN bookings b ON b.booking_id = p.booking_id
                WHERE b.user_id = ?
                """;
        String deleteLinkedServicesSql = """
                DELETE bls FROM booking_linked_services bls
                JOIN bookings b ON b.booking_id = bls.booking_id
                WHERE b.user_id = ?
                """;
        String deleteReviewsSql = "DELETE FROM reviews WHERE user_id = ?";
        String deleteMemoriesSql = "DELETE FROM memories WHERE user_id = ?";
        String deleteBookingsSql = "DELETE FROM bookings WHERE user_id = ?";
        String deleteUserSql = "DELETE FROM users WHERE user_id = ? AND role = 'Customer'";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                runDelete(conn, deletePaymentsSql, userId);
                runDelete(conn, deleteLinkedServicesSql, userId);
                runDelete(conn, deleteReviewsSql, userId);
                runDelete(conn, deleteMemoriesSql, userId);
                runDelete(conn, deleteBookingsSql, userId);

                int affectedUsers = runDelete(conn, deleteUserSql, userId);
                conn.commit();
                return affectedUsers > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Map<String, Object>> fetchUserBookingHistoryForAdmin(int userId) throws SQLException {
        String sql = """
                SELECT b.booking_id, p.title AS package_title,
                       b.booking_date, b.travel_date, b.number_of_people, b.status,
                       COALESCE(SUM(pay.amount), 0) AS total_amount,
                       COALESCE(MAX(pay.payment_status), 'Pending') AS payment_status
                FROM bookings b
                JOIN packages p ON p.package_id = b.package_id
                LEFT JOIN payments pay ON pay.booking_id = b.booking_id
                WHERE b.user_id = ?
                GROUP BY b.booking_id, p.title, b.booking_date, b.travel_date, b.number_of_people, b.status
                ORDER BY b.booking_id DESC
                """;

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("bookingId", rs.getInt("booking_id"));
                    row.put("packageTitle", rs.getString("package_title"));
                    row.put("bookingDate", rs.getDate("booking_date"));
                    row.put("travelDate", rs.getDate("travel_date"));
                    row.put("numberOfPeople", rs.getInt("number_of_people"));
                    String status = rs.getString("status");
                    row.put("status", status);
                    row.put("statusClass", status == null ? "" : status.toLowerCase(Locale.ROOT));
                    row.put("totalAmount", rs.getBigDecimal("total_amount"));
                    String paymentStatus = rs.getString("payment_status");
                    row.put("paymentStatus", paymentStatus);
                    row.put("paymentStatusClass", paymentStatus == null ? "" : paymentStatus.toLowerCase(Locale.ROOT));
                    rows.add(row);
                }
            }
        }

        return rows;
    }

    public List<Map<String, Object>> fetchDestinationsAdmin() throws SQLException {
        String sql = """
                SELECT destination_id, city, country, description, image_url
                FROM destinations
                ORDER BY destination_id DESC
                """;

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("destinationId", rs.getInt("destination_id"));
                row.put("city", rs.getString("city"));
                row.put("country", rs.getString("country"));
                row.put("description", rs.getString("description"));
                row.put("imageUrl", rs.getString("image_url"));
                rows.add(row);
            }
        }
        return rows;
    }

    public List<Map<String, Object>> fetchExperiencesAdmin() throws SQLException {
        String sql = """
                SELECT e.experience_id, d.city, e.title, e.type, e.price, e.duration_hours, e.created_at
                FROM experiences e
                JOIN destinations d ON d.destination_id = e.destination_id
                ORDER BY e.experience_id DESC
                """;

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("experienceId", rs.getInt("experience_id"));
                row.put("city", rs.getString("city"));
                row.put("title", rs.getString("title"));
                row.put("type", rs.getString("type"));
                row.put("price", rs.getBigDecimal("price"));
                row.put("durationHours", rs.getInt("duration_hours"));
                row.put("createdAt", rs.getTimestamp("created_at"));
                rows.add(row);
            }
        }
        return rows;
    }

    public List<Map<String, Object>> fetchMemoriesAdmin() throws SQLException {
        String sql = """
                SELECT m.memory_id, u.name AS user_name, d.city, m.caption, m.status, m.created_at
                FROM memories m
                JOIN users u ON u.user_id = m.user_id
                LEFT JOIN destinations d ON d.destination_id = m.destination_id
                ORDER BY m.memory_id DESC
                """;

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("memoryId", rs.getInt("memory_id"));
                row.put("userName", rs.getString("user_name"));
                row.put("city", rs.getString("city"));
                row.put("caption", rs.getString("caption"));
                row.put("status", rs.getString("status"));
                row.put("createdAt", rs.getTimestamp("created_at"));
                rows.add(row);
            }
        }
        return rows;
    }

    public List<Map<String, Object>> fetchBudgetRulesAdmin() throws SQLException {
        String sql = """
                SELECT rule_id, min_budget, max_budget, min_days, max_days, recommendation, created_at
                FROM budget_rules
                ORDER BY rule_id DESC
                """;

        return fetchGenericAdminRows(sql, "rule_id", "min_budget", "max_budget", "min_days",
                "max_days", "recommendation", "created_at");
    }

    public List<Map<String, Object>> fetchTripTagsAdmin() throws SQLException {
        String sql = """
                SELECT t.tag_id, p.title, t.tag
                FROM trip_tags t
                JOIN packages p ON p.package_id = t.package_id
                ORDER BY t.tag_id DESC
                """;

        return fetchGenericAdminRows(sql, "tag_id", "title", "tag");
    }

    public List<Map<String, Object>> fetchPricingRulesAdmin() throws SQLException {
        String sql = """
                SELECT rule_id, label, base_price, per_person, duration_multiplier, created_at
                FROM pricing_rules
                ORDER BY rule_id DESC
                """;

        return fetchGenericAdminRows(sql, "rule_id", "label", "base_price", "per_person",
                "duration_multiplier", "created_at");
    }

    public List<Map<String, Object>> fetchDestinationInfoAdmin() throws SQLException {
        String sql = """
                SELECT i.info_id, d.city, i.best_season, i.climate, i.highlights
                FROM destination_info i
                JOIN destinations d ON d.destination_id = i.destination_id
                ORDER BY i.info_id DESC
                """;

        return fetchGenericAdminRows(sql, "info_id", "city", "best_season", "climate", "highlights");
    }

    public void addDestinationForAdmin(String city, String country, String description, String imageUrl) throws SQLException {
        String sql = """
                INSERT INTO destinations (city, country, description, image_url)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, city);
            ps.setString(2, country);
            ps.setString(3, description == null || description.isBlank() ? null : description);
            ps.setString(4, imageUrl == null || imageUrl.isBlank() ? null : imageUrl);
            ps.executeUpdate();
        }
    }

    public void addExperienceForAdmin(int destinationId, String title, String type,
                                      BigDecimal price, int durationHours, String description) throws SQLException {
        String sql = """
                INSERT INTO experiences (destination_id, title, type, price, duration_hours, description)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            ps.setString(2, title);
            ps.setString(3, type == null || type.isBlank() ? null : type);
            ps.setBigDecimal(4, price == null ? BigDecimal.ZERO : price);
            ps.setInt(5, durationHours > 0 ? durationHours : 1);
            ps.setString(6, description == null || description.isBlank() ? null : description);
            ps.executeUpdate();
        }
    }

    public void addMemoryForAdmin(int userId, Integer destinationId, String imageUrl,
                                  String caption, String status) throws SQLException {
        String sql = """
                INSERT INTO memories (user_id, destination_id, image_url, caption, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (destinationId == null) {
                ps.setObject(2, null);
            } else {
                ps.setInt(2, destinationId);
            }
            ps.setString(3, imageUrl == null || imageUrl.isBlank() ? null : imageUrl);
            ps.setString(4, caption);
            ps.setString(5, status == null || status.isBlank() ? "Pending" : status);
            ps.executeUpdate();
        }
    }

    public void addBudgetRuleForAdmin(BigDecimal minBudget, BigDecimal maxBudget,
                                      int minDays, int maxDays, String recommendation) throws SQLException {
        String sql = """
                INSERT INTO budget_rules (min_budget, max_budget, min_days, max_days, recommendation)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, minBudget);
            ps.setBigDecimal(2, maxBudget);
            ps.setInt(3, minDays);
            ps.setInt(4, maxDays);
            ps.setString(5, recommendation);
            ps.executeUpdate();
        }
    }

    public void addTripTagForAdmin(int packageId, String tag) throws SQLException {
        String sql = "INSERT INTO trip_tags (package_id, tag) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, packageId);
            ps.setString(2, tag);
            ps.executeUpdate();
        }
    }

    public void addPricingRuleForAdmin(String label, BigDecimal basePrice,
                                       BigDecimal perPerson, BigDecimal durationMultiplier) throws SQLException {
        String sql = """
                INSERT INTO pricing_rules (label, base_price, per_person, duration_multiplier)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, label);
            ps.setBigDecimal(2, basePrice);
            ps.setBigDecimal(3, perPerson);
            ps.setBigDecimal(4, durationMultiplier);
            ps.executeUpdate();
        }
    }

    public void addDestinationInfoForAdmin(int destinationId, String bestSeason,
                                           String climate, String highlights) throws SQLException {
        String sql = """
                INSERT INTO destination_info (destination_id, best_season, climate, highlights)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            ps.setString(2, bestSeason == null || bestSeason.isBlank() ? null : bestSeason);
            ps.setString(3, climate == null || climate.isBlank() ? null : climate);
            ps.setString(4, highlights == null || highlights.isBlank() ? null : highlights);
            ps.executeUpdate();
        }
    }

    public int seedAdminDemoData() throws SQLException {
        int inserted = 0;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int destinationId = ensureDemoDestination(conn);
                int userId = ensureDemoCustomer(conn);
                int packageId = ensureDemoPackage(conn, destinationId);

                inserted += ensureDemoExperience(conn, destinationId);
                inserted += ensureDemoMemory(conn, userId, destinationId);
                inserted += ensureDemoBudgetRule(conn);
                inserted += ensureDemoTripTag(conn, packageId);
                inserted += ensureDemoPricingRule(conn);
                inserted += ensureDemoDestinationInfo(conn, destinationId);
                inserted += ensureDemoBookingAndPayment(conn, userId, packageId);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        return inserted;
    }

    private int ensureDemoDestination(Connection conn) throws SQLException {
        String findSql = "SELECT destination_id FROM destinations WHERE city = ? AND country = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setString(1, "Goa");
            ps.setString(2, "India");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        String insertSql = "INSERT INTO destinations (city, country, description, image_url) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Goa");
            ps.setString(2, "India");
            ps.setString(3, "Beach destination with nightlife and water sports.");
            ps.setString(4, "https://picsum.photos/seed/goa-destination/800/500");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private int ensureDemoCustomer(Connection conn) throws SQLException {
        String findSql = "SELECT user_id FROM users WHERE email = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setString(1, "demo.customer@aerotrail.com");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        ensureUserStatusColumn(conn);
        String insertSql = """
                INSERT INTO users (name, email, password, phone, role, is_active, profile_image)
                VALUES (?, ?, ?, ?, 'Customer', 1, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Demo Customer");
            ps.setString(2, "demo.customer@aerotrail.com");
            ps.setString(3, "demo123");
            ps.setString(4, "9876543210");
            ps.setString(5, "https://picsum.photos/seed/demo-customer/300/300");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private int ensureDemoPackage(Connection conn, int destinationId) throws SQLException {
        String findSql = "SELECT package_id FROM packages WHERE title = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setString(1, "Goa Weekend Escape");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        ensurePackageManagementColumns(conn);
        String insertSql = """
                INSERT INTO packages (destination_id, title, description, price, duration_days, max_people, main_image,
                                      category, available_slots, discount_percent, is_available)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, destinationId);
            ps.setString(2, "Goa Weekend Escape");
            ps.setString(3, "3-day curated Goa trip with beachside stay and transfers.");
            ps.setBigDecimal(4, new BigDecimal("14999.00"));
            ps.setInt(5, 3);
            ps.setInt(6, 4);
            ps.setString(7, "https://picsum.photos/seed/goa-package/960/540");
            ps.setString(8, "family");
            ps.setInt(9, 25);
            ps.setBigDecimal(10, new BigDecimal("10.00"));
            ps.setBoolean(11, true);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private int ensureDemoExperience(Connection conn, int destinationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM experiences WHERE destination_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    return 0;
                }
            }
        }
        String insertSql = """
                INSERT INTO experiences (destination_id, title, type, price, duration_hours, description)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, destinationId);
            ps.setString(2, "Dolphin Cruise");
            ps.setString(3, "Water Activity");
            ps.setBigDecimal(4, new BigDecimal("1999.00"));
            ps.setInt(5, 3);
            ps.setString(6, "Morning cruise with dolphin spotting.");
            return ps.executeUpdate();
        }
    }

    private int ensureDemoMemory(Connection conn, int userId, int destinationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM memories WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    return 0;
                }
            }
        }
        String insertSql = """
                INSERT INTO memories (user_id, destination_id, image_url, caption, status)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, destinationId);
            ps.setString(3, "https://picsum.photos/seed/goa-memory/800/500");
            ps.setString(4, "Sunset at Candolim beach");
            ps.setString(5, "Approved");
            return ps.executeUpdate();
        }
    }

    private int ensureDemoBudgetRule(Connection conn) throws SQLException {
        if (singleCount(conn, "SELECT COUNT(*) FROM budget_rules") > 0) {
            return 0;
        }
        String insertSql = """
                INSERT INTO budget_rules (min_budget, max_budget, min_days, max_days, recommendation)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setBigDecimal(1, new BigDecimal("10000"));
            ps.setBigDecimal(2, new BigDecimal("30000"));
            ps.setInt(3, 2);
            ps.setInt(4, 4);
            ps.setString(5, "Weekend city break with flight + hotel deals");
            return ps.executeUpdate();
        }
    }

    private int ensureDemoTripTag(Connection conn, int packageId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM trip_tags WHERE package_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, packageId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    return 0;
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO trip_tags (package_id, tag) VALUES (?, ?)")) {
            ps.setInt(1, packageId);
            ps.setString(2, "beach");
            return ps.executeUpdate();
        }
    }

    private int ensureDemoPricingRule(Connection conn) throws SQLException {
        if (singleCount(conn, "SELECT COUNT(*) FROM pricing_rules") > 0) {
            return 0;
        }
        String insertSql = """
                INSERT INTO pricing_rules (label, base_price, per_person, duration_multiplier)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, "Standard Dynamic Rule");
            ps.setBigDecimal(2, new BigDecimal("12000"));
            ps.setBigDecimal(3, new BigDecimal("2500"));
            ps.setBigDecimal(4, new BigDecimal("1.15"));
            return ps.executeUpdate();
        }
    }

    private int ensureDemoDestinationInfo(Connection conn, int destinationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM destination_info WHERE destination_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    return 0;
                }
            }
        }
        String insertSql = """
                INSERT INTO destination_info (destination_id, best_season, climate, highlights)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, destinationId);
            ps.setString(2, "November to February");
            ps.setString(3, "Warm tropical climate");
            ps.setString(4, "Beaches, water sports, nightlife");
            return ps.executeUpdate();
        }
    }

    private int ensureDemoBookingAndPayment(Connection conn, int userId, int packageId) throws SQLException {
        String sql = "SELECT booking_id FROM bookings WHERE user_id = ? AND package_id = ? LIMIT 1";
        int bookingId = -1;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, packageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    bookingId = rs.getInt(1);
                }
            }
        }

        int inserted = 0;
        if (bookingId <= 0) {
            String insertBooking = """
                    INSERT INTO bookings (user_id, package_id, booking_date, travel_date, number_of_people,
                                          traveler_name, contact_phone, special_request, status)
                    VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 15 DAY), 2, ?, ?, ?, 'Confirmed')
                    """;
            try (PreparedStatement ps = conn.prepareStatement(insertBooking, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setInt(2, packageId);
                ps.setString(3, "Demo Customer");
                ps.setString(4, "9876543210");
                ps.setString(5, "Need sea-facing room");
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    bookingId = keys.getInt(1);
                }
            }
            inserted++;
        }

        if (singleCount(conn, "SELECT COUNT(*) FROM payments WHERE booking_id = " + bookingId) == 0) {
            ensurePaymentManagementColumns(conn);
            String insertPayment = """
                    INSERT INTO payments (booking_id, amount, payment_method, payment_status, payment_date,
                                          gateway_transaction_id, refund_status)
                    VALUES (?, ?, 'UPI', 'Paid', NOW(), ?, 'NotRequested')
                    """;
            try (PreparedStatement ps = conn.prepareStatement(insertPayment)) {
                ps.setInt(1, bookingId);
                ps.setBigDecimal(2, new BigDecimal("26998.00"));
                ps.setString(3, "SIM-DEMO-" + bookingId);
                ps.executeUpdate();
            }
            inserted++;
        }

        return inserted;
    }

    private int singleCount(Connection conn, String sql) throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private List<Map<String, Object>> fetchSimpleRows(String sql, String... fields) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (String field : fields) {
                    row.put(field, rs.getObject(field));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    private List<Map<String, Object>> fetchGenericAdminRows(String sql, String... fields) throws SQLException {
        return fetchSimpleRows(sql, fields);
    }

    private List<Map<String, Object>> fetchBookings(String sql, Integer userId) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId != null) {
                ps.setInt(1, userId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(bookingFromResult(rs));
                }
            }
        }
        return rows;
    }

    private List<Map<String, Object>> fetchPayments(String sql, Integer userId) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ensurePaymentManagementColumns(conn);
            if (userId != null) {
                ps.setInt(1, userId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(paymentFromResult(rs));
                }
            }
        }
        return rows;
    }

    private int singleInt(Statement statement, String sql) throws SQLException {
        try (ResultSet rs = statement.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private BigDecimal singleDecimal(Statement statement, String sql) throws SQLException {
        try (ResultSet rs = statement.executeQuery(sql)) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    private String orderBy(String sort) {
        if (sort == null) {
            return " ORDER BY p.package_id DESC";
        }

        return switch (sort) {
            case "price_asc" -> " ORDER BY p.price ASC";
            case "price_desc" -> " ORDER BY p.price DESC";
            case "rating_desc" -> " ORDER BY h.rating DESC";
            default -> " ORDER BY p.package_id DESC";
        };
    }

    private Map<String, Object> packageFromResult(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("packageId", rs.getInt("package_id"));
        row.put("title", rs.getString("title"));
        row.put("price", rs.getBigDecimal("price"));
        row.put("durationDays", rs.getInt("duration_days"));
        row.put("mainImage", rs.getString("main_image"));
        row.put("category", rs.getString("category"));
        row.put("availableSlots", rs.getInt("available_slots"));
        row.put("discountPercent", rs.getBigDecimal("discount_percent"));
        row.put("isAvailable", rs.getBoolean("is_available"));
        row.put("finalPrice", rs.getBigDecimal("final_price"));
        row.put("city", rs.getString("city"));
        row.put("country", rs.getString("country"));
        row.put("hotelName", rs.getString("hotel_name"));
        row.put("hotelRating", rs.getBigDecimal("hotel_rating"));
        return row;
    }

    private Map<String, Object> bookingFromResult(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("bookingId", rs.getInt("booking_id"));
        row.put("userName", rs.getString("user_name"));
        row.put("packageTitle", rs.getString("package_title"));
        row.put("bookingDate", rs.getDate("booking_date"));
        row.put("travelDate", rs.getDate("travel_date"));
        row.put("numberOfPeople", rs.getInt("number_of_people"));
        row.put("travelerName", rs.getString("traveler_name"));
        row.put("contactPhone", rs.getString("contact_phone"));
        row.put("specialRequest", rs.getString("special_request"));
        String status = rs.getString("status");
        row.put("status", status);
        row.put("statusClass", status == null ? "" : status.toLowerCase());
        row.put("linkedServices", rs.getString("linked_services"));
        return row;
    }

    private void ensureLinkedServicesTable(Connection conn) throws SQLException {
        String createSql = """
                CREATE TABLE IF NOT EXISTS booking_linked_services (
                    linked_service_id INT PRIMARY KEY AUTO_INCREMENT,
                    booking_id INT NOT NULL,
                    service_type VARCHAR(20) NOT NULL,
                    provider_name VARCHAR(150) NOT NULL,
                    traveler_count INT DEFAULT 1,
                    service_status VARCHAR(30) DEFAULT 'Reserved',
                    notes VARCHAR(200),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
                )
                """;
        try (Statement statement = conn.createStatement()) {
            statement.execute(createSql);
        }
    }

    private void linkServicesForBooking(Connection conn, int bookingId, int packageId, int travelers) throws SQLException {
        String insertSql = """
                INSERT INTO booking_linked_services (
                    booking_id, service_type, provider_name, traveler_count, service_status, notes
                )
                VALUES (?, ?, ?, ?, 'Reserved', ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            String hotel = fetchHotelForPackage(conn, packageId);
            insertLinkedService(ps, bookingId, "Hotel", hotel, travelers, "Auto-linked from selected package");

            insertLinkedService(ps, bookingId, "Flight", fetchTransportProvider(conn, packageId, "flight"),
                    travelers, "Auto-linked by system");
            insertLinkedService(ps, bookingId, "Train", fetchTransportProvider(conn, packageId, "train"),
                    travelers, "Auto-linked by system");
            insertLinkedService(ps, bookingId, "Bus", fetchTransportProvider(conn, packageId, "bus"),
                    travelers, "Auto-linked by system");
            insertLinkedService(ps, bookingId, "Cab", fetchTransportProvider(conn, packageId, "cab"),
                    travelers, "Auto-linked by system");
        }
    }

    private void insertLinkedService(PreparedStatement ps, int bookingId, String serviceType, String provider,
                                     int travelers, String notes) throws SQLException {
        ps.setInt(1, bookingId);
        ps.setString(2, serviceType);
        ps.setString(3, provider);
        ps.setInt(4, travelers);
        ps.setString(5, notes);
        ps.executeUpdate();
    }

    private String fetchHotelForPackage(Connection conn, int packageId) throws SQLException {
        String sql = """
                SELECT h.name
                FROM package_details pd
                JOIN hotels h ON h.hotel_id = pd.hotel_id
                WHERE pd.package_id = ?
                ORDER BY pd.detail_id
                LIMIT 1
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, packageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        }

        return "Hotel to be assigned";
    }

    private String fetchTransportProvider(Connection conn, int packageId, String typeKeyword) throws SQLException {
        String packageLinkedSql = """
                SELECT t.provider
                FROM package_details pd
                JOIN transport t ON t.transport_id = pd.transport_id
                WHERE pd.package_id = ?
                  AND LOWER(t.type) LIKE ?
                ORDER BY pd.detail_id
                LIMIT 1
                """;

        String fallbackSql = """
                SELECT provider
                FROM transport
                WHERE LOWER(type) LIKE ?
                ORDER BY transport_id
                LIMIT 1
                """;

        String like = "%" + typeKeyword.toLowerCase(Locale.ROOT) + "%";

        try (PreparedStatement ps = conn.prepareStatement(packageLinkedSql)) {
            ps.setInt(1, packageId);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("provider");
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(fallbackSql)) {
            ps.setString(1, like);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("provider");
                }
            }
        }

        String prettyType = typeKeyword.substring(0, 1).toUpperCase(Locale.ROOT) + typeKeyword.substring(1).toLowerCase(Locale.ROOT);
        return prettyType + " to be assigned";
    }

    private void ensureBookingDetailColumns(Connection conn) throws SQLException {
        String[] statements = {
                "ALTER TABLE bookings ADD COLUMN IF NOT EXISTS traveler_name VARCHAR(80)",
                "ALTER TABLE bookings ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(15)",
                "ALTER TABLE bookings ADD COLUMN IF NOT EXISTS special_request VARCHAR(250)"
        };
        String[] fallbackStatements = {
                "ALTER TABLE bookings ADD COLUMN traveler_name VARCHAR(80)",
                "ALTER TABLE bookings ADD COLUMN contact_phone VARCHAR(15)",
                "ALTER TABLE bookings ADD COLUMN special_request VARCHAR(250)"
        };

        try (Statement statement = conn.createStatement()) {
            for (int i = 0; i < statements.length; i++) {
                try {
                    statement.execute(statements[i]);
                } catch (SQLSyntaxErrorException ignored) {
                    try {
                        statement.execute(fallbackStatements[i]);
                    } catch (SQLSyntaxErrorException duplicateIgnored) {
                        // Ignore duplicate-column error in fallback mode.
                    }
                }
            }
        }
    }

    private void ensureUserStatusColumn(Connection conn) throws SQLException {
        String alterWithIf = "ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active TINYINT(1) NOT NULL DEFAULT 1";
        String alterFallback = "ALTER TABLE users ADD COLUMN is_active TINYINT(1) NOT NULL DEFAULT 1";

        try (Statement statement = conn.createStatement()) {
            try {
                statement.execute(alterWithIf);
            } catch (SQLSyntaxErrorException ignored) {
                try {
                    statement.execute(alterFallback);
                } catch (SQLSyntaxErrorException duplicateIgnored) {
                    // Ignore duplicate-column error in fallback mode.
                }
            }
        }
    }

    private void ensurePackageManagementColumns(Connection conn) throws SQLException {
        String[] statements = {
                "ALTER TABLE packages ADD COLUMN IF NOT EXISTS category VARCHAR(30) NOT NULL DEFAULT 'family'",
                "ALTER TABLE packages ADD COLUMN IF NOT EXISTS available_slots INT NOT NULL DEFAULT 0",
                "ALTER TABLE packages ADD COLUMN IF NOT EXISTS discount_percent DECIMAL(5,2) NOT NULL DEFAULT 0",
                "ALTER TABLE packages ADD COLUMN IF NOT EXISTS is_available TINYINT(1) NOT NULL DEFAULT 1"
        };
        String[] fallbackStatements = {
                "ALTER TABLE packages ADD COLUMN category VARCHAR(30) NOT NULL DEFAULT 'family'",
                "ALTER TABLE packages ADD COLUMN available_slots INT NOT NULL DEFAULT 0",
                "ALTER TABLE packages ADD COLUMN discount_percent DECIMAL(5,2) NOT NULL DEFAULT 0",
                "ALTER TABLE packages ADD COLUMN is_available TINYINT(1) NOT NULL DEFAULT 1"
        };

        try (Statement statement = conn.createStatement()) {
            for (int i = 0; i < statements.length; i++) {
                try {
                    statement.execute(statements[i]);
                } catch (SQLSyntaxErrorException ignored) {
                    try {
                        statement.execute(fallbackStatements[i]);
                    } catch (SQLSyntaxErrorException duplicateIgnored) {
                        // Ignore duplicate-column error in fallback mode.
                    }
                }
            }
        }
    }

    private void ensurePaymentManagementColumns(Connection conn) throws SQLException {
        String[] statements = {
                "ALTER TABLE payments ADD COLUMN IF NOT EXISTS gateway_transaction_id VARCHAR(80)",
                "ALTER TABLE payments ADD COLUMN IF NOT EXISTS refund_status VARCHAR(30) NOT NULL DEFAULT 'NotRequested'",
                "ALTER TABLE payments ADD COLUMN IF NOT EXISTS refund_amount DECIMAL(10,2)",
                "ALTER TABLE payments ADD COLUMN IF NOT EXISTS refund_reason VARCHAR(255)",
                "ALTER TABLE payments ADD COLUMN IF NOT EXISTS refunded_at TIMESTAMP NULL"
        };
        String[] fallbackStatements = {
                "ALTER TABLE payments ADD COLUMN gateway_transaction_id VARCHAR(80)",
                "ALTER TABLE payments ADD COLUMN refund_status VARCHAR(30) NOT NULL DEFAULT 'NotRequested'",
                "ALTER TABLE payments ADD COLUMN refund_amount DECIMAL(10,2)",
                "ALTER TABLE payments ADD COLUMN refund_reason VARCHAR(255)",
                "ALTER TABLE payments ADD COLUMN refunded_at TIMESTAMP NULL"
        };

        try (Statement statement = conn.createStatement()) {
            for (int i = 0; i < statements.length; i++) {
                try {
                    statement.execute(statements[i]);
                } catch (SQLSyntaxErrorException ignored) {
                    try {
                        statement.execute(fallbackStatements[i]);
                    } catch (SQLSyntaxErrorException duplicateIgnored) {
                        // Ignore duplicate-column error in fallback mode.
                    }
                }
            }
        }
    }

    private int runDelete(Connection conn, String sql, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate();
        }
    }

    private Map<String, Object> paymentFromResult(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("paymentId", rs.getInt("payment_id"));
        row.put("bookingId", rs.getInt("booking_id"));
        row.put("amount", rs.getBigDecimal("amount"));
        row.put("paymentMethod", rs.getString("payment_method"));
        String paymentStatus = rs.getString("payment_status");
        row.put("paymentStatus", paymentStatus);
        row.put("paymentStatusClass", paymentStatus == null ? "" : paymentStatus.toLowerCase());
        row.put("paymentDate", rs.getTimestamp("payment_date"));
        row.put("gatewayTransactionId", rs.getString("gateway_transaction_id"));
        String refundStatus = rs.getString("refund_status");
        row.put("refundStatus", refundStatus);
        row.put("refundStatusClass", refundStatus == null ? "" : refundStatus.toLowerCase(Locale.ROOT));
        row.put("refundAmount", rs.getBigDecimal("refund_amount"));
        row.put("refundReason", rs.getString("refund_reason"));
        row.put("refundedAt", rs.getTimestamp("refunded_at"));
        return row;
    }

    private Map<String, Object> reviewFromResult(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("reviewId", rs.getInt("review_id"));
        row.put("userName", rs.getString("user_name"));
        row.put("packageTitle", rs.getString("package_title"));
        row.put("rating", rs.getInt("rating"));
        row.put("comment", rs.getString("comment"));
        row.put("createdAt", rs.getTimestamp("created_at"));
        return row;
    }

    private String profileSeed(String email) {
        return email.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }
}

