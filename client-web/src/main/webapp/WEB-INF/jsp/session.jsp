<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Chi tiết ca thi</title>
    <style>
        body { font-family: "Segoe UI", sans-serif; margin: 24px; background: #f5f7fb; color: #1f2937; }
        .card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08); margin-bottom: 20px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { border-bottom: 1px solid #e5e7eb; padding: 10px; text-align: left; }
        a { color: #0f766e; text-decoration: none; }
    </style>
</head>
<body>
<div class="card">
    <p><a href="${pageContext.request.contextPath}/assignments/${assignmentId}">Quay lại chi tiết</a></p>
    <h1>Chi tiết ca ${session.sessionNo}</h1>
</div>

<div class="card">
    <h2>Phân công giám thị</h2>
    <table>
        <thead>
        <tr>
            <th>Phòng</th>
            <th>Giám thị 1</th>
            <th>Giám thị 2</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${session.roomAssignments}" var="assignment">
            <tr>
                <td>${assignment.room.roomName}</td>
                <td>${assignment.invigilatorOne.staffCode} - ${assignment.invigilatorOne.fullName}</td>
                <td>${assignment.invigilatorTwo.staffCode} - ${assignment.invigilatorTwo.fullName}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<div class="card">
    <h2>Giám sát hành lang</h2>
    <table>
        <thead>
        <tr>
            <th>Mã cán bộ</th>
            <th>Họ tên</th>
            <th>Dải phòng</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${session.hallMonitorAssignments}" var="monitor">
            <tr>
                <td>${monitor.staff.staffCode}</td>
                <td>${monitor.staff.fullName}</td>
                <td>${monitor.rangeText}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
</body>
</html>
