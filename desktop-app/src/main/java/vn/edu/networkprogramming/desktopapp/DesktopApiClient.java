package vn.edu.networkprogramming.desktopapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

final class DesktopApiClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private String serverBaseUrl;

    DesktopApiClient(String serverBaseUrl) {
        setServerBaseUrl(serverBaseUrl);
    }

    void setServerBaseUrl(String serverBaseUrl) {
        String normalized = serverBaseUrl == null || serverBaseUrl.isBlank()
                ? "http://localhost:8081/assign-server"
                : serverBaseUrl.trim();
        this.serverBaseUrl = normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    String serverBaseUrl() {
        return serverBaseUrl;
    }

    List<StaffDatasetView> listStaffDatasets(boolean includeArchived) throws IOException, InterruptedException {
        HttpResponse<String> response = sendJsonGet("/api/staff-datasets?includeArchived=" + includeArchived);
        return List.of(objectMapper.readValue(response.body(), StaffDatasetView[].class));
    }

    List<RoomDatasetView> listRoomDatasets(boolean includeArchived) throws IOException, InterruptedException {
        HttpResponse<String> response = sendJsonGet("/api/room-datasets?includeArchived=" + includeArchived);
        return List.of(objectMapper.readValue(response.body(), RoomDatasetView[].class));
    }

    List<ScheduleBranchView> listBranches(boolean includeArchived) throws IOException, InterruptedException {
        HttpResponse<String> response = sendJsonGet("/api/branches?includeArchived=" + includeArchived);
        return List.of(objectMapper.readValue(response.body(), ScheduleBranchView[].class));
    }

    DatasetUploadResultView<StaffDatasetView> uploadStaffDataset(String name, String filename, byte[] content)
            throws IOException, InterruptedException {
        return postFile("/api/staff-datasets", name, filename, content, StaffDatasetView.class);
    }

    DatasetUploadResultView<RoomDatasetView> uploadRoomDataset(String name, String filename, byte[] content)
            throws IOException, InterruptedException {
        return postFile("/api/room-datasets", name, filename, content, RoomDatasetView.class);
    }

    ScheduleBranchView createBranch(
            String name,
            String staffDatasetId,
            String roomDatasetId,
            int requestedStaffCount,
            int requestedRoomCount,
            int sessionCount
    ) throws IOException, InterruptedException {
        String body = "name=" + encode(name)
                + "&staffDatasetId=" + encode(staffDatasetId)
                + "&roomDatasetId=" + encode(roomDatasetId)
                + "&requestedStaffCount=" + requestedStaffCount
                + "&requestedRoomCount=" + requestedRoomCount
                + "&sessionCount=" + sessionCount;
        return postForm("/api/branches", body, ScheduleBranchView.class);
    }

    ScheduleBranchView appendSessions(
            String branchId,
            int sessionCount,
            Integer requestedStaffCount,
            Integer requestedRoomCount
    ) throws IOException, InterruptedException {
        StringBuilder body = new StringBuilder("sessionCount=").append(sessionCount);
        if (requestedStaffCount != null) {
            body.append("&requestedStaffCount=").append(requestedStaffCount);
        }
        if (requestedRoomCount != null) {
            body.append("&requestedRoomCount=").append(requestedRoomCount);
        }
        return postForm("/api/branches/" + encode(branchId) + "/sessions", body.toString(), ScheduleBranchView.class);
    }

    ScheduleBranchView resetBranch(String branchId, String name) throws IOException, InterruptedException {
        return postForm("/api/branches/" + encode(branchId) + "/reset", "name=" + encode(name), ScheduleBranchView.class);
    }

    void renameBranch(String branchId, String name) throws IOException, InterruptedException {
        postNoContent("/api/branches/" + encode(branchId) + "/rename", "name=" + encode(name));
    }

    void archiveBranch(String branchId) throws IOException, InterruptedException {
        postNoContent("/api/branches/" + encode(branchId) + "/archive", "");
    }

    void restoreBranch(String branchId) throws IOException, InterruptedException {
        postNoContent("/api/branches/" + encode(branchId) + "/restore", "");
    }

    void renameStaffDataset(String datasetId, String name) throws IOException, InterruptedException {
        postNoContent("/api/staff-datasets/" + encode(datasetId) + "/rename", "name=" + encode(name));
    }

    void archiveStaffDataset(String datasetId) throws IOException, InterruptedException {
        postNoContent("/api/staff-datasets/" + encode(datasetId) + "/archive", "");
    }

    void restoreStaffDataset(String datasetId) throws IOException, InterruptedException {
        postNoContent("/api/staff-datasets/" + encode(datasetId) + "/restore", "");
    }

    void renameRoomDataset(String datasetId, String name) throws IOException, InterruptedException {
        postNoContent("/api/room-datasets/" + encode(datasetId) + "/rename", "name=" + encode(name));
    }

    void archiveRoomDataset(String datasetId) throws IOException, InterruptedException {
        postNoContent("/api/room-datasets/" + encode(datasetId) + "/archive", "");
    }

    void restoreRoomDataset(String datasetId) throws IOException, InterruptedException {
        postNoContent("/api/room-datasets/" + encode(datasetId) + "/restore", "");
    }

    BranchDetailView getBranchDetail(String branchId) throws IOException, InterruptedException {
        HttpResponse<String> response = sendJsonGet("/api/branches/" + encode(branchId));
        return objectMapper.readValue(response.body(), BranchDetailView.class);
    }

    BranchPreviewView getBranchPreview(String branchId) throws IOException, InterruptedException {
        HttpResponse<String> response = sendJsonGet("/api/branches/" + encode(branchId) + "/preview");
        return objectMapper.readValue(response.body(), BranchPreviewView.class);
    }

    BranchSessionRecordView getBranchSession(String branchId, int sessionNo) throws IOException, InterruptedException {
        HttpResponse<String> response = sendJsonGet("/api/branches/" + encode(branchId) + "/sessions/" + sessionNo);
        return objectMapper.readValue(response.body(), BranchSessionRecordView.class);
    }

    DownloadedFile downloadFile(String branchId, String type) throws IOException, InterruptedException {
        HttpRequest request = newRequestBuilder()
                .uri(URI.create(serverBaseUrl + "/api/branches/" + encode(branchId) + "/downloads/" + type))
                .GET()
                .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        ensureSuccess(response.statusCode(), new String(response.body(), StandardCharsets.UTF_8));
        String filename = response.headers()
                .firstValue("Content-Disposition")
                .map(value -> {
                    int index = value.indexOf("filename=\"");
                    if (index < 0) {
                        return type + ".xlsx";
                    }
                    return value.substring(index + 10, value.lastIndexOf('"'));
                })
                .orElse(type + ".xlsx");
        return new DownloadedFile(filename, response.body());
    }

    DownloadedFile downloadSessionFile(String branchId, int sessionNo, String type) throws IOException, InterruptedException {
        HttpRequest request = newRequestBuilder()
                .uri(URI.create(serverBaseUrl + "/api/branches/" + encode(branchId) + "/sessions/" + sessionNo + "/downloads/" + type))
                .GET()
                .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        ensureSuccess(response.statusCode(), new String(response.body(), StandardCharsets.UTF_8));
        String filename = response.headers()
                .firstValue("Content-Disposition")
                .map(value -> {
                    int index = value.indexOf("filename=\"");
                    if (index < 0) {
                        return "ca-" + sessionNo + "-" + type + ".xlsx";
                    }
                    return value.substring(index + 10, value.lastIndexOf('"'));
                })
                .orElse("ca-" + sessionNo + "-" + type + ".xlsx");
        return new DownloadedFile(filename, response.body());
    }

    private HttpResponse<String> sendJsonGet(String path) throws IOException, InterruptedException {
        HttpRequest request = newRequestBuilder()
                .uri(URI.create(serverBaseUrl + path))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        ensureSuccess(response.statusCode(), response.body());
        return response;
    }

    private void postNoContent(String path, String body) throws IOException, InterruptedException {
        postForm(path, body, ApiErrorResponse.class);
    }

    private <T> T postForm(String path, String body, Class<T> type) throws IOException, InterruptedException {
        HttpRequest request = newRequestBuilder()
                .uri(URI.create(serverBaseUrl + path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        ensureSuccess(response.statusCode(), response.body());
        return objectMapper.readValue(response.body(), type);
    }

    private <T> DatasetUploadResultView<T> postFile(String path, String name, String filename, byte[] content, Class<T> datasetType)
            throws IOException, InterruptedException {
        String boundary = "----Boundary" + UUID.randomUUID();
        byte[] body = new MultipartBuilder(boundary)
                .addField("name", name == null ? "" : name)
                .addFile("file", filename, content)
                .build();
        HttpRequest request = newRequestBuilder()
                .uri(URI.create(serverBaseUrl + path))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        ensureSuccess(response.statusCode(), response.body());
        return objectMapper.readValue(
                response.body(),
                objectMapper.getTypeFactory().constructParametricType(DatasetUploadResultView.class, datasetType)
        );
    }

    private HttpRequest.Builder newRequestBuilder() {
        return HttpRequest.newBuilder()
                .header("User-Agent", "ExamSchedulerDesktop/1.0")
                .header("Accept", "application/json, text/plain, */*");
    }

    private void ensureSuccess(int statusCode, String body) throws IOException {
        if (statusCode / 100 == 2) {
            return;
        }
        try {
            ApiErrorResponse error = objectMapper.readValue(body, ApiErrorResponse.class);
            throw new IOException(error.message());
        } catch (Exception parseException) {
            throw new IOException("Backend trả lỗi HTTP " + statusCode + ": " + body, parseException);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    record DownloadedFile(String filename, byte[] content) {
    }

    private static final class MultipartBuilder {
        private final String boundary;
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        private MultipartBuilder(String boundary) {
            this.boundary = boundary;
        }

        MultipartBuilder addField(String name, String value) throws IOException {
            outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            outputStream.write((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
            return this;
        }

        MultipartBuilder addFile(String fieldName, String filename, byte[] content) throws IOException {
            outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + filename + "\"\r\n")
                    .getBytes(StandardCharsets.UTF_8));
            outputStream.write("Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\r\n\r\n"
                    .getBytes(StandardCharsets.UTF_8));
            outputStream.write(content);
            outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
            return this;
        }

        byte[] build() throws IOException {
            outputStream.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            return outputStream.toByteArray();
        }
    }
}
