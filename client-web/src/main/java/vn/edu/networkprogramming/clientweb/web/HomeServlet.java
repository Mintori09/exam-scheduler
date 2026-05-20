package vn.edu.networkprogramming.clientweb.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/", "/assignments"})
public class HomeServlet extends BasePageServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            boolean includeArchived = Boolean.parseBoolean(req.getParameter("includeArchived"));
            req.setAttribute("message", req.getParameter("message"));
            req.setAttribute("error", req.getParameter("error"));
            req.setAttribute("includeArchived", includeArchived);
            req.setAttribute("staffDatasets", assignServerClient().listStaffDatasets(includeArchived));
            req.setAttribute("roomDatasets", assignServerClient().listRoomDatasets(includeArchived));
            req.setAttribute("branches", assignServerClient().listBranches(includeArchived));
            req.setAttribute("activeStaffDatasets", assignServerClient().listStaffDatasets(false));
            req.setAttribute("activeRoomDatasets", assignServerClient().listRoomDatasets(false));
            req.setAttribute("selectedStaffDatasetId", req.getParameter("staffDatasetId"));
            req.setAttribute("selectedRoomDatasetId", req.getParameter("roomDatasetId"));
            req.setAttribute("branchName", valueOrEmpty(firstNonBlank(req.getParameter("branchName"), req.getParameter("name"))));
            req.setAttribute("requestedStaffCount", valueOrEmpty(req.getParameter("requestedStaffCount")));
            req.setAttribute("requestedRoomCount", valueOrEmpty(req.getParameter("requestedRoomCount")));
            req.setAttribute("sessionCount", valueOrEmpty(req.getParameter("sessionCount")));
            forward("home.jsp", req, resp);
        } catch (Exception exception) {
            req.setAttribute("error", exception.getMessage());
            try {
                forward("home.jsp", req, resp);
            } catch (Exception nested) {
                throw new IOException(nested);
            }
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }
}
