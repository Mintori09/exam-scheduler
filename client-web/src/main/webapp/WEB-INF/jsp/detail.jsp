<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Chi tiết nhánh phân công</title>
    <style>
        :root {
            --bg: #f5f1e8;
            --panel: #fffdf9;
            --line: #ddd2c0;
            --text: #2f261b;
            --muted: #706556;
            --brand: #0f6b5e;
            --brand-soft: #e7f3f0;
            --warn: #b85c38;
            --shadow: 0 18px 40px rgba(53, 41, 22, 0.10);
        }
        * { box-sizing: border-box; }
        body { margin: 0; background: var(--bg); color: var(--text); font-family: "Segoe UI", sans-serif; }
        .page { max-width: 1180px; margin: 0 auto; padding: 24px; }
        .panel {
            background: var(--panel);
            border: 1px solid var(--line);
            border-radius: 22px;
            box-shadow: var(--shadow);
            padding: 22px;
            margin-bottom: 18px;
        }
        .top, .panel-head, .actions, .summary-grid, .meta-grid { display: flex; gap: 14px; flex-wrap: wrap; }
        .top, .panel-head { justify-content: space-between; align-items: flex-start; }
        .summary-grid { margin-top: 18px; }
        .summary-box {
            flex: 1 1 180px;
            border: 1px solid var(--line);
            border-radius: 16px;
            background: #fffaf2;
            padding: 16px;
        }
        .summary-box strong { display: block; margin-top: 6px; font-size: 28px; }
        h1, h2 { margin: 0; line-height: 1.15; }
        h1 { font-size: 34px; }
        h2 { font-size: 22px; }
        p { margin: 0; color: var(--muted); line-height: 1.6; }
        a { color: var(--brand); text-decoration: none; font-weight: 700; }
        .badge {
            display: inline-flex;
            align-items: center;
            padding: 4px 10px;
            border-radius: 999px;
            background: var(--brand-soft);
            color: var(--brand);
            font-size: 12px;
            font-weight: 700;
        }
        .mono { font-family: Consolas, monospace; }
        .small { color: var(--muted); font-size: 12px; }
        button, input {
            width: 100%;
            padding: 12px 14px;
            font: inherit;
            border-radius: 12px;
        }
        input { border: 1px solid var(--line); }
        button {
            border: none;
            background: var(--brand);
            color: #fff;
            cursor: pointer;
            font-weight: 700;
        }
        button.alt { background: #eadfcd; color: var(--text); }
        .actions form { flex: 1 1 180px; }
        .meta-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
            gap: 12px;
            margin-top: 16px;
        }
        .meta-item {
            border: 1px solid #efe5d7;
            border-radius: 14px;
            padding: 14px;
            background: #fffaf2;
        }
        .meta-item strong { display: block; margin-bottom: 6px; }
        .session-list { display: grid; gap: 14px; }
        .session-card {
            border: 1px solid #efe5d7;
            border-radius: 16px;
            padding: 16px;
            background: #fffaf4;
        }
        .session-card h3 { margin: 0 0 8px; }
        .session-meta {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: 10px;
            margin-top: 12px;
        }
        .session-meta div {
            border: 1px solid #efe5d7;
            border-radius: 12px;
            padding: 10px 12px;
            background: #fff;
        }
        @media (max-width: 720px) {
            .page { padding: 14px; }
            .panel { border-radius: 18px; }
        }
    </style>
</head>
<body>
<div class="page">
    <div class="panel">
        <div class="top">
            <div>
                <p><a href="${pageContext.request.contextPath}/">← Quay lại trang chính</a></p>
                <h1>${detail.branch.name}</h1>
                <p class="mono">${detail.branch.branchId}</p>
            </div>
        </div>

        <div class="summary-grid">
            <div class="summary-box">
                <span class="small">Đã tạo</span>
                <strong>${detail.branch.sessionCreatedCount}</strong>
                <span class="small">ca thi</span>
            </div>
            <div class="summary-box">
                <span class="small">Ca kế tiếp</span>
                <strong>${detail.branch.nextSessionNo}</strong>
                <span class="small">sẵn sàng tạo</span>
            </div>
            <div class="summary-box">
                <span class="small">Xem trước</span>
                <strong>
                    <c:if test="${preview.canCreateNextSession}">Có thể</c:if>
                    <c:if test="${not preview.canCreateNextSession}">Tạm dừng</c:if>
                </strong>
                <span class="small">${preview.message}</span>
            </div>
        </div>

        <div class="meta-grid">
            <div class="meta-item">
                <strong>Bộ dữ liệu cán bộ</strong>
                ${detail.branch.staffDatasetName}
            </div>
            <div class="meta-item">
                <strong>Bộ dữ liệu phòng</strong>
                ${detail.branch.roomDatasetName}
            </div>
            <div class="meta-item">
                <strong>Cấu hình mỗi ca</strong>
                ${detail.branch.requestedStaffCount} cán bộ, ${detail.branch.requestedRoomCount} phòng
            </div>
            <div class="meta-item">
                <strong>Ràng buộc đã dùng</strong>
                ${preview.usedPairCount} cặp cán bộ, ${preview.constrainedStaffCount} cán bộ đã có lịch sử
            </div>
        </div>
    </div>

    <div class="panel">
        <div class="panel-head">
            <div>
                <h2>Thao tác nhanh</h2>
                <br>
                <!-- <p>Xuất file luôn lấy toàn bộ số ca đã tạo của nhánh hiện tại.</p> -->
            </div>
        </div>

        <div class="actions">
            <form action="${pageContext.request.contextPath}/branch-actions/${detail.branch.branchId}/sessions" method="post">
                <label>Số ca thêm</label>
                <input type="number" name="sessionCount" min="1" value="1" required />
                <button type="submit" style="margin-top: 10px;">Tạo thêm</button>
            </form>
            <form action="${pageContext.request.contextPath}/branch-actions/${detail.branch.branchId}/reset" method="post">
                <label>Tạo lại nhánh</label>
                <input type="text" name="name" placeholder="Để trống để dùng tên mặc định" />
                <button class="alt" type="submit" style="margin-top: 10px;">Làm lại</button>
            </form>
        </div>

        <div class="actions" style="margin-top: 14px;">
            <form action="${pageContext.request.contextPath}/branches/${detail.branch.branchId}/downloads/invigilators" method="get">
                <button type="submit">Tải danh sách coi thi</button>
            </form>
            <form action="${pageContext.request.contextPath}/branches/${detail.branch.branchId}/downloads/monitors" method="get">
                <button class="alt" type="submit">Tải danh sách giám sát</button>
            </form>
        </div>
    </div>

    <div class="panel">
        <div class="panel-head">
            <div>
                <h2>Lịch sử các ca</h2>
                <br>
                <!-- <p>Không hiển thị subset chọn ngẫu nhiên; chỉ hiển thị thông tin người dùng cần xem.</p> -->
            </div>
        </div>
        <div class="session-list">
            <c:forEach items="${detail.sessions}" var="record">
                <div class="session-card">
                    <h3>Ca ${record.sessionNo}</h3>
                    <p class="small">${record.createdAtDisplay}</p>
                    <div class="session-meta">
                        <div><strong>${record.summary.roomAssignmentCount}</strong><br/><span class="small">phòng được phân công</span></div>
                        <div><strong>${record.summary.hallMonitorCount}</strong><br/><span class="small">cán bộ giám sát</span></div>
                        <div><a href="${pageContext.request.contextPath}/branches/${detail.branch.branchId}/sessions/${record.sessionNo}">Xem chi tiết ca</a></div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</div>
</body>
</html>
