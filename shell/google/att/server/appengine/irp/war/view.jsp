<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="com.uc.irp.server.PMF" %>
<%@ page import="com.uc.irp.server.IncidentReport" %>
<%@ page import="com.google.appengine.api.datastore.Text" %>

<html>
  <head>
<Title>Incident Reporting Platform - United CommTel</Title>
    <link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
  </head>

  <body>

<p>Incident Reporting Platform - View Reports

<%
	PersistenceManager pm = PMF.get().getPersistenceManager();
    String query = "select from " + IncidentReport.class.getName();
    List<IncidentReport> reports = (List<IncidentReport>) pm.newQuery(query).execute();
    System.out.println (reports);
    if (reports.isEmpty()) {
%>
<p>No reports found.</p>
<%
	} else {
%>
Total reports found: <%=reports.size()%>
<%
	for (IncidentReport report : reports) {
        	Text text = report.getContent();
%>
<p>
<pre>
<%= text.getValue() %>
</pre>
</p>
<%
        }
    }
    pm.close();
%>

  </body>
</html>
