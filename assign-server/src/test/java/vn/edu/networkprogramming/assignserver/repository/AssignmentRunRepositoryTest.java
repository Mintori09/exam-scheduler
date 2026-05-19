package vn.edu.networkprogramming.assignserver.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import vn.edu.networkprogramming.assignserver.model.AssignmentRun;

class AssignmentRunRepositoryTest {

    @Test
    void storesAndLoadsAssignmentRuns() throws Exception {
        Path dbFile = Files.createTempFile("assign-run", ".db");
        AssignmentRunRepository repository = new AssignmentRunRepository(dbFile);
        repository.initialize();

        AssignmentRun saved = new AssignmentRun(
                "run-1",
                "FAILED",
                "Khong tim thay phan cong hop le",
                Instant.parse("2026-05-07T10:15:30Z"),
                Instant.parse("2026-05-07T10:15:35Z"),
                3,
                1,
                2,
                2,
                "FAILED",
                "Loi xuat file",
                null,
                null
        );

        repository.createRun(saved);
        repository.completeRun("run-1", "FAILED", "Khong tim thay phan cong hop le", 1, "[]", "[]");

        List<AssignmentRun> runs = repository.findAll();
        assertEquals(1, runs.size());
        assertEquals("run-1", runs.get(0).assignmentId());
        assertEquals("FAILED", runs.get(0).status());

        AssignmentRun loaded = repository.findById("run-1");
        assertNotNull(loaded);
        assertEquals("Khong tim thay phan cong hop le", loaded.message());
        assertEquals(3, loaded.sessionCount());
    }
}
