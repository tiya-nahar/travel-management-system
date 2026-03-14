package com.travel.servlet;

import com.travel.dao.TravelDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/admin")
public class AdminDashboardServlet extends HttpServlet {

    private final TravelDao travelDao = new TravelDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("message", req.getParameter("msg"));

        try {
            req.setAttribute("stats", travelDao.fetchDashboardStats());
            req.setAttribute("recentBookings", travelDao.fetchRecentBookings(5));
            req.setAttribute("packages", travelDao.fetchPackages(null, null, null, null));
            req.setAttribute("destinations", travelDao.fetchDestinationsAdmin());
            req.setAttribute("experiences", travelDao.fetchExperiencesAdmin());
            req.setAttribute("bookings", travelDao.fetchBookings());
            req.setAttribute("users", travelDao.fetchAdminUsersList());
            req.setAttribute("memories", travelDao.fetchMemoriesAdmin());
            req.setAttribute("budgetRules", travelDao.fetchBudgetRulesAdmin());
            req.setAttribute("tripTags", travelDao.fetchTripTagsAdmin());
            req.setAttribute("pricingRules", travelDao.fetchPricingRulesAdmin());
            req.setAttribute("destinationInfo", travelDao.fetchDestinationInfoAdmin());
            req.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Unable to load admin dashboard", e);
        }
    }
}
