<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle}" default="Trang quản trị"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
    <script>var contextPath = "${pageContext.request.contextPath}";</script>
    <c:if test="${not empty pageScript}">
        <script defer src="${pageContext.request.contextPath}${pageScript}"></script>
    </c:if>
</head>
<body>
    <%@ include file="common/header.jspf" %>
    <jsp:include page="${contentPage}" flush="true"/>
    <%@ include file="common/footer.jspf" %>
</body>
</html>
