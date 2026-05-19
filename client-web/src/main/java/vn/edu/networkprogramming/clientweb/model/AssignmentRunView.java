package vn.edu.networkprogramming.clientweb.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(APP_ZONE);

    public String getAssignmentId() { return assignmentId; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getCreatedAtDisplay() { return formatDisplay(createdAt); }
    public String getUpdatedAtDisplay() { return formatDisplay(updatedAt); }
    public int getSessionCount() { return sessionCount; }
    public int getCompletedSessionCount() { return completedSessionCount; }
    public int getRoomCount() { return roomCount; }
    public int getStaffCount() { return staffCount; }
    public String getOutputStatus() { return outputStatus; }
    public String getOutputError() { return outputError; }
    public String getInvigilatorFilePath() { return invigilatorFilePath; }
    public String getMonitorFilePath() { return monitorFilePath; }

    private String formatDisplay(Instant value) {
        if (value == null) {
            return "";
        }
        return DISPLAY_TIME_FORMATTER.format(value);
    }
}
