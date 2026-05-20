package vn.edu.networkprogramming.clientweb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import vn.edu.networkprogramming.clientweb.model.ApiErrorResponse;
import vn.edu.networkprogramming.clientweb.model.BranchDetailView;
import vn.edu.networkprogramming.clientweb.model.BranchPreviewView;
import vn.edu.networkprogramming.clientweb.model.BranchSessionRecordView;
import vn.edu.networkprogramming.clientweb.model.DatasetUploadResultView;
import vn.edu.networkprogramming.clientweb.model.RoomDatasetView;
import vn.edu.networkprogramming.clientweb.model.ScheduleBranchView;
import vn.edu.networkprogramming.clientweb.model.StaffDatasetView;

public class AssignServerClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String serverBaseUrl;

    public AssignServerClient(String serverBaseUrl, JsonService jsonService) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = jsonService.objectMapper();
        this.serverBaseUrl = serverBaseUrl.endsWith("/") ? serverBaseUrl.substring(0, serverBaseUrl.length() - 1) : serverBaseUrl;
    }

    public DatasetUploadResultView<StaffDatasetView> uploadStaffDataset(String name, String filename, byte[] content)
            throws IOException, InterruptedException {
        return postFile("/api/staff-datasets", name, filename, content, StaffDatasetView.class);
    }

    public DatasetUploadResultView<RoomDatasetView> uploadRoomDataset(String name, String filename, byte[] content)
            throws IOException, InterruptedException {
        return postFile("/api/room-datasets", name, filename, content, RoomDatasetView.class);
    }

    public List<StaffDatasetView> listStaffDatasets(boolean includeArchived) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/api/staff-datasets?includeArchived=" + includeArchived))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        ensureSuccess(response);
        return List.of(objectMapper.readValue(response.body(), StaffDatasetView[].class));
    }

    public List<RoomDatasetView> listRoomDatasets(boolean includeArchived) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/api/room-datasets?includeArchived=" + includeArchived))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        ensureSuccess(response);
        return List.of(objectMapper.readValue(response.body(), RoomDatasetView[].class));
    }

    public void archiveStaffDataset(String datasetId) throws IOException, InterruptedException {
        postNoContent("/api/staff-datasets/" + encode(datasetId) + "/archive");
    }

    public void renameStaffDataset(String datasetId, String name) throws IOException, InterruptedException {
        postNoContent("/api/staff-datasets/" + encode(datasetId) + "/rename", "name=" + encodeValue(name));
    }

    public void restoreStaffDataset(String datasetId) throws IOException, InterruptedException {
        postNoContent("/api/staff-datasets/" + encode(datasetId) + "/restore");
    }

    public void archiveRoomDataset(String datasetId) throws IOException, InterruptedException {
        postNoContent("/api/room-datasets/" + encode(datasetId) + "/archive");
    }

    public void renameRoomDataset(String datasetId, String name) throws IOException, InterruptedException {
        postNoContent("/api/room-datasets/" + encode(datasetId) + "/rename", "name=" + encodeValue(name));
    }

    public void restoreRoomDataset(String datasetId) throws IOException, InterruptedException {
        postNoContent("/api/room-datasets/" + encode(datasetId) + "/restore");
    }

    public ScheduleBranchView createBranch(
            String name,
            String staffDatasetId,
            String roomDatasetId,
            int requestedStaffCount,
            int requestedRoomCount,
            int sessionCount
    )
            throws IOException, InterruptedException {
        String body = "name=" + encodeValue(name == null ? "" : name)
                + "&staffDatasetId=" + encodeValue(staffDatasetId)
                + "&roomDatasetId=" + encodeValue(roomDatasetId)
                + "&requestedStaffCount=" + requestedStaffCount
                + "&requestedRoomCount=" + requestedRoomCount
                + "&sessionCount=" + sessionCount;
        return postForm("/api/branches", body, ScheduleBranchView.class);
    }

    public ScheduleBranchView createNextSession(String branchId, int sessionCount) throws IOException, InterruptedException {
        return postForm(
                "/api/branches/" + encode(branchId) + "/sessions",
                "sessionCount=" + sessionCount,
                ScheduleBranchView.class
        );
    }

    public ScheduleBranchView resetBranch(String branchId, String name) throws IOException, InterruptedException {
        String body = "name=" + encodeValue(name == null ? "" : name);
        return postForm("/api/branches/" + encode(branchId) + "/reset", body, ScheduleBranchView.class);
    }

    public void archiveBranch(String branchId) throws IOException, InterruptedException {
        postNoContent("/api/branches/" + encode(branchId) + "/archive");
    }

    public void renameBranch(String branchId, String name) throws IOException, InterruptedException {
        postNoContent("/api/branches/" + encode(branchId) + "/rename", "name=" + encodeValue(name));
    }

    public void restoreBranch(String branchId) throws IOException, InterruptedException {
        postNoContent("/api/branches/" + encode(branchId) + "/restore");
    }

    public List<ScheduleBranchView> listBranches(boolean includeArchived) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/api/branches?includeArchived=" + includeArchived))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        ensureSuccess(response);
        return List.of(objectMapper.readValue(response.body(), ScheduleBranchView[].class));
    }

    public BranchDetailView getBranchDetail(String branchId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/api/branches/" + encode(branchId)))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return parseResponse(response, BranchDetailView.class);
    }

    public BranchSessionRecordView getBranchSessionDetail(String branchId, int sessionNo) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/api/branches/" + encode(branchId) + "/sessions/" + sessionNo))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return parseResponse(response, BranchSessionRecordView.class);
    }

    public BranchPreviewView previewBranch(String branchId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/api/branches/" + encode(branchId) + "/preview"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return parseResponse(response, BranchPreviewView.class);
    }

    public HttpResponse<byte[]> downloadBranchFile(String branchId, String type) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/api/branches/" + encode(branchId) + "/downloads/" + type))
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    private <T> DatasetUploadResultView<T> postFile(String path, String name, String filename, byte[] content, Class<T> datasetType)
            throws IOException, InterruptedException {
        String boundary = "----Boundary" + UUID.randomUUID();
        byte[] body = MultipartBodyBuilder.create(boundary)
                .addField("name", name == null ? "" : name)
                .addFile("file", filename, content)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + path))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        ensureSuccess(response);
        return objectMapper.readValue(
                response.body(),
                objectMapper.getTypeFactory().constructParametricType(DatasetUploadResultView.class, datasetType)
        );
    }

    private void postNoContent(String path) throws IOException, InterruptedException {
        postNoContent(path, "");
    }

    private void postNoContent(String path, String body) throws IOException, InterruptedException {
        postForm(path, body, ApiErrorResponse.class);
    }

    private <T> T postForm(String path, String body, Class<T> type) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return parseResponse(response, type);
    }

    private <T> T parseResponse(HttpResponse<String> response, Class<T> type) throws IOException {
        ensureSuccess(response);
        return objectMapper.readValue(response.body(), type);
    }

    private void ensureSuccess(HttpResponse<String> response) throws IOException {
        if (response.statusCode() / 100 == 2) {
            return;
        }
        String body = response.body();
        try {
            ApiErrorResponse error = objectMapper.readValue(body, ApiErrorResponse.class);
            throw new IOException(error.message());
        } catch (IOException parseException) {
            String preview = body == null ? "" : body.substring(0, Math.min(body.length(), 240));
            throw new IOException("Assign-server tra loi loi HTTP " + response.statusCode()
                    + ". Body preview: " + preview, parseException);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String encodeValue(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
