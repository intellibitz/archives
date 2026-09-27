package com.uc.irp.server;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.util.ajax.JSON;

import com.google.appengine.api.datastore.Text;

public class UploadServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(UploadServlet.class
			.getName());

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String content = req.getParameter("content");
		log.info("Content received from clients: " + content);

		Map jsonMap = (Map) JSON.parse(content);

		Text textContent = new Text(content);
		IncidentReport incidentReport = new IncidentReport(textContent);
		Object sid = jsonMap.get("Subscriber Id");
		if (null != sid){
			incidentReport.setSubscriberId(sid.toString());
		}
		Object event = jsonMap.get("Event");
		if (null != event){
			incidentReport.setEvent(event.toString());
		}
		Object location = jsonMap.get("Location");
		if (null != location){
			String loc = location.toString();
			String cloc = "";
			String[]loca = loc.split(",");
			for (String items: loca){
				if (items.contains("mLatitude") || items.contains("mLongitude")){
					cloc += items + ",";
				}
			}
			if (null != loc){
				int s = loc.indexOf("mLatitude=");
			}
			incidentReport.setLocation(cloc);
		}
		try {
			Object tm = jsonMap.get("Time");
			if (tm != null){
				incidentReport.setCaptureTime(SimpleDateFormat
						.getDateTimeInstance()
						.parse(tm.toString()));
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PersistenceManager pm = PMF.get().getPersistenceManager();

		String result = "FAIL";
		try {
			pm.makePersistent(incidentReport);
			result = "OK";
		} finally {
			pm.close();
		}

		resp.setContentType("text/plain");
		resp.getWriter().print(result);
	}
}
