<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Travel Hub" />
<%@ include file="fragments/header.jspf" %>

<div class="page-head">
    <div>
        <h1>Travel Hub</h1>
        <p>Directory of travelers, destinations, hotels, and transport partners.</p>
    </div>
</div>

<div class="hub-grid">
    <article class="hub-card">
        <h3>Travelers</h3>
        <ul>
            <c:forEach var="u" items="${users}">
                <li>${u.name} | ${u.role}</li>
            </c:forEach>
        </ul>
    </article>
    <article class="hub-card">
        <h3>Destinations</h3>
        <ul>
            <c:forEach var="d" items="${destinations}">
                <li style="display: flex; align-items: center; gap: 10px;">
                    <c:if test="${not empty d.image_url}">
                        <img src="${d.image_url}" alt="${d.city}" style="width: 52px; height: 52px; object-fit: cover; border-radius: 12px;" />
                    </c:if>
                    <span>${d.city}, ${d.country}</span>
                </li>
            </c:forEach>
        </ul>
    </article>
    <article class="hub-card">
        <h3>Hotels</h3>
        <ul>
            <c:forEach var="h" items="${hotels}">
                <li>${h.name} | ${h.city} (${h.rating})</li>
            </c:forEach>
        </ul>
    </article>
    <article class="hub-card">
        <h3>Transport</h3>
        <ul>
            <c:forEach var="t" items="${transport}">
                <li>${t.type} | ${t.provider} | ${t.seat_capacity}</li>
            </c:forEach>
        </ul>
    </article>
</div>

<%@ include file="fragments/footer.jspf" %>
