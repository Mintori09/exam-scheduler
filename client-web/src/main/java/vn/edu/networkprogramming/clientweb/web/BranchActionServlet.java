package vn.edu.networkprogramming.clientweb.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet({"/branch-actions/create", "/branch-actions/*"})
public class BranchActionServlet extends BasePageServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String servletPath = req.getServletPath();
            String pathInfo = req.getPathInfo() == null ? "" : req.getPathInfo();
            if ("/branch-actions/create".equals(servletPath)) {
                handleCreate(req, resp);
                return;
            }
            if ("/branch-actions".equals(servletPath) && pathInfo.endsWith("/sessions")) {
                String branchId = pathInfo.substring(1, pathInfo.length() - "/sessions".length());
                var sessionCountValidation = validator().validatePositiveCount(req.getParameter("sessionCount"), "Số ca thi");
                if (!sessionCountValidation.valid()) {
                    throw new IOException(sessionCountValidation.message());
                }
                Integer requestedStaffCount = validateOptionalPositiveCount(req.getParameter("requestedStaffCount"), "Số cán bộ");
                Integer requestedRoomCount = validateOptionalPositiveCount(req.getParameter("requestedRoomCount"), "Số phòng");
                assignServerClient().createNextSession(
                        branchId,
                        Integer.parseInt(req.getParameter("sessionCount")),
                        requestedStaffCount,
                        requestedRoomCount
                );
                resp.sendRedirect(req.getContextPath() + "/branches/" + branchId + "?message=Đã+tạo+thêm+ca+thi");
                return;
            }
            if ("/branch-actions".equals(servletPath) && pathInfo.endsWith("/reset")) {
                String branchId = pathInfo.substring(1, pathInfo.length() - "/reset".length());
                var branch = assignServerClient().resetBranch(branchId, req.getParameter("name"));
                resp.sendRedirect(req.getContextPath() + "/branches/" + branch.branchId() + "?message=Đã+tạo+lại+nhánh+mới");
                return;
            }
            if ("/branch-actions".equals(servletPath) && pathInfo.endsWith("/rename")) {
                String branchId = pathInfo.substring(1, pathInfo.length() - "/rename".length());
                assignServerClient().renameBranch(branchId, req.getParameter("name"));
                resp.sendRedirect(homeUrl(req, "Đã đổi tên nhánh phân công", null));
                return;
            }
            if ("/branch-actions".equals(servletPath) && pathInfo.endsWith("/archive")) {
                String branchId = pathInfo.substring(1, pathInfo.length() - "/archive".length());
                assignServerClient().archiveBranch(branchId);
                resp.sendRedirect(homeUrl(req, "Đã ẩn nhánh phân công", null));
                return;
            }
            if ("/branch-actions".equals(servletPath) && pathInfo.endsWith("/restore")) {
                String branchId = pathInfo.substring(1, pathInfo.length() - "/restore".length());
                assignServerClient().restoreBranch(branchId);
                resp.sendRedirect(homeUrl(req, "Đã hiện lại nhánh phân công", null));
                return;
            }
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception exception) {
            resp.sendRedirect(homeUrl(req, null, exception.getMessage()));
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        var staffCountValidation = validator().validatePositiveCount(req.getParameter("requestedStaffCount"), "Số cán bộ");
        if (!staffCountValidation.valid()) {
            throw new IOException(staffCountValidation.message());
        }
        var roomCountValidation = validator().validatePositiveCount(req.getParameter("requestedRoomCount"), "Số phòng");
        if (!roomCountValidation.valid()) {
            throw new IOException(roomCountValidation.message());
        }
        var sessionCountValidation = validator().validatePositiveCount(req.getParameter("sessionCount"), "Số ca thi");
        if (!sessionCountValidation.valid()) {
            throw new IOException(sessionCountValidation.message());
        }
        var branch = assignServerClient().createBranch(
                req.getParameter("name"),
                req.getParameter("staffDatasetId"),
                req.getParameter("roomDatasetId"),
                Integer.parseInt(req.getParameter("requestedStaffCount")),
                Integer.parseInt(req.getParameter("requestedRoomCount")),
                Integer.parseInt(req.getParameter("sessionCount"))
        );
        resp.sendRedirect(req.getContextPath() + "/branches/" + branch.branchId());
    }

    private String homeUrl(HttpServletRequest req, String message, String error) {
        StringBuilder url = new StringBuilder(req.getContextPath()).append("/?");
        appendQuery(url, "includeArchived", req.getParameter("includeArchived"));
        appendQuery(url, "message", message);
        appendQuery(url, "error", error);
        appendQuery(url, "staffDatasetId", req.getParameter("staffDatasetId"));
        appendQuery(url, "roomDatasetId", req.getParameter("roomDatasetId"));
        appendQuery(url, "name", req.getParameter("name"));
        appendQuery(url, "requestedStaffCount", req.getParameter("requestedStaffCount"));
        appendQuery(url, "requestedRoomCount", req.getParameter("requestedRoomCount"));
        appendQuery(url, "sessionCount", req.getParameter("sessionCount"));
        return url.toString();
    }

    private Integer validateOptionalPositiveCount(String raw, String label) throws IOException {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        var validation = validator().validatePositiveCount(raw, label);
        if (!validation.valid()) {
            throw new IOException(validation.message());
        }
        return Integer.parseInt(raw);
    }

    private void appendQuery(StringBuilder url, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (url.charAt(url.length() - 1) != '?') {
            url.append('&');
        }
        url.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
        url.append('=');
        url.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}
