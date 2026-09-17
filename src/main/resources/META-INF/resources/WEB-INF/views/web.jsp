<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle}" default="ShopNamTrung"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
    <script>var contextPath = "${pageContext.request.contextPath}";</script>
    <c:if test="${not empty pageScript}">
        <script defer src="${pageContext.request.contextPath}${pageScript}"></script>
    </c:if>
</head>
<body>
    <header class="topbar">
        <a class="brand" href="${pageContext.request.contextPath}/">ShopNamTrung</a>
    </header>
    <main class="main-content">
        <c:choose>
            <c:when test="${not empty contentPage}">
                <jsp:include page="${contentPage}" flush="true"/>
            </c:when>
            <c:otherwise>
                <div class="card">Chào mừng bạn đến với ShopNamTrung.</div>
            </c:otherwise>
        </c:choose>
    </main>
    <footer class="footer">ShopNamTrung</footer>
</body>
</html>
