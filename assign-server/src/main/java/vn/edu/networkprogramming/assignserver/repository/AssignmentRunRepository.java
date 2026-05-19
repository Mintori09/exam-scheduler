package vn.edu.networkprogramming.assignserver.repository;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import vn.edu.networkprogramming.assignserver.model.AssignmentFileContent;
import vn.edu.networkprogramming.assignserver.model.AssignmentRun;
import vn.edu.networkprogramming.assignserver.model.StoredAssignmentRun;

public class AssignmentRunRepository {

    private final String jdbcUrl;

    public AssignmentRunRepository(Path dbPath) {
        this.jdbcUrl = "jdbc:sqlite:" + dbPath.toAbsolutePath();
    }

    public void initialize() throws SQLException {
        try (Connection connection = open(); Statement statement = connection.createStatement()) {
            statement.execute("pragma journal_mode = WAL");
            statement.execute("pragma busy_timeout = 10000");
            statement.execute("""
                    create table if not exists assignment_runs (
                        assignment_id text primary key,
                        status text not null,
                        message text,
                        created_at text not null,
                        updated_at text not null,
                        session_count integer not null,
                        completed_session_count integer not null default 0,
                        room_count integer not null,
                        staff_count integer not null,
                        invigilator_file_path text,
                        monitor_file_path text,
                        sessions_json text not null,
                        summary_json text not null
                    )
                    """);
            ensureColumn(statement, "assignment_runs", "updated_at", "text");
            ensureColumn(statement, "assignment_runs", "completed_session_count", "integer not null default 0");
            statement.execute("""
                    create table if not exists assignment_sessions (
                        assignment_id text not null,
                        session_no integer not null,
                        session_json text not null,
                        summary_json text not null,
                        created_at text not null,
                        primary key (assignment_id, session_no)
                    )
                    """);
            statement.execute("""
                    create table if not exists assignment_files (
                        assignment_id text not null,
                        file_role text not null,
                        original_name text not null,
                        mime_type text not null,
                        content_blob blob not null,
                        created_at text not null,
                        primary key (assignment_id, file_role)
                    )
                    """);
        }
    }

    public void createRun(AssignmentRun run) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     insert into assignment_runs (
                         assignment_id, status, message, created_at, updated_at, session_count, completed_session_count,
                         room_count, staff_count, invigilator_file_path, monitor_file_path, sessions_json, summary_json
                     ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                     """)) {
            bindRun(statement, run, "[]", "[]");
            statement.executeUpdate();
        }
    }

    public void updateRunStatus(String assignmentId, String status, String message, int completedSessionCount) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     update assignment_runs
                     set status = ?, message = ?, completed_session_count = ?, updated_at = ?
                     where assignment_id = ?
                     """)) {
            statement.setString(1, status);
            statement.setString(2, message);
            statement.setInt(3, completedSessionCount);
            statement.setString(4, Instant.now().toString());
            statement.setString(5, assignmentId);
            statement.executeUpdate();
        }
    }

    public void completeRun(
            String assignmentId,
            String status,
            String message,
            int completedSessionCount,
            String sessionsJson,
            String summaryJson
    ) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     update assignment_runs
                     set status = ?, message = ?, completed_session_count = ?, updated_at = ?,
                         sessions_json = ?, summary_json = ?
                     where assignment_id = ?
                     """)) {
            statement.setString(1, status);
            statement.setString(2, message);
            statement.setInt(3, completedSessionCount);
            statement.setString(4, Instant.now().toString());
            statement.setString(5, sessionsJson);
            statement.setString(6, summaryJson);
            statement.setString(7, assignmentId);
            statement.executeUpdate();
        }
    }

    public void upsertSession(String assignmentId, int sessionNo, String sessionJson, String summaryJson) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     insert into assignment_sessions (assignment_id, session_no, session_json, summary_json, created_at)
                     values (?, ?, ?, ?, ?)
                     on conflict(assignment_id, session_no) do update set
                        session_json = excluded.session_json,
                        summary_json = excluded.summary_json
                     """)) {
            statement.setString(1, assignmentId);
            statement.setInt(2, sessionNo);
            statement.setString(3, sessionJson);
            statement.setString(4, summaryJson);
            statement.setString(5, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    public List<String> findSessionJsonByAssignmentId(String assignmentId) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     select session_json
                     from assignment_sessions
                     where assignment_id = ?
                     order by session_no asc
                     """)) {
            statement.setString(1, assignmentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<String> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(resultSet.getString("session_json"));
                }
                return result;
            }
        }
    }

    public String findSessionJsonByAssignmentIdAndSessionNo(String assignmentId, int sessionNo) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     select session_json
                     from assignment_sessions
                     where assignment_id = ? and session_no = ?
                     """)) {
            statement.setString(1, assignmentId);
            statement.setInt(2, sessionNo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getString("session_json");
            }
        }
    }

    public List<String> findSummaryJsonByAssignmentId(String assignmentId) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     select summary_json
                     from assignment_sessions
                     where assignment_id = ?
                     order by session_no asc
                     """)) {
            statement.setString(1, assignmentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<String> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(resultSet.getString("summary_json"));
                }
                return result;
            }
        }
    }

    public void upsertFile(String assignmentId, String role, String originalName, String mimeType, byte[] content) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     insert into assignment_files (assignment_id, file_role, original_name, mime_type, content_blob, created_at)
                     values (?, ?, ?, ?, ?, ?)
                     on conflict(assignment_id, file_role) do update set
                         original_name = excluded.original_name,
                         mime_type = excluded.mime_type,
                         content_blob = excluded.content_blob
                     """)) {
            statement.setString(1, assignmentId);
            statement.setString(2, role);
            statement.setString(3, originalName);
            statement.setString(4, mimeType);
            statement.setBytes(5, content);
            statement.setString(6, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    public AssignmentFileContent findFile(String assignmentId, String role) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     select original_name, mime_type, content_blob
                     from assignment_files
                     where assignment_id = ? and file_role = ?
                     """)) {
            statement.setString(1, assignmentId);
            statement.setString(2, role);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new AssignmentFileContent(
                        role,
                        resultSet.getString("original_name"),
                        resultSet.getString("mime_type"),
                        resultSet.getBytes("content_blob")
                );
            }
        }
    }

    public List<AssignmentRun> findAll() throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     select assignment_id, status, message, created_at, updated_at, session_count, completed_session_count,
                            room_count, staff_count, invigilator_file_path, monitor_file_path
                     from assignment_runs
                     order by created_at desc
                     """);
             ResultSet resultSet = statement.executeQuery()) {
            List<AssignmentRun> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(map(resultSet));
            }
            return result;
        }
    }

    public AssignmentRun findById(String assignmentId) throws SQLException {
        StoredAssignmentRun stored = findStoredById(assignmentId);
        return stored == null ? null : stored.run();
    }

    public StoredAssignmentRun findStoredById(String assignmentId) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     select assignment_id, status, message, created_at, updated_at, session_count, completed_session_count,
                            room_count, staff_count, invigilator_file_path, monitor_file_path, sessions_json, summary_json
                     from assignment_runs
                     where assignment_id = ?
                     """)) {
            statement.setString(1, assignmentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new StoredAssignmentRun(
                        map(resultSet),
                        resultSet.getString("sessions_json"),
                        resultSet.getString("summary_json")
                );
            }
        }
    }

    private void bindRun(PreparedStatement statement, AssignmentRun run, String sessionsJson, String summaryJson) throws SQLException {
        statement.setString(1, run.assignmentId());
        statement.setString(2, run.status());
        statement.setString(3, run.message());
        statement.setString(4, run.createdAt().toString());
        statement.setString(5, run.updatedAt().toString());
        statement.setInt(6, run.sessionCount());
        statement.setInt(7, run.completedSessionCount());
        statement.setInt(8, run.roomCount());
        statement.setInt(9, run.staffCount());
        statement.setString(10, run.invigilatorFilePath());
        statement.setString(11, run.monitorFilePath());
        statement.setString(12, sessionsJson);
        statement.setString(13, summaryJson);
    }

    private AssignmentRun map(ResultSet resultSet) throws SQLException {
        String createdAtRaw = resultSet.getString("created_at");
        String updatedAtRaw = resultSet.getString("updated_at");
        Instant createdAt = Instant.parse(createdAtRaw);
        Instant updatedAt = updatedAtRaw == null || updatedAtRaw.isBlank() ? createdAt : Instant.parse(updatedAtRaw);
        return new AssignmentRun(
                resultSet.getString("assignment_id"),
                resultSet.getString("status"),
                resultSet.getString("message"),
                createdAt,
                updatedAt,
                resultSet.getInt("session_count"),
                resultSet.getInt("completed_session_count"),
                resultSet.getInt("room_count"),
                resultSet.getInt("staff_count"),
                resultSet.getString("invigilator_file_path"),
                resultSet.getString("monitor_file_path")
        );
    }

    private void ensureColumn(Statement statement, String table, String column, String ddl) throws SQLException {
        try (ResultSet rs = statement.executeQuery("pragma table_info(" + table + ")")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    return;
                }
            }
        }
        statement.execute("alter table " + table + " add column " + column + " " + ddl);
    }

    private Connection open() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("pragma busy_timeout = 10000");
            statement.execute("pragma journal_mode = WAL");
        }
        return connection;
    }
}
