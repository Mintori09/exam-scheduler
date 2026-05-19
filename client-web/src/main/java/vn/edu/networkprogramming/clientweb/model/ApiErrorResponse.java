package vn.edu.networkprogramming.clientweb.model;

public record ApiErrorResponse(
        String status,
        String message
) {
}
