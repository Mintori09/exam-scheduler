package vn.edu.networkprogramming.assignserver.repository;

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
import vn.edu.networkprogramming.assignserver.model.RoomDataset;
import vn.edu.networkprogramming.assignserver.model.ScheduleBranch;
import vn.edu.networkprogramming.assignserver.model.StaffDataset;

public class SchedulingRepository {

    public static final String FILE_ROLE_OUTPUT_INVIGILATORS = "output_invigilators";
    public static final String FILE_ROLE_OUTPUT_MONITORS = "output_monitors";

    private final String jdbcUrl;
    private final String jdbcUser;
    private final String jdbcPassword;

    public SchedulingRepository(String jdbcUrl) {
        this(jdbcUrl, null, null);
    }

    public SchedulingRepository(String jdbcUrl, String jdbcUser, String jdbcPassword) {
        this.jdbcUrl = jdbcUrl;
        this.jdbcUser = jdbcUser;
        this.jdbcPassword = jdbcPassword;
    }

    public void initialize() throws SQLException {
        try (Connection connection = open(); Statement statement = connection.createStatement()) {
            statement.execute("""
                    create table if not exists staff_datasets (
                        dataset_id varchar(191) primary key,
                        name varchar(255) not null,
                        content_hash varchar(64) not null unique,
                        original_file_name varchar(255) not null,
                        mime_type varchar(255) not null,
                        content_blob longblob not null,
                        staff_count int not null,
                        created_at varchar(40) not null,
                        archived tinyint(1) not null default 0
                    )
                    """);
            statement.execute("""
                    create table if not exists room_datasets (
                        dataset_id varchar(191) primary key,
                        name varchar(255) not null,
                        content_hash varchar(64) not null unique,
                        original_file_name varchar(255) not null,
                        mime_type varchar(255) not null,
                        content_blob longblob not null,
                        room_count int not null,
                        created_at varchar(40) not null,
                        archived tinyint(1) not null default 0
                    )
                    """);
            statement.execute("""
                    create table if not exists schedule_branches (
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
                        next_session_no int not null,
                        session_created_count int not null default 0,
                        archived tinyint(1) not null default 0,
                        output_status varchar(40) not null default 'NONE',
                        output_error text,
                        foreign key (staff_dataset_id) references staff_datasets(dataset_id),
                        foreign key (room_dataset_id) references room_datasets(dataset_id)
                    )
                    """);
            statement.execute("""
                    create table if not exists branch_sessions (
                        branch_id varchar(191) not null,
                        session_no int not null,
                        selection_seed bigint not null,
                        requested_staff_count int not null,
                        requested_room_count int not null,
                        selected_staff_codes_json longtext not null,
                        selected_room_names_json longtext not null,
                        session_json longtext not null,
                        summary_json longtext not null,
                        created_at varchar(40) not null,
                        primary key (branch_id, session_no),
                        foreign key (branch_id) references schedule_branches(branch_id)
                    )
                    """);
            statement.execute("""
                    create table if not exists branch_files (
                        branch_id varchar(191) not null,
                        file_role varchar(64) not null,
                        original_name varchar(255) not null,
                        mime_type varchar(255) not null,
                        content_blob longblob not null,
                        created_at varchar(40) not null,
                        primary key (branch_id, file_role),
                        foreign key (branch_id) references schedule_branches(branch_id)
                    )
                    """);
            migrateSchema(connection, statement);
        }
    }

    private void migrateSchema(Connection connection, Statement statement) throws SQLException {
        ensureColumn(connection, statement, "staff_datasets", "archived", "tinyint(1) not null default 0");
        ensureColumn(connection, statement, "room_datasets", "archived", "tinyint(1) not null default 0");

        ensureColumn(connection, statement, "schedule_branches", "session_created_count", "int not null default 0");
        ensureColumn(connection, statement, "schedule_branches", "archived", "tinyint(1) not null default 0");
        ensureColumn(connection, statement, "schedule_branches", "output_status", "varchar(40) not null default 'NONE'");
        ensureColumn(connection, statement, "schedule_branches", "output_error", "text");

        ensureColumn(connection, statement, "branch_sessions", "requested_staff_count", "int not null default 0");
        ensureColumn(connection, statement, "branch_sessions", "requested_room_count", "int not null default 0");

        statement.executeUpdate("""
                update branch_sessions bs
                set requested_staff_count = (
                    select sb.requested_staff_count
                    from schedule_branches sb
                    where sb.branch_id = bs.branch_id
                )
                where requested_staff_count = 0
                """);
        statement.executeUpdate("""
                update branch_sessions bs
                set requested_room_count = (
                    select sb.requested_room_count
                    from schedule_branches sb
                    where sb.branch_id = bs.branch_id
                )
                where requested_room_count = 0
                """);
        statement.executeUpdate("""
                update schedule_branches sb
                set session_created_count = (
                    select count(*)
                    from branch_sessions bs
                    where bs.branch_id = sb.branch_id
                )
                where session_created_count = 0
                """);
    }

    private void ensureColumn(Connection connection, Statement statement, String table, String column, String ddl) throws SQLException {
        if (columnExists(connection, table, column)) {
            return;
        }
        statement.execute("alter table " + table + " add column " + column + " " + ddl);
    }

    private boolean columnExists(Connection connection, String table, String column) throws SQLException {
        try (ResultSet rs = connection.getMetaData().getColumns(connection.getCatalog(), null, table, column)) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = connection.getMetaData().getColumns(connection.getCatalog(), null, table.toUpperCase(), column.toUpperCase())) {
            return rs.next();
        }
    }

    public StaffDataset findStaffDatasetByHash(String contentHash) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     select dataset_id, name, content_hash, original_file_name, staff_count, created_at, archived
                     from staff_datasets where content_hash = ?
                     """)) {
            statement.setString(1, contentHash);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapStaffDataset(rs) : null;
            }
        }
    }

    public RoomDataset findRoomDatasetByHash(String contentHash) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     select dataset_id, name, content_hash, original_file_name, room_count, created_at, archived
                     from room_datasets where content_hash = ?
                     """)) {
            statement.setString(1, contentHash);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapRoomDataset(rs) : null;
            }
        }
    }

    public void createStaffDataset(String datasetId, String name, String hash, String fileName, String mimeType, byte[] content, int staffCount)
            throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     insert into staff_datasets (
                         dataset_id, name, content_hash, original_file_name, mime_type, content_blob, staff_count, created_at, archived
                     ) values (?, ?, ?, ?, ?, ?, ?, ?, 0)
                     """)) {
            statement.setString(1, datasetId);
            statement.setString(2, name);
            statement.setString(3, hash);
            statement.setString(4, fileName);
            statement.setString(5, mimeType);
            statement.setBytes(6, content);
            statement.setInt(7, staffCount);
            statement.setString(8, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    public void createRoomDataset(String datasetId, String name, String hash, String fileName, String mimeType, byte[] content, int roomCount)
            throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     insert into room_datasets (
                         dataset_id, name, content_hash, original_file_name, mime_type, content_blob, room_count, created_at, archived
                     ) values (?, ?, ?, ?, ?, ?, ?, ?, 0)
                     """)) {
            statement.setString(1, datasetId);
            statement.setString(2, name);
            statement.setString(3, hash);
            statement.setString(4, fileName);
            statement.setString(5, mimeType);
            statement.setBytes(6, content);
            statement.setInt(7, roomCount);
            statement.setString(8, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    public StaffDataset findStaffDatasetById(String datasetId) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     select dataset_id, name, content_hash, original_file_name, staff_count, created_at, archived
                     from staff_datasets where dataset_id = ?
                     """)) {
            statement.setString(1, datasetId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapStaffDataset(rs) : null;
            }
        }
    }

    public RoomDataset findRoomDatasetById(String datasetId) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     select dataset_id, name, content_hash, original_file_name, room_count, created_at, archived
                     from room_datasets where dataset_id = ?
                     """)) {
            statement.setString(1, datasetId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapRoomDataset(rs) : null;
            }
        }
    }

    public byte[] findStaffDatasetContent(String datasetId) throws SQLException {
        return findDatasetBlob("staff_datasets", datasetId);
    }

    public byte[] findRoomDatasetContent(String datasetId) throws SQLException {
        return findDatasetBlob("room_datasets", datasetId);
    }

    public List<StaffDataset> findAllStaffDatasets(boolean includeArchived) throws SQLException {
        String sql = """
                select dataset_id, name, content_hash, original_file_name, staff_count, created_at, archived
                from staff_datasets
                """ + (includeArchived ? "" : " where archived = 0") + " order by created_at desc";
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<StaffDataset> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapStaffDataset(rs));
            }
            return result;
        }
    }

    public List<RoomDataset> findAllRoomDatasets(boolean includeArchived) throws SQLException {
        String sql = """
                select dataset_id, name, content_hash, original_file_name, room_count, created_at, archived
                from room_datasets
                """ + (includeArchived ? "" : " where archived = 0") + " order by created_at desc";
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<RoomDataset> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRoomDataset(rs));
            }
            return result;
        }
    }

    public void archiveStaffDataset(String datasetId) throws SQLException {
        archive("staff_datasets", "dataset_id", datasetId);
    }

    public void renameStaffDataset(String datasetId, String name) throws SQLException {
        renameRecord("staff_datasets", "dataset_id", datasetId, name);
    }

    public void unarchiveStaffDataset(String datasetId) throws SQLException {
        unarchive("staff_datasets", "dataset_id", datasetId);
    }

    public void archiveRoomDataset(String datasetId) throws SQLException {
        archive("room_datasets", "dataset_id", datasetId);
    }

    public void renameRoomDataset(String datasetId, String name) throws SQLException {
        renameRecord("room_datasets", "dataset_id", datasetId, name);
    }

    public void unarchiveRoomDataset(String datasetId) throws SQLException {
        unarchive("room_datasets", "dataset_id", datasetId);
    }

    public void createBranch(ScheduleBranch branch) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     insert into schedule_branches (
                         branch_id, name, status, message, created_at, updated_at,
                         staff_dataset_id, room_dataset_id, requested_staff_count, requested_room_count,
                         next_session_no, session_created_count, archived, output_status, output_error
                     ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                     """)) {
            statement.setString(1, branch.branchId());
            statement.setString(2, branch.name());
            statement.setString(3, branch.status());
            statement.setString(4, branch.message());
            statement.setString(5, branch.createdAt().toString());
            statement.setString(6, branch.updatedAt().toString());
            statement.setString(7, branch.staffDatasetId());
            statement.setString(8, branch.roomDatasetId());
            statement.setInt(9, branch.requestedStaffCount());
            statement.setInt(10, branch.requestedRoomCount());
            statement.setInt(11, branch.nextSessionNo());
            statement.setInt(12, branch.sessionCreatedCount());
            statement.setInt(13, branch.archived() ? 1 : 0);
            statement.setString(14, branch.outputStatus());
            statement.setString(15, branch.outputError());
            statement.executeUpdate();
        }
    }

    public void updateBranchAfterSession(String branchId, String status, String message, int nextSessionNo, int sessionCreatedCount)
            throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     update schedule_branches
                     set status = ?, message = ?, next_session_no = ?, session_created_count = ?, updated_at = ?
                     where branch_id = ?
                     """)) {
            statement.setString(1, status);
            statement.setString(2, message);
            statement.setInt(3, nextSessionNo);
            statement.setInt(4, sessionCreatedCount);
            statement.setString(5, Instant.now().toString());
            statement.setString(6, branchId);
            statement.executeUpdate();
        }
    }

    public void updateBranchOutputStatus(String branchId, String outputStatus, String outputError) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     update schedule_branches
                     set output_status = ?, output_error = ?, updated_at = ?
                     where branch_id = ?
                     """)) {
            statement.setString(1, outputStatus);
            statement.setString(2, outputError);
            statement.setString(3, Instant.now().toString());
            statement.setString(4, branchId);
            statement.executeUpdate();
        }
    }

    public ScheduleBranch findBranchById(String branchId) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     select b.branch_id, b.name, b.status, b.message, b.created_at, b.updated_at,
                            b.staff_dataset_id, sd.name as staff_dataset_name,
                            b.room_dataset_id, rd.name as room_dataset_name,
                            b.requested_staff_count, b.requested_room_count,
                            b.next_session_no, b.session_created_count, b.archived,
                            b.output_status, b.output_error
                     from schedule_branches b
                     join staff_datasets sd on sd.dataset_id = b.staff_dataset_id
                     join room_datasets rd on rd.dataset_id = b.room_dataset_id
                     where b.branch_id = ?
                     """)) {
            statement.setString(1, branchId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapBranch(rs) : null;
            }
        }
    }

    public List<ScheduleBranch> findAllBranches(boolean includeArchived) throws SQLException {
        String sql = """
                select b.branch_id, b.name, b.status, b.message, b.created_at, b.updated_at,
                       b.staff_dataset_id, sd.name as staff_dataset_name,
                       b.room_dataset_id, rd.name as room_dataset_name,
                       b.requested_staff_count, b.requested_room_count,
                       b.next_session_no, b.session_created_count, b.archived,
                       b.output_status, b.output_error
                from schedule_branches b
                join staff_datasets sd on sd.dataset_id = b.staff_dataset_id
                join room_datasets rd on rd.dataset_id = b.room_dataset_id
                """ + (includeArchived ? "" : " where b.archived = 0") + " order by b.created_at desc";
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<ScheduleBranch> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapBranch(rs));
            }
            return result;
        }
    }

    public void archiveBranch(String branchId) throws SQLException {
        archive("schedule_branches", "branch_id", branchId);
    }

    public void renameBranch(String branchId, String name) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     update schedule_branches
                     set name = ?, updated_at = ?
                     where branch_id = ?
                     """)) {
            statement.setString(1, name);
            statement.setString(2, Instant.now().toString());
            statement.setString(3, branchId);
            statement.executeUpdate();
        }
    }

    public void unarchiveBranch(String branchId) throws SQLException {
        unarchive("schedule_branches", "branch_id", branchId);
    }

    public void insertBranchSession(
            String branchId,
            int sessionNo,
            long selectionSeed,
            int requestedStaffCount,
            int requestedRoomCount,
            String selectedStaffCodesJson,
            String selectedRoomNamesJson,
            String sessionJson,
            String summaryJson
    ) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     insert into branch_sessions (
                         branch_id, session_no, selection_seed, requested_staff_count, requested_room_count,
                         selected_staff_codes_json, selected_room_names_json, session_json, summary_json, created_at
                     ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                     """)) {
            statement.setString(1, branchId);
            statement.setInt(2, sessionNo);
            statement.setLong(3, selectionSeed);
            statement.setInt(4, requestedStaffCount);
            statement.setInt(5, requestedRoomCount);
            statement.setString(6, selectedStaffCodesJson);
            statement.setString(7, selectedRoomNamesJson);
            statement.setString(8, sessionJson);
            statement.setString(9, summaryJson);
            statement.setString(10, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    public List<BranchSessionRow> findBranchSessionRows(String branchId) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     select branch_id, session_no, selection_seed, requested_staff_count, requested_room_count, selected_staff_codes_json,
                            selected_room_names_json, session_json, summary_json, created_at
                     from branch_sessions
                     where branch_id = ?
                     order by session_no asc
                     """)) {
            statement.setString(1, branchId);
            try (ResultSet rs = statement.executeQuery()) {
                List<BranchSessionRow> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(new BranchSessionRow(
                            rs.getString("branch_id"),
                            rs.getInt("session_no"),
                            rs.getLong("selection_seed"),
                            rs.getInt("requested_staff_count"),
                            rs.getInt("requested_room_count"),
                            rs.getString("selected_staff_codes_json"),
                            rs.getString("selected_room_names_json"),
                            rs.getString("session_json"),
                            rs.getString("summary_json"),
                            Instant.parse(rs.getString("created_at"))
                    ));
                }
                return result;
            }
        }
    }

    public BranchSessionRow findBranchSessionRow(String branchId, int sessionNo) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     select branch_id, session_no, selection_seed, requested_staff_count, requested_room_count, selected_staff_codes_json,
                            selected_room_names_json, session_json, summary_json, created_at
                     from branch_sessions
                     where branch_id = ? and session_no = ?
                     """)) {
            statement.setString(1, branchId);
            statement.setInt(2, sessionNo);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new BranchSessionRow(
                        rs.getString("branch_id"),
                        rs.getInt("session_no"),
                        rs.getLong("selection_seed"),
                        rs.getInt("requested_staff_count"),
                        rs.getInt("requested_room_count"),
                        rs.getString("selected_staff_codes_json"),
                        rs.getString("selected_room_names_json"),
                        rs.getString("session_json"),
                        rs.getString("summary_json"),
                        Instant.parse(rs.getString("created_at"))
                );
            }
        }
    }

    public void upsertBranchFile(String branchId, String role, String fileName, String mimeType, byte[] content) throws SQLException {
        try (Connection connection = open();
             PreparedStatement update = connection.prepareStatement("""
                     update branch_files
                     set original_name = ?, mime_type = ?, content_blob = ?, created_at = ?
                     where branch_id = ? and file_role = ?
                     """)) {
            String now = Instant.now().toString();
            update.setString(1, fileName);
            update.setString(2, mimeType);
            update.setBytes(3, content);
            update.setString(4, now);
            update.setString(5, branchId);
            update.setString(6, role);
            int updated = update.executeUpdate();
            if (updated > 0) {
                return;
            }
        }

        try (Connection connection = open();
             PreparedStatement insert = connection.prepareStatement("""
                     insert into branch_files (branch_id, file_role, original_name, mime_type, content_blob, created_at)
                     values (?, ?, ?, ?, ?, ?)
                     """)) {
            insert.setString(1, branchId);
            insert.setString(2, role);
            insert.setString(3, fileName);
            insert.setString(4, mimeType);
            insert.setBytes(5, content);
            insert.setString(6, Instant.now().toString());
            insert.executeUpdate();
        }
    }

    public AssignmentFileContent findBranchFile(String branchId, String role) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("""
                     select original_name, mime_type, content_blob
                     from branch_files
                     where branch_id = ? and file_role = ?
                     """)) {
            statement.setString(1, branchId);
            statement.setString(2, role);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new AssignmentFileContent(
                        role,
                        rs.getString("original_name"),
                        rs.getString("mime_type"),
                        rs.getBytes("content_blob")
                );
            }
        }
    }

    private byte[] findDatasetBlob(String table, String datasetId) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement("select content_blob from " + table + " where dataset_id = ?")) {
            statement.setString(1, datasetId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getBytes("content_blob") : null;
            }
        }
    }

    private void archive(String table, String keyColumn, String id) throws SQLException {
        setArchived(table, keyColumn, id, true);
    }

    private void unarchive(String table, String keyColumn, String id) throws SQLException {
        setArchived(table, keyColumn, id, false);
    }

    private void setArchived(String table, String keyColumn, String id, boolean archived) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement(
                     "update " + table + " set archived = ? where " + keyColumn + " = ?")) {
            statement.setInt(1, archived ? 1 : 0);
            statement.setString(2, id);
            statement.executeUpdate();
        }
    }

    private void renameRecord(String table, String keyColumn, String id, String name) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement(
                     "update " + table + " set name = ? where " + keyColumn + " = ?")) {
            statement.setString(1, name);
            statement.setString(2, id);
            statement.executeUpdate();
        }
    }

    private StaffDataset mapStaffDataset(ResultSet rs) throws SQLException {
        return new StaffDataset(
                rs.getString("dataset_id"),
                rs.getString("name"),
                rs.getString("content_hash"),
                rs.getString("original_file_name"),
                rs.getInt("staff_count"),
                Instant.parse(rs.getString("created_at")),
                rs.getInt("archived") != 0
        );
    }

    private RoomDataset mapRoomDataset(ResultSet rs) throws SQLException {
        return new RoomDataset(
                rs.getString("dataset_id"),
                rs.getString("name"),
                rs.getString("content_hash"),
                rs.getString("original_file_name"),
                rs.getInt("room_count"),
                Instant.parse(rs.getString("created_at")),
                rs.getInt("archived") != 0
        );
    }

    private ScheduleBranch mapBranch(ResultSet rs) throws SQLException {
        return new ScheduleBranch(
                rs.getString("branch_id"),
                rs.getString("name"),
                rs.getString("status"),
                rs.getString("message"),
                Instant.parse(rs.getString("created_at")),
                Instant.parse(rs.getString("updated_at")),
                rs.getString("staff_dataset_id"),
                rs.getString("staff_dataset_name"),
                rs.getString("room_dataset_id"),
                rs.getString("room_dataset_name"),
                rs.getInt("requested_staff_count"),
                rs.getInt("requested_room_count"),
                rs.getInt("next_session_no"),
                rs.getInt("session_created_count"),
                rs.getInt("archived") != 0,
                rs.getString("output_status"),
                rs.getString("output_error")
        );
    }

    private Connection open() throws SQLException {
        if (jdbcUser == null || jdbcUser.isBlank()) {
            return DriverManager.getConnection(jdbcUrl);
        }
        return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword == null ? "" : jdbcPassword);
    }

    public record BranchSessionRow(
            String branchId,
            int sessionNo,
            long selectionSeed,
            int requestedStaffCount,
            int requestedRoomCount,
            String selectedStaffCodesJson,
            String selectedRoomNamesJson,
            String sessionJson,
            String summaryJson,
            Instant createdAt
    ) {
    }
}
