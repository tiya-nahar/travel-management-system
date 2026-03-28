package com.travel.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.travel.dao.TravelDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final TravelDao travelDao = new TravelDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ViewUtil.setCommon(req, "dashboard");
        req.setAttribute("message", req.getParameter("msg"));
        try {
            req.setAttribute("stats", travelDao.fetchDashboardStats());
            req.setAttribute("topPackages", travelDao.fetchTopPackages(3));
            req.setAttribute("holidayPackages", travelDao.fetchTopPackages(6));
            req.setAttribute("latestReviews", travelDao.fetchLatestReviews(3));
            req.setAttribute("cities", travelDao.fetchCities());
            req.setAttribute("hotelOptions", travelDao.fetchHotelsHub());

            List<Map<String, Object>> transportOptions = travelDao.fetchTransportHub();
            List<Map<String, Object>> flightOptions = filterTransport(transportOptions, "flight", "air");
            List<Map<String, Object>> trainOptions = filterTransport(transportOptions, "train", "rail");
            List<Map<String, Object>> cabOptions = filterTransport(transportOptions, "cab", "taxi");
            List<Map<String, Object>> busOptions = filterTransport(transportOptions, "bus", "coach");

            // Keep sections populated even if dedicated train data is not seeded yet.
            if (trainOptions.isEmpty()) {
                trainOptions = busOptions;
            }

            req.setAttribute("flightOptions", flightOptions);
            req.setAttribute("trainOptions", trainOptions);
            req.setAttribute("cabOptions", cabOptions);
            req.setAttribute("busOptions", busOptions);

            req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Unable to load dashboard", e);
        }
    }

    private List<Map<String, Object>> filterTransport(List<Map<String, Object>> options, String... keywords) {
        return options.stream()
                .filter(option -> {
                    String type = String.valueOf(option.get("type")).toLowerCase();
                    for (String keyword : keywords) {
                        if (type.contains(keyword)) {
                            return true;
                        }
                    }
                    return false;
                })
                .toList();
    }
}
