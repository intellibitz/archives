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

<p>Incident Reporting Platform - Clean Database

<%
	PersistenceManager pm = PMF.get().getPersistenceManager();
    String query = "select from " + IncidentReport.class.getName();
    List<IncidentReport> reports = (List<IncidentReport>) pm.newQuery(query).execute();
    if (reports.isEmpty()) {
%>
<p>No reports found to be deleted.</p>
<%
	} else {
%>
<p>
<pre>
Deleting <%=reports.size()%> reports.
</pre>
</p>
<%
	}
    
    /** workaround for 500 delete limit */

    if (reports.size() > 500){
      List<IncidentReport> chunk = null;
      int index = 0;
      int indexHigh = 0;
      while (index < reports.size() ){
        indexHigh = index + 499;
        if (indexHigh >= reports.size()){
          indexHigh = reports.size() - 1;
        }
        chunk = reports.subList(index, indexHigh);
        pm.deletePersistentAll(chunk);
      }
    } else {

      pm.deletePersistentAll(reports); 
    }
    
    pm.close();
%>

  </body>
</html>
