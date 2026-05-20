package vn.edu.networkprogramming.clientweb.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record ScheduleBranchView(
        String branchId,
        String name,
        String status,
        String message,
        Instant createdAt,
        Instant updatedAt,
        String staffDatasetId,
        String staffDatasetName,
        String roomDatasetId,
        String roomDatasetName,
        int requestedStaffCount,
        int requestedRoomCount,
        int nextSessionNo,
        int sessionCreatedCount,
        boolean archived,
        String outputStatus,
        String outputError
) {
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(APP_ZONE);

    public String getBranchId() { return branchId; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public String getStaffDatasetName() { return staffDatasetName; }
    public String getRoomDatasetName() { return roomDatasetName; }
    public int getRequestedStaffCount() { return requestedStaffCount; }
    public int getRequestedRoomCount() { return requestedRoomCount; }
    public int getNextSessionNo() { return nextSessionNo; }
    public int getSessionCreatedCount() { return sessionCreatedCount; }
    public boolean isArchived() { return archived; }
    public String getOutputStatus() { return outputStatus; }
    public String getOutputError() { return outputError; }
    public String getCreatedAtDisplay() { return createdAt == null ? "" : DISPLAY_TIME_FORMATTER.format(createdAt); }
    public String getUpdatedAtDisplay() { return updatedAt == null ? "" : DISPLAY_TIME_FORMATTER.format(updatedAt); }
}
