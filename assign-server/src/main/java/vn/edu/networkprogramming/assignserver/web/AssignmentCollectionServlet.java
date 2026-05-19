package vn.edu.networkprogramming.assignserver.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import vn.edu.networkprogramming.assignserver.service.exception.ValidationException;

@WebServlet("/api/assignments")
@MultipartConfig
public class AssignmentCollectionServlet extends BaseJsonServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            writeJson(resp, HttpServletResponse.SC_OK, assignmentService().findAllRuns());
        } catch (Exception exception) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiErrorResponse("FAILED", exception.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            byte[] staffContent = req.getPart("staffFile").getInputStream().readAllBytes();
            byte[] roomContent = req.getPart("roomFile").getInputStream().readAllBytes();
            String staffFileName = req.getPart("staffFile").getSubmittedFileName();
            String roomFileName = req.getPart("roomFile").getSubmittedFileName();
            int sessionCount = Integer.parseInt(req.getParameter("sessionCount"));
            var run = assignmentService().createAssignmentAsync(
                    staffFileName,
                    staffContent,
                    roomFileName,
                    roomContent,
                    sessionCount
            );
            writeJson(resp, HttpServletResponse.SC_ACCEPTED, run);
        } catch (ValidationException exception) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiErrorResponse("FAILED", exception.getMessage()));
        } catch (Exception exception) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiErrorResponse("FAILED", exception.getMessage()));
        }
    }
}
