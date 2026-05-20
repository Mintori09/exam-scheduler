package vn.edu.networkprogramming.assignserver.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import vn.edu.networkprogramming.assignserver.service.exception.ValidationException;

@WebServlet("/api/staff-datasets")
@MultipartConfig
public class StaffDatasetServlet extends BaseJsonServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            boolean includeArchived = Boolean.parseBoolean(req.getParameter("includeArchived"));
            writeJson(resp, HttpServletResponse.SC_OK, assignmentService().findAllStaffDatasets(includeArchived));
        } catch (Exception exception) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiErrorResponse("FAILED", exception.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            byte[] content = req.getPart("file").getInputStream().readAllBytes();
            String fileName = req.getPart("file").getSubmittedFileName();
            String name = req.getParameter("name");
            writeJson(resp, HttpServletResponse.SC_OK, assignmentService().uploadStaffDataset(name, fileName, content));
        } catch (ValidationException exception) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiErrorResponse("FAILED", exception.getMessage()));
        } catch (Exception exception) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiErrorResponse("FAILED", exception.getMessage()));
        }
    }
}
