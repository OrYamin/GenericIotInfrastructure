<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Main Page</title>
    <style>
        body {
            margin: 0;
            padding: 0;
            font-family: Arial, sans-serif;
            background-image: url('images/AI_Microsoft_Research_Header_1920x720.png');
            background-size: cover;
            background-position: center;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }

        form {
            background-color: rgba(255, 255, 255, 0.8);
            padding: 20px 40px;
            border-radius: 20px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
            text-align: center;
            margin: 20px;
        }

        button {
            background-color: #179DAA ;
            color: white;
            border: none;
            padding: 10px 20px;
            font-size: 16px;
            border-radius: 10px;
            cursor: pointer;
            transition: background-color width 1s ease;
        }

        button:hover {
            background-color: #45a079;
            width: 30vh;
        }

        button:active {
            background-color: #3e8e41;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1 style="color: white;">Welcome to the Company and Product Registration Portal</h1>

        <!-- Button to register a company -->
        <form action="http://10.10.1.209:8080/registercompany" method="get">
            <button type="submit">Register Company</button>
        </form>

        <!-- Button to register a product -->
        <form action="http://10.10.1.209:8080/registerproduct" method="get">
            <button type="submit">Register Product</button>
        </form>

        <!-- Button to get a company -->
        <form action="http://10.10.1.209:8080/getcompany" method="get">
              <button type="submit">Get Company</button>
        </form>

        <!-- Button to get companies -->
        <form action="http://10.10.1.209:8080/getcompanies" method="get">
            <button type="submit">Get Companies</button>
        </form>

        <!-- Button to get a product -->
        <form action="http://10.10.1.209:8080/getproduct" method="get">
              <button type="submit">Get Product</button>
        </form>

        <!-- Button to get products -->
        <form action="http://10.10.1.209:8080/getproducts" method="get">
            <button type="submit">Get Products</button>
        </form>
    </div>
</body>
</html>
