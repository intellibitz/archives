package com.uc.irp.client

import com.google.gwt.user.client.rpc.AsyncCallback

/**
 * The interface for the RPC server endpoint that provides school calendar
 * information for clients that will be calling asynchronously.
 */
interface IncidentReportServiceAsync {

//  fun getIncidentReports(startIndex: Int, maxCount: Int, callback: AsyncCallback<Array<ReportInfo>>)
    fun getIncidentReports(callback: AsyncCallback<Array<ReportInfo>>)
}
