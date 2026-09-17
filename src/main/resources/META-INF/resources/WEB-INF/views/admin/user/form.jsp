<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${formTitle}"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body>
<%@ include file="../../common/header.jspf" %>
<div class="page-heading"><div><h1><c:out value="${formTitle}"/></h1><p>Nhập đầy đủ thông tin tài khoản bên dưới.</p></div></div>
<div class="card form-card user-form-card">
    <form:form method="post" action="${pageContext.request.contextPath}/admin/users/save" modelAttribute="user">
        <form:hidden path="id"/>
        <form:errors path="*" cssClass="alert error block-error"/>
        <div class="form-grid">
            <div class="form-group">
                <label for="userName">Tên đăng nhập <span>*</span></label>
                <form:input path="userName" id="userName" maxlength="50" autocomplete="username"/>
                <form:errors path="userName" cssClass="field-error"/>
            </div>
            <div class="form-group">
                <label for="fullName">Họ và tên <span>*</span></label>
                <form:input path="fullName" id="fullName" maxlength="100"/>
                <form:errors path="fullName" cssClass="field-error"/>
            </div>
            <div class="form-group">
                <label for="email">Email <span>*</span></label>
                <form:input path="email" id="email" type="email" maxlength="100"/>
                <form:errors path="email" cssClass="field-error"/>
            </div>
            <div class="form-group">
                <label for="phone">Số điện thoại <span>*</span></label>
                <form:input path="phone" id="phone" maxlength="11" inputmode="numeric"/>
                <form:errors path="phone" cssClass="field-error"/>
            </div>
            <div class="form-group">
                <label for="passWord">Mật khẩu <c:if test="${empty user.id}"><span>*</span></c:if></label>
                <form:password path="passWord" id="passWord" maxlength="72" autocomplete="new-password"/>
                <c:if test="${not empty user.id}"><small>Để trống nếu không muốn đổi mật khẩu.</small></c:if>
                <form:errors path="passWord" cssClass="field-error"/>
            </div>
            <div class="form-group">
                <label for="roleid">Vai trò <span>*</span></label>
                <form:select path="roleid" id="roleid">
                    <form:option value="1">Admin</form:option>
                    <form:option value="2">Manager</form:option>
                    <form:option value="3">User</form:option>
                </form:select>
                <form:errors path="roleid" cssClass="field-error"/>
            </div>
            <div class="form-group">
                <label for="avatar">Ảnh đại diện/đường dẫn ảnh</label>
                <form:input path="avatar" id="avatar" maxlength="255"/>
                <form:errors path="avatar" cssClass="field-error"/>
            </div>
            <div class="form-group">
                <label for="createdDate">Ngày tạo</label>
                <form:input path="createdDate" id="createdDate" type="date"/>
                <form:errors path="createdDate" cssClass="field-error"/>
            </div>
        </div>
        <div class="form-actions">
            <button class="btn btn-primary" type="submit">Lưu thông tin</button>
            <a class="btn btn-light" href="${pageContext.request.contextPath}/admin/users">Hủy</a>
        </div>
    </form:form>
</div>
<%@ include file="../../common/footer.jspf" %>
</body>
</html>
