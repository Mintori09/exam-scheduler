<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Chi tiết phân công</title>
    <style>
        body { font-family: "Segoe UI", sans-serif; margin: 24px; background: #f5f7fb; color: #1f2937; }
        .card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08); margin-bottom: 20px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { border-bottom: 1px solid #e5e7eb; padding: 10px; text-align: left; }
        a, .button { color: #0f766e; text-decoration: none; }
    </style>
</head>
<body>
<div class="card">
    <p><a href="${pageContext.request.contextPath}/assignments">Quay lại</a></p>
    <h1>Chi tiết lần chạy ${detail.run.assignmentId}</h1>
    <p>Trạng thái: ${detail.run.status}</p>
    <p>Xuất file: ${detail.run.outputStatus}</p>
    <c:if test="${not empty detail.run.outputError}">
        <p>Lỗi xuất file: ${detail.run.outputError}</p>
    </c:if>
    <p>Tiến độ: ${detail.run.completedSessionCount}/${detail.run.sessionCount}</p>
    <p>Thông điệp: ${detail.run.message}</p>
    <p>Thời gian: ${detail.run.createdAt}</p>
    <c:if test="${detail.run.status == 'RUNNING'}">
        <p>Đang xử lý... trang sẽ tự tải lại sau 3 giây.</p>
    </c:if>
    <c:if test="${detail.run.outputStatus == 'READY' && detail.invigilatorFileAvailable}">
        <p><a class="button" href="${pageContext.request.contextPath}/assignments/${detail.run.assignmentId}/downloads/invigilators">Tải DANHSACH_PHANCONG.xlsx</a></p>
    </c:if>
    <c:if test="${detail.run.outputStatus == 'READY' && detail.monitorFileAvailable}">
        <p><a class="button" href="${pageContext.request.contextPath}/assignments/${detail.run.assignmentId}/downloads/monitors">Tải DANHSACH_GIAMSAT.xlsx</a></p>
    </c:if>
</div>

<div class="card">
    <h2>Tóm tắt các ca</h2>
    <table>
        <thead>
        <tr>
            <th>Ca</th>
            <th>Số phòng</th>
            <th>Số giám sát</th>
            <th>Xem</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${detail.sessionSummaries}" var="session">
            <tr>
                <td>${session.sessionNo}</td>
                <td>${session.roomAssignmentCount}</td>
                <td>${session.hallMonitorCount}</td>
                <td><a href="${pageContext.request.contextPath}/assignments/${detail.run.assignmentId}/sessions/${session.sessionNo}">Chi tiết</a></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<c:if test="${detail.run.status == 'RUNNING' || detail.run.outputStatus == 'GENERATING'}">
    <script>
        setTimeout(function() {
            window.location.reload();
        }, 3000);
    </script>
</c:if>
</body>
</html>
