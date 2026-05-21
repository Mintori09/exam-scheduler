package vn.edu.networkprogramming.assignserver.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import vn.edu.networkprogramming.assignserver.model.ScheduleBranch;

class SchedulingRepositoryMigrationTest {

    @Test
    void initializeMigratesExistingBranchSessionsTable() throws Exception {
        String jdbcUrl = "jdbc:h2:mem:migration_test_" + System.nanoTime() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, "sa", "");
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    create table staff_datasets (
                        dataset_id varchar(191) primary key,
                        name varchar(255) not null,
                        content_hash varchar(64) not null,
                        original_file_name varchar(255) not null,
                        mime_type varchar(255) not null,
                        content_blob longblob not null,
                        staff_count int not null,
                        created_at varchar(40) not null
                    )
                    """);
            statement.execute("""
                    create table room_datasets (
                        dataset_id varchar(191) primary key,
                        name varchar(255) not null,
                        content_hash varchar(64) not null,
                        original_file_name varchar(255) not null,
                        mime_type varchar(255) not null,
                        content_blob longblob not null,
                        room_count int not null,
                        created_at varchar(40) not null
                    )
                    """);
            statement.execute("""
                    create table schedule_branches (
                        branch_id varchar(191) primary key,
                        name varchar(255) not null,
                        status varchar(40) not null,
                        message text,
                        created_at varchar(40) not null,
                        updated_at varchar(40) not null,
                        staff_dataset_id varchar(191) not null,
                        room_dataset_id varchar(191) not null,
                        requested_staff_count int not null,
                        requested_room_count int not null,
                        next_session_no int not null
                    )
                    """);
            statement.execute("""
                    create table branch_sessions (
                        branch_id varchar(191) not null,
                        session_no int not null,
                        selection_seed bigint not null,
                        selected_staff_codes_json longtext not null,
                        selected_room_names_json longtext not null,
                        session_json longtext not null,
                        summary_json longtext not null,
                        created_at varchar(40) not null,
                        primary key (branch_id, session_no)
                    )
                    """);
            statement.execute("""
                    insert into staff_datasets (
                        dataset_id, name, content_hash, original_file_name, mime_type, content_blob, staff_count, created_at
                    ) values (
                        'staff-1', 'Can bo 1', 'hash-1', 'canbo.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
                        X'00', 10, '2026-05-21T00:00:00Z'
                    )
                    """);
            statement.execute("""
                    insert into room_datasets (
                        dataset_id, name, content_hash, original_file_name, mime_type, content_blob, room_count, created_at
                    ) values (
                        'room-1', 'Phong 1', 'hash-2', 'phong.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
                        X'00', 5, '2026-05-21T00:00:00Z'
                    )
                    """);
            statement.execute("""
                    insert into schedule_branches (
                        branch_id, name, status, message, created_at, updated_at,
                        staff_dataset_id, room_dataset_id, requested_staff_count, requested_room_count, next_session_no
                    ) values (
                        'branch-1', 'Nhanh 1', 'SUCCESS', 'Da tao', '2026-05-21T00:00:00Z', '2026-05-21T00:00:00Z',
                        'staff-1', 'room-1', 8, 3, 2
                    )
                    """);
            statement.execute("""
                    insert into branch_sessions (
                        branch_id, session_no, selection_seed, selected_staff_codes_json, selected_room_names_json,
                        session_json, summary_json, created_at
                    ) values (
                        'branch-1', 1, 12345, '[]', '[]', '{}', '{}', '2026-05-21T00:00:00Z'
                    )
                    """);
        }

        SchedulingRepository repository = new SchedulingRepository(jdbcUrl, "sa", "");
        repository.initialize();

        var rows = repository.findBranchSessionRows("branch-1");
        assertEquals(1, rows.size());
        assertEquals(8, rows.get(0).requestedStaffCount());
        assertEquals(3, rows.get(0).requestedRoomCount());

        ScheduleBranch branch = repository.findBranchById("branch-1");
        assertEquals(1, branch.sessionCreatedCount());
        assertEquals("NONE", branch.outputStatus());
    }
}
