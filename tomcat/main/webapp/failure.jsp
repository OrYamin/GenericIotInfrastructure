<!-- failure.jsp -->
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Failure</title>
</head>
<body>
    <h1>Failure</h1>
    <p><%= request.getParameter("message") %></p> <!-- Display the custom failure message -->
    <a href="/">Go Back to Home</a>
</body>
</html>
