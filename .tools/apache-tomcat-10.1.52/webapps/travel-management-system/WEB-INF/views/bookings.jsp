<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Bookings" />
<%@ include file="fragments/header.jspf" %>

<div class="page-head">
    <div>
        <h1>Bookings</h1>
        <p>Table: bookings (joined with users/packages)</p>
    </div>
    <a class="btn btn-soft" href="${pageContext.request.contextPath}/packages#book">Create New Booking</a>
</div>

<c:if test="${not empty message}">
    <div class="panel" style="margin-bottom:12px;">${message}</div>
</c:if>

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
        <c:forEach var="booking" items="${bookings}">
            <tr>
                <td>${booking.bookingId}</td>
                <td>${booking.userName}</td>
                <td>${booking.packageTitle}</td>
                <td>${booking.bookingDate}</td>
                <td>${booking.travelDate}</td>
                <td>${booking.numberOfPeople}</td>
                <td><span class="badge ${booking.statusClass}">${booking.status}</span></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<%@ include file="fragments/footer.jspf" %>
