package com.uc.irp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.uc.irp.client.IncidentReportService;
import com.uc.irp.client.ReportInfo;
import com.google.appengine.api.datastore.Text;

/**
 * The implemenation of the RPC service which runs on the server.
 */
public class IncidentReportServiceImpl extends RemoteServiceServlet implements
		IncidentReportService {

	private static final Logger LOG = Logger
			.getLogger(IncidentReportServiceImpl.class.getName());

	public ReportInfo[] getIncidentReports() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		ReportInfo[] result;
		int i = 0;
		try {
			Query q = pm.newQuery("select from "
					+ IncidentReport.class.getName());
			q.setOrdering("captureTime");
			List<IncidentReport> incidentReports = (List<IncidentReport>) q
					.execute();
			result = new ReportInfo[incidentReports.size()];
			for (IncidentReport incidentReport : incidentReports) {
				ReportInfo info = new ReportInfo();
				info.setSubscriberId(incidentReport.getSubscriberId());
				info.setEvent((incidentReport.getEvent()));
				info.setLocation((incidentReport.getLocation()));
				info.setCaptureTime((incidentReport.getCaptureTime()));
				info.setContent(incidentReport.getContent().toString());
				result[i++] = info;
			}
		} finally {
			pm.close();
		}
		return result;
	}

}
