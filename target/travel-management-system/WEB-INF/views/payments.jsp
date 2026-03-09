<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Payments" />
<%@ include file="fragments/header.jspf" %>

<div class="page-head">
    <div>
        <h1>Payments</h1>
        <p>Table: payments with booking references</p>
    </div>
    <a class="btn btn-soft" href="${pageContext.request.contextPath}/bookings">View Bookings</a>
</div>

<div class="table-wrap">
    <table>
        <thead>
        <tr>
            <th>ID</th>
            <th>Booking ID</th>
            <th>Amount</th>
            <th>Method</th>
            <th>Status</th>
            <th>Date</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="payment" items="${payments}">
            <tr>
                <td>${payment.paymentId}</td>
                <td>${payment.bookingId}</td>
                <td>Rs. <fmt:formatNumber value="${payment.amount}" type="number" /></td>
                <td>${payment.paymentMethod}</td>
                <td><span class="badge ${payment.paymentStatusClass}">${payment.paymentStatus}</span></td>
                <td>${payment.paymentDate}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<%@ include file="fragments/footer.jspf" %>
