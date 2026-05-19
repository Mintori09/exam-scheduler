package vn.edu.networkprogramming.assignserver.model;

import java.util.List;

public record AssignmentResult(
        String status,
        String message,
        List<SessionAssignment> sessions
) {
}
