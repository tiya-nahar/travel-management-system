<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Dashboard" />
<%@ include file="fragments/header.jspf" %>

<section class="hero">
    <p>Travel Management System</p>
    <h1>JSP + Servlet + JDBC + MySQL Dashboard</h1>
    <p>Real-time data from `travel_management_system` database.</p>
    <div class="hero-grid">
        <div id="liveStats" class="panel">
            <h3>Live Snapshot</h3>
            <p>Total Users: <strong>${stats.users}</strong></p>
            <p>Total Packages: <strong>${stats.packages}</strong></p>
            <p>Active Bookings: <strong>${stats.bookings}</strong></p>
            <p>Revenue (Paid): <strong>INR <fmt:formatNumber value="${stats.revenue}" type="number" /></strong></p>
        </div>
        <div class="panel">
            <h3>Fast Actions</h3>
            <p style="margin: 8px 0 12px;">Create bookings and track payments from dedicated pages.</p>
            <a class="btn btn-soft" href="${pageContext.request.contextPath}/packages">Open Packages</a>
        </div>
    </div>
</section>

<section style="margin-top: 18px;">
    <div class="page-head">
        <h2>Overview</h2>
        <p>Operational metrics</p>
    </div>
    <div id="kpiGrid" class="kpi-grid">
        <article class="panel kpi"><p>Total Destinations</p><strong>${stats.destinations}</strong></article>
        <article class="panel kpi"><p>Total Bookings</p><strong>${stats.bookings}</strong></article>
        <article class="panel kpi"><p>Pending Payments</p><strong>${stats.pendingPayments}</strong></article>
        <article class="panel kpi"><p>Total Reviews</p><strong>${stats.reviews}</strong></article>
    </div>
</section>

<section style="margin-top: 18px;">
    <div class="page-head">
        <h2>Top Packages</h2>
        <p>From packages, destination and hotel data</p>
    </div>
    <div class="package-grid">
        <c:forEach var="pkg" items="${topPackages}">
            <article class="card">
                <img src="${pkg.mainImage}" alt="${pkg.title}" />
                <div class="card-body">
                    <h3>${pkg.title}</h3>
                    <p class="meta">${pkg.city}, ${pkg.country} | ${pkg.durationDays} days</p>
                    <p class="meta">Hotel: ${pkg.hotelName} (${pkg.hotelRating} star)</p>
                    <p class="price">INR <fmt:formatNumber value="${pkg.price}" type="number" /></p>
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/packages">Book This</a>
                </div>
            </article>
        </c:forEach>
    </div>
</section>

<%@ include file="fragments/footer.jspf" %>
