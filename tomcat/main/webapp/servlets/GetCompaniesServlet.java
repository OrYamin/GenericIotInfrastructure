package main.webapp.classes;

import dbms.AdminDBManager;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/getcompanies")
public class GetCompaniesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        AdminDBManager adminDBManager = AdminDBManager.getInstance();
        ResultSet resultSet = adminDBManager.read("SELECT * FROM companies");
        // Convert ResultSet to List of Maps for easier access in JSP
        List<Map<String, Object>> companyData = new ArrayList<>();

        try {
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", resultSet.getInt("company_id"));
                row.put("name", resultSet.getString("name"));
                row.put("number_of_products", resultSet.getInt("number_of_products"));
                companyData.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Set the company data as a request attribute
        request.setAttribute("companyData", companyData);

        // Forward to JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/pages/companyDetails.jsp");
        dispatcher.forward(request, resp);
    }
}
