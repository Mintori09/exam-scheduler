package vn.edu.networkprogramming.assignserver.model;

import java.util.List;

public record AssignmentDetail(
        AssignmentRun run,
        List<AssignmentSummary> sessionSummaries,
        boolean invigilatorFileAvailable,
        boolean monitorFileAvailable
) {
}
