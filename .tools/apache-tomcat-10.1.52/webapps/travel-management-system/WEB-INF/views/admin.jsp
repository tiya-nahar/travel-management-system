<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>AeroTrail | Admin Console</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
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
        <a href="#payments" class="admin-link">Payments</a>
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
                <article class="panel"><h3>Total Bookings</h3><p class="price">${stats.bookings}</p></article>
                <article class="panel"><h3>Total Revenue</h3><p class="price">Rs. <fmt:formatNumber value="${stats.revenue}" type="number" maxFractionDigits="2" /></p></article>
                <article class="panel"><h3>Pending Payments</h3><p class="price">${stats.pendingPayments}</p></article>
            </div>

            <div class="panel" style="margin-top: 16px;">
                <h3 style="margin-bottom: 10px;">Monthly Booking Trend</h3>
                <div style="position: relative; height: 280px;">
                    <canvas id="monthlyBookingsChart"></canvas>
                </div>
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

            <form method="get" action="${pageContext.request.contextPath}/admin" class="admin-form-grid" style="margin-bottom: 12px;">
                <div class="input-wrap">
                    <label for="packageCategory">Filter By Category</label>
                    <select id="packageCategory" name="packageCategory">
                        <option value="" ${empty packageCategory ? 'selected' : ''}>All Categories</option>
                        <option value="adventure" ${packageCategory eq 'adventure' ? 'selected' : ''}>Adventure</option>
                        <option value="family" ${packageCategory eq 'family' ? 'selected' : ''}>Family</option>
                        <option value="honeymoon" ${packageCategory eq 'honeymoon' ? 'selected' : ''}>Honeymoon</option>
                    </select>
                </div>
                <div class="admin-form-actions">
                    <button type="submit" class="btn btn-primary">Apply Category Filter</button>
                    <a href="${pageContext.request.contextPath}/admin#packages" class="btn btn-ghost">Reset</a>
                </div>
            </form>

            <div class="panel" style="margin-bottom: 14px;">
                <h3 style="margin-bottom: 10px;">
                    <c:choose>
                        <c:when test="${not empty editingPackage}">Edit Package #${editingPackage.packageId}</c:when>
                        <c:otherwise>Add New Package</c:otherwise>
                    </c:choose>
                </h3>

                <form method="post" action="${pageContext.request.contextPath}/admin/packages" enctype="multipart/form-data" class="admin-form-grid">
                    <input type="hidden" name="action" value="${not empty editingPackage ? 'edit' : 'add'}" />
                    <c:if test="${not empty editingPackage}">
                        <input type="hidden" name="packageId" value="${editingPackage.packageId}" />
                        <input type="hidden" name="existingImageUrl" value="${editingPackage.mainImage}" />
                    </c:if>

                    <div class="input-wrap full">
                        <label for="pkgTitle">Title</label>
                        <input id="pkgTitle" type="text" name="title" required value="${editingPackage.title}" placeholder="Dream Goa Escape" />
                    </div>

                    <div class="input-wrap">
                        <label for="pkgDestination">Destination</label>
                        <select id="pkgDestination" name="destinationId" required>
                            <option value="">Select destination</option>
                            <c:forEach var="destination" items="${destinations}">
                                <option value="${destination.destinationId}" ${not empty editingPackage and editingPackage.destinationId eq destination.destinationId ? 'selected' : ''}>
                                    ${destination.city}, ${destination.country}
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="input-wrap">
                        <label for="pkgCategory">Category</label>
                        <select id="pkgCategory" name="category" required>
                            <option value="adventure" ${not empty editingPackage and editingPackage.category eq 'adventure' ? 'selected' : ''}>Adventure</option>
                            <option value="family" ${empty editingPackage or editingPackage.category eq 'family' ? 'selected' : ''}>Family</option>
                            <option value="honeymoon" ${not empty editingPackage and editingPackage.category eq 'honeymoon' ? 'selected' : ''}>Honeymoon</option>
                        </select>
                    </div>

                    <div class="input-wrap">
                        <label for="pkgDuration">Duration (days)</label>
                        <input id="pkgDuration" type="number" name="durationDays" min="1" required value="${not empty editingPackage ? editingPackage.durationDays : 5}" />
                    </div>

                    <div class="input-wrap">
                        <label for="pkgMaxPeople">Max People</label>
                        <input id="pkgMaxPeople" type="number" name="maxPeople" min="1" required value="${not empty editingPackage ? editingPackage.maxPeople : 4}" />
                    </div>

                    <div class="input-wrap">
                        <label for="pkgPrice">Base Price (INR)</label>
                        <input id="pkgPrice" type="number" step="0.01" name="price" min="0" required value="${not empty editingPackage ? editingPackage.price : ''}" />
                    </div>

                    <div class="input-wrap">
                        <label for="pkgAvailability">Available Slots</label>
                        <input id="pkgAvailability" type="number" name="availableSlots" min="0" required value="${not empty editingPackage ? editingPackage.availableSlots : 0}" />
                    </div>

                    <div class="input-wrap">
                        <label for="pkgDiscount">Discount %</label>
                        <input id="pkgDiscount" type="number" step="0.01" name="discountPercent" min="0" max="90" value="${not empty editingPackage ? editingPackage.discountPercent : 0}" />
                    </div>

                    <div class="input-wrap full">
                        <label for="pkgDescription">Description</label>
                        <textarea id="pkgDescription" name="description" rows="3" placeholder="Write package highlights...">${editingPackage.description}</textarea>
                    </div>

                    <div class="input-wrap full">
                        <label for="pkgImage">Package Image</label>
                        <input id="pkgImage" type="file" name="imageFile" accept=".jpg,.jpeg,.png,.webp" />
                        <c:if test="${not empty editingPackage.mainImage}">
                            <p class="meta" style="margin-top: 6px;">Current image is set. Upload new file to replace it.</p>
                        </c:if>
                    </div>

                    <div class="admin-form-actions full">
                        <button type="submit" class="btn btn-primary">
                            <c:choose>
                                <c:when test="${not empty editingPackage}">Update Package</c:when>
                                <c:otherwise>Add Package</c:otherwise>
                            </c:choose>
                        </button>
                        <c:if test="${not empty editingPackage}">
                            <a href="${pageContext.request.contextPath}/admin#packages" class="btn btn-ghost">Cancel Edit</a>
                        </c:if>
                    </div>
                </form>
            </div>

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
                                <th>Image</th>
                                <th>Title</th>
                                <th>Destination</th>
                                <th>Category</th>
                                <th>Duration</th>
                                <th>Availability</th>
                                <th>Price</th>
                                <th>Discount</th>
                                <th>Final Price</th>
                                <th>Actions</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="pkg" items="${packages}">
                                <tr>
                                    <td>${pkg.packageId}</td>
                                    <td>
                                        <c:if test="${not empty pkg.mainImage}">
                                            <img src="${pkg.mainImage}" alt="${pkg.title}" style="width: 72px; height: 52px; object-fit: cover; border-radius: 10px;" />
                                        </c:if>
                                    </td>
                                    <td>${pkg.title}</td>
                                    <td>${pkg.city}, ${pkg.country}</td>
                                    <td>${pkg.category}</td>
                                    <td>${pkg.durationDays} days</td>
                                    <td>${pkg.availableSlots}</td>
                                    <td>Rs. <fmt:formatNumber value="${pkg.price}" type="number" /></td>
                                    <td>${pkg.discountPercent}%</td>
                                    <td>Rs. <fmt:formatNumber value="${pkg.finalPrice}" type="number" maxFractionDigits="2" /></td>
                                    <td>
                                        <div class="admin-row-actions">
                                            <a href="${pageContext.request.contextPath}/admin?editPackageId=${pkg.packageId}#packages" class="btn btn-ghost btn-xs">Edit</a>
                                            <form method="post" action="${pageContext.request.contextPath}/admin/packages" class="inline-form" onsubmit="return confirm('Delete this package?');">
                                                <input type="hidden" name="action" value="delete" />
                                                <input type="hidden" name="packageId" value="${pkg.packageId}" />
                                                <button type="submit" class="btn btn-danger btn-xs">Delete</button>
                                            </form>
                                        </div>
                                    </td>
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

            <form method="get" action="${pageContext.request.contextPath}/admin" class="admin-form-grid" style="margin-bottom: 12px;">
                <div class="input-wrap">
                    <label for="bookingStartDate">Booking Start Date</label>
                    <input id="bookingStartDate" type="date" name="bookingStartDate" value="${bookingStartDate}" />
                </div>
                <div class="input-wrap">
                    <label for="bookingEndDate">Booking End Date</label>
                    <input id="bookingEndDate" type="date" name="bookingEndDate" value="${bookingEndDate}" />
                </div>
                <div class="input-wrap">
                    <label for="bookingUserId">User</label>
                    <select id="bookingUserId" name="bookingUserId">
                        <option value="">All Users</option>
                        <c:forEach var="u" items="${bookingUsers}">
                            <option value="${u.userId}" ${not empty bookingUserId and bookingUserId eq u.userId ? 'selected' : ''}>${u.name}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="input-wrap">
                    <label for="bookingPackageId">Package</label>
                    <select id="bookingPackageId" name="bookingPackageId">
                        <option value="">All Packages</option>
                        <c:forEach var="pkgOpt" items="${bookingPackages}">
                            <option value="${pkgOpt.packageId}" ${not empty bookingPackageId and bookingPackageId eq pkgOpt.packageId ? 'selected' : ''}>${pkgOpt.title}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="input-wrap">
                    <label for="bookingStatus">Status</label>
                    <select id="bookingStatus" name="bookingStatus">
                        <option value="" ${empty bookingStatus ? 'selected' : ''}>All Statuses</option>
                        <option value="Pending" ${bookingStatus eq 'Pending' ? 'selected' : ''}>Pending</option>
                        <option value="Confirmed" ${bookingStatus eq 'Confirmed' ? 'selected' : ''}>Confirmed</option>
                        <option value="Cancelled" ${bookingStatus eq 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                    </select>
                </div>
                <div class="admin-form-actions">
                    <button type="submit" class="btn btn-primary">Apply Filters</button>
                    <a href="${pageContext.request.contextPath}/admin#bookings" class="btn btn-ghost">Reset</a>
                </div>
            </form>

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
                                <th>Actions</th>
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
                                    <td>
                                        <div class="admin-row-actions">
                                            <form method="post" action="${pageContext.request.contextPath}/admin" class="inline-form">
                                                <input type="hidden" name="action" value="booking-status" />
                                                <input type="hidden" name="bookingId" value="${booking.bookingId}" />
                                                <select name="newStatus" class="status-select" onchange="this.form.submit()">
                                                    <option value="Pending" ${booking.status eq 'Pending' ? 'selected' : ''}>Pending</option>
                                                    <option value="Confirmed" ${booking.status eq 'Confirmed' ? 'selected' : ''}>Confirmed</option>
                                                    <option value="Cancelled" ${booking.status eq 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                                                </select>
                                            </form>
                                            <form method="post" action="${pageContext.request.contextPath}/admin" class="inline-form">
                                                <input type="hidden" name="action" value="booking-approve" />
                                                <input type="hidden" name="bookingId" value="${booking.bookingId}" />
                                                <button type="submit" class="btn btn-primary btn-xs">Approve</button>
                                            </form>
                                            <form method="post" action="${pageContext.request.contextPath}/admin" class="inline-form">
                                                <input type="hidden" name="action" value="booking-reject" />
                                                <input type="hidden" name="bookingId" value="${booking.bookingId}" />
                                                <button type="submit" class="btn btn-danger btn-xs">Reject</button>
                                            </form>
                                            <form method="post" action="${pageContext.request.contextPath}/admin" class="inline-form">
                                                <input type="hidden" name="action" value="booking-invoice" />
                                                <input type="hidden" name="bookingId" value="${booking.bookingId}" />
                                                <button type="submit" class="btn btn-ghost btn-xs">Invoice</button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>

            <c:if test="${not empty invoiceData}">
                <div class="panel" style="margin-top: 16px;">
                    <h3 style="margin-bottom: 10px;">Invoice Preview</h3>
                    <div class="table-wrap">
                        <table>
                            <tbody>
                            <tr><th>Invoice No</th><td>${invoiceData.invoiceNumber}</td></tr>
                            <tr><th>Booking ID</th><td>${invoiceData.bookingId}</td></tr>
                            <tr><th>User</th><td>${invoiceData.userName} (${invoiceData.userEmail})</td></tr>
                            <tr><th>Package</th><td>${invoiceData.packageTitle}</td></tr>
                            <tr><th>Booking Date</th><td>${invoiceData.bookingDate}</td></tr>
                            <tr><th>Travel Date</th><td>${invoiceData.travelDate}</td></tr>
                            <tr><th>Travellers</th><td>${invoiceData.numberOfPeople}</td></tr>
                            <tr><th>Base Price</th><td>Rs. <fmt:formatNumber value="${invoiceData.basePrice}" type="number" maxFractionDigits="2" /></td></tr>
                            <tr><th>Discount</th><td>${invoiceData.discountPercent}%</td></tr>
                            <tr><th>Total Amount</th><td>Rs. <fmt:formatNumber value="${invoiceData.amount}" type="number" maxFractionDigits="2" /></td></tr>
                            <tr><th>Payment Status</th><td>${invoiceData.paymentStatus}</td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:if>
        </section>

        <section id="payments" class="admin-section panel">
            <h2 class="section-title" style="margin-bottom: 10px;">Payment Management</h2>

            <form method="get" action="${pageContext.request.contextPath}/admin" class="admin-form-grid" style="margin-bottom: 12px;">
                <div class="input-wrap">
                    <label for="paymentStartDate">Payment Start Date</label>
                    <input id="paymentStartDate" type="date" name="paymentStartDate" value="${paymentStartDate}" />
                </div>
                <div class="input-wrap">
                    <label for="paymentEndDate">Payment End Date</label>
                    <input id="paymentEndDate" type="date" name="paymentEndDate" value="${paymentEndDate}" />
                </div>
                <div class="input-wrap">
                    <label for="paymentUserId">User</label>
                    <select id="paymentUserId" name="paymentUserId">
                        <option value="">All Users</option>
                        <c:forEach var="u" items="${bookingUsers}">
                            <option value="${u.userId}" ${not empty paymentUserId and paymentUserId eq u.userId ? 'selected' : ''}>${u.name}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="input-wrap">
                    <label for="paymentPackageId">Package</label>
                    <select id="paymentPackageId" name="paymentPackageId">
                        <option value="">All Packages</option>
                        <c:forEach var="pkgOpt" items="${bookingPackages}">
                            <option value="${pkgOpt.packageId}" ${not empty paymentPackageId and paymentPackageId eq pkgOpt.packageId ? 'selected' : ''}>${pkgOpt.title}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="input-wrap">
                    <label for="paymentStatus">Payment Status</label>
                    <select id="paymentStatus" name="paymentStatus">
                        <option value="" ${empty paymentStatus ? 'selected' : ''}>All</option>
                        <option value="Paid" ${paymentStatus eq 'Paid' ? 'selected' : ''}>Paid</option>
                        <option value="Pending" ${paymentStatus eq 'Pending' ? 'selected' : ''}>Pending</option>
                        <option value="Refunded" ${paymentStatus eq 'Refunded' ? 'selected' : ''}>Refunded</option>
                        <option value="Failed" ${paymentStatus eq 'Failed' ? 'selected' : ''}>Failed</option>
                    </select>
                </div>
                <div class="admin-form-actions full">
                    <button type="submit" class="btn btn-primary">Apply Payment Filters</button>
                    <a href="${pageContext.request.contextPath}/admin#payments" class="btn btn-ghost">Reset</a>
                </div>
            </form>

            <div class="panel" style="margin-bottom: 12px;">
                <h3 style="margin-bottom: 8px;">Gateway Simulation</h3>
                <form method="post" action="${pageContext.request.contextPath}/admin" class="admin-form-grid">
                    <input type="hidden" name="action" value="payment-simulate" />
                    <div class="input-wrap">
                        <label for="simBookingId">Booking ID</label>
                        <input id="simBookingId" type="number" name="bookingId" min="1" required />
                    </div>
                    <div class="input-wrap">
                        <label for="simMethod">Payment Method</label>
                        <input id="simMethod" type="text" name="paymentMethod" placeholder="Card / UPI / NetBanking" />
                    </div>
                    <div class="admin-form-actions">
                        <button type="submit" class="btn btn-primary">Simulate Paid Transaction</button>
                    </div>
                </form>
            </div>

            <c:choose>
                <c:when test="${empty payments}">
                    <p class="meta">No payments found.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th>Payment ID</th>
                                <th>Booking ID</th>
                                <th>User</th>
                                <th>Package</th>
                                <th>Amount</th>
                                <th>Method</th>
                                <th>Status</th>
                                <th>Gateway Txn</th>
                                <th>Refund</th>
                                <th>Paid On</th>
                                <th>Action</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="payment" items="${payments}">
                                <tr>
                                    <td>${payment.paymentId}</td>
                                    <td>${payment.bookingId}</td>
                                    <td>${payment.userName}</td>
                                    <td>${payment.packageTitle}</td>
                                    <td>Rs. <fmt:formatNumber value="${payment.amount}" type="number" maxFractionDigits="2" /></td>
                                    <td>${payment.paymentMethod}</td>
                                    <td><span class="badge ${payment.paymentStatusClass}">${payment.paymentStatus}</span></td>
                                    <td>${empty payment.gatewayTransactionId ? '-' : payment.gatewayTransactionId}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty payment.refundStatus}">
                                                <span class="badge ${payment.refundStatusClass}">${payment.refundStatus}</span>
                                                <c:if test="${not empty payment.refundAmount}">
                                                    <div class="meta">Rs. <fmt:formatNumber value="${payment.refundAmount}" type="number" maxFractionDigits="2" /></div>
                                                </c:if>
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${payment.paymentDate}</td>
                                    <td>
                                        <c:if test="${payment.paymentStatus eq 'Paid'}">
                                            <form method="post" action="${pageContext.request.contextPath}/admin" class="inline-form">
                                                <input type="hidden" name="action" value="payment-refund" />
                                                <input type="hidden" name="paymentId" value="${payment.paymentId}" />
                                                <input type="number" step="0.01" min="0" max="${payment.amount}" name="refundAmount" class="status-select" placeholder="Amount" />
                                                <input type="text" name="refundReason" class="status-select" placeholder="Reason" />
                                                <button type="submit" class="btn btn-danger btn-xs">Refund</button>
                                            </form>
                                        </c:if>
                                    </td>
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

            <form method="get" action="${pageContext.request.contextPath}/admin" class="admin-form-grid" style="margin-bottom: 12px;">
                <div class="input-wrap">
                    <label for="userSearch">Search User (Name / Email)</label>
                    <input id="userSearch" type="text" name="userSearch" value="${userSearch}" placeholder="Search by name or email" />
                </div>
                <div class="admin-form-actions">
                    <button type="submit" class="btn btn-primary">Search</button>
                    <a href="${pageContext.request.contextPath}/admin#users" class="btn btn-ghost">Reset</a>
                </div>
            </form>

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
                                <th>Status</th>
                                <th>Created</th>
                                <th>Actions</th>
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
                                    <td>
                                        <span class="badge ${user.isActive ? 'confirmed' : 'pending'}">${user.statusLabel}</span>
                                    </td>
                                    <td>${user.createdAt}</td>
                                    <td>
                                        <div class="admin-row-actions">
                                            <c:if test="${user.isActive}">
                                                <form method="post" action="${pageContext.request.contextPath}/admin" class="inline-form">
                                                    <input type="hidden" name="action" value="deactivate" />
                                                    <input type="hidden" name="userId" value="${user.userId}" />
                                                    <button type="submit" class="btn btn-soft btn-xs">Deactivate</button>
                                                </form>
                                            </c:if>
                                            <c:if test="${not user.isActive}">
                                                <form method="post" action="${pageContext.request.contextPath}/admin" class="inline-form">
                                                    <input type="hidden" name="action" value="activate" />
                                                    <input type="hidden" name="userId" value="${user.userId}" />
                                                    <button type="submit" class="btn btn-primary btn-xs">Activate</button>
                                                </form>
                                            </c:if>
                                            <form method="post" action="${pageContext.request.contextPath}/admin" class="inline-form">
                                                <input type="hidden" name="action" value="history" />
                                                <input type="hidden" name="userId" value="${user.userId}" />
                                                <button type="submit" class="btn btn-ghost btn-xs">History</button>
                                            </form>
                                            <form method="post" action="${pageContext.request.contextPath}/admin" class="inline-form" onsubmit="return confirm('Delete this user and all linked records?');">
                                                <input type="hidden" name="action" value="delete" />
                                                <input type="hidden" name="userId" value="${user.userId}" />
                                                <button type="submit" class="btn btn-danger btn-xs">Delete</button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>

            <c:if test="${not empty historyUserId}">
                <div class="panel" style="margin-top: 16px;">
                    <h3 style="margin-bottom: 10px;">Booking History for User #${historyUserId}</h3>
                    <c:choose>
                        <c:when test="${empty userBookingHistory}">
                            <p class="meta">No bookings found for selected user.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="table-wrap">
                                <table>
                                    <thead>
                                    <tr>
                                        <th>Booking ID</th>
                                        <th>Package</th>
                                        <th>Booking Date</th>
                                        <th>Travel Date</th>
                                        <th>People</th>
                                        <th>Status</th>
                                        <th>Total Amount</th>
                                        <th>Payment Status</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach var="history" items="${userBookingHistory}">
                                        <tr>
                                            <td>${history.bookingId}</td>
                                            <td>${history.packageTitle}</td>
                                            <td>${history.bookingDate}</td>
                                            <td>${history.travelDate}</td>
                                            <td>${history.numberOfPeople}</td>
                                            <td><span class="badge ${history.statusClass}">${history.status}</span></td>
                                            <td>Rs. <fmt:formatNumber value="${history.totalAmount}" type="number" maxFractionDigits="2" /></td>
                                            <td><span class="badge ${history.paymentStatusClass}">${history.paymentStatus}</span></td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:if>
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

<script>
    (function () {
        var labels = [
            <c:forEach var="row" items="${monthlyBookings}" varStatus="loop">
            "${row.monthLabel}"<c:if test="${not loop.last}">,</c:if>
            </c:forEach>
        ];

        var values = [
            <c:forEach var="row" items="${monthlyBookings}" varStatus="loop">
            ${row.bookingCount}<c:if test="${not loop.last}">,</c:if>
            </c:forEach>
        ];

        var ctx = document.getElementById('monthlyBookingsChart');
        if (!ctx) {
            return;
        }

        if (!labels.length) {
            labels = ['No Data'];
            values = [0];
        }

        new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Bookings',
                    data: values,
                    borderColor: '#0b5fff',
                    backgroundColor: 'rgba(11, 95, 255, 0.14)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.25,
                    pointRadius: 3,
                    pointHoverRadius: 5
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        }
                    }
                }
            }
        });
    })();
</script>
</body>
</html>
