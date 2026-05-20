package vn.edu.networkprogramming.assignserver.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import vn.edu.networkprogramming.assignserver.service.exception.ValidationException;

@WebServlet("/api/branches")
public class BranchCollectionServlet extends BaseJsonServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            boolean includeArchived = Boolean.parseBoolean(req.getParameter("includeArchived"));
            writeJson(resp, HttpServletResponse.SC_OK, assignmentService().findAllBranches(includeArchived));
        } catch (Exception exception) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiErrorResponse("FAILED", exception.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            String name = req.getParameter("name");
            String staffDatasetId = req.getParameter("staffDatasetId");
            String roomDatasetId = req.getParameter("roomDatasetId");
            int requestedStaffCount = Integer.parseInt(req.getParameter("requestedStaffCount"));
            int requestedRoomCount = Integer.parseInt(req.getParameter("requestedRoomCount"));
            int sessionCount = Integer.parseInt(req.getParameter("sessionCount"));
            writeJson(resp, HttpServletResponse.SC_OK, assignmentService().createBranchAndSessions(
                    name,
                    staffDatasetId,
                    roomDatasetId,
                    requestedStaffCount,
                    requestedRoomCount,
                    sessionCount
            ));
        } catch (ValidationException exception) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, new ApiErrorResponse("FAILED", exception.getMessage()));
        } catch (Exception exception) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ApiErrorResponse("FAILED", exception.getMessage()));
        }
    }
}
