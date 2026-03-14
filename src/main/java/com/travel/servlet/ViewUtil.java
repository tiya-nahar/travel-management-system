package com.travel.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

public final class ViewUtil {

    private ViewUtil() {
    }

    public static void setCommon(HttpServletRequest request, String activePage) {
        request.setAttribute("activePage", activePage);
        request.setAttribute("currentUser", currentCustomer(request));
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> currentCustomer(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object value = session.getAttribute(SessionKeys.CUSTOMER_USER);
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }

        return null;
    }

    public static Integer currentCustomerId(HttpServletRequest request) {
        Map<String, Object> user = currentCustomer(request);
        if (user == null) {
            return null;
        }

        Object userId = user.get("userId");
        if (userId instanceof Number number) {
            return number.intValue();
        }

        return null;
    }
}
