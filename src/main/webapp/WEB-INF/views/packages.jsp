<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Packages" />
<%@ include file="fragments/header.jspf" %>

<div class="page-head">
    <div>
        <h1>Packages</h1>
        <p>Tables: destinations, packages, package_details, hotels, transport</p>
    </div>
</div>

<c:if test="${not empty param.msg}">
    <div class="panel" style="margin-bottom:12px;">${param.msg}</div>
</c:if>

<form method="get" action="${pageContext.request.contextPath}/packages" class="filters">
    <input name="search" type="search" placeholder="Search title or city" value="${search}" />
    <select name="city">
        <option value="">All Cities</option>
        <c:forEach var="cityName" items="${cities}">
            <option value="${cityName}" ${city eq cityName ? 'selected' : ''}>${cityName}</option>
        </c:forEach>
    </select>
    <select name="duration">
        <option value="">Any Duration</option>
        <option value="3" ${duration eq '3' ? 'selected' : ''}>Up to 3 Days</option>
        <option value="5" ${duration eq '5' ? 'selected' : ''}>Up to 5 Days</option>
        <option value="7" ${duration eq '7' ? 'selected' : ''}>Up to 7 Days</option>
    </select>
    <select name="sort">
        <option value="">Sort By</option>
        <option value="price_asc" ${sort eq 'price_asc' ? 'selected' : ''}>Price Low to High</option>
        <option value="price_desc" ${sort eq 'price_desc' ? 'selected' : ''}>Price High to Low</option>
        <option value="rating_desc" ${sort eq 'rating_desc' ? 'selected' : ''}>Hotel Rating</option>
    </select>
    <button class="btn btn-soft" type="submit">Apply</button>
</form>

<div class="package-grid">
    <c:choose>
        <c:when test="${empty packages}">
            <p class="panel">No packages matched current filters.</p>
        </c:when>
        <c:otherwise>
            <c:forEach var="pkg" items="${packages}">
                <article class="card">
                    <img src="${pkg.mainImage}" alt="${pkg.title}" />
                    <div class="card-body">
                        <h3>${pkg.title}</h3>
                        <p class="meta">${pkg.city}, ${pkg.country} | ${pkg.durationDays} days</p>
                        <p class="meta">Hotel: ${pkg.hotelName} (${pkg.hotelRating} star)</p>
                        <p class="price">INR <fmt:formatNumber value="${pkg.price}" type="number" /></p>
                    </div>
                </article>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</div>

<div id="book" class="panel" style="margin-top: 14px;">
    <h3 style="margin-bottom: 8px;">Create Booking</h3>
    <p style="margin-bottom: 12px; color: #607182;">Mapped: bookings + payments tables</p>
    <form action="${pageContext.request.contextPath}/bookings/create" method="post" class="booking-form">
        <select name="userId" required>
            <option value="">Select User</option>
            <c:forEach var="user" items="${users}">
                <option value="${user.userId}">${user.name}</option>
            </c:forEach>
        </select>
        <select name="packageId" required>
            <option value="">Select Package</option>
            <c:forEach var="pkg" items="${packageOptions}">
                <option value="${pkg.packageId}">${pkg.title}</option>
            </c:forEach>
        </select>
        <input name="travelDate" type="date" required />
        <input name="people" type="number" min="1" max="12" value="2" required />
        <select name="status">
            <option value="Confirmed">Confirmed</option>
            <option value="Pending">Pending</option>
            <option value="Cancelled">Cancelled</option>
        </select>
        <select name="paymentMethod">
            <option value="UPI">UPI</option>
            <option value="Card">Card</option>
            <option value="Net Banking">Net Banking</option>
            <option value="Cash">Cash</option>
        </select>
        <button class="btn btn-primary" type="submit">Save Booking + Payment</button>
    </form>
</div>

<%@ include file="fragments/footer.jspf" %>
