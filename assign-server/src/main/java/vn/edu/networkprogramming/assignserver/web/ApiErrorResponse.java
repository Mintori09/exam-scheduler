package vn.edu.networkprogramming.assignserver.web;

public record ApiErrorResponse(
        String status,
        String message
) {
}
