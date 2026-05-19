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
            req.setAttribute("runs", assignServerClient().listAssignments());
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
}
