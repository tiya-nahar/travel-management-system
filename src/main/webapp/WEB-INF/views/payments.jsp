<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Payments" />
<%@ include file="fragments/header.jspf" %>

<div class="page-head">
    <div>
        <h1>Payments</h1>
        <p>View completed, pending, and failed transactions in one view.</p>
    </div>
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
        <c:choose>
            <c:when test="${empty payments}">
                <tr>
                    <td colspan="6">You do not have any payments yet.</td>
                </tr>
            </c:when>
            <c:otherwise>
                <c:forEach var="p" items="${payments}">
                    <tr>
                        <td>${p.paymentId}</td>
                        <td>${p.bookingId}</td>
                        <td>Rs. <fmt:formatNumber value="${p.amount}" type="number" /></td>
                        <td>${p.paymentMethod}</td>
                        <td><span class="badge ${p.paymentStatusClass}">${p.paymentStatus}</span></td>
                        <td>${p.paymentDate}</td>
                    </tr>
                </c:forEach>
            </c:otherwise>
        </c:choose>
        </tbody>
    </table>
</div>

<%@ include file="fragments/footer.jspf" %>
