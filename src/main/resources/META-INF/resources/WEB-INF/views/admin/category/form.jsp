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
<div class="page-heading"><div><h1><c:out value="${formTitle}"/></h1><p>Nhập đầy đủ thông tin bên dưới.</p></div></div>
<div class="card form-card">
    <form:form method="post" action="${pageContext.request.contextPath}/admin/categories/save"
               modelAttribute="category" enctype="multipart/form-data">
        <form:hidden path="id"/>
        <form:hidden path="icon"/>
        <div class="form-group">
            <label for="name">Tên danh mục <span>*</span></label>
            <form:input path="name" id="name" maxlength="100" placeholder="Ví dụ: Điện thoại"/>
            <form:errors path="name" cssClass="field-error"/>
        </div>
        <div class="form-group">
            <label for="imageFile">Chọn ảnh danh mục</label>
            <input type="file" id="imageFile" name="imageFile"
                   accept="image/jpeg,image/png,image/gif,image/webp">
            <small>Khi cập nhật, để trống nếu muốn giữ ảnh hiện tại.</small>
        </div>
        <div class="form-actions">
            <button class="btn btn-primary" type="submit">Lưu thông tin</button>
            <a class="btn btn-light" href="${pageContext.request.contextPath}/admin/categories">Hủy</a>
        </div>
    </form:form>
</div>
<%@ include file="../../common/footer.jspf" %>
</body>
</html>
