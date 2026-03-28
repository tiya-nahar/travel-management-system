package com.travel.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
                WHERE LOWER(email) = ? AND password = ? AND role = 'Customer'
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

    public void createBookingWithPayment(int userId, int packageId, Date travelDate, int people,
                                         String travelerName, String contactPhone, String specialRequest,
                                         String status, String paymentMethod) throws SQLException {
        String packageSql = "SELECT price FROM packages WHERE package_id = ?";
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

                BigDecimal price;
                try (PreparedStatement ps = conn.prepareStatement(packageSql)) {
                    ps.setInt(1, packageId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Package not found");
                        }
                        price = rs.getBigDecimal("price");
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
                SELECT payment_id, booking_id, amount, payment_method, payment_status, payment_date
                FROM payments
                ORDER BY payment_id DESC
                """;

        return fetchPayments(sql, null);
    }

    public List<Map<String, Object>> fetchPaymentsForUser(int userId) throws SQLException {
        String sql = """
                SELECT pay.payment_id, pay.booking_id, pay.amount, pay.payment_method,
                       pay.payment_status, pay.payment_date
                FROM payments pay
                JOIN bookings b ON b.booking_id = pay.booking_id
                WHERE b.user_id = ?
                ORDER BY pay.payment_id DESC
                """;

        return fetchPayments(sql, userId);
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
        String sql = """
                SELECT user_id, name, email, phone, role, created_at
                FROM users
                ORDER BY user_id DESC
                """;

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("userId", rs.getInt("user_id"));
                row.put("name", rs.getString("name"));
                row.put("email", rs.getString("email"));
                row.put("phone", rs.getString("phone"));
                row.put("role", rs.getString("role"));
                row.put("createdAt", rs.getTimestamp("created_at"));
                rows.add(row);
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
