package vn.edu.networkprogramming.assignserver.model;

import java.time.Instant;

public record AssignmentRun(
        String assignmentId,
        String status,
        String message,
        Instant createdAt,
        Instant updatedAt,
        int sessionCount,
        int completedSessionCount,
        int roomCount,
        int staffCount,
        String outputStatus,
        String outputError,
        String invigilatorFilePath,
        String monitorFilePath
) {
}
