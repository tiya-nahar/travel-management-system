<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Reviews" />
<%@ include file="fragments/header.jspf" %>

<div class="page-head">
    <div>
        <h1>Reviews</h1>
        <p>Table: reviews linked with users and packages</p>
    </div>
</div>

<c:if test="${not empty message}">
    <div class="panel" style="margin-bottom:12px;">${message}</div>
</c:if>

<form action="${pageContext.request.contextPath}/reviews/add" method="post" class="review-form panel" style="padding: 12px; margin-bottom: 12px;">
    <select name="userId" required>
        <option value="">Select User</option>
        <c:forEach var="user" items="${users}">
            <option value="${user.userId}">${user.name}</option>
        </c:forEach>
    </select>
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

<div class="review-list">
    <c:forEach var="review" items="${reviews}">
        <article class="review-item">
            <p><strong>${review.userName}</strong> on <strong>${review.packageTitle}</strong></p>
            <p>Rating: ${review.rating}/5</p>
            <p>${review.comment}</p>
        </article>
    </c:forEach>
</div>

<%@ include file="fragments/footer.jspf" %>
