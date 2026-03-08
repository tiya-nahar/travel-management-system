package com.travel.servlet;

import com.travel.dao.TravelDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@WebServlet("/reviews/add")
public class AddReviewServlet extends HttpServlet {

    private final TravelDao travelDao = new TravelDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int userId = Integer.parseInt(req.getParameter("userId"));
            int packageId = Integer.parseInt(req.getParameter("packageId"));
            int rating = Integer.parseInt(req.getParameter("rating"));
            String comment = req.getParameter("comment");

            travelDao.addReview(userId, packageId, rating, comment);
            String msg = URLEncoder.encode("Review added", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/reviews?msg=" + msg);
        } catch (IllegalArgumentException | SQLException e) {
            String msg = URLEncoder.encode("Review failed: " + e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/reviews?msg=" + msg);
        }
    }
}
