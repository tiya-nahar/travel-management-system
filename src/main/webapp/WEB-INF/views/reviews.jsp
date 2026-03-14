<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Reviews" />
<%@ include file="fragments/header.jspf" %>

<div class="page-head">
    <div>
        <h1>Traveler Reviews</h1>
        <p>Share what you loved and help others plan smarter trips.</p>
    </div>
</div>

<c:if test="${not empty message}">
    <div class="panel" style="margin-bottom:12px;">${message}</div>
</c:if>

<c:choose>
    <c:when test="${empty currentUser}">
        <div class="panel" style="margin-bottom:12px;">
            <p class="meta">Sign in to submit your own review.</p>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/login?next=%2Freviews">User Login</a>
        </div>
    </c:when>
    <c:when test="${empty packageOptions}">
        <div class="panel" style="margin-bottom:12px;">
            <p class="meta">No packages available yet, so reviews are currently disabled.</p>
        </div>
    </c:when>
    <c:otherwise>
        <form action="${pageContext.request.contextPath}/reviews/add" method="post" class="review-form panel" style="padding: 12px; margin-bottom: 12px;">
            <select name="packageId" required>
                <option value="">Select Package</option>
                <c:forEach var="pkg" items="${packageOptions}">
                    <option value="${pkg.packageId}">${pkg.title}</option>
                </c:forEach>
            </select>
            <select name="rating">
                <option value="5">5 Star</option>
                <option value="4">4 Star</option>
                <option value="3">3 Star</option>
                <option value="2">2 Star</option>
                <option value="1">1 Star</option>
            </select>
            <input name="comment" type="text" maxlength="180" placeholder="Write comment" required />
            <button class="btn btn-primary" type="submit">Add Review</button>
        </form>
    </c:otherwise>
</c:choose>

<div class="review-list">
    <c:choose>
        <c:when test="${empty reviews}">
            <div class="panel">No reviews yet.</div>
        </c:when>
        <c:otherwise>
            <c:forEach var="r" items="${reviews}">
                <article class="review-item">
                    <p><strong>${r.userName}</strong> on <strong>${r.packageTitle}</strong></p>
                    <p>Rating: ${r.rating}/5</p>
                    <p>${r.comment}</p>
                </article>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="fragments/footer.jspf" %>
