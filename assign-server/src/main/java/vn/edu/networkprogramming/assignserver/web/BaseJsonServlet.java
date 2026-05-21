package vn.edu.networkprogramming.assignserver.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;
import vn.edu.networkprogramming.assignserver.service.AssignmentApplicationService;
import vn.edu.networkprogramming.assignserver.service.JsonService;

public abstract class BaseJsonServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BaseJsonServlet.class.getName());

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.service(req, resp);
        int status = resp.getStatus();
        if (status >= 200 && status < 300) {
            LOGGER.info(() -> "Client ket noi thanh cong: method=" + req.getMethod()
                    + ", uri=" + req.getRequestURI()
                    + ", status=" + status);
        }
    }

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
