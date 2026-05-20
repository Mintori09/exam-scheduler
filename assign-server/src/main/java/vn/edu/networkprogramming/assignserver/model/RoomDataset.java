package vn.edu.networkprogramming.assignserver.model;

import java.time.Instant;

public record RoomDataset(
        String datasetId,
        String name,
        String contentHash,
        String originalFileName,
        int roomCount,
        Instant createdAt,
        boolean archived
) {
}
