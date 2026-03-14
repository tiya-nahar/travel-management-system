package com.travel.dao;

import com.travel.db.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
                       b.booking_date, b.travel_date, b.number_of_people, b.status
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
                                         String status, String paymentMethod) throws SQLException {
        String packageSql = "SELECT price FROM packages WHERE package_id = ?";
        String bookingSql = """
                INSERT INTO bookings (user_id, package_id, booking_date, travel_date, number_of_people, status)
                VALUES (?, ?, CURDATE(), ?, ?, ?)
                """;
        String paymentSql = """
                INSERT INTO payments (booking_id, amount, payment_method, payment_status, payment_date)
                VALUES (?, ?, ?, ?, NOW())
                """;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
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
                    ps.setString(5, status);
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
                       b.booking_date, b.travel_date, b.number_of_people, b.status
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
                       b.booking_date, b.travel_date, b.number_of_people, b.status
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
        String status = rs.getString("status");
        row.put("status", status);
        row.put("statusClass", status == null ? "" : status.toLowerCase());
        return row;
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
