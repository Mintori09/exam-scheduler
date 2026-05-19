package vn.edu.networkprogramming.assignserver.web;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.nio.file.Files;
import java.nio.file.Path;
import vn.edu.networkprogramming.assignserver.repository.AssignmentRunRepository;
import vn.edu.networkprogramming.assignserver.service.AssignmentApplicationService;
import vn.edu.networkprogramming.assignserver.service.AssignmentPlanner;
import vn.edu.networkprogramming.assignserver.service.AssignmentWorkbookExportService;
import vn.edu.networkprogramming.assignserver.service.ExcelAssignmentInputService;
import vn.edu.networkprogramming.assignserver.service.JsonService;

@WebListener
public class AppContextListener implements ServletContextListener {

    public static final String ASSIGNMENT_SERVICE_KEY = "assignmentService";
    public static final String JSON_SERVICE_KEY = "jsonService";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ServletContext context = sce.getServletContext();
            String dataDirConfig = context.getInitParameter("dataDir");
            Path dataDir = dataDirConfig == null || dataDirConfig.isBlank()
                    ? Path.of(System.getProperty("user.home"), "assign-server-data")
                    : Path.of(dataDirConfig);
            if (!dataDir.isAbsolute()) {
                Path workingDir = Path.of(System.getProperty("user.dir"));
                Path fromWorkingDir = workingDir.resolve(dataDir).normalize();
                Path fromParentDir = workingDir.getParent() == null
                        ? fromWorkingDir
                        : workingDir.getParent().resolve(dataDir).normalize();
                dataDir = Files.exists(fromParentDir) ? fromParentDir : fromWorkingDir;
            }
            Files.createDirectories(dataDir);
            Path dbPath = dataDir.resolve("assignments.db");
            Path storageRoot = dataDir.resolve("runs");

            JsonService jsonService = new JsonService();
            AssignmentRunRepository repository = new AssignmentRunRepository(dbPath);
            repository.initialize();

            AssignmentApplicationService service = new AssignmentApplicationService(
                    new ExcelAssignmentInputService(),
                    new AssignmentPlanner(),
                    new AssignmentWorkbookExportService(),
                    repository,
                    jsonService,
                    storageRoot
            );

            context.setAttribute(ASSIGNMENT_SERVICE_KEY, service);
            context.setAttribute(JSON_SERVICE_KEY, jsonService);
        } catch (Exception exception) {
            throw new IllegalStateException("Khong khoi tao duoc assign-server", exception);
        }
    }
}
