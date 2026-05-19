package vn.edu.networkprogramming.clientweb.model;

public record ClientValidationResult(
        boolean valid,
        String message
) {
}
