<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Phân công coi thi</title>
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
            --warn-soft: #f8ebe4;
            --danger: #b42318;
            --danger-soft: #fdecea;
            --shadow: 0 18px 40px rgba(53, 41, 22, 0.10);
        }
        * { box-sizing: border-box; }
        body {
            margin: 0;
            font-family: "Segoe UI", sans-serif;
            color: var(--text);
            background:
                radial-gradient(circle at top right, rgba(15,107,94,0.10), transparent 25%),
                radial-gradient(circle at top left, rgba(184,92,56,0.10), transparent 25%),
                var(--bg);
        }
        .page {
            max-width: 1360px;
            margin: 0 auto;
            padding: 24px;
        }
        .hero, .panel, .item-card {
            background: var(--panel);
            border: 1px solid rgba(221, 210, 192, 0.9);
            border-radius: 22px;
            box-shadow: var(--shadow);
        }
        .hero {
            padding: 28px 30px;
            margin-bottom: 18px;
        }
        .hero-top, .panel-head {
            display: flex;
            justify-content: space-between;
            gap: 16px;
            align-items: flex-start;
            flex-wrap: wrap;
        }
        .eyebrow {
            display: inline-block;
            padding: 6px 10px;
            border-radius: 999px;
            background: var(--brand-soft);
            color: var(--brand);
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.04em;
        }
        h1, h2, h3 { margin: 0; line-height: 1.15; }
        h1 { font-size: clamp(28px, 4vw, 40px); margin-top: 10px; }
        h2 { font-size: 22px; }
        h3 { font-size: 17px; }
        p { margin: 0; line-height: 1.6; color: var(--muted); }
        .notice {
            margin-top: 16px;
            padding: 14px 16px;
            border-radius: 16px;
            border: 1px solid var(--line);
            background: var(--brand-soft);
            color: var(--brand);
        }
        .notice.error {
            background: var(--danger-soft);
            color: var(--danger);
            border-color: rgba(180,35,24,0.16);
        }
        .stats, .two-col, .three-col, .library-grid, .branch-grid {
            display: grid;
            gap: 16px;
        }
        .stats { grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); margin-top: 18px; }
        .two-col { grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); }
        .three-col { grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); }
        .library-grid { grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); }
        .branch-grid { grid-template-columns: repeat(auto-fit, minmax(360px, 1fr)); }
        .stat {
            padding: 16px 18px;
            border-radius: 18px;
            border: 1px solid var(--line);
            background: linear-gradient(180deg, #fffaf1 0%, #fbf6ec 100%);
        }
        .stat strong {
            display: block;
            margin-top: 6px;
            font-size: 30px;
            color: var(--text);
        }
        .panel { padding: 22px; margin-bottom: 18px; }
        label {
            display: block;
            margin: 14px 0 6px;
            color: var(--muted);
            font-size: 13px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.04em;
        }
        input, select, button {
            width: 100%;
            padding: 12px 14px;
            font: inherit;
            border-radius: 12px;
            border: 1px solid var(--line);
            background: #fff;
            color: var(--text);
        }
        input:focus, select:focus {
            outline: 2px solid rgba(15,107,94,0.18);
            border-color: var(--brand);
        }
        button {
            border: none;
            background: var(--brand);
            color: #fff;
            cursor: pointer;
            font-weight: 700;
        }
        button.alt { background: #eadfcd; color: var(--text); }
        button.warn { background: var(--warn); }
        button.ghost {
            background: transparent;
            color: var(--brand);
            border: 1px solid rgba(15,107,94,0.22);
        }
        .toolbar {
            display: flex;
            gap: 10px;
            align-items: center;
            flex-wrap: wrap;
        }
        .toolbar .checkbox {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 10px 14px;
            border: 1px solid var(--line);
            border-radius: 999px;
            background: #fffaf2;
            color: var(--muted);
        }
        .toolbar .checkbox input { width: auto; margin: 0; }
        .helper {
            margin-top: 10px;
            font-size: 13px;
            color: var(--muted);
        }
        .badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 4px 10px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 700;
            background: var(--brand-soft);
            color: var(--brand);
        }
        .badge.warn { background: var(--warn-soft); color: var(--warn); }
        .badge.error { background: var(--danger-soft); color: var(--danger); }
        .item-card {
            padding: 18px;
        }
        .meta {
            display: grid;
            gap: 10px;
            margin-top: 14px;
        }
        .meta-row {
            display: flex;
            justify-content: space-between;
            gap: 12px;
        }
        .meta-row span:last-child {
            text-align: right;
            color: var(--text);
            font-weight: 600;
        }
        .mono { font-family: Consolas, monospace; }
        .small { color: var(--muted); font-size: 12px; }
        .actions, .inline-actions {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }
        .actions form, .inline-actions form { flex: 1 1 140px; }
        .rename-form {
            display: grid;
            grid-template-columns: 1fr auto;
            gap: 10px;
            margin-top: 12px;
        }
        .rename-form input {
            min-width: 0;
        }
        .rename-form button {
            min-width: 120px;
            white-space: nowrap;
        }
        .branch-actions {
            margin-top: 16px;
            padding-top: 16px;
            border-top: 1px solid #efe5d7;
        }
        .branch-count {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 10px;
            align-items: end;
            margin-bottom: 12px;
        }
        .branch-buttons {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 12px;
            align-items: stretch;
        }
        .branch-buttons form,
        .branch-buttons .button-block,
        .branch-count .button-block {
            display: flex;
        }
        .branch-buttons button,
        .branch-count button {
            height: 100%;
            min-height: 48px;
        }
        .empty {
            padding: 16px;
            border-radius: 16px;
            border: 1px dashed var(--line);
            background: #fffaf2;
            color: var(--muted);
        }
        a {
            color: var(--brand);
            text-decoration: none;
            font-weight: 700;
        }
        @media (max-width: 720px) {
            .page { padding: 14px; }
            .hero, .panel, .item-card { border-radius: 18px; }
            .branch-count { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<div class="page">
    <div class="hero">
        <div class="hero-top">
            <div>
                <!-- <span class="eyebrow">Hệ thống phân công coi thi</span> -->
                <h1>Hệ thống phân công coi thi</h1>
                <!-- <p>Tải dữ liệu, tạo nhánh và sinh các ca thi.</p> -->
            </div>
            <form class="toolbar" action="${pageContext.request.contextPath}/" method="get">
                <label class="checkbox">
                    <input type="checkbox" name="includeArchived" value="true" <c:if test="${includeArchived}">checked</c:if> />
                    Hiện dữ liệu đã ẩn
                </label>
                <button class="alt" type="submit">Lọc</button>
            </form>
        </div>

        <c:if test="${not empty error}">
            <div class="notice error">${error}</div>
        </c:if>
        <c:if test="${not empty message}">
            <div class="notice">${message}</div>
        </c:if>

        <div class="stats">
            <div class="stat">
                <span class="small">Bộ dữ liệu cán bộ đang dùng</span>
                <strong>${activeStaffDatasets.size()}</strong>
            </div>
            <div class="stat">
                <span class="small">Bộ dữ liệu phòng đang dùng</span>
                <strong>${activeRoomDatasets.size()}</strong>
            </div>
            <div class="stat">
                <span class="small">Nhánh phân công đang hiển thị</span>
                <strong>${branches.size()}</strong>
            </div>
        </div>
    </div>

    <div class="panel">
        <div class="panel-head">
            <div>
                <h2>Tải bộ dữ liệu</h2>
                <p>Chọn file cán bộ, file phòng hoặc cả hai.</p>
            </div>
        </div>

        <form action="${pageContext.request.contextPath}/datasets/upload-bundle" method="post" enctype="multipart/form-data">
            <input type="hidden" name="includeArchived" value="${includeArchived}" />
            <input type="hidden" name="staffDatasetId" value="${selectedStaffDatasetId}" />
            <input type="hidden" name="roomDatasetId" value="${selectedRoomDatasetId}" />
            <input type="hidden" name="branchName" value="${branchName}" />
            <input type="hidden" name="requestedStaffCount" value="${requestedStaffCount}" />
            <input type="hidden" name="requestedRoomCount" value="${requestedRoomCount}" />
            <input type="hidden" name="sessionCount" value="${sessionCount}" />

            <div class="two-col">
                <div>
                    <label>Tên bộ dữ liệu cán bộ</label>
                    <input type="text" name="staffName" placeholder="Để trống để dùng tên file + thời gian" />
                    <label>File cán bộ (.xlsx)</label>
                    <input type="file" name="staffFile" accept=".xlsx" />
                </div>
                <div>
                    <label>Tên bộ dữ liệu phòng</label>
                    <input type="text" name="roomName" placeholder="Để trống để dùng tên file + thời gian" />
                    <label>File phòng (.xlsx)</label>
                    <input type="file" name="roomFile" accept=".xlsx" />
                </div>
            </div>

            <div class="actions" style="margin-top: 16px;">
                <form action="${pageContext.request.contextPath}/datasets/upload-bundle" method="post"></form>
                <button type="submit">Tải dữ liệu</button>
            </div>
        </form>
        <div class="helper">Nếu file đã có, hệ thống sẽ dùng lại bộ dữ liệu đó.</div>
    </div>

    <div class="panel">
        <div class="panel-head">
            <div>
                <h2>Tạo nhánh mới</h2>
                <p>Tạo một nhánh phân công mới.</p>
            </div>
        </div>

        <c:if test="${empty activeStaffDatasets or empty activeRoomDatasets}">
            <div class="empty">Cần có ít nhất một bộ dữ liệu cán bộ và một bộ dữ liệu phòng đang hoạt động.</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/branch-actions/create" method="post">
            <input type="hidden" name="includeArchived" value="${includeArchived}" />

            <label>Tên nhánh phân công</label>
            <input type="text" name="name" value="${branchName}" placeholder="Ví dụ: Kỳ thi đợt 1" />

            <div class="two-col">
                <div>
                    <label>Bộ dữ liệu cán bộ</label>
                    <select name="staffDatasetId" required>
                        <option value="">Chọn bộ dữ liệu cán bộ</option>
                        <c:forEach items="${activeStaffDatasets}" var="dataset">
                            <option value="${dataset.datasetId}" <c:if test="${selectedStaffDatasetId == dataset.datasetId}">selected</c:if>>
                                ${dataset.name} (${dataset.staffCount} cán bộ)
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div>
                    <label>Bộ dữ liệu phòng</label>
                    <select name="roomDatasetId" required>
                        <option value="">Chọn bộ dữ liệu phòng</option>
                        <c:forEach items="${activeRoomDatasets}" var="dataset">
                            <option value="${dataset.datasetId}" <c:if test="${selectedRoomDatasetId == dataset.datasetId}">selected</c:if>>
                                ${dataset.name} (${dataset.roomCount} phòng)
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="three-col">
                <div>
                    <label>Số cán bộ dùng mỗi ca</label>
                    <input type="number" name="requestedStaffCount" min="1" value="${requestedStaffCount}" required />
                </div>
                <div>
                    <label>Số phòng dùng mỗi ca</label>
                    <input type="number" name="requestedRoomCount" min="1" value="${requestedRoomCount}" required />
                </div>
                <div>
                    <label>Số ca cần tạo</label>
                    <input type="number" name="sessionCount" min="1" value="${empty sessionCount ? '1' : sessionCount}" required />
                </div>
            </div>

            <div class="actions" style="margin-top: 16px;">
                <button type="submit">Tạo nhánh</button>
            </div>
        </form>
    </div>

    <div class="panel">
        <div class="panel-head">
            <div>
                <h2>Thư viện bộ dữ liệu</h2>
                <p>Danh sách bộ dữ liệu đang có.</p>
            </div>
        </div>

        <div class="library-grid">
            <div>
                <h3 style="margin-bottom: 12px;">Bộ dữ liệu cán bộ</h3>
                <div class="branch-grid">
                    <c:forEach items="${staffDatasets}" var="dataset">
                        <div class="item-card">
                            <div class="panel-head">
                                <div>
                                    <strong>${dataset.name}</strong><br/>
                                    <span class="small">${dataset.originalFileName}</span>
                                </div>
                                <c:choose>
                                    <c:when test="${dataset.archived}">
                                        <span class="badge warn">Đã ẩn</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge">Đang hiển thị</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="meta">
                                <div class="meta-row"><span>Số cán bộ</span><span>${dataset.staffCount}</span></div>
                                <div class="meta-row"><span>Mã kiểm tra</span><span class="mono">${dataset.shortHash}</span></div>
                                <div class="meta-row"><span>Thời gian</span><span>${dataset.createdAtDisplay}</span></div>
                            </div>
                            <form class="rename-form" action="${pageContext.request.contextPath}/staff-datasets/${dataset.datasetId}/rename" method="post">
                                <input type="hidden" name="includeArchived" value="${includeArchived}" />
                                <input type="text" name="name" value="${dataset.name}" required />
                                <button class="alt" type="submit">Đổi tên</button>
                            </form>
                            <div class="inline-actions" style="margin-top: 16px;">
                                <c:choose>
                                    <c:when test="${dataset.archived}">
                                        <form action="${pageContext.request.contextPath}/staff-datasets/${dataset.datasetId}/restore" method="post">
                                            <input type="hidden" name="includeArchived" value="${includeArchived}" />
                                            <button class="ghost" type="submit">Hiện lại</button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <form action="${pageContext.request.contextPath}/staff-datasets/${dataset.datasetId}/archive" method="post">
                                            <input type="hidden" name="includeArchived" value="${includeArchived}" />
                                            <button class="warn" type="submit">Ẩn</button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </c:forEach>
                    <c:if test="${empty staffDatasets}">
                        <div class="empty">Chưa có bộ dữ liệu cán bộ.</div>
                    </c:if>
                </div>
            </div>

            <div>
                <h3 style="margin-bottom: 12px;">Bộ dữ liệu phòng</h3>
                <div class="branch-grid">
                    <c:forEach items="${roomDatasets}" var="dataset">
                        <div class="item-card">
                            <div class="panel-head">
                                <div>
                                    <strong>${dataset.name}</strong><br/>
                                    <span class="small">${dataset.originalFileName}</span>
                                </div>
                                <c:choose>
                                    <c:when test="${dataset.archived}">
                                        <span class="badge warn">Đã ẩn</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge">Đang hiển thị</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="meta">
                                <div class="meta-row"><span>Số phòng</span><span>${dataset.roomCount}</span></div>
                                <div class="meta-row"><span>Mã kiểm tra</span><span class="mono">${dataset.shortHash}</span></div>
                                <div class="meta-row"><span>Thời gian</span><span>${dataset.createdAtDisplay}</span></div>
                            </div>
                            <form class="rename-form" action="${pageContext.request.contextPath}/room-datasets/${dataset.datasetId}/rename" method="post">
                                <input type="hidden" name="includeArchived" value="${includeArchived}" />
                                <input type="text" name="name" value="${dataset.name}" required />
                                <button class="alt" type="submit">Đổi tên</button>
                            </form>
                            <div class="inline-actions" style="margin-top: 16px;">
                                <c:choose>
                                    <c:when test="${dataset.archived}">
                                        <form action="${pageContext.request.contextPath}/room-datasets/${dataset.datasetId}/restore" method="post">
                                            <input type="hidden" name="includeArchived" value="${includeArchived}" />
                                            <button class="ghost" type="submit">Hiện lại</button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <form action="${pageContext.request.contextPath}/room-datasets/${dataset.datasetId}/archive" method="post">
                                            <input type="hidden" name="includeArchived" value="${includeArchived}" />
                                            <button class="warn" type="submit">Ẩn</button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </c:forEach>
                    <c:if test="${empty roomDatasets}">
                        <div class="empty">Chưa có bộ dữ liệu phòng.</div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <div class="panel">
        <div class="panel-head">
            <div>
                <h2>Các nhánh phân công</h2>
                <p>Danh sách các nhánh đã tạo.</p>
            </div>
        </div>

        <div class="branch-grid">
            <c:forEach items="${branches}" var="branch">
                <div class="item-card">
                    <div class="panel-head">
                        <div>
                            <a href="${pageContext.request.contextPath}/branches/${branch.branchId}">${branch.name}</a><br/>
                            <span class="small mono">${branch.branchId}</span>
                        </div>
                        <c:if test="${branch.archived}">
                            <span class="badge warn">Đã ẩn</span>
                        </c:if>
                    </div>

                    <div class="meta">
                        <div class="meta-row"><span>Bộ cán bộ</span><span>${branch.staffDatasetName}</span></div>
                        <div class="meta-row"><span>Bộ phòng</span><span>${branch.roomDatasetName}</span></div>
                        <div class="meta-row"><span>Cấu hình</span><span>${branch.requestedStaffCount} cán bộ / ${branch.requestedRoomCount} phòng</span></div>
                        <div class="meta-row"><span>Đã tạo</span><span>${branch.sessionCreatedCount} ca</span></div>
                        <div class="meta-row"><span>Ca kế tiếp</span><span>${branch.nextSessionNo}</span></div>
                    </div>
                    <form class="rename-form" action="${pageContext.request.contextPath}/branch-actions/${branch.branchId}/rename" method="post">
                        <input type="hidden" name="includeArchived" value="${includeArchived}" />
                        <input type="text" name="name" value="${branch.name}" required />
                        <button class="alt" type="submit">Đổi tên</button>
                    </form>

                    <c:if test="${not empty branch.outputError}">
                        <div class="notice error" style="margin-top: 14px;">${branch.outputError}</div>
                    </c:if>

                    <div class="branch-actions">
                        <c:choose>
                            <c:when test="${branch.archived}">
                                <div class="actions">
                                    <form action="${pageContext.request.contextPath}/branch-actions/${branch.branchId}/restore" method="post">
                                        <input type="hidden" name="includeArchived" value="${includeArchived}" />
                                        <button class="ghost" type="submit">Hiện lại</button>
                                    </form>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <form action="${pageContext.request.contextPath}/branch-actions/${branch.branchId}/sessions" method="post">
                                    <div class="branch-count">
                                        <div>
                                            <label>Số ca thêm</label>
                                            <input type="number" name="sessionCount" min="1" value="1" required />
                                        </div>
                                        <div class="button-block">
                                            <button type="submit">Tạo thêm</button>
                                        </div>
                                    </div>
                                </form>
                                <div class="branch-buttons">
                                    <form action="${pageContext.request.contextPath}/branch-actions/${branch.branchId}/reset" method="post">
                                        <button class="alt" type="submit">Làm lại</button>
                                    </form>
                                    <form action="${pageContext.request.contextPath}/branch-actions/${branch.branchId}/archive" method="post">
                                        <input type="hidden" name="includeArchived" value="${includeArchived}" />
                                        <button class="warn" type="submit">Ẩn</button>
                                    </form>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:forEach>
            <c:if test="${empty branches}">
                <div class="empty">Chưa có nhánh phân công nào.</div>
            </c:if>
        </div>
    </div>
</div>
</body>
</html>
