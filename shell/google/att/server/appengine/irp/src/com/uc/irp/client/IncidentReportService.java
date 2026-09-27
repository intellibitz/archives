package com.uc.irp.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.uc.irp.client.ReportInfo;;

/**
 * The interface for the RPC server endpoint to get school calendar
 * information.
 */
public interface IncidentReportService extends RemoteService {
  
//  ReportInfo[] getIncidentReports(int startIndex, int maxCount);
  ReportInfo[] getIncidentReports();
  
}
