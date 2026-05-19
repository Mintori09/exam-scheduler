package vn.edu.networkprogramming.assignserver.web;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import vn.edu.networkprogramming.assignserver.model.AssignmentDetail;

@WebServlet(value = "/api/assignments/*", asyncSupported = true)
public class AssignmentItemServlet extends BaseJsonServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String[] parts = pathParts(req);
            if (parts.length == 1) {
                handleDetail(resp, parts[0]);
                return;
            }
            if (parts.length == 3 && "sessions".equals(parts[1])) {
                handleSession(resp, parts[0], Integer.parseInt(parts[2]));
                return;
            }
            if (parts.length == 3 && "downloads".equals(parts[1])) {
                handleDownload(resp, parts[0], parts[2]);
                return;
            }
            if (parts.length == 2 && "events".equals(parts[1])) {
                handleEvents(req, resp, parts[0]);
                return;
            }
            writeJson(resp, HttpServletResponse.SC_NOT_FOUND, new ApiErrorResponse("FAILED", "API khong ton tai"));
        } catch (Exception exception) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiErrorResponse("FAILED", exception.getMessage()));
        }
    }

    private void handleDetail(HttpServletResponse resp, String assignmentId) throws Exception {
        var detail = assignmentService().getAssignmentDetail(assignmentId);
        if (detail == null) {
            writeJson(resp, HttpServletResponse.SC_NOT_FOUND, new ApiErrorResponse("FAILED", "Khong tim thay assignmentId"));
            return;
        }
        writeJson(resp, HttpServletResponse.SC_OK, detail);
    }

    private void handleSession(HttpServletResponse resp, String assignmentId, int sessionNo) throws Exception {
        var session = assignmentService().getSessionDetail(assignmentId, sessionNo);
        if (session == null) {
            writeJson(resp, HttpServletResponse.SC_NOT_FOUND, new ApiErrorResponse("FAILED", "Khong tim thay chi tiet ca thi"));
            return;
        }
        writeJson(resp, HttpServletResponse.SC_OK, session);
    }

    private void handleDownload(HttpServletResponse resp, String assignmentId, String type) throws Exception {
        if (!"invigilators".equals(type) && !"monitors".equals(type)) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiErrorResponse("FAILED", "Loai file khong hop le"));
            return;
        }
        var detail = assignmentService().getAssignmentDetail(assignmentId);
        if (detail == null) {
            writeJson(resp, HttpServletResponse.SC_NOT_FOUND, new ApiErrorResponse("FAILED", "Khong tim thay assignmentId"));
            return;
        }
        if (!"SUCCESS".equals(detail.run().status())) {
            writeJson(resp, HttpServletResponse.SC_CONFLICT, new ApiErrorResponse(
                    "FAILED",
                    "Ket qua chua san sang de xuat file. status=" + detail.run().status()
            ));
            return;
        }
        var file = "invigilators".equals(type)
                ? assignmentService().getInvigilatorFile(assignmentId)
                : "monitors".equals(type) ? assignmentService().getMonitorFile(assignmentId) : null;
        if (file == null) {
            writeJson(resp, HttpServletResponse.SC_NOT_FOUND, new ApiErrorResponse("FAILED", "Khong tim thay file ket qua"));
            return;
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(file.mimeType());
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + file.fileName() + "\"");
        resp.getOutputStream().write(file.content());
    }

    private void handleEvents(HttpServletRequest req, HttpServletResponse resp, String assignmentId) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/event-stream;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Connection", "keep-alive");
        String origin = req.getHeader("Origin");
        resp.setHeader("Access-Control-Allow-Origin", origin == null || origin.isBlank() ? "*" : origin);

        AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(0);
        asyncContext.start(() -> streamEvents(asyncContext, assignmentId));
    }

    private void streamEvents(AsyncContext asyncContext, String assignmentId) {
        String lastFingerprint = null;
        try {
            HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
            PrintWriter writer = response.getWriter();
            while (true) {
                AssignmentDetail detail = assignmentService().getAssignmentDetail(assignmentId);
                if (detail == null) {
                    writer.write("event: error\n");
                    writer.write("data: {\"message\":\"Khong tim thay assignmentId\"}\n\n");
                    writer.flush();
                    break;
                }
                String fingerprint = detail.run().status()
                        + "|" + detail.run().outputStatus()
                        + "|" + detail.run().completedSessionCount()
                        + "|" + detail.sessionSummaries().size()
                        + "|" + detail.invigilatorFileAvailable()
                        + "|" + detail.monitorFileAvailable()
                        + "|" + detail.run().message();
                if (!Objects.equals(lastFingerprint, fingerprint)) {
                    String json = objectMapper().writeValueAsString(detail);
                    writer.write("event: detail\n");
                    writer.write("data: " + json + "\n\n");
                    writer.flush();
                    lastFingerprint = fingerprint;
                }
                if (!"RUNNING".equals(detail.run().status()) && !"GENERATING".equals(detail.run().outputStatus())) {
                    break;
                }
                Thread.sleep(1000L);
            }
        } catch (Exception ignored) {
            // Client disconnected or stream ended.
        } finally {
            asyncContext.complete();
        }
    }

    private String[] pathParts(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            return new String[0];
        }
        return java.util.Arrays.stream(pathInfo.split("/"))
                .filter(part -> !part.isBlank())
                .toArray(String[]::new);
    }
}
