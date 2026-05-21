package vn.edu.networkprogramming.clientweb.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record BranchSessionRecordView(
        String branchId,
        int sessionNo,
        long selectionSeed,
        int requestedStaffCount,
        int requestedRoomCount,
        List<String> selectedStaffCodes,
        List<String> selectedRoomNames,
        SessionAssignmentView session,
        AssignmentSummaryView summary,
        Instant createdAt
) {
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(APP_ZONE);

    public String getBranchId() { return branchId; }
    public int getSessionNo() { return sessionNo; }
    public long getSelectionSeed() { return selectionSeed; }
    public int getRequestedStaffCount() { return requestedStaffCount; }
    public int getRequestedRoomCount() { return requestedRoomCount; }
    public List<String> getSelectedStaffCodes() { return selectedStaffCodes; }
    public List<String> getSelectedRoomNames() { return selectedRoomNames; }
    public SessionAssignmentView getSession() { return session; }
    public AssignmentSummaryView getSummary() { return summary; }
    public String getCreatedAtDisplay() { return createdAt == null ? "" : DISPLAY_TIME_FORMATTER.format(createdAt); }
}
