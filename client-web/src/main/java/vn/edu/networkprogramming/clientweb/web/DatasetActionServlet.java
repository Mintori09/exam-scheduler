package vn.edu.networkprogramming.clientweb.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/datasets/upload-bundle", "/staff-datasets/upload", "/room-datasets/upload", "/staff-datasets/*", "/room-datasets/*"})
@MultipartConfig
public class DatasetActionServlet extends BasePageServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            String servletPath = req.getServletPath();
            String pathInfo = req.getPathInfo() == null ? "" : req.getPathInfo();
            if ("/datasets/upload-bundle".equals(servletPath)) {
                handleBundleUpload(req, resp);
                return;
            }
            if ("/staff-datasets/upload".equals(servletPath)) {
                handleStaffUpload(req, resp);
                return;
            }
            if ("/room-datasets/upload".equals(servletPath)) {
                handleRoomUpload(req, resp);
                return;
            }
            if ("/staff-datasets".equals(servletPath) && pathInfo.endsWith("/archive")) {
                String datasetId = pathInfo.substring(1, pathInfo.length() - "/archive".length());
                assignServerClient().archiveStaffDataset(datasetId);
                resp.sendRedirect(homeUrl(req, "Đã ẩn bộ dữ liệu cán bộ"));
                return;
            }
            if ("/staff-datasets".equals(servletPath) && pathInfo.endsWith("/rename")) {
                String datasetId = pathInfo.substring(1, pathInfo.length() - "/rename".length());
                assignServerClient().renameStaffDataset(datasetId, req.getParameter("name"));
                resp.sendRedirect(homeUrl(req, "Đã đổi tên bộ dữ liệu cán bộ"));
                return;
            }
            if ("/staff-datasets".equals(servletPath) && pathInfo.endsWith("/restore")) {
                String datasetId = pathInfo.substring(1, pathInfo.length() - "/restore".length());
                assignServerClient().restoreStaffDataset(datasetId);
                resp.sendRedirect(homeUrl(req, "Đã hiện lại bộ dữ liệu cán bộ"));
                return;
            }
            if ("/room-datasets".equals(servletPath) && pathInfo.endsWith("/archive")) {
                String datasetId = pathInfo.substring(1, pathInfo.length() - "/archive".length());
                assignServerClient().archiveRoomDataset(datasetId);
                resp.sendRedirect(homeUrl(req, "Đã ẩn bộ dữ liệu phòng"));
                return;
            }
            if ("/room-datasets".equals(servletPath) && pathInfo.endsWith("/rename")) {
                String datasetId = pathInfo.substring(1, pathInfo.length() - "/rename".length());
                assignServerClient().renameRoomDataset(datasetId, req.getParameter("name"));
                resp.sendRedirect(homeUrl(req, "Đã đổi tên bộ dữ liệu phòng"));
                return;
            }
            if ("/room-datasets".equals(servletPath) && pathInfo.endsWith("/restore")) {
                String datasetId = pathInfo.substring(1, pathInfo.length() - "/restore".length());
                assignServerClient().restoreRoomDataset(datasetId);
                resp.sendRedirect(homeUrl(req, "Đã hiện lại bộ dữ liệu phòng"));
                return;
            }
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception exception) {
            resp.sendRedirect(homeUrl(req, null, exception.getMessage()));
        }
    }

    private void handleBundleUpload(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        var staffPart = req.getPart("staffFile");
        var roomPart = req.getPart("roomFile");
        String staffFileName = staffPart.getSubmittedFileName();
        String roomFileName = roomPart.getSubmittedFileName();
        String staffName = req.getParameter("staffName");
        String roomName = req.getParameter("roomName");
        boolean hasStaff = staffFileName != null && !staffFileName.isBlank();
        boolean hasRoom = roomFileName != null && !roomFileName.isBlank();
        if (!hasStaff && !hasRoom) {
            throw new IOException("Vui lòng chọn ít nhất một file để tải lên");
        }

        String selectedStaffDatasetId = req.getParameter("staffDatasetId");
        String selectedRoomDatasetId = req.getParameter("roomDatasetId");

        if (hasStaff) {
            byte[] staffContent = staffPart.getInputStream().readAllBytes();
            var staffValidation = validator().validateStaffFile(staffFileName, staffContent);
            if (!staffValidation.valid()) {
                throw new IOException(staffValidation.message());
            }
            var staffResult = assignServerClient().uploadStaffDataset(staffName, staffFileName, staffContent);
            selectedStaffDatasetId = staffResult.dataset().datasetId();
        }

        if (hasRoom) {
            byte[] roomContent = roomPart.getInputStream().readAllBytes();
            var roomValidation = validator().validateRoomFile(roomFileName, roomContent);
            if (!roomValidation.valid()) {
                throw new IOException(roomValidation.message());
            }
            var roomResult = assignServerClient().uploadRoomDataset(roomName, roomFileName, roomContent);
            selectedRoomDatasetId = roomResult.dataset().datasetId();
        }

        String message = hasStaff && hasRoom
                ? "Đã tải 2 bộ dữ liệu"
                : (hasStaff ? "Đã tải bộ dữ liệu cán bộ" : "Đã tải bộ dữ liệu phòng");
        resp.sendRedirect(homeUrl(
                req,
                message,
                null,
                selectedStaffDatasetId,
                selectedRoomDatasetId,
                req.getParameter("branchName"),
                req.getParameter("requestedStaffCount"),
                req.getParameter("requestedRoomCount"),
                req.getParameter("sessionCount")
        ));
    }

    private void handleStaffUpload(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        byte[] content = req.getPart("file").getInputStream().readAllBytes();
        String fileName = req.getPart("file").getSubmittedFileName();
        String name = req.getParameter("name");
        var validation = validator().validateStaffFile(fileName, content);
        if (!validation.valid()) {
            throw new IOException(validation.message());
        }
        var result = assignServerClient().uploadStaffDataset(name, fileName, content);
        String message = result.reused()
                ? "Đã dùng lại bộ dữ liệu cán bộ có sẵn"
                : "Đã tải bộ dữ liệu cán bộ mới";
        resp.sendRedirect(homeUrl(
                req,
                message,
                null,
                result.dataset().datasetId(),
                req.getParameter("roomDatasetId"),
                req.getParameter("branchName"),
                req.getParameter("requestedStaffCount"),
                req.getParameter("requestedRoomCount"),
                req.getParameter("sessionCount")
        ));
    }

    private void handleRoomUpload(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        byte[] content = req.getPart("file").getInputStream().readAllBytes();
        String fileName = req.getPart("file").getSubmittedFileName();
        String name = req.getParameter("name");
        var validation = validator().validateRoomFile(fileName, content);
        if (!validation.valid()) {
            throw new IOException(validation.message());
        }
        var result = assignServerClient().uploadRoomDataset(name, fileName, content);
        String message = result.reused()
                ? "Đã dùng lại bộ dữ liệu phòng có sẵn"
                : "Đã tải bộ dữ liệu phòng mới";
        resp.sendRedirect(homeUrl(
                req,
                message,
                null,
                req.getParameter("staffDatasetId"),
                result.dataset().datasetId(),
                req.getParameter("branchName"),
                req.getParameter("requestedStaffCount"),
                req.getParameter("requestedRoomCount"),
                req.getParameter("sessionCount")
        ));
    }

    private String homeUrl(HttpServletRequest req, String message) {
        return homeUrl(req, message, null);
    }

    private String homeUrl(HttpServletRequest req, String message, String error) {
        return homeUrl(
                req,
                message,
                error,
                req.getParameter("staffDatasetId"),
                req.getParameter("roomDatasetId"),
                firstNonBlank(req.getParameter("branchName"), req.getParameter("name")),
                req.getParameter("requestedStaffCount"),
                req.getParameter("requestedRoomCount"),
                req.getParameter("sessionCount")
        );
    }

    private String homeUrl(
            HttpServletRequest req,
            String message,
            String error,
            String staffDatasetId,
            String roomDatasetId,
            String branchName,
            String requestedStaffCount,
            String requestedRoomCount,
            String sessionCount
    ) {
        StringBuilder url = new StringBuilder(req.getContextPath()).append("/?");
        appendQuery(url, "includeArchived", req.getParameter("includeArchived"));
        appendQuery(url, "message", message);
        appendQuery(url, "error", error);
        appendQuery(url, "staffDatasetId", staffDatasetId);
        appendQuery(url, "roomDatasetId", roomDatasetId);
        appendQuery(url, "branchName", branchName);
        appendQuery(url, "requestedStaffCount", requestedStaffCount);
        appendQuery(url, "requestedRoomCount", requestedRoomCount);
        appendQuery(url, "sessionCount", sessionCount);
        return url.toString();
    }

    private void appendQuery(StringBuilder url, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (url.charAt(url.length() - 1) != '?') {
            url.append('&');
        }
        url.append(java.net.URLEncoder.encode(key, java.nio.charset.StandardCharsets.UTF_8));
        url.append('=');
        url.append(java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8));
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }
}
