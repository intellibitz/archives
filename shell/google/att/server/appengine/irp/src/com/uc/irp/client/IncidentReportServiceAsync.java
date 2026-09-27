package com.uc.irp.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The interface for the RPC server endpoint that provides school calendar
 * information for clients that will be calling asynchronously. 
 */
public interface IncidentReportServiceAsync {

//  void getIncidentReports(int startIndex, int maxCount, AsyncCallback<ReportInfo[]> callback);
  void getIncidentReports(AsyncCallback<ReportInfo[]> callback);

}
