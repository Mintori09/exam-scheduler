package vn.edu.networkprogramming.clientweb.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/assignments/create")
@MultipartConfig
public class CreateAssignmentServlet extends BasePageServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            byte[] staffContent = req.getPart("staffFile").getInputStream().readAllBytes();
            byte[] roomContent = req.getPart("roomFile").getInputStream().readAllBytes();
            String staffFileName = req.getPart("staffFile").getSubmittedFileName();
            String roomFileName = req.getPart("roomFile").getSubmittedFileName();
            String sessionCountRaw = req.getParameter("sessionCount");

            var staffValidation = validator().validateStaffFile(staffFileName, staffContent);
            if (!staffValidation.valid()) {
                throw new IOException(staffValidation.message());
            }
            var roomValidation = validator().validateRoomFile(roomFileName, roomContent);
            if (!roomValidation.valid()) {
                throw new IOException(roomValidation.message());
            }
            var sessionValidation = validator().validateSessionCount(sessionCountRaw);
            if (!sessionValidation.valid()) {
                throw new IOException(sessionValidation.message());
            }

            var run = assignServerClient().createAssignment(
                    staffFileName,
                    staffContent,
                    roomFileName,
                    roomContent,
                    Integer.parseInt(sessionCountRaw)
            );
            resp.sendRedirect(req.getContextPath() + "/assignments/" + run.assignmentId());
        } catch (Exception exception) {
            req.setAttribute("error", exception.getMessage());
            try {
                req.setAttribute("runs", assignServerClient().listAssignments());
                forward("home.jsp", req, resp);
            } catch (Exception nested) {
                req.setAttribute("warning", "Khong the tai danh sach assignment hien tai: " + nested.getMessage());
                req.setAttribute("runs", List.of());
                forward("home.jsp", req, resp);
            }
        }
    }
}
