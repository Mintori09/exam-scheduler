package vn.edu.networkprogramming.assignserver.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/assignments/*")
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
