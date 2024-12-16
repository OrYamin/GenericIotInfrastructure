package main.webapp.classes;

import dbms.AdminDBManager;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/registercompany")
public class RegisterCompanyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/pages/registercompany.html");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve form parameters
        String companyName = req.getParameter("companyName");
        String numberOfProducts = req.getParameter("numberOfProducts");
        int id = 0;
        try {
            AdminDBManager adminDBManager = AdminDBManager.getInstance();
            id = adminDBManager.create("companies", "name, number_of_products", "('" + companyName + "', " + numberOfProducts + ")");
        } catch (Exception s){
            // Forward to failure.jsp
            RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/pages/failure.jsp?message=Failed to register company. Please try again.");
            dispatcher.forward(req, response);
        }

        // Forward to success.jsp
        RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/pages/success.jsp?message=" + "Company registered successfully!\nYour company id: " + id);
        dispatcher.forward(req, response);
    }
}
