<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="com.uc.irp.server.IncidentReport" %>

<html>
  <head>
<Title>Incident Reporting Platform - United CommTel</Title>
    <link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
  </head>

  <body>

<p>Incident Reporting Platform - Test Upload

    <form action="/upload" method="post">
      <div><textarea name="content" rows="3" cols="60"></textarea></div>
      <div><input type="submit" value="Post Greeting" /></div>
    </form>

  </body>
</html>
