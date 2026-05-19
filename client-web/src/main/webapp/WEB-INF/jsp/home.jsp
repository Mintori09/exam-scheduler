<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Phân công cán bộ coi thi</title>
    <style>
        body { font-family: "Segoe UI", sans-serif; margin: 24px; background: #f5f7fb; color: #1f2937; }
        .card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08); margin-bottom: 20px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { border-bottom: 1px solid #e5e7eb; padding: 10px; text-align: left; }
        .error { color: #b91c1c; margin-bottom: 12px; }
        .warning { color: #92400e; margin-bottom: 12px; }
        input, button { padding: 10px; margin: 6px 0; }
        button { background: #0f766e; color: #fff; border: none; border-radius: 8px; cursor: pointer; }
        a { color: #0f766e; text-decoration: none; }
    </style>
</head>
<body>
<div class="card">
    <h1>Hệ thống Phân Công Cán Bộ Coi Thi</h1>
    <c:if test="${not empty error}">
        <div class="error">${error}</div>
    </c:if>
    <c:if test="${not empty warning}">
        <div class="warning">${warning}</div>
    </c:if>
    <form action="${pageContext.request.contextPath}/assignments/create" method="post" enctype="multipart/form-data">
        <div>
            <label>Danh sách cán bộ (.xlsx)</label><br/>
            <input type="file" name="staffFile" accept=".xlsx" required />
        </div>
        <div>
            <label>Danh sách phòng thi (.xlsx)</label><br/>
            <input type="file" name="roomFile" accept=".xlsx" required />
        </div>
        <div>
            <label>Số ca thi</label><br/>
            <input type="number" name="sessionCount" min="1" required />
        </div>
        <button type="submit">Tạo phân công</button>
    </form>
</div>

<div class="card">
    <h2>Lịch sử chạy</h2>
    <table>
        <thead>
        <tr>
            <th>Thời gian</th>
            <th>Mã lần chạy</th>
            <th>Trạng thái</th>
            <th>Xuất file</th>
            <th>Tiến độ</th>
            <th>Thông điệp</th>
            <th>Ca</th>
            <th>Phòng</th>
            <th>Cán bộ</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${runs}" var="run">
            <tr>
                <td>${run.createdAt}</td>
                <td><a href="${pageContext.request.contextPath}/assignments/${run.assignmentId}">${run.assignmentId}</a></td>
                <td>${run.status}</td>
                <td>${run.outputStatus}</td>
                <td>${run.completedSessionCount}/${run.sessionCount}</td>
                <td>${run.message}</td>
                <td>${run.sessionCount}</td>
                <td>${run.roomCount}</td>
                <td>${run.staffCount}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
</body>
</html>
