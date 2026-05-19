package vn.edu.networkprogramming.clientweb.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.http.HttpResponse;

@WebServlet("/assignments/*")
public class AssignmentDetailServlet extends BasePageServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String[] parts = pathParts(req);
            if (parts.length == 1) {
                req.setAttribute("detail", assignServerClient().getAssignmentDetail(parts[0]));
                forward("detail.jsp", req, resp);
                return;
            }
            if (parts.length == 3 && "sessions".equals(parts[1])) {
                req.setAttribute("session", assignServerClient().getSessionDetail(parts[0], Integer.parseInt(parts[2])));
                req.setAttribute("assignmentId", parts[0]);
                forward("session.jsp", req, resp);
                return;
            }
            if (parts.length == 3 && "downloads".equals(parts[1])) {
                HttpResponse<byte[]> response = assignServerClient().download(parts[0], parts[2]);
                if (response.statusCode() / 100 != 2) {
                    throw new IOException("Khong tai duoc file");
                }
                String filename = "invigilators".equals(parts[2]) ? "DANHSACH_PHANCONG.xlsx" : "DANHSACH_GIAMSAT.xlsx";
                resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                resp.getOutputStream().write(response.body());
                return;
            }
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception exception) {
            req.setAttribute("error", exception.getMessage());
            try {
                req.setAttribute("runs", assignServerClient().listAssignments());
                forward("home.jsp", req, resp);
            } catch (Exception nested) {
                throw new IOException(nested);
            }
        }
    }

    private String[] pathParts(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            return new String[0];
        }
        return java.util.Arrays.stream(pathInfo.split("/"))
                .filter(part -> !part.isBlank())
                .toArray(String[]::new);
    }
}
