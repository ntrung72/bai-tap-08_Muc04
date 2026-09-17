<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý người dùng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body>
<%@ include file="../../common/header.jspf" %>
<div class="page-heading">
    <div><h1>Quản lý người dùng</h1><p>Thêm, sửa, xóa và tìm kiếm tài khoản.</p></div>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/users/new">+ Thêm người dùng</a>
</div>
<c:if test="${not empty success}"><div class="alert success"><c:out value="${success}"/></div></c:if>
<c:if test="${not empty error}"><div class="alert error"><c:out value="${error}"/></div></c:if>
<form class="search-box" method="get" action="${pageContext.request.contextPath}/admin/users">
    <input type="search" name="keyword" value="<c:out value='${keyword}'/>" placeholder="Tên đăng nhập, họ tên, email, số điện thoại...">
    <button class="btn btn-primary" type="submit">Tìm kiếm</button>
    <c:if test="${not empty keyword}"><a class="btn btn-light" href="${pageContext.request.contextPath}/admin/users">Xóa lọc</a></c:if>
</form>
<div class="card table-wrap wide-table">
    <table>
        <thead><tr><th>ID</th><th>Tên đăng nhập</th><th>Họ tên</th><th>Email</th><th>Điện thoại</th><th>Vai trò</th><th>Ngày tạo</th><th class="actions-col">Hành động</th></tr></thead>
        <tbody>
        <c:forEach items="${users}" var="user">
            <tr>
                <td><c:out value="${user.id}"/></td>
                <td><strong><c:out value="${user.userName}"/></strong></td>
                <td><c:out value="${user.fullName}"/></td>
                <td><c:out value="${user.email}"/></td>
                <td><c:out value="${user.phone}"/></td>
                <td>
                    <c:choose>
                        <c:when test="${user.roleid eq 1}"><span class="role admin">Admin</span></c:when>
                        <c:when test="${user.roleid eq 2}"><span class="role manager">Manager</span></c:when>
                        <c:otherwise><span class="role user">User</span></c:otherwise>
                    </c:choose>
                </td>
                <td><c:out value="${user.createdDate}"/></td>
                <td>
                    <div class="action-group">
                        <a class="btn btn-warning" href="${pageContext.request.contextPath}/admin/users/${user.id}/edit">Sửa</a>
                        <form method="post" action="${pageContext.request.contextPath}/admin/users/${user.id}/delete" onsubmit="return confirm('Bạn có chắc muốn xóa người dùng này?');">
                            <button class="btn btn-danger" type="submit">Xóa</button>
                        </form>
                    </div>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty users}"><tr><td colspan="8" class="empty">Không tìm thấy người dùng phù hợp.</td></tr></c:if>
        </tbody>
    </table>
</div>
<%@ include file="../../common/footer.jspf" %>
</body>
</html>
