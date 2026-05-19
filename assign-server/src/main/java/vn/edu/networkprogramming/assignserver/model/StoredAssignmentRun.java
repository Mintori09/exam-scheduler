package vn.edu.networkprogramming.assignserver.model;

public record StoredAssignmentRun(
        AssignmentRun run,
        String sessionsJson,
        String summaryJson
) {
}
