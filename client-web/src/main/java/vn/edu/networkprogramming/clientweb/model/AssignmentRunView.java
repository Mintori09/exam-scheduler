package vn.edu.networkprogramming.clientweb.model;

import java.time.Instant;

public record AssignmentRunView(
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
    public String getAssignmentId() { return assignmentId; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public int getSessionCount() { return sessionCount; }
    public int getCompletedSessionCount() { return completedSessionCount; }
    public int getRoomCount() { return roomCount; }
    public int getStaffCount() { return staffCount; }
    public String getOutputStatus() { return outputStatus; }
    public String getOutputError() { return outputError; }
    public String getInvigilatorFilePath() { return invigilatorFilePath; }
    public String getMonitorFilePath() { return monitorFilePath; }
}
