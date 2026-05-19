package vn.edu.networkprogramming.assignserver.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import vn.edu.networkprogramming.assignserver.service.AssignmentApplicationService;
import vn.edu.networkprogramming.assignserver.service.JsonService;

public abstract class BaseJsonServlet extends HttpServlet {

    protected AssignmentApplicationService assignmentService() {
        ServletContext context = getServletContext();
        return (AssignmentApplicationService) context.getAttribute(AppContextListener.ASSIGNMENT_SERVICE_KEY);
    }

    protected ObjectMapper objectMapper() {
        JsonService jsonService = (JsonService) getServletContext().getAttribute(AppContextListener.JSON_SERVICE_KEY);
        return jsonService.objectMapper();
    }

    protected void writeJson(HttpServletResponse response, int status, Object payload) throws IOException {
        byte[] jsonBytes = objectMapper().writeValueAsBytes(payload);
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.setContentLength(jsonBytes.length);
        response.getOutputStream().write(jsonBytes);
    }
}
