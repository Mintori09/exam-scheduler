package vn.edu.networkprogramming.assignserver.model;

public record AssignmentFileContent(
        String role,
        String fileName,
        String mimeType,
        byte[] content
) {
}
