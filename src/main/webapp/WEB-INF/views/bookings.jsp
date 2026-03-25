<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="My Trips" />
<%@ include file="fragments/header.jspf" %>

<div class="page-head">
    <div>
        <h1>My Trips</h1>
        <p>Track upcoming journeys, statuses, and payment confirmations.</p>
    </div>
</div>

<div class="table-wrap">
    <table>
        <thead>
        <tr>
            <th>ID</th>
            <th>Package</th>
            <th>Booking Date</th>
            <th>Travel Date</th>
            <th>People</th>
            <th>Traveler</th>
            <th>Contact</th>
            <th>Special Request</th>
            <th>Status</th>
        </tr>
        </thead>
        <tbody>
        <c:choose>
            <c:when test="${empty bookings}">
                <tr>
                    <td colspan="9">You do not have any bookings yet.</td>
                </tr>
            </c:when>
            <c:otherwise>
                <c:forEach var="b" items="${bookings}">
                    <tr>
                        <td>${b.bookingId}</td>
                        <td>${b.packageTitle}</td>
                        <td>${b.bookingDate}</td>
                        <td>${b.travelDate}</td>
                        <td>${b.numberOfPeople}</td>
                        <td>${empty b.travelerName ? '-' : b.travelerName}</td>
                        <td>${empty b.contactPhone ? '-' : b.contactPhone}</td>
                        <td>${empty b.specialRequest ? '-' : b.specialRequest}</td>
                        <td><span class="badge ${b.statusClass}">${b.status}</span></td>
                    </tr>
                </c:forEach>
            </c:otherwise>
        </c:choose>
        </tbody>
    </table>
</div>

<%@ include file="fragments/footer.jspf" %>
