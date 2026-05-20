package vn.edu.networkprogramming.assignserver.model;

import java.time.Instant;

public record StaffDataset(
        String datasetId,
        String name,
        String contentHash,
        String originalFileName,
        int staffCount,
        Instant createdAt,
        boolean archived
) {
}
