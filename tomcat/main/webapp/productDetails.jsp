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
    <h2>Product Details</h2>

    <%
        // Retrieve the productData attribute from the request
        List<Map<String, Object>> productData = (List<Map<String, Object>>) request.getAttribute("productData");

        // Check if productData is not null and not empty
        if (productData != null && !productData.isEmpty()) {
    %>
        <table>
            <tr>
                <th>Product ID</th>
                <th>Company ID</th>
                <th>Product Name</th>
            </tr>
            <%
                // Iterate over productData to display each product
                for (Map<String, Object> company : productData) {
            %>
                <tr>
                    <td><%= company.get("pid") %></td>
                    <td><%= company.get("cid") %></td>
                    <td><%= company.get("name") %></td>
                </tr>
            <%
                }
            %>
        </table>
    <%
        } else {
    %>
        <p>No product found with the given ID.</p>
    <%
        }
    %>
</body>
</html>
