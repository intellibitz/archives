package com.androidrocks.bex.client;

import com.androidrocks.bex.client.ReportInfo;
import com.google.gwt.user.client.rpc.RemoteService;

/**
 * The interface for the RPC server endpoint to get school calendar
 * information.
 */
public interface BEXService extends RemoteService {
  
//  ReportInfo[] getIncidentReports(int startIndex, int maxCount);
  ReportInfo[] getIncidentReports();
  
}
