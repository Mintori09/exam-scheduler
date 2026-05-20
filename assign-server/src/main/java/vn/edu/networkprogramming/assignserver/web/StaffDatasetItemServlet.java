package vn.edu.networkprogramming.assignserver.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/staff-datasets/*")
public class StaffDatasetItemServlet extends BaseJsonServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String[] parts = pathParts(req);
            if (parts.length == 2 && "archive".equals(parts[1])) {
                assignmentService().archiveStaffDataset(parts[0]);
                writeJson(resp, HttpServletResponse.SC_OK, new ApiErrorResponse("SUCCESS", "Đã ẩn bộ dữ liệu cán bộ"));
                return;
            }
            if (parts.length == 2 && "rename".equals(parts[1])) {
                assignmentService().renameStaffDataset(parts[0], req.getParameter("name"));
                writeJson(resp, HttpServletResponse.SC_OK, new ApiErrorResponse("SUCCESS", "Đã đổi tên bộ dữ liệu cán bộ"));
                return;
            }
            if (parts.length == 2 && "restore".equals(parts[1])) {
                assignmentService().unarchiveStaffDataset(parts[0]);
                writeJson(resp, HttpServletResponse.SC_OK, new ApiErrorResponse("SUCCESS", "Đã hiện lại bộ dữ liệu cán bộ"));
                return;
            }
            writeJson(resp, HttpServletResponse.SC_NOT_FOUND, new ApiErrorResponse("FAILED", "API khong ton tai"));
        } catch (Exception exception) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiErrorResponse("FAILED", exception.getMessage()));
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
