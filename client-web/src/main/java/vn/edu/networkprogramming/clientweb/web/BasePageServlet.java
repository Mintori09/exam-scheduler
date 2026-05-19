package vn.edu.networkprogramming.clientweb.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import vn.edu.networkprogramming.clientweb.service.AssignServerClient;
import vn.edu.networkprogramming.clientweb.service.ExcelClientValidationService;

public abstract class BasePageServlet extends HttpServlet {

    protected AssignServerClient assignServerClient() {
        ServletContext context = getServletContext();
        return (AssignServerClient) context.getAttribute(AppContextListener.CLIENT_KEY);
    }

    protected ExcelClientValidationService validator() {
        ServletContext context = getServletContext();
        return (ExcelClientValidationService) context.getAttribute(AppContextListener.VALIDATOR_KEY);
    }

    protected void forward(String jsp, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/" + jsp);
        dispatcher.forward(request, response);
    }
}
