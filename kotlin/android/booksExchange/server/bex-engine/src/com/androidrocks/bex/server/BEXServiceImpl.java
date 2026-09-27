package com.androidrocks.bex.server;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import com.androidrocks.bex.client.BEXService;
import com.androidrocks.bex.client.ReportInfo;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.appengine.api.datastore.Text;

/**
 * The implemenation of the RPC service which runs on the server.
 */
public class BEXServiceImpl extends RemoteServiceServlet implements
		BEXService {

	private static final Logger LOG = Logger
			.getLogger(BEXServiceImpl.class.getName());

	public ReportInfo[] getIncidentReports() {
		// TODO Auto-generated method stub
		return null;
	}

}
