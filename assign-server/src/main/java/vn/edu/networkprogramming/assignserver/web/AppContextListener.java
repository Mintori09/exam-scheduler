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
    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/exam_scheduler?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8";
    private static final String DEFAULT_DB_USER = "root";

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
            Path storageRoot = dataDir.resolve("runs");
            String jdbcUrl = resolveDbUrl(context);
            String jdbcUser = resolveDbUser(context);
            String jdbcPassword = resolveDbPassword(context);

            JsonService jsonService = new JsonService();
            SchedulingRepository repository = new SchedulingRepository(jdbcUrl, jdbcUser, jdbcPassword);
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
        String fromSystemProperty = System.getProperty("assign.dataDir");
        if (fromSystemProperty != null && !fromSystemProperty.isBlank()) {
            return fromSystemProperty;
        }
        fromSystemProperty = System.getProperty("dataDir");
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

    private String resolveDbUrl(ServletContext context) {
        return resolveConfigValue(context, "assign.dbUrl", "ASSIGN_DB_URL", "dbUrl", DEFAULT_DB_URL);
    }

    private String resolveDbUser(ServletContext context) {
        return resolveConfigValue(context, "assign.dbUser", "ASSIGN_DB_USER", "dbUser", DEFAULT_DB_USER);
    }

    private String resolveDbPassword(ServletContext context) {
        return resolveConfigValue(context, "assign.dbPassword", "ASSIGN_DB_PASSWORD", "dbPassword", "");
    }

    private String resolveConfigValue(
            ServletContext context,
            String systemPropertyName,
            String environmentName,
            String contextParamName,
            String defaultValue
    ) {
        String fromSystemProperty = System.getProperty(systemPropertyName);
        if (fromSystemProperty != null && !fromSystemProperty.isBlank()) {
            return fromSystemProperty;
        }
        String fromEnvironment = System.getenv(environmentName);
        if (fromEnvironment != null && !fromEnvironment.isBlank()) {
            return fromEnvironment;
        }
        String fromContextParam = context.getInitParameter(contextParamName);
        if (fromContextParam != null && !fromContextParam.isBlank()) {
            return fromContextParam;
        }
        return defaultValue;
    }
}
