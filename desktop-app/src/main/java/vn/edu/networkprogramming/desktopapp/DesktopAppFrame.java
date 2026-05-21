package vn.edu.networkprogramming.desktopapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.JTextComponent;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

final class DesktopAppFrame extends JFrame {

    private static final Color BG = new Color(245, 241, 232);
    private static final Color PANEL = new Color(255, 253, 249);
    private static final Color LINE = new Color(221, 210, 192);
    private static final Color TEXT = new Color(47, 38, 27);
    private static final Color MUTED = new Color(112, 101, 86);
    private static final Color BRAND = new Color(15, 107, 94);
    private static final Color BRAND_DARK = new Color(11, 84, 74);
    private static final Color ACCENT = new Color(191, 100, 57);
    private static final Color SOFT = new Color(234, 223, 205);

    private final DesktopApiClient apiClient;

    private final JTextField serverUrlField = new JTextField();
    private final JCheckBox includeArchivedDatasetsCheck = new JCheckBox("Hiện dữ liệu đã ẩn");
    private final JCheckBox includeArchivedBranchesCheck = new JCheckBox("Hiện nhánh đã ẩn");
    private final JLabel statusLabel = new JLabel("Sẵn sàng");

    private final JTextField staffNameField = new JTextField();
    private final JTextField roomNameField = new JTextField();
    private final JLabel staffFileLabel = new JLabel("Chưa chọn file");
    private final JLabel roomFileLabel = new JLabel("Chưa chọn file");
    private File selectedStaffFile;
    private File selectedRoomFile;

    private final DatasetTableModel<StaffDatasetView> staffTableModel = new DatasetTableModel<>("cán bộ");
    private final DatasetTableModel<RoomDatasetView> roomTableModel = new DatasetTableModel<>("phòng");
    private final BranchTableModel branchTableModel = new BranchTableModel();
    private final SessionTableModel sessionTableModel = new SessionTableModel();

    private final JTable staffTable = createTable(staffTableModel);
    private final JTable roomTable = createTable(roomTableModel);
    private final JTable branchTable = createTable(branchTableModel);
    private final JTable sessionTable = createTable(sessionTableModel);

    private final DefaultComboBoxModel<DatasetOption> staffDatasetComboModel = new DefaultComboBoxModel<>();
    private final DefaultComboBoxModel<DatasetOption> roomDatasetComboModel = new DefaultComboBoxModel<>();
    private final JComboBox<DatasetOption> staffDatasetCombo = new JComboBox<>(staffDatasetComboModel);
    private final JComboBox<DatasetOption> roomDatasetCombo = new JComboBox<>(roomDatasetComboModel);
    private final JTextField branchNameField = new JTextField();
    private final JSpinner branchStaffSpinner = new JSpinner(new SpinnerNumberModel(500, 1, 1_000_000, 1));
    private final JSpinner branchRoomSpinner = new JSpinner(new SpinnerNumberModel(200, 1, 1_000_000, 1));
    private final JSpinner branchSessionSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));

    private final JLabel detailNameLabel = new JLabel("Chưa chọn nhánh");
    private final JLabel detailMetaLabel = new JLabel(" ");
    private final JLabel detailConfigLabel = new JLabel(" ");
    private final JLabel detailPreviewLabel = new JLabel(" ");
    private final JLabel detailOutputLabel = new JLabel(" ");
    private final JTextField renameBranchField = new JTextField();
    private final JTextField resetBranchField = new JTextField();
    private final JSpinner appendSessionSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
    private final JTextField appendStaffField = new JTextField();
    private final JTextField appendRoomField = new JTextField();

    private List<StaffDatasetView> staffDatasets = List.of();
    private List<RoomDatasetView> roomDatasets = List.of();
    private List<ScheduleBranchView> branches = List.of();
    private BranchDetailView currentDetail;
    private BranchPreviewView currentPreview;

    DesktopAppFrame(DesktopApiClient apiClient) {
        this.apiClient = apiClient;
        setTitle("Exam Scheduler Desktop");
        setSize(1480, 920);
        setMinimumSize(new Dimension(1260, 760));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainTabs(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
        refreshAll();
    }

    private Component buildHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setBackground(BRAND);
        header.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("<html><div style='font-size:28px;font-weight:700;color:#fff'>Exam Scheduler Desktop</div>"
                + "<div style='font-size:13px;color:#d9f0ec'>Quản lý dữ liệu, nhánh phân công và các ca thi trên ứng dụng Java.</div></html>");
        header.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout(10, 8));
        right.setOpaque(false);
        JLabel urlLabel = new JLabel("Địa chỉ backend");
        urlLabel.setForeground(new Color(224, 243, 239));
        urlLabel.setFont(urlLabel.getFont().deriveFont(Font.BOLD, 12f));
        right.add(urlLabel, BorderLayout.NORTH);

        JPanel urlRow = new JPanel(new BorderLayout(8, 0));
        urlRow.setOpaque(false);
        styleTextField(serverUrlField);
        serverUrlField.setText(apiClient.serverBaseUrl());
        JButton reconnectButton = createButton("Kết nối lại", BRAND_DARK, Color.WHITE);
        reconnectButton.addActionListener(event -> reconnect());
        urlRow.add(serverUrlField, BorderLayout.CENTER);
        urlRow.add(reconnectButton, BorderLayout.EAST);
        right.add(urlRow, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private Component buildMainTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(tabs.getFont().deriveFont(Font.BOLD, 13f));
        tabs.addTab("Bộ dữ liệu", buildDatasetsTab());
        tabs.addTab("Nhánh phân công", buildBranchesTab());
        return tabs;
    }

    private Component buildDatasetsTab() {
        JPanel container = new JPanel(new BorderLayout(18, 18));
        container.setBackground(BG);
        container.setBorder(new EmptyBorder(18, 18, 18, 18));
        container.add(buildUploadArea(), BorderLayout.NORTH);

        JPanel tables = new JPanel(new GridLayout(1, 2, 18, 18));
        tables.setOpaque(false);
        tables.add(buildDatasetSection("Bộ dữ liệu cán bộ", staffTable, true));
        tables.add(buildDatasetSection("Bộ dữ liệu phòng", roomTable, false));
        container.add(tables, BorderLayout.CENTER);
        return container;
    }

    private Component buildUploadArea() {
        JPanel panel = createCardPanel();
        panel.setLayout(new BorderLayout(18, 18));
        panel.add(sectionTitle("Tải dữ liệu", "Có thể chọn một file hoặc cả hai file."), BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 2, 18, 18));
        cards.setOpaque(false);
        cards.add(buildUploadCard("Bộ cán bộ", staffNameField, staffFileLabel, true));
        cards.add(buildUploadCard("Bộ phòng", roomNameField, roomFileLabel, false));
        panel.add(cards, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        footer.setOpaque(false);
        includeArchivedDatasetsCheck.setOpaque(false);
        includeArchivedDatasetsCheck.setForeground(MUTED);
        includeArchivedDatasetsCheck.addActionListener(event -> refreshDatasets());
        JButton refreshButton = createButton("Làm mới", SOFT, TEXT);
        refreshButton.addActionListener(event -> refreshDatasets());
        footer.add(includeArchivedDatasetsCheck);
        footer.add(refreshButton);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private Component buildUploadCard(String title, JTextField nameField, JLabel fileLabel, boolean staff) {
        JPanel panel = createInnerCard();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        styleTextField(nameField);
        nameField.setPreferredSize(new Dimension(220, 38));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(textLabel(title, 19f, true));
        panel.add(Box.createVerticalStrut(6));
        panel.add(subtleLabel("Để trống tên nếu muốn dùng tên mặc định."));
        panel.add(Box.createVerticalStrut(12));
        panel.add(labeled("Tên hiển thị", nameField));
        panel.add(Box.createVerticalStrut(14));

        JButton chooseButton = createButton("Chọn file", BRAND, Color.WHITE);
        chooseButton.addActionListener(event -> chooseFile(staff));
        JButton uploadButton = createButton("Tải lên", ACCENT, Color.WHITE);
        uploadButton.addActionListener(event -> uploadDataset(staff));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.add(chooseButton);
        actions.add(uploadButton);

        fileLabel.setForeground(MUTED);
        fileLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
        fileLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(actions);
        panel.add(fileLabel);
        return panel;
    }

    private Component buildDatasetSection(String title, JTable table, boolean staff) {
        JPanel panel = createCardPanel();
        panel.setLayout(new BorderLayout(12, 12));
        panel.add(sectionTitle(title, "Đổi tên, ẩn hoặc hiện lại trực tiếp trong ứng dụng."), BorderLayout.NORTH);

        JScrollPane scrollPane = createTableScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton renameButton = createButton("Đổi tên", SOFT, TEXT);
        renameButton.addActionListener(event -> renameSelectedDataset(staff));
        JButton archiveButton = createButton("Ẩn", ACCENT, Color.WHITE);
        archiveButton.addActionListener(event -> archiveSelectedDataset(staff));
        JButton restoreButton = createButton("Hiện lại", BRAND, Color.WHITE);
        restoreButton.addActionListener(event -> restoreSelectedDataset(staff));
        actions.add(renameButton);
        actions.add(archiveButton);
        actions.add(restoreButton);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private Component buildBranchesTab() {
        JPanel container = new JPanel(new BorderLayout(18, 18));
        container.setBackground(BG);
        container.setBorder(new EmptyBorder(18, 18, 18, 18));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildBranchLeftPanel(), buildBranchRightPanel());
        splitPane.setResizeWeight(0.48);
        splitPane.setBorder(null);
        container.add(splitPane, BorderLayout.CENTER);
        return container;
    }

    private Component buildBranchLeftPanel() {
        JPanel left = new JPanel(new BorderLayout(18, 18));
        left.setOpaque(false);
        left.add(buildCreateBranchPanel(), BorderLayout.NORTH);

        JPanel listPanel = createCardPanel();
        listPanel.setLayout(new BorderLayout(12, 12));
        listPanel.add(sectionTitle("Các nhánh phân công", "Chọn một nhánh để xem chi tiết và thao tác."), BorderLayout.NORTH);
        branchTable.getSelectionModel().addListSelectionListener(this::onBranchSelectionChanged);
        listPanel.add(createTableScrollPane(branchTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        includeArchivedBranchesCheck.setOpaque(false);
        includeArchivedBranchesCheck.setForeground(MUTED);
        includeArchivedBranchesCheck.addActionListener(event -> refreshBranches());
        JButton refreshButton = createButton("Làm mới", SOFT, TEXT);
        refreshButton.addActionListener(event -> refreshBranches());
        JButton renameButton = createButton("Đổi tên", SOFT, TEXT);
        renameButton.addActionListener(event -> renameSelectedBranch());
        JButton archiveButton = createButton("Ẩn", ACCENT, Color.WHITE);
        archiveButton.addActionListener(event -> archiveSelectedBranch());
        actions.add(includeArchivedBranchesCheck);
        actions.add(refreshButton);
        actions.add(renameButton);
        actions.add(archiveButton);
        listPanel.add(actions, BorderLayout.SOUTH);

        left.add(listPanel, BorderLayout.CENTER);
        return left;
    }

    private Component buildCreateBranchPanel() {
        JPanel panel = createCardPanel();
        panel.setLayout(new BorderLayout(12, 12));
        panel.add(sectionTitle("Tạo nhánh mới", "Chọn bộ dữ liệu, nhập cấu hình mặc định và số ca đầu tiên."), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        styleTextField(branchNameField);
        styleCombo(staffDatasetCombo);
        styleCombo(roomDatasetCombo);
        styleSpinner(branchStaffSpinner);
        styleSpinner(branchRoomSpinner);
        styleSpinner(branchSessionSpinner);

        form.add(labeled("Tên nhánh", branchNameField), gbc);
        gbc.gridy++;
        form.add(labeled("Bộ dữ liệu cán bộ", staffDatasetCombo), gbc);
        gbc.gridy++;
        form.add(labeled("Bộ dữ liệu phòng", roomDatasetCombo), gbc);
        gbc.gridy++;

        JPanel numbers = new JPanel(new GridLayout(1, 3, 12, 0));
        numbers.setOpaque(false);
        numbers.add(labeled("Số cán bộ", branchStaffSpinner));
        numbers.add(labeled("Số phòng", branchRoomSpinner));
        numbers.add(labeled("Số ca đầu", branchSessionSpinner));
        form.add(numbers, gbc);

        panel.add(form, BorderLayout.CENTER);

        JButton createButton = createButton("Tạo nhánh", BRAND, Color.WHITE);
        createButton.addActionListener(event -> createBranch());
        panel.add(createButton, BorderLayout.SOUTH);
        return panel;
    }

    private Component buildBranchRightPanel() {
        JPanel right = new JPanel(new BorderLayout(18, 18));
        right.setOpaque(false);

        JPanel detailPanel = createCardPanel();
        detailPanel.setLayout(new BorderLayout(14, 14));
        detailPanel.setPreferredSize(new Dimension(0, 430));
        detailPanel.add(sectionTitle("Chi tiết nhánh", "Theo dõi cấu hình, preview và các thao tác nhanh."), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setOpaque(false);
        body.add(buildBranchDetailInfo(), BorderLayout.NORTH);
        body.add(buildBranchActionPanels(), BorderLayout.CENTER);
        detailPanel.add(body, BorderLayout.CENTER);

        JPanel sessionsPanel = createCardPanel();
        sessionsPanel.setLayout(new BorderLayout(12, 12));
        sessionsPanel.add(sectionTitle("Các ca đã tạo", "Xem nhanh từng ca và mở chi tiết phân công."), BorderLayout.NORTH);
        sessionsPanel.add(createTableScrollPane(sessionTable), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton viewButton = createButton("Xem ca", BRAND, Color.WHITE);
        viewButton.addActionListener(event -> showSelectedSessionDetail());
        JButton invButton = createButton("Tải ca coi thi", BRAND_DARK, Color.WHITE);
        invButton.addActionListener(event -> downloadSelectedSessionOutput("invigilators"));
        JButton monButton = createButton("Tải ca giám sát", SOFT, TEXT);
        monButton.addActionListener(event -> downloadSelectedSessionOutput("monitors"));
        actions.add(viewButton);
        actions.add(invButton);
        actions.add(monButton);
        sessionsPanel.add(actions, BorderLayout.SOUTH);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, detailPanel, sessionsPanel);
        verticalSplit.setBorder(null);
        verticalSplit.setResizeWeight(0.30);
        verticalSplit.setDividerSize(8);
        SwingUtilities.invokeLater(() -> verticalSplit.setDividerLocation(430));
        right.add(verticalSplit, BorderLayout.CENTER);
        return right;
    }

    private Component buildBranchDetailInfo() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        detailNameLabel.setFont(detailNameLabel.getFont().deriveFont(Font.BOLD, 22f));
        detailNameLabel.setForeground(TEXT);
        detailMetaLabel.setForeground(MUTED);
        detailConfigLabel.setForeground(TEXT);
        detailPreviewLabel.setForeground(BRAND_DARK);
        detailOutputLabel.setForeground(ACCENT);
        panel.add(detailNameLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(detailMetaLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(detailConfigLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(detailPreviewLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(detailOutputLabel);
        return panel;
    }

    private Component buildBranchActionPanels() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(buildAppendPanel());
        panel.add(Box.createVerticalStrut(12));
        panel.add(buildBranchMaintenancePanel());
        return panel;
    }

    private Component buildAppendPanel() {
        JPanel panel = createInnerCard();
        panel.setLayout(new BorderLayout(12, 12));
        panel.setPreferredSize(new Dimension(0, 165));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 165));
        panel.add(textLabel("Tạo thêm ca", 17f, true), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        styleSpinner(appendSessionSpinner);
        styleTextField(appendStaffField);
        styleTextField(appendRoomField);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        gbc.gridx = 0;
        form.add(labeled("Số ca", appendSessionSpinner), gbc);
        gbc.gridx = 1;
        form.add(labeled("Số cán bộ", appendStaffField), gbc);
        gbc.gridx = 2;
        form.add(labeled("Số phòng", appendRoomField), gbc);

        JButton appendButton = createButton("Tạo thêm", BRAND, Color.WHITE);
        appendButton.addActionListener(event -> appendSessions());
        JPanel buttonWrap = new JPanel(new BorderLayout());
        buttonWrap.setOpaque(false);
        buttonWrap.add(Box.createVerticalStrut(22), BorderLayout.NORTH);
        buttonWrap.add(appendButton, BorderLayout.CENTER);
        gbc.gridx = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weightx = 0.9;
        form.add(buttonWrap, gbc);
        panel.add(form, BorderLayout.CENTER);
        panel.add(subtleLabel("Để trống số cán bộ hoặc số phòng để dùng cấu hình mặc định của nhánh."), BorderLayout.SOUTH);
        return panel;
    }

    private Component buildBranchMaintenancePanel() {
        JPanel panel = createInnerCard();
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(0, 200));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        styleTextField(renameBranchField);
        styleTextField(resetBranchField);
        setCompactHeight(renameBranchField, 34);
        setCompactHeight(resetBranchField, 34);

        JPanel renamePanel = new JPanel(new BorderLayout(8, 8));
        renamePanel.setOpaque(false);
        renamePanel.add(textLabel("Đổi tên nhánh", 16f, true), BorderLayout.NORTH);
        renamePanel.add(wrapTopAligned(renameBranchField), BorderLayout.CENTER);
        JButton renameButton = createButton("Lưu tên mới", SOFT, TEXT);
        renameButton.addActionListener(event -> renameSelectedBranch());
        renamePanel.add(renameButton, BorderLayout.SOUTH);

        JPanel resetPanel = new JPanel(new BorderLayout(8, 8));
        resetPanel.setOpaque(false);
        resetPanel.add(textLabel("Làm lại nhánh", 16f, true), BorderLayout.NORTH);
        resetPanel.add(wrapTopAligned(resetBranchField), BorderLayout.CENTER);
        JButton resetButton = createButton("Tạo nhánh mới", BRAND_DARK, Color.WHITE);
        resetButton.addActionListener(event -> resetBranch());
        resetPanel.add(resetButton, BorderLayout.SOUTH);

        JPanel archivePanel = new JPanel(new BorderLayout(0, 6));
        archivePanel.setOpaque(false);
        archivePanel.add(wrapTopAligned(textLabel("Ẩn / hiện nhánh", 16f, true)), BorderLayout.NORTH);
        JButton archiveButton = createButton("Ẩn nhánh", ACCENT, Color.WHITE);
        archiveButton.addActionListener(event -> archiveSelectedBranch());
        JButton restoreButton = createButton("Hiện lại nhánh", BRAND, Color.WHITE);
        restoreButton.addActionListener(event -> restoreSelectedBranch());
        JPanel archiveButtons = new JPanel(new GridLayout(2, 1, 0, 8));
        archiveButtons.setOpaque(false);
        archiveButtons.add(archiveButton);
        archiveButtons.add(restoreButton);
        archivePanel.add(archiveButtons, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 12);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;

        gbc.gridx = 0;
        panel.add(renamePanel, gbc);
        gbc.gridx = 1;
        panel.add(resetPanel, gbc);
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(archivePanel, gbc);
        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(new EmptyBorder(10, 18, 10, 18));
        statusBar.setBackground(PANEL);
        statusLabel.setForeground(MUTED);
        statusBar.add(statusLabel, BorderLayout.WEST);
        return statusBar;
    }

    private void chooseFile(boolean staff) {
        File file = showNativeOpenDialog();
        if (file == null) {
            return;
        }
        if (staff) {
            selectedStaffFile = file;
            staffFileLabel.setText(file.getName());
        } else {
            selectedRoomFile = file;
            roomFileLabel.setText(file.getName());
        }
    }

    private void uploadDataset(boolean staff) {
        File file = staff ? selectedStaffFile : selectedRoomFile;
        if (file == null) {
            showWarning("Hãy chọn file trước khi tải lên.");
            return;
        }
        JTextField nameField = staff ? staffNameField : roomNameField;
        runTask(staff ? "Đang tải bộ dữ liệu cán bộ..." : "Đang tải bộ dữ liệu phòng...", () -> {
            byte[] content = Files.readAllBytes(file.toPath());
            return staff
                    ? apiClient.uploadStaffDataset(nameField.getText().trim(), file.getName(), content)
                    : apiClient.uploadRoomDataset(nameField.getText().trim(), file.getName(), content);
        }, result -> {
            showInfo(result.reused() ? "Bộ dữ liệu đã tồn tại, hệ thống dùng lại dữ liệu cũ." : "Tải dữ liệu thành công.");
            nameField.setText("");
            if (staff) {
                selectedStaffFile = null;
                staffFileLabel.setText("Chưa chọn file");
            } else {
                selectedRoomFile = null;
                roomFileLabel.setText("Chưa chọn file");
            }
            refreshAll();
        });
    }

    private void refreshAll() {
        refreshDatasets();
        refreshBranches();
    }

    private void refreshDatasets() {
        boolean includeArchived = includeArchivedDatasetsCheck.isSelected();
        runTask("Đang tải danh sách bộ dữ liệu...", () -> {
            List<StaffDatasetView> staffItems = apiClient.listStaffDatasets(includeArchived);
            List<RoomDatasetView> roomItems = apiClient.listRoomDatasets(includeArchived);
            List<StaffDatasetView> activeStaff = apiClient.listStaffDatasets(false);
            List<RoomDatasetView> activeRoom = apiClient.listRoomDatasets(false);
            return new DatasetBundle(staffItems, roomItems, activeStaff, activeRoom);
        }, bundle -> {
            staffDatasets = bundle.staffItems();
            roomDatasets = bundle.roomItems();
            staffTableModel.setItems(staffDatasets);
            roomTableModel.setItems(roomDatasets);
            reloadDatasetCombo(staffDatasetComboModel, bundle.activeStaffItems(), true);
            reloadDatasetCombo(roomDatasetComboModel, bundle.activeRoomItems(), false);
            status("Đã tải xong bộ dữ liệu.");
        });
    }

    private void refreshBranches() {
        boolean includeArchived = includeArchivedBranchesCheck.isSelected();
        String selectedBranchId = selectedBranchId();
        runTask("Đang tải danh sách nhánh...", () -> apiClient.listBranches(includeArchived), items -> {
            branches = items;
            branchTableModel.setItems(branches);
            restoreBranchSelection(selectedBranchId);
            status("Đã tải xong danh sách nhánh.");
        });
    }

    private void reloadDatasetCombo(DefaultComboBoxModel<DatasetOption> model, List<?> items, boolean staff) {
        Object current = model.getSelectedItem();
        String currentId = current instanceof DatasetOption option ? option.id() : null;
        model.removeAllElements();
        for (Object item : items) {
            if (staff) {
                StaffDatasetView dataset = (StaffDatasetView) item;
                model.addElement(new DatasetOption(dataset.datasetId(), dataset.name(), dataset.staffCount(), "cán bộ"));
            } else {
                RoomDatasetView dataset = (RoomDatasetView) item;
                model.addElement(new DatasetOption(dataset.datasetId(), dataset.name(), dataset.roomCount(), "phòng"));
            }
        }
        if (currentId != null) {
            for (int index = 0; index < model.getSize(); index++) {
                if (Objects.equals(model.getElementAt(index).id(), currentId)) {
                    model.setSelectedItem(model.getElementAt(index));
                    return;
                }
            }
        }
        if (model.getSize() > 0) {
            model.setSelectedItem(model.getElementAt(0));
        }
    }

    private void createBranch() {
        DatasetOption staffOption = (DatasetOption) staffDatasetCombo.getSelectedItem();
        DatasetOption roomOption = (DatasetOption) roomDatasetCombo.getSelectedItem();
        if (staffOption == null || roomOption == null) {
            showWarning("Cần chọn đủ bộ dữ liệu cán bộ và bộ dữ liệu phòng.");
            return;
        }
        runTask("Đang tạo nhánh mới...", () -> apiClient.createBranch(
                branchNameField.getText().trim(),
                staffOption.id(),
                roomOption.id(),
                (Integer) branchStaffSpinner.getValue(),
                (Integer) branchRoomSpinner.getValue(),
                (Integer) branchSessionSpinner.getValue()
        ), branch -> {
            showInfo("Đã tạo nhánh mới thành công.");
            branchNameField.setText("");
            refreshBranches();
        });
    }

    private void onBranchSelectionChanged(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }
        ScheduleBranchView branch = selectedBranch();
        if (branch == null) {
            currentDetail = null;
            currentPreview = null;
            sessionTableModel.setItems(List.of());
            fillBranchDetail(null, null);
            return;
        }
        if (branch.archived()) {
            currentDetail = null;
            currentPreview = null;
            sessionTableModel.setItems(List.of());
            fillArchivedBranchDetail(branch);
            status("Đã chọn nhánh đang ẩn.");
            return;
        }
        runTask("Đang tải chi tiết nhánh...", () -> new BranchScreenData(
                apiClient.getBranchDetail(branch.branchId()),
                apiClient.getBranchPreview(branch.branchId())
        ), data -> {
            currentDetail = data.detail();
            currentPreview = data.preview();
            fillBranchDetail(currentDetail, currentPreview);
            sessionTableModel.setItems(currentDetail.sessions());
            status("Đã tải chi tiết nhánh.");
        });
    }

    private void fillBranchDetail(BranchDetailView detail, BranchPreviewView preview) {
        if (detail == null) {
            detailNameLabel.setText("Chưa chọn nhánh");
            detailMetaLabel.setText(" ");
            detailConfigLabel.setText(" ");
            detailPreviewLabel.setText(" ");
            detailOutputLabel.setText(" ");
            renameBranchField.setText("");
            resetBranchField.setText("");
            appendStaffField.setText("");
            appendRoomField.setText("");
            return;
        }
        ScheduleBranchView branch = detail.branch();
        detailNameLabel.setText(branch.name());
        detailMetaLabel.setText(branch.branchId() + " | tạo lúc " + branch.createdAtDisplay());
        detailConfigLabel.setText("Mặc định: " + branch.requestedStaffCount() + " cán bộ / "
                + branch.requestedRoomCount() + " phòng | đã tạo " + branch.sessionCreatedCount()
                + " ca | ca kế tiếp " + branch.nextSessionNo());
        detailPreviewLabel.setText(preview == null
                ? " "
                : preview.message() + " | đã dùng " + preview.usedPairCount()
                + " cặp, " + preview.constrainedStaffCount() + " cán bộ có lịch sử.");
        detailOutputLabel.setText("File kết quả: "
                + (detail.invigilatorFileAvailable() ? "đã có danh sách coi thi" : "chưa có danh sách coi thi")
                + " | "
                + (detail.monitorFileAvailable() ? "đã có danh sách giám sát" : "chưa có danh sách giám sát"));
        renameBranchField.setText(branch.name());
        resetBranchField.setText(branch.name() + " - làm lại");
        appendStaffField.setText("");
        appendRoomField.setText("");
    }

    private void fillArchivedBranchDetail(ScheduleBranchView branch) {
        detailNameLabel.setText(branch.name());
        detailMetaLabel.setText(branch.branchId() + " | tạo lúc " + branch.createdAtDisplay());
        detailConfigLabel.setText("Mặc định: " + branch.requestedStaffCount() + " cán bộ / "
                + branch.requestedRoomCount() + " phòng | đã tạo " + branch.sessionCreatedCount()
                + " ca | ca kế tiếp " + branch.nextSessionNo());
        detailPreviewLabel.setText("Nhánh này đang ẩn. Hãy bấm \"Hiện lại nhánh\" để tiếp tục sử dụng.");
        detailOutputLabel.setText("Khi nhánh đang ẩn, ứng dụng không tải chi tiết ca và file kết quả.");
        renameBranchField.setText(branch.name());
        resetBranchField.setText(branch.name() + " - làm lại");
        appendStaffField.setText("");
        appendRoomField.setText("");
    }

    private void appendSessions() {
        ScheduleBranchView branch = selectedBranch();
        if (branch == null) {
            showWarning("Hãy chọn một nhánh trước.");
            return;
        }
        if (branch.archived()) {
            showWarning("Nhánh đang ẩn. Hãy hiện lại nhánh trước khi tạo thêm ca.");
            return;
        }
        Integer requestedStaffCount = parseOptionalPositive(appendStaffField.getText(), "Số cán bộ");
        Integer requestedRoomCount = parseOptionalPositive(appendRoomField.getText(), "Số phòng");
        if (requestedStaffCount == Integer.MIN_VALUE || requestedRoomCount == Integer.MIN_VALUE) {
            return;
        }
        runTask("Đang tạo thêm ca...", () -> apiClient.appendSessions(
                branch.branchId(),
                (Integer) appendSessionSpinner.getValue(),
                requestedStaffCount,
                requestedRoomCount
        ), updated -> {
            showInfo("Đã tạo thêm ca thi.");
            refreshBranches();
        });
    }

    private void renameSelectedBranch() {
        ScheduleBranchView branch = selectedBranch();
        if (branch == null) {
            showWarning("Hãy chọn một nhánh trước.");
            return;
        }
        String newName = renameBranchField.getText().trim();
        if (newName.isBlank()) {
            showWarning("Tên nhánh không được để trống.");
            return;
        }
        runTask("Đang đổi tên nhánh...", () -> {
            apiClient.renameBranch(branch.branchId(), newName);
            return null;
        }, ignored -> refreshBranches());
    }

    private void archiveSelectedBranch() {
        ScheduleBranchView branch = selectedBranch();
        if (branch == null) {
            showWarning("Hãy chọn một nhánh trước.");
            return;
        }
        if (!confirm("Ẩn nhánh \"" + branch.name() + "\"?")) {
            return;
        }
        runTask("Đang ẩn nhánh...", () -> {
            apiClient.archiveBranch(branch.branchId());
            return null;
        }, ignored -> refreshBranches());
    }

    private void restoreSelectedBranch() {
        ScheduleBranchView branch = selectedBranch();
        if (branch == null) {
            showWarning("Hãy chọn một nhánh trước.");
            return;
        }
        runTask("Đang hiện lại nhánh...", () -> {
            apiClient.restoreBranch(branch.branchId());
            return null;
        }, ignored -> refreshBranches());
    }

    private void resetBranch() {
        ScheduleBranchView branch = selectedBranch();
        if (branch == null) {
            showWarning("Hãy chọn một nhánh trước.");
            return;
        }
        if (branch.archived()) {
            showWarning("Nhánh đang ẩn. Hãy hiện lại nhánh trước khi làm lại từ ca đầu.");
            return;
        }
        String name = resetBranchField.getText().trim();
        runTask("Đang tạo nhánh làm lại...", () -> apiClient.resetBranch(branch.branchId(), name), created -> {
            showInfo("Đã tạo nhánh mới từ ca đầu.");
            refreshBranches();
        });
    }

    private void renameSelectedDataset(boolean staff) {
        if (staff) {
            StaffDatasetView dataset = selectedStaffDataset();
            if (dataset == null) {
                showWarning("Hãy chọn một bộ dữ liệu cán bộ.");
                return;
            }
            String value = JOptionPane.showInputDialog(this, "Tên mới", dataset.name());
            if (value == null || value.isBlank()) {
                return;
            }
            runTask("Đang đổi tên bộ dữ liệu cán bộ...", () -> {
                apiClient.renameStaffDataset(dataset.datasetId(), value.trim());
                return null;
            }, ignored -> refreshDatasets());
        } else {
            RoomDatasetView dataset = selectedRoomDataset();
            if (dataset == null) {
                showWarning("Hãy chọn một bộ dữ liệu phòng.");
                return;
            }
            String value = JOptionPane.showInputDialog(this, "Tên mới", dataset.name());
            if (value == null || value.isBlank()) {
                return;
            }
            runTask("Đang đổi tên bộ dữ liệu phòng...", () -> {
                apiClient.renameRoomDataset(dataset.datasetId(), value.trim());
                return null;
            }, ignored -> refreshDatasets());
        }
    }

    private void archiveSelectedDataset(boolean staff) {
        if (staff) {
            StaffDatasetView dataset = selectedStaffDataset();
            if (dataset == null) {
                showWarning("Hãy chọn một bộ dữ liệu cán bộ.");
                return;
            }
            runTask("Đang ẩn bộ dữ liệu cán bộ...", () -> {
                apiClient.archiveStaffDataset(dataset.datasetId());
                return null;
            }, ignored -> refreshDatasets());
        } else {
            RoomDatasetView dataset = selectedRoomDataset();
            if (dataset == null) {
                showWarning("Hãy chọn một bộ dữ liệu phòng.");
                return;
            }
            runTask("Đang ẩn bộ dữ liệu phòng...", () -> {
                apiClient.archiveRoomDataset(dataset.datasetId());
                return null;
            }, ignored -> refreshDatasets());
        }
    }

    private void restoreSelectedDataset(boolean staff) {
        if (staff) {
            StaffDatasetView dataset = selectedStaffDataset();
            if (dataset == null) {
                showWarning("Hãy chọn một bộ dữ liệu cán bộ.");
                return;
            }
            runTask("Đang hiện lại bộ dữ liệu cán bộ...", () -> {
                apiClient.restoreStaffDataset(dataset.datasetId());
                return null;
            }, ignored -> refreshDatasets());
        } else {
            RoomDatasetView dataset = selectedRoomDataset();
            if (dataset == null) {
                showWarning("Hãy chọn một bộ dữ liệu phòng.");
                return;
            }
            runTask("Đang hiện lại bộ dữ liệu phòng...", () -> {
                apiClient.restoreRoomDataset(dataset.datasetId());
                return null;
            }, ignored -> refreshDatasets());
        }
    }

    private void showSelectedSessionDetail() {
        BranchSessionRecordView session = selectedSession();
        if (session == null) {
            showWarning("Hãy chọn một ca để xem.");
            return;
        }
        runTask("Đang tải chi tiết ca...", () -> apiClient.getBranchSession(session.branchId(), session.sessionNo()), this::showSessionDialog);
    }

    private void showSessionDialog(BranchSessionRecordView session) {
        JDialog dialog = new JDialog(this, "Ca " + session.sessionNo(), true);
        dialog.setSize(1080, 720);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG);
        dialog.setLayout(new BorderLayout(12, 12));

        JPanel top = createCardPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(textLabel("Chi tiết ca " + session.sessionNo(), 22f, true));
        top.add(Box.createVerticalStrut(6));
        top.add(subtleLabel("Tạo lúc " + session.createdAtDisplay()
                + " | dùng " + session.requestedStaffCount() + " cán bộ / " + session.requestedRoomCount() + " phòng"));
        dialog.add(top, BorderLayout.NORTH);

        JTable roomAssignmentTable = new JTable(new SessionRoomAssignmentModel(session.session().roomAssignments()));
        JTable hallMonitorTable = new JTable(new HallMonitorTableModel(session.session().hallMonitorAssignments()));
        styleTable(roomAssignmentTable);
        styleTable(hallMonitorTable);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                wrapTableCard("Bảng phân công coi thi", roomAssignmentTable),
                wrapTableCard("Bảng giám sát hành lang", hallMonitorTable)
        );
        splitPane.setResizeWeight(0.6);
        splitPane.setBorder(null);
        dialog.add(splitPane, BorderLayout.CENTER);

        dialog.setVisible(true);
    }

    private JPanel wrapTableCard(String title, JTable table) {
        JPanel panel = createCardPanel();
        panel.setLayout(new BorderLayout(12, 12));
        panel.add(sectionTitle(title, null), BorderLayout.NORTH);
        panel.add(createTableScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void downloadOutput(String type) {
        ScheduleBranchView branch = selectedBranch();
        if (branch == null) {
            showWarning("Hãy chọn một nhánh trước.");
            return;
        }
        if (branch.archived()) {
            showWarning("Nhánh đang ẩn. Hãy hiện lại nhánh trước khi tải file kết quả.");
            return;
        }
        runTask("Đang tải file kết quả...", () -> apiClient.downloadFile(branch.branchId(), type), file -> {
            Path path = showNativeSaveDialog(file.filename());
            if (path == null) {
                return;
            }
            Files.write(path, file.content());
            showInfo("Đã lưu file tại: " + path);
        });
    }

    private void downloadSelectedSessionOutput(String type) {
        ScheduleBranchView branch = selectedBranch();
        if (branch == null) {
            showWarning("Hãy chọn một nhánh trước.");
            return;
        }
        if (branch.archived()) {
            showWarning("Nhánh đang ẩn. Hãy hiện lại nhánh trước khi tải file kết quả.");
            return;
        }
        BranchSessionRecordView session = selectedSession();
        if (session == null) {
            showWarning("Hãy chọn một ca để tải file.");
            return;
        }
        runTask("Đang tải file của ca đã chọn...", () -> apiClient.downloadSessionFile(branch.branchId(), session.sessionNo(), type), file -> {
            Path path = showNativeSaveDialog(file.filename());
            if (path == null) {
                return;
            }
            Files.write(path, file.content());
            showInfo("Đã lưu file tại: " + path);
        });
    }

    private File showNativeOpenDialog() {
        FileDialog dialog = new FileDialog(this, "Chọn file dữ liệu", FileDialog.LOAD);
        dialog.setFilenameFilter(excelFileFilter());
        dialog.setVisible(true);
        String directory = dialog.getDirectory();
        String fileName = dialog.getFile();
        if (directory == null || fileName == null) {
            return null;
        }
        return new File(directory, fileName);
    }

    private Path showNativeSaveDialog(String defaultFileName) {
        FileDialog dialog = new FileDialog(this, "Lưu file kết quả", FileDialog.SAVE);
        dialog.setFile(defaultFileName);
        dialog.setFilenameFilter(excelFileFilter());
        dialog.setVisible(true);
        String directory = dialog.getDirectory();
        String fileName = dialog.getFile();
        if (directory == null || fileName == null) {
            return null;
        }
        return Path.of(directory, fileName);
    }

    private FilenameFilter excelFileFilter() {
        return (directory, name) -> {
            String lower = name == null ? "" : name.toLowerCase();
            return lower.endsWith(".xlsx") || lower.endsWith(".xls");
        };
    }

    private void reconnect() {
        apiClient.setServerBaseUrl(serverUrlField.getText().trim());
        refreshAll();
    }

    private ScheduleBranchView selectedBranch() {
        int selected = branchTable.getSelectedRow();
        if (selected < 0) {
            return null;
        }
        return branchTableModel.getItem(branchTable.convertRowIndexToModel(selected));
    }

    private String selectedBranchId() {
        ScheduleBranchView branch = selectedBranch();
        return branch == null ? null : branch.branchId();
    }

    private BranchSessionRecordView selectedSession() {
        int selected = sessionTable.getSelectedRow();
        if (selected < 0) {
            return null;
        }
        return sessionTableModel.getItem(sessionTable.convertRowIndexToModel(selected));
    }

    private StaffDatasetView selectedStaffDataset() {
        int selected = staffTable.getSelectedRow();
        if (selected < 0) {
            return null;
        }
        return staffTableModel.getItem(staffTable.convertRowIndexToModel(selected));
    }

    private RoomDatasetView selectedRoomDataset() {
        int selected = roomTable.getSelectedRow();
        if (selected < 0) {
            return null;
        }
        return roomTableModel.getItem(roomTable.convertRowIndexToModel(selected));
    }

    private void restoreBranchSelection(String branchId) {
        if (branchId == null) {
            if (branchTableModel.getRowCount() > 0) {
                branchTable.setRowSelectionInterval(0, 0);
            }
            return;
        }
        for (int index = 0; index < branchTableModel.getRowCount(); index++) {
            if (Objects.equals(branchTableModel.getItem(index).branchId(), branchId)) {
                int viewIndex = branchTable.convertRowIndexToView(index);
                branchTable.setRowSelectionInterval(viewIndex, viewIndex);
                return;
            }
        }
        if (branchTableModel.getRowCount() > 0) {
            branchTable.setRowSelectionInterval(0, 0);
        } else {
            fillBranchDetail(null, null);
            sessionTableModel.setItems(List.of());
        }
    }

    private Integer parseOptionalPositive(String raw, String label) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            if (value <= 0) {
                showWarning(label + " phải là số nguyên dương.");
                return Integer.MIN_VALUE;
            }
            return value;
        } catch (NumberFormatException exception) {
            showWarning(label + " phải là số nguyên dương.");
            return Integer.MIN_VALUE;
        }
    }

    private boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void status(String message) {
        statusLabel.setText(message);
    }

    private <T> void runTask(String message, BackgroundTask<T> task, BackgroundSuccess<T> success) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        status(message);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.run();
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    success.accept(get());
                } catch (Exception exception) {
                    status("Có lỗi xảy ra.");
                    showWarning(exception.getMessage());
                }
            }
        }.execute();
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(LINE, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));
        return panel;
    }

    private JPanel createInnerCard() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(255, 250, 242));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(236, 226, 211), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));
        return panel;
    }

    private JPanel sectionTitle(String title, String subtitle) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(textLabel(title, 22f, true));
        if (subtitle != null && !subtitle.isBlank()) {
            panel.add(Box.createVerticalStrut(4));
            panel.add(subtleLabel(subtitle));
        }
        return panel;
    }

    private JLabel textLabel(String text, float size, boolean bold) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(label.getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN, size));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel subtleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JComponent labeled(String title, JComponent component) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel(title);
        label.setForeground(MUTED);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setBorder(new EmptyBorder(0, 0, 6, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(component);
        return panel;
    }

    private JComponent wrapTopAligned(JComponent component) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(component, BorderLayout.NORTH);
        return panel;
    }

    private JComponent wrapFillHorizontal(JComponent component) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setUI(new BasicButtonUI());
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setRolloverEnabled(false);
        button.setBorderPainted(true);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
        return button;
    }

    private void styleTextField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(LINE, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT);
    }

    private void setCompactHeight(JTextComponent component, int height) {
        Dimension preferred = component.getPreferredSize();
        component.setPreferredSize(new Dimension(preferred.width, height));
        component.setMinimumSize(new Dimension(120, height));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }

    private void styleCombo(JComboBox<?> comboBox) {
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT);
        comboBox.setBorder(new LineBorder(LINE, 1, true));
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(new EmptyBorder(6, 8, 6, 8));
                return this;
            }
        });
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setBorder(new LineBorder(LINE, 1, true));
        spinner.setBackground(Color.WHITE);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            defaultEditor.getTextField().setBorder(new EmptyBorder(8, 10, 8, 10));
            defaultEditor.getTextField().setBackground(Color.WHITE);
            defaultEditor.getTextField().setForeground(TEXT);
        }
    }

    private JTable createTable(AbstractTableModel model) {
        JTable table = new JTable(model);
        styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        return table;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(236, 226, 211));
        table.setSelectionBackground(new Color(231, 243, 240));
        table.setSelectionForeground(TEXT);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(new Color(250, 245, 236));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        renderer.setForeground(TEXT);
        renderer.setBackground(Color.WHITE);
        table.setDefaultRenderer(Object.class, renderer);
    }

    private JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(236, 226, 211), 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(320, 420));
        return scrollPane;
    }

    private record DatasetOption(String id, String name, int count, String unit) {
        @Override
        public String toString() {
            return name + " (" + count + " " + unit + ")";
        }
    }

    private record DatasetBundle(
            List<StaffDatasetView> staffItems,
            List<RoomDatasetView> roomItems,
            List<StaffDatasetView> activeStaffItems,
            List<RoomDatasetView> activeRoomItems
    ) {
    }

    private record BranchScreenData(BranchDetailView detail, BranchPreviewView preview) {
    }

    @FunctionalInterface
    private interface BackgroundTask<T> {
        T run() throws Exception;
    }

    @FunctionalInterface
    private interface BackgroundSuccess<T> {
        void accept(T value) throws Exception;
    }

    private abstract static class ListBackedTableModel<T> extends AbstractTableModel {
        private final List<T> items = new ArrayList<>();

        void setItems(List<T> values) {
            items.clear();
            items.addAll(values);
            fireTableDataChanged();
        }

        T getItem(int index) {
            return items.get(index);
        }

        @Override
        public int getRowCount() {
            return items.size();
        }
    }

    private static final class DatasetTableModel<T> extends ListBackedTableModel<T> {
        private final String unit;

        private DatasetTableModel(String unit) {
            this.unit = unit;
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> "Tên";
                case 1 -> "Số lượng";
                case 2 -> "File";
                case 3 -> "Thời gian";
                case 4 -> "Trạng thái";
                default -> "";
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Object item = getItem(rowIndex);
            if (item instanceof StaffDatasetView staff) {
                return switch (columnIndex) {
                    case 0 -> staff.name();
                    case 1 -> staff.staffCount() + " " + unit;
                    case 2 -> staff.originalFileName();
                    case 3 -> staff.createdAtDisplay();
                    case 4 -> staff.archived() ? "Đã ẩn" : "Đang dùng";
                    default -> "";
                };
            }
            RoomDatasetView room = (RoomDatasetView) item;
            return switch (columnIndex) {
                case 0 -> room.name();
                case 1 -> room.roomCount() + " " + unit;
                case 2 -> room.originalFileName();
                case 3 -> room.createdAtDisplay();
                case 4 -> room.archived() ? "Đã ẩn" : "Đang dùng";
                default -> "";
            };
        }
    }

    private static final class BranchTableModel extends ListBackedTableModel<ScheduleBranchView> {
        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> "Tên nhánh";
                case 1 -> "Cán bộ / phòng";
                case 2 -> "Đã tạo";
                case 3 -> "Ca kế tiếp";
                case 4 -> "Bộ dữ liệu";
                case 5 -> "Trạng thái";
                default -> "";
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ScheduleBranchView item = getItem(rowIndex);
            return switch (columnIndex) {
                case 0 -> item.name();
                case 1 -> item.requestedStaffCount() + " / " + item.requestedRoomCount();
                case 2 -> item.sessionCreatedCount();
                case 3 -> item.nextSessionNo();
                case 4 -> item.staffDatasetName() + " | " + item.roomDatasetName();
                case 5 -> item.archived() ? "Đã ẩn" : "Đang hoạt động";
                default -> "";
            };
        }
    }

    private static final class SessionTableModel extends ListBackedTableModel<BranchSessionRecordView> {
        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> "Ca";
                case 1 -> "Cấu hình";
                case 2 -> "Phòng phân công";
                case 3 -> "Giám sát";
                case 4 -> "Thời gian";
                default -> "";
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            BranchSessionRecordView item = getItem(rowIndex);
            return switch (columnIndex) {
                case 0 -> item.sessionNo();
                case 1 -> item.requestedStaffCount() + " cán bộ / " + item.requestedRoomCount() + " phòng";
                case 2 -> item.summary().roomAssignmentCount();
                case 3 -> item.summary().hallMonitorCount();
                case 4 -> item.createdAtDisplay();
                default -> "";
            };
        }
    }

    private static final class SessionRoomAssignmentModel extends ListBackedTableModel<RoomAssignmentView> {
        private SessionRoomAssignmentModel(List<RoomAssignmentView> items) {
            setItems(items);
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> "Phòng";
                case 1 -> "Mã cán bộ 1";
                case 2 -> "Họ tên cán bộ 1";
                case 3 -> "Mã cán bộ 2";
                case 4 -> "Họ tên cán bộ 2";
                default -> "";
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            RoomAssignmentView item = getItem(rowIndex);
            return switch (columnIndex) {
                case 0 -> item.room().roomName();
                case 1 -> item.invigilatorOne().staffCode();
                case 2 -> item.invigilatorOne().fullName();
                case 3 -> item.invigilatorTwo().staffCode();
                case 4 -> item.invigilatorTwo().fullName();
                default -> "";
            };
        }
    }

    private static final class HallMonitorTableModel extends ListBackedTableModel<HallMonitorAssignmentView> {
        private HallMonitorTableModel(List<HallMonitorAssignmentView> items) {
            setItems(items);
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> "Mã cán bộ";
                case 1 -> "Họ tên";
                case 2 -> "Dải phòng";
                default -> "";
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            HallMonitorAssignmentView item = getItem(rowIndex);
            return switch (columnIndex) {
                case 0 -> item.staff().staffCode();
                case 1 -> item.staff().fullName();
                case 2 -> item.rangeText();
                default -> "";
            };
        }
    }
}
