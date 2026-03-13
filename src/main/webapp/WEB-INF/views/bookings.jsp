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
            <th>User</th>
            <th>Package</th>
            <th>Booking Date</th>
            <th>Travel Date</th>
            <th>People</th>
            <th>Status</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="b" items="${bookings}">
            <tr>
                <td>${b.bookingId}</td>
                <td>${b.userName}</td>
                <td>${b.packageTitle}</td>
                <td>${b.bookingDate}</td>
                <td>${b.travelDate}</td>
                <td>${b.numberOfPeople}</td>
                <td><span class="badge ${b.statusClass}">${b.status}</span></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<%@ include file="fragments/footer.jspf" %>
