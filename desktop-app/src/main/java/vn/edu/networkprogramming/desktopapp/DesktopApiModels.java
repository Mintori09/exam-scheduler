package vn.edu.networkprogramming.desktopapp;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

final class DesktopApiModels {

    private DesktopApiModels() {
    }

    static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    static final DateTimeFormatter DISPLAY_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(APP_ZONE);

    static String formatInstant(Instant value) {
        return value == null ? "" : DISPLAY_TIME_FORMATTER.format(value);
    }

    static String shortHash(String value) {
        return value == null ? "" : value.substring(0, Math.min(12, value.length()));
    }
}

record ApiErrorResponse(String status, String message) {
}

record DatasetUploadResultView<T>(T dataset, boolean reused) {
}

record StaffDatasetView(
        String datasetId,
        String name,
        String contentHash,
        String originalFileName,
        int staffCount,
        Instant createdAt,
        boolean archived
) {
    String createdAtDisplay() {
        return DesktopApiModels.formatInstant(createdAt);
    }

    String shortHash() {
        return DesktopApiModels.shortHash(contentHash);
    }
}

record RoomDatasetView(
        String datasetId,
        String name,
        String contentHash,
        String originalFileName,
        int roomCount,
        Instant createdAt,
        boolean archived
) {
    String createdAtDisplay() {
        return DesktopApiModels.formatInstant(createdAt);
    }

    String shortHash() {
        return DesktopApiModels.shortHash(contentHash);
    }
}

record ScheduleBranchView(
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
    String createdAtDisplay() {
        return DesktopApiModels.formatInstant(createdAt);
    }

    String updatedAtDisplay() {
        return DesktopApiModels.formatInstant(updatedAt);
    }
}

record BranchDetailView(
        ScheduleBranchView branch,
        List<BranchSessionRecordView> sessions,
        boolean invigilatorFileAvailable,
        boolean monitorFileAvailable
) {
}

record BranchPreviewView(
        boolean canCreateNextSession,
        int nextSessionNo,
        int staffDatasetSize,
        int roomDatasetSize,
        int usedPairCount,
        int constrainedStaffCount,
        String message
) {
}

record BranchSessionRecordView(
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
    String createdAtDisplay() {
        return DesktopApiModels.formatInstant(createdAt);
    }
}

record AssignmentSummaryView(
        int sessionNo,
        int roomAssignmentCount,
        int hallMonitorCount
) {
}

record SessionAssignmentView(
        int sessionNo,
        List<RoomAssignmentView> roomAssignments,
        List<HallMonitorAssignmentView> hallMonitorAssignments
) {
}

record RoomAssignmentView(
        RoomRecordView room,
        StaffRecordView invigilatorOne,
        StaffRecordView invigilatorTwo
) {
}

record HallMonitorAssignmentView(
        StaffRecordView staff,
        String rangeText,
        int roomCount
) {
}

record StaffRecordView(
        int stt,
        String fullName,
        String birthDate,
        String staffCode,
        String department
) {
}

record RoomRecordView(
        int stt,
        String roomName,
        String location
) {
}
