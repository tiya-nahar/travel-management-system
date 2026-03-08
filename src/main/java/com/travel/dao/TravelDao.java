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

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
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
                rows.add(row);
            }
        }
        return rows;
    }

    public List<Map<String, Object>> fetchPayments() throws SQLException {
        String sql = """
                SELECT payment_id, booking_id, amount, payment_method, payment_status, payment_date
                FROM payments
                ORDER BY payment_id DESC
                """;

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("paymentId", rs.getInt("payment_id"));
                row.put("bookingId", rs.getInt("booking_id"));
                row.put("amount", rs.getBigDecimal("amount"));
                row.put("paymentMethod", rs.getString("payment_method"));
                String paymentStatus = rs.getString("payment_status");
                row.put("paymentStatus", paymentStatus);
                row.put("paymentStatusClass", paymentStatus == null ? "" : paymentStatus.toLowerCase());
                row.put("paymentDate", rs.getTimestamp("payment_date"));
                rows.add(row);
            }
        }
        return rows;
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
        return fetchSimpleRows("SELECT city, country FROM destinations ORDER BY destination_id", "city", "country");
    }

    public List<Map<String, Object>> fetchHotelsHub() throws SQLException {
        return fetchSimpleRows("SELECT name, city, rating FROM hotels ORDER BY hotel_id", "name", "city", "rating");
    }

    public List<Map<String, Object>> fetchTransportHub() throws SQLException {
        return fetchSimpleRows("SELECT type, provider, seat_capacity FROM transport ORDER BY transport_id",
                "type", "provider", "seat_capacity");
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
}
