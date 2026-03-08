package com.travel.servlet;

import com.travel.dao.TravelDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/packages")
public class PackagesServlet extends HttpServlet {

    private final TravelDao travelDao = new TravelDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ViewUtil.setCommon(req, "packages");

        String search = req.getParameter("search");
        String city = req.getParameter("city");
        String durationRaw = req.getParameter("duration");
        String sort = req.getParameter("sort");

        Integer duration = null;
        if (durationRaw != null && !durationRaw.isBlank()) {
            try {
                duration = Integer.parseInt(durationRaw);
            } catch (NumberFormatException ignored) {
                duration = null;
            }
        }

        req.setAttribute("search", search == null ? "" : search);
        req.setAttribute("city", city == null ? "" : city);
        req.setAttribute("duration", durationRaw == null ? "" : durationRaw);
        req.setAttribute("sort", sort == null ? "" : sort);

        try {
            req.setAttribute("packages", travelDao.fetchPackages(search, city, duration, sort));
            req.setAttribute("cities", travelDao.fetchCities());
            req.setAttribute("users", travelDao.fetchCustomers());
            req.setAttribute("packageOptions", travelDao.fetchPackageOptions());
            req.getRequestDispatcher("/WEB-INF/views/packages.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Unable to load packages", e);
        }
    }
}
