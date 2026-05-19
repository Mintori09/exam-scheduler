package vn.edu.networkprogramming.clientweb.web;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import vn.edu.networkprogramming.clientweb.service.AssignServerClient;
import vn.edu.networkprogramming.clientweb.service.ExcelClientValidationService;
import vn.edu.networkprogramming.clientweb.service.JsonService;

@WebListener
public class AppContextListener implements ServletContextListener {

    public static final String CLIENT_KEY = "assignServerClient";
    public static final String VALIDATOR_KEY = "excelValidator";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        String serverBaseUrl = resolveServerBaseUrl(context);
        JsonService jsonService = new JsonService();
        context.setAttribute(CLIENT_KEY, new AssignServerClient(serverBaseUrl, jsonService));
        context.setAttribute(VALIDATOR_KEY, new ExcelClientValidationService());
        context.setAttribute("serverBaseUrl", serverBaseUrl);
    }

    private String resolveServerBaseUrl(ServletContext context) {
        String fromSystemProperty = System.getProperty("serverBaseUrl");
        if (fromSystemProperty != null && !fromSystemProperty.isBlank()) {
            return fromSystemProperty;
        }
        String fromEnvironment = System.getenv("SERVER_BASE_URL");
        if (fromEnvironment != null && !fromEnvironment.isBlank()) {
            return fromEnvironment;
        }
        String fromContextParam = context.getInitParameter("serverBaseUrl");
        if (fromContextParam != null && !fromContextParam.isBlank()) {
            return fromContextParam;
        }
        return "http://localhost:8081/assign-server";
    }
}
