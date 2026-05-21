<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Chi tiết ca thi</title>
    <style>
        :root {
            --bg: #f5f1e8;
            --panel: #fffdf9;
            --line: #ddd2c0;
            --text: #2f261b;
            --muted: #706556;
            --brand: #0f6b5e;
            --shadow: 0 18px 40px rgba(53, 41, 22, 0.10);
        }
        * { box-sizing: border-box; }
        body { margin: 0; background: var(--bg); color: var(--text); font-family: "Segoe UI", sans-serif; }
        .page { max-width: 1280px; margin: 0 auto; padding: 24px; }
        .panel {
            background: var(--panel);
            border: 1px solid var(--line);
            border-radius: 22px;
            box-shadow: var(--shadow);
            padding: 22px;
            margin-bottom: 18px;
        }
        h1, h2 { margin: 0 0 10px; }
        p { margin: 0; color: var(--muted); line-height: 1.6; }
        a { color: var(--brand); text-decoration: none; font-weight: 700; }
        .table-wrap { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; min-width: 900px; }
        th, td {
            padding: 12px;
            text-align: left;
            vertical-align: top;
            border-bottom: 1px solid #ece2d3;
        }
        th {
            color: var(--muted);
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 0.04em;
        }
        .summary {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: 14px;
            margin-top: 16px;
        }
        .summary-item {
            border: 1px solid var(--line);
            border-radius: 16px;
            padding: 16px;
            background: #fffaf2;
        }
        .summary-item strong { display: block; margin-top: 6px; font-size: 28px; color: var(--text); }
        @media (max-width: 720px) {
            .page { padding: 14px; }
            .panel { border-radius: 18px; }
        }
    </style>
</head>
<body>
<div class="page">
    <div class="panel">
        <p><a href="${pageContext.request.contextPath}/branches/${branchId}">← Quay lại nhánh phân công</a></p>
        <h1>Chi tiết ca ${sessionRecord.sessionNo}</h1>
        <p class="small">${sessionRecord.createdAtDisplay}</p>
        <div class="summary">
            <div class="summary-item">
                <span>Số phòng</span>
                <strong>${sessionRecord.summary.roomAssignmentCount}</strong>
            </div>
            <div class="summary-item">
                <span>Cán bộ giám sát</span>
                <strong>${sessionRecord.summary.hallMonitorCount}</strong>
            </div>
            <div class="summary-item">
                <span>Cấu hình dùng cho ca này</span>
                <strong>${sessionRecord.requestedStaffCount} / ${sessionRecord.requestedRoomCount}</strong>
            </div>
        </div>
    </div>

    <div class="panel">
        <h2>Bảng phân công coi thi</h2>
        <div class="table-wrap">
            <table>
                <thead>
                <tr>
                    <th>Phòng</th>
                    <th>Mã cán bộ 1</th>
                    <th>Họ tên cán bộ 1</th>
                    <th>Mã cán bộ 2</th>
                    <th>Họ tên cán bộ 2</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${sessionRecord.session.roomAssignments}" var="assignment">
                    <tr>
                        <td>${assignment.room.roomName}</td>
                        <td>${assignment.invigilatorOne.staffCode}</td>
                        <td>${assignment.invigilatorOne.fullName}</td>
                        <td>${assignment.invigilatorTwo.staffCode}</td>
                        <td>${assignment.invigilatorTwo.fullName}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="panel">
        <h2>Bảng giám sát hành lang</h2>
        <div class="table-wrap">
            <table>
                <thead>
                <tr>
                    <th>Mã cán bộ</th>
                    <th>Họ tên</th>
                    <th>Dải phòng phụ trách</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${sessionRecord.session.hallMonitorAssignments}" var="monitor">
                    <tr>
                        <td>${monitor.staff.staffCode}</td>
                        <td>${monitor.staff.fullName}</td>
                        <td>${monitor.rangeText}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>
