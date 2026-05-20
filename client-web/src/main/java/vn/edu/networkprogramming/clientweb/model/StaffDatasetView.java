package vn.edu.networkprogramming.clientweb.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record StaffDatasetView(
        String datasetId,
        String name,
        String contentHash,
        String originalFileName,
        int staffCount,
        Instant createdAt,
        boolean archived
) {
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(APP_ZONE);

    public String getDatasetId() { return datasetId; }
    public String getName() { return name; }
    public String getContentHash() { return contentHash; }
    public String getOriginalFileName() { return originalFileName; }
    public int getStaffCount() { return staffCount; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isArchived() { return archived; }
    public String getCreatedAtDisplay() { return createdAt == null ? "" : DISPLAY_TIME_FORMATTER.format(createdAt); }
    public String getShortHash() { return contentHash == null ? "" : contentHash.substring(0, Math.min(12, contentHash.length())); }
}
