<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Holidays" />
<%@ include file="fragments/header.jspf" %>

<section class="hero" style="grid-template-columns: 1fr;">
    <div class="hero-copy">
        <p class="eyebrow">Holidays and bundles</p>
        <h1>Curated packages with stays, transfers, and experiences.</h1>
        <p class="lead">Filter by city, duration, price, and hotel rating. Book instantly or save for later.</p>
        <div class="chip-row">
            <span class="chip">Family trips</span>
            <span class="chip">Honeymoon picks</span>
            <span class="chip">Weekend escapes</span>
        </div>
    </div>
</section>

<div class="section">
    <div class="section-head">
        <div>
            <h2 class="section-title">Find your holiday</h2>
            <p>Use filters to narrow by destination, duration, and rating.</p>
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
                            <p class="price">Rs. <fmt:formatNumber value="${pkg.price}" type="number" /></p>
                        </div>
                    </article>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<div class="action-grid">
    <div id="account" class="panel">
        <c:choose>
            <c:when test="${not empty currentUser}">
                <h3 style="margin-bottom: 8px;">Signed In Traveler</h3>
                <p class="meta" style="margin-bottom: 12px;">Bookings will be created for your current account.</p>
                <p><strong>${currentUser.name}</strong></p>
                <p class="meta">${currentUser.email}</p>
            </c:when>
            <c:otherwise>
                <h3 style="margin-bottom: 8px;">Create Account or Sign In</h3>
                <p class="meta" style="margin-bottom: 12px;">Bookings now use your own user session, so sign in before reserving a package.</p>
                <div class="admin-inline">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/login">User Login</a>
                    <a class="btn btn-soft" href="${pageContext.request.contextPath}/login?mode=register">Create Account</a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <div id="book" class="panel">
        <h3 style="margin-bottom: 8px;">Create Booking</h3>
        <c:choose>
            <c:when test="${empty currentUser}">
                <p class="meta">Sign in first to book a package with your own traveler session.</p>
            </c:when>
            <c:when test="${empty packageOptions}">
                <p class="meta">No packages are available to book right now.</p>
            </c:when>
            <c:otherwise>
                <form action="${pageContext.request.contextPath}/bookings/create" method="post" class="booking-form">
                    <select name="packageId" required>
                        <option value="">Select Package</option>
                        <c:forEach var="pkg" items="${packageOptions}">
                            <option value="${pkg.packageId}">${pkg.title}</option>
                        </c:forEach>
                    </select>
                    <input name="travelDate" type="date" required />
                    <input name="people" type="number" min="1" max="12" value="2" required />
                    <input name="status" type="hidden" value="Pending" />
                    <select name="paymentMethod">
                        <option value="UPI">UPI</option>
                        <option value="Card">Card</option>
                        <option value="Net Banking">Net Banking</option>
                        <option value="Cash">Cash</option>
                    </select>
                    <button class="btn btn-primary" type="submit">Save Booking + Payment</button>
                </form>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%@ include file="fragments/footer.jspf" %>
