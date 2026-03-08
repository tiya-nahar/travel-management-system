package com.travel.servlet;

import jakarta.servlet.http.HttpServletRequest;

public final class ViewUtil {

    private ViewUtil() {
    }

    public static void setCommon(HttpServletRequest request, String activePage) {
        request.setAttribute("activePage", activePage);
    }
}
