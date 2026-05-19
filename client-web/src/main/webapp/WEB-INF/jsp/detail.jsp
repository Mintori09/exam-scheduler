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
    <p>Trạng thái: <span id="run-status">${detail.run.status}</span></p>
    <p>Xuất file: <span id="run-output-status">${detail.run.outputStatus}</span></p>
    <c:if test="${not empty detail.run.outputError}">
        <p id="run-output-error">Lỗi xuất file: ${detail.run.outputError}</p>
    </c:if>
    <p>Tiến độ: <span id="run-progress">${detail.run.completedSessionCount}/${detail.run.sessionCount}</span></p>
    <p>Thông điệp: <span id="run-message">${detail.run.message}</span></p>
    <p>Thời gian: ${detail.run.createdAtDisplay} (UTC+7)</p>
    <p id="live-note"></p>
    <div id="download-links">
        <c:if test="${detail.run.status == 'SUCCESS'}">
            <p><a class="button" href="${pageContext.request.contextPath}/assignments/${detail.run.assignmentId}/downloads/invigilators">Tải DANHSACH_PHANCONG.xlsx</a></p>
            <p><a class="button" href="${pageContext.request.contextPath}/assignments/${detail.run.assignmentId}/downloads/monitors">Tải DANHSACH_GIAMSAT.xlsx</a></p>
        </c:if>
    </div>
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
        <tbody id="session-summary-body">
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
<script>
    (function() {
        var assignmentId = "${detail.run.assignmentId}";
        var initialStatus = "${detail.run.status}";
        var initialOutputStatus = "${detail.run.outputStatus}";
        var shouldTrackLive = initialStatus === "RUNNING" || initialOutputStatus === "GENERATING";
        if (!shouldTrackLive) {
            document.getElementById("live-note").textContent = "";
            return;
        }
        var apiBaseUrl = "${applicationScope.serverBaseUrl}";
        var sseUrl = apiBaseUrl + "/api/assignments/" + encodeURIComponent(assignmentId) + "/events";
        var note = document.getElementById("live-note");
        var statusEl = document.getElementById("run-status");
        var outputStatusEl = document.getElementById("run-output-status");
        var progressEl = document.getElementById("run-progress");
        var messageEl = document.getElementById("run-message");
        var outputErrorEl = document.getElementById("run-output-error");
        var downloadLinksEl = document.getElementById("download-links");
        var summaryBodyEl = document.getElementById("session-summary-body");
        var terminalReached = false;
        var es = new EventSource(sseUrl);

        note.textContent = "Đang theo dõi realtime...";
        es.addEventListener("detail", function(event) {
            var detail = JSON.parse(event.data);
            var run = detail.run;
            renderRun(run);
            renderDownloadLinks(run);
            renderSummaries(detail.sessionSummaries || [], run.assignmentId);
            if (run.status !== "RUNNING" && run.outputStatus !== "GENERATING") {
                terminalReached = true;
                es.close();
                if (run.status === "FAILED") {
                    note.textContent = "Đã thất bại, không tiếp tục theo dõi realtime.";
                    return;
                }
                note.textContent = "Đã hoàn tất. Đang tải lại trang...";
                setTimeout(function() { window.location.reload(); }, 700);
            }
        });
        es.onerror = function() {
            if (terminalReached) {
                return;
            }
            note.textContent = "Mất kết nối realtime, sẽ tải lại sau 10 giây.";
            es.close();
            setTimeout(function() { window.location.reload(); }, 10000);
        };

        function renderRun(run) {
            statusEl.textContent = run.status;
            outputStatusEl.textContent = run.outputStatus;
            progressEl.textContent = run.completedSessionCount + "/" + run.sessionCount;
            messageEl.textContent = run.message || "";
            if (run.outputError) {
                if (outputErrorEl) {
                    outputErrorEl.textContent = "Lỗi xuất file: " + run.outputError;
                } else {
                    outputStatusEl.insertAdjacentHTML("afterend", '<p id="run-output-error">Lỗi xuất file: ' + escapeHtml(run.outputError) + '</p>');
                    outputErrorEl = document.getElementById("run-output-error");
                }
            } else if (outputErrorEl) {
                outputErrorEl.remove();
                outputErrorEl = null;
            }
        }

        function renderDownloadLinks(run) {
            if (run.status !== "SUCCESS") {
                downloadLinksEl.innerHTML = "";
                return;
            }
            downloadLinksEl.innerHTML = [
                '<p><a class="button" href="/assignments/' + encodeURIComponent(run.assignmentId) + '/downloads/invigilators">Tải DANHSACH_PHANCONG.xlsx</a></p>',
                '<p><a class="button" href="/assignments/' + encodeURIComponent(run.assignmentId) + '/downloads/monitors">Tải DANHSACH_GIAMSAT.xlsx</a></p>'
            ].join("");
        }

        function renderSummaries(summaries, assignmentId) {
            summaryBodyEl.innerHTML = summaries.map(function(session) {
                return [
                    "<tr>",
                    "<td>" + escapeHtml(String(session.sessionNo)) + "</td>",
                    "<td>" + escapeHtml(String(session.roomAssignmentCount)) + "</td>",
                    "<td>" + escapeHtml(String(session.hallMonitorCount)) + "</td>",
                    '<td><a href="/assignments/' + encodeURIComponent(assignmentId) + '/sessions/' + encodeURIComponent(String(session.sessionNo)) + '">Chi tiết</a></td>',
                    "</tr>"
                ].join("");
            }).join("");
        }

        function escapeHtml(value) {
            return value
                .replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll('"', "&quot;")
                .replaceAll("'", "&#39;");
        }
    })();
</script>
</body>
</html>
