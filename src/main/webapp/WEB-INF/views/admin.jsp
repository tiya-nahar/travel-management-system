<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>AeroTrail | Admin Console</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;700;800&family=Space+Grotesk:wght@600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css" />
</head>
<body class="admin-body">
<header class="admin-topbar">
    <div class="brand">
        <span class="brand-mark"></span>
        <div class="brand-text">
            <div class="brand-name">AeroTrail Admin</div>
            <div class="brand-tag">Database Console</div>
        </div>
    </div>
    <div class="admin-actions">
        <a class="btn btn-ghost" href="${pageContext.request.contextPath}/dashboard">Back to Site</a>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin-logout">Logout</a>
    </div>
</header>

<div class="admin-shell">
    <aside class="admin-nav">
        <a href="#overview" class="admin-link">Overview</a>
        <a href="#packages" class="admin-link">Packages</a>
        <a href="#destinations" class="admin-link">Destinations</a>
        <a href="#experiences" class="admin-link">Experiences</a>
        <a href="#bookings" class="admin-link">Bookings</a>
        <a href="#users" class="admin-link">Users</a>
        <a href="#memories" class="admin-link">Memories</a>
        <a href="#budget" class="admin-link">Budget Rules</a>
        <a href="#trip-tags" class="admin-link">Trip Tags</a>
        <a href="#pricing" class="admin-link">Pricing</a>
        <a href="#weather" class="admin-link">Weather Info</a>
    </aside>

    <main class="admin-content">
        <section id="overview" class="admin-section">
            <div class="section-head">
                <div>
                    <h2 class="section-title">Dashboard Overview</h2>
                    <p>All cards and tables below are coming from the real database only.</p>
                </div>
            </div>

            <c:if test="${not empty message}">
                <div class="panel" style="margin-bottom: 12px;">${message}</div>
            </c:if>

            <div class="admin-grid">
                <article class="panel"><h3>Total Users</h3><p class="price">${stats.users}</p></article>
                <article class="panel"><h3>Total Packages</h3><p class="price">${stats.packages}</p></article>
                <article class="panel"><h3>Total Bookings</h3><p class="price">${stats.bookings}</p></article>
                <article class="panel"><h3>Pending Payments</h3><p class="price">${stats.pendingPayments}</p></article>
            </div>

            <div class="panel" style="margin-top: 16px;">
                <h3 style="margin-bottom: 10px;">Recent Bookings</h3>
                <c:choose>
                    <c:when test="${empty recentBookings}">
                        <p class="meta">No bookings yet.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="table-wrap">
                            <table>
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>User</th>
                                    <th>Package</th>
                                    <th>Travel Date</th>
                                    <th>Status</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="booking" items="${recentBookings}">
                                    <tr>
                                        <td>${booking.bookingId}</td>
                                        <td>${booking.userName}</td>
                                        <td>${booking.packageTitle}</td>
                                        <td>${booking.travelDate}</td>
                                        <td><span class="badge ${booking.statusClass}">${booking.status}</span></td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <section id="packages" class="admin-section panel">
            <h2 class="section-title" style="margin-bottom: 10px;">Packages</h2>
            <c:choose>
                <c:when test="${empty packages}">
                    <p class="meta">No packages in database.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Title</th>
                                <th>Destination</th>
                                <th>Duration</th>
                                <th>Hotel</th>
                                <th>Price</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="pkg" items="${packages}">
                                <tr>
                                    <td>${pkg.packageId}</td>
                                    <td>${pkg.title}</td>
                                    <td>${pkg.city}, ${pkg.country}</td>
                                    <td>${pkg.durationDays} days</td>
                                    <td>${pkg.hotelName}</td>
                                    <td>Rs. <fmt:formatNumber value="${pkg.price}" type="number" /></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <section id="destinations" class="admin-section panel">
            <h2 class="section-title" style="margin-bottom: 10px;">Destinations</h2>
            <c:choose>
                <c:when test="${empty destinations}">
                    <p class="meta">No destinations in database.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Image</th>
                                <th>City</th>
                                <th>Country</th>
                                <th>Description</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="destination" items="${destinations}">
                                <tr>
                                    <td>${destination.destinationId}</td>
                                    <td>
                                        <c:if test="${not empty destination.imageUrl}">
                                            <img src="${destination.imageUrl}" alt="${destination.city}" style="width: 72px; height: 52px; object-fit: cover; border-radius: 10px;" />
                                        </c:if>
                                    </td>
                                    <td>${destination.city}</td>
                                    <td>${destination.country}</td>
                                    <td>${destination.description}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <section id="experiences" class="admin-section panel">
            <h2 class="section-title" style="margin-bottom: 10px;">Experiences</h2>
            <c:choose>
                <c:when test="${empty experiences}">
                    <p class="meta">No experiences in database.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Title</th>
                                <th>Destination</th>
                                <th>Type</th>
                                <th>Price</th>
                                <th>Duration</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="experience" items="${experiences}">
                                <tr>
                                    <td>${experience.experienceId}</td>
                                    <td>${experience.title}</td>
                                    <td>${experience.city}</td>
                                    <td>${experience.type}</td>
                                    <td><fmt:formatNumber value="${experience.price}" type="number" /></td>
                                    <td>${experience.durationHours} hrs</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <section id="bookings" class="admin-section panel">
            <h2 class="section-title" style="margin-bottom: 10px;">Bookings</h2>
            <c:choose>
                <c:when test="${empty bookings}">
                    <p class="meta">No bookings in database.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>User</th>
                                <th>Package</th>
                                <th>Travel Date</th>
                                <th>People</th>
                                <th>Traveler</th>
                                <th>Contact</th>
                                <th>Special Request</th>
                                <th>Status</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="booking" items="${bookings}">
                                <tr>
                                    <td>${booking.bookingId}</td>
                                    <td>${booking.userName}</td>
                                    <td>${booking.packageTitle}</td>
                                    <td>${booking.travelDate}</td>
                                    <td>${booking.numberOfPeople}</td>
                                    <td>${empty booking.travelerName ? '-' : booking.travelerName}</td>
                                    <td>${empty booking.contactPhone ? '-' : booking.contactPhone}</td>
                                    <td>${empty booking.specialRequest ? '-' : booking.specialRequest}</td>
                                    <td><span class="badge ${booking.statusClass}">${booking.status}</span></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <section id="users" class="admin-section panel">
            <h2 class="section-title" style="margin-bottom: 10px;">Users</h2>
            <c:choose>
                <c:when test="${empty users}">
                    <p class="meta">No users in database.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Phone</th>
                                <th>Role</th>
                                <th>Created</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="user" items="${users}">
                                <tr>
                                    <td>${user.userId}</td>
                                    <td>${user.name}</td>
                                    <td>${user.email}</td>
                                    <td>${user.phone}</td>
                                    <td>${user.role}</td>
                                    <td>${user.createdAt}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <section id="memories" class="admin-section panel">
            <h2 class="section-title" style="margin-bottom: 10px;">Memories</h2>
            <c:choose>
                <c:when test="${empty memories}">
                    <p class="meta">No memories in database.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>User</th>
                                <th>Destination</th>
                                <th>Caption</th>
                                <th>Status</th>
                                <th>Created</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="memory" items="${memories}">
                                <tr>
                                    <td>${memory.memoryId}</td>
                                    <td>${memory.userName}</td>
                                    <td>${memory.city}</td>
                                    <td>${memory.caption}</td>
                                    <td>${memory.status}</td>
                                    <td>${memory.createdAt}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <section id="budget" class="admin-section panel">
            <h2 class="section-title" style="margin-bottom: 10px;">Budget Rules</h2>
            <c:choose>
                <c:when test="${empty budgetRules}">
                    <p class="meta">No budget rules in database.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Min Budget</th>
                                <th>Max Budget</th>
                                <th>Min Days</th>
                                <th>Max Days</th>
                                <th>Recommendation</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="rule" items="${budgetRules}">
                                <tr>
                                    <td>${rule.rule_id}</td>
                                    <td>${rule.min_budget}</td>
                                    <td>${rule.max_budget}</td>
                                    <td>${rule.min_days}</td>
                                    <td>${rule.max_days}</td>
                                    <td>${rule.recommendation}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <section id="trip-tags" class="admin-section panel">
            <h2 class="section-title" style="margin-bottom: 10px;">Trip Tags</h2>
            <c:choose>
                <c:when test="${empty tripTags}">
                    <p class="meta">No trip tags in database.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Package</th>
                                <th>Tag</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="tripTag" items="${tripTags}">
                                <tr>
                                    <td>${tripTag.tag_id}</td>
                                    <td>${tripTag.title}</td>
                                    <td>${tripTag.tag}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <section id="pricing" class="admin-section panel">
            <h2 class="section-title" style="margin-bottom: 10px;">Pricing Rules</h2>
            <c:choose>
                <c:when test="${empty pricingRules}">
                    <p class="meta">No pricing rules in database.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Label</th>
                                <th>Base Price</th>
                                <th>Per Person</th>
                                <th>Multiplier</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="priceRule" items="${pricingRules}">
                                <tr>
                                    <td>${priceRule.rule_id}</td>
                                    <td>${priceRule.label}</td>
                                    <td>${priceRule.base_price}</td>
                                    <td>${priceRule.per_person}</td>
                                    <td>${priceRule.duration_multiplier}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <section id="weather" class="admin-section panel">
            <h2 class="section-title" style="margin-bottom: 10px;">Destination Info</h2>
            <c:choose>
                <c:when test="${empty destinationInfo}">
                    <p class="meta">No destination info in database.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>City</th>
                                <th>Best Season</th>
                                <th>Climate</th>
                                <th>Highlights</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="info" items="${destinationInfo}">
                                <tr>
                                    <td>${info.info_id}</td>
                                    <td>${info.city}</td>
                                    <td>${info.best_season}</td>
                                    <td>${info.climate}</td>
                                    <td>${info.highlights}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
</div>
</body>
</html>
