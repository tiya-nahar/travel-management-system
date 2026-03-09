<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Data Hub" />
<%@ include file="fragments/header.jspf" %>

<div class="page-head">
    <div>
        <h1>Data Hub</h1>
        <p>Tables: users, destinations, hotels, transport</p>
    </div>
</div>

<div class="hub-grid">
    <article class="hub-card">
        <h3>Users</h3>
        <ul>
            <c:forEach var="user" items="${users}">
                <li>${user.name} | ${user.role}</li>
            </c:forEach>
        </ul>
    </article>

    <article class="hub-card">
        <h3>Destinations</h3>
        <ul>
            <c:forEach var="destination" items="${destinations}">
                <li>${destination.city}, ${destination.country}</li>
            </c:forEach>
        </ul>
    </article>

    <article class="hub-card">
        <h3>Hotels</h3>
        <ul>
            <c:forEach var="hotel" items="${hotels}">
                <li>${hotel.name} | ${hotel.city} (${hotel.rating})</li>
            </c:forEach>
        </ul>
    </article>

    <article class="hub-card">
        <h3>Transport</h3>
        <ul>
            <c:forEach var="item" items="${transport}">
                <li>${item.type} | ${item.provider} | ${item.seat_capacity}</li>
            </c:forEach>
        </ul>
    </article>
</div>

<%@ include file="fragments/footer.jspf" %>
