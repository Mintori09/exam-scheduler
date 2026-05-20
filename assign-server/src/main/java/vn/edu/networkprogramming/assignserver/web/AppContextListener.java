package vn.edu.networkprogramming.assignserver.web;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.nio.file.Files;
import java.nio.file.Path;
import vn.edu.networkprogramming.assignserver.repository.SchedulingRepository;
import vn.edu.networkprogramming.assignserver.service.AssignmentApplicationService;
import vn.edu.networkprogramming.assignserver.service.AssignmentPlanner;
import vn.edu.networkprogramming.assignserver.service.AssignmentWorkbookExportService;
import vn.edu.networkprogramming.assignserver.service.ExcelAssignmentInputService;
import vn.edu.networkprogramming.assignserver.service.JsonService;

@WebListener
public class AppContextListener implements ServletContextListener {

    public static final String ASSIGNMENT_SERVICE_KEY = "assignmentService";
    public static final String JSON_SERVICE_KEY = "jsonService";
    private static final String DEFAULT_DATA_DIR = "assign-server-data";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ServletContext context = sce.getServletContext();
            Path dataDir = Path.of(resolveDataDir(context));
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
            SchedulingRepository repository = new SchedulingRepository(dbPath);
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

    private String resolveDataDir(ServletContext context) {
        String fromSystemProperty = System.getProperty("dataDir");
        if (fromSystemProperty != null && !fromSystemProperty.isBlank()) {
            return fromSystemProperty;
        }
        String fromEnvironment = System.getenv("ASSIGN_SERVER_DATA_DIR");
        if (fromEnvironment != null && !fromEnvironment.isBlank()) {
            return fromEnvironment;
        }
        String fromContextParam = context.getInitParameter("dataDir");
        if (fromContextParam != null && !fromContextParam.isBlank()) {
            return fromContextParam;
        }
        return Path.of(System.getProperty("user.home"), DEFAULT_DATA_DIR).toString();
    }
}
