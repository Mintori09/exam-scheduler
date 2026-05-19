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
import vn.edu.networkprogramming.clientweb.model.AssignmentDetailView;
import vn.edu.networkprogramming.clientweb.model.AssignmentRunView;
import vn.edu.networkprogramming.clientweb.model.SessionAssignmentView;

public class AssignServerClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String serverBaseUrl;

    public AssignServerClient(String serverBaseUrl, JsonService jsonService) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = jsonService.objectMapper();
        this.serverBaseUrl = serverBaseUrl.endsWith("/") ? serverBaseUrl.substring(0, serverBaseUrl.length() - 1) : serverBaseUrl;
    }

    public AssignmentRunView createAssignment(String staffFilename, byte[] staffContent, String roomFilename, byte[] roomContent, int sessionCount)
            throws IOException, InterruptedException {
        String boundary = "----Boundary" + UUID.randomUUID();
        byte[] body = MultipartBodyBuilder.create(boundary)
                .addFile("staffFile", staffFilename, staffContent)
                .addFile("roomFile", roomFilename, roomContent)
                .addField("sessionCount", String.valueOf(sessionCount))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/api/assignments"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return parseResponse(response, AssignmentRunView.class);
    }

    public List<AssignmentRunView> listAssignments() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/api/assignments"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        ensureSuccess(response);
        try {
            return List.of(objectMapper.readValue(response.body(), AssignmentRunView[].class));
        } catch (IOException exception) {
            String body = response.body();
            String preview = body == null ? "" : body.substring(0, Math.min(body.length(), 200));
            throw new IOException("Khong the doc danh sach assignment tu assign-server. HTTP "
                    + response.statusCode() + ", body preview: " + preview, exception);
        }
    }

    public AssignmentDetailView getAssignmentDetail(String assignmentId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/api/assignments/" + encode(assignmentId)))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return parseResponse(response, AssignmentDetailView.class);
    }

    public SessionAssignmentView getSessionDetail(String assignmentId, int sessionNo) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/api/assignments/" + encode(assignmentId) + "/sessions/" + sessionNo))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return parseResponse(response, SessionAssignmentView.class);
    }

    public HttpResponse<byte[]> download(String assignmentId, String type) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/api/assignments/" + encode(assignmentId) + "/downloads/" + type))
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
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
}
