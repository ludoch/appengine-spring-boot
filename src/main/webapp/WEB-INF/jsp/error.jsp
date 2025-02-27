
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP error Page</title>
    </head>
    <body>
        <h1>error page.</h1>
        <!-- See also application.yaml config that disables default server.error.whitelabel:enabled: false -->
        <%
            Exception e = (Exception) request.getAttribute("jakarta.servlet.error.exception");
            e.printStackTrace(response.getWriter());
        %>

    </body>
</html>
