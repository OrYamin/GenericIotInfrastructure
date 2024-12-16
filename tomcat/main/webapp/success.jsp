<!-- success.jsp -->
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Success</title>
</head>
<body>
    <h1>Success</h1>
    <p><%= request.getParameter("message") %></p> <!-- Display the custom success message -->
    <a href="/">Go Back to Home</a>
</body>
</html>
