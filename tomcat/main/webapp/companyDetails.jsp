<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="java.util.List, java.util.Map" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Company Details</title>
    <style>
        table {
            width: 50%;
            border-collapse: collapse;
        }
        th, td {
            padding: 8px;
            border: 1px solid #ddd;
            text-align: center;
        }
        th {
            background-color: #f2f2f2;
        }
    </style>
</head>
<body>
    <h2>Company Details</h2>

    <%
        // Retrieve the companyData attribute from the request
        List<Map<String, Object>> companyData = (List<Map<String, Object>>) request.getAttribute("companyData");

        // Check if companyData is not null and not empty
        if (companyData != null && !companyData.isEmpty()) {
    %>
        <table>
            <tr>
                <th>Company ID</th>
                <th>Company Name</th>
                <th>Number of Products</th>
            </tr>
            <%
                // Iterate over companyData to display each company
                for (Map<String, Object> company : companyData) {
            %>
                <tr>
                    <td><%= company.get("id") %></td>
                    <td><%= company.get("name") %></td>
                    <td><%= company.get("number_of_products") %></td>
                </tr>
            <%
                }
            %>
        </table>
    <%
        } else {
    %>
        <p>No company found with the given ID.</p>
    <%
        }
    %>
</body>
</html>
