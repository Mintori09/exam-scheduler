package vn.edu.networkprogramming.assignserver.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import vn.edu.networkprogramming.assignserver.service.exception.ValidationException;

@WebServlet("/api/branches/*")
public class BranchItemServlet extends BaseJsonServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String[] parts = pathParts(req);
            if (parts.length == 1) {
                var detail = assignmentService().getBranchDetail(parts[0]);
                if (detail == null) {
                    writeJson(resp, HttpServletResponse.SC_NOT_FOUND, new ApiErrorResponse("FAILED", "Khong tim thay branch"));
                    return;
                }
                writeJson(resp, HttpServletResponse.SC_OK, detail);
                return;
            }
            if (parts.length == 3 && "sessions".equals(parts[1])) {
                var session = assignmentService().getBranchSessionDetail(parts[0], Integer.parseInt(parts[2]));
                if (session == null) {
                    writeJson(resp, HttpServletResponse.SC_NOT_FOUND, new ApiErrorResponse("FAILED", "Khong tim thay chi tiet ca thi"));
                    return;
                }
                writeJson(resp, HttpServletResponse.SC_OK, session);
                return;
            }
            if (parts.length == 2 && "preview".equals(parts[1])) {
                writeJson(resp, HttpServletResponse.SC_OK, assignmentService().previewBranch(parts[0]));
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String[] parts = pathParts(req);
            if (parts.length == 2 && "sessions".equals(parts[1])) {
                int sessionCount = Integer.parseInt(req.getParameter("sessionCount"));
                writeJson(resp, HttpServletResponse.SC_OK, assignmentService().appendNextSessions(parts[0], sessionCount));
                return;
            }
            if (parts.length == 2 && "reset".equals(parts[1])) {
                writeJson(resp, HttpServletResponse.SC_OK, assignmentService().resetBranch(parts[0], req.getParameter("name")));
                return;
            }
            if (parts.length == 2 && "archive".equals(parts[1])) {
                assignmentService().archiveBranch(parts[0]);
                writeJson(resp, HttpServletResponse.SC_OK, new ApiErrorResponse("SUCCESS", "Đã ẩn nhánh phân công"));
                return;
            }
            if (parts.length == 2 && "rename".equals(parts[1])) {
                assignmentService().renameBranch(parts[0], req.getParameter("name"));
                writeJson(resp, HttpServletResponse.SC_OK, new ApiErrorResponse("SUCCESS", "Đã đổi tên nhánh phân công"));
                return;
            }
            if (parts.length == 2 && "restore".equals(parts[1])) {
                assignmentService().unarchiveBranch(parts[0]);
                writeJson(resp, HttpServletResponse.SC_OK, new ApiErrorResponse("SUCCESS", "Đã hiện lại nhánh phân công"));
                return;
            }
            writeJson(resp, HttpServletResponse.SC_NOT_FOUND, new ApiErrorResponse("FAILED", "API khong ton tai"));
        } catch (ValidationException exception) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiErrorResponse("FAILED", exception.getMessage()));
        } catch (Exception exception) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiErrorResponse("FAILED", exception.getMessage()));
        }
    }

    private void handleDownload(HttpServletResponse resp, String branchId, String type) throws Exception {
        if (!"invigilators".equals(type) && !"monitors".equals(type)) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiErrorResponse("FAILED", "Loai file khong hop le"));
            return;
        }
        var file = "invigilators".equals(type)
                ? assignmentService().getInvigilatorFile(branchId)
                : assignmentService().getMonitorFile(branchId);
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
