package com.uc.irp.client

import com.google.gwt.user.client.rpc.RemoteService

/**
 * The interface for the RPC server endpoint to get school calendar
 * information.
 */
interface IncidentReportService : RemoteService {

//  fun getIncidentReports(startIndex: Int, maxCount: Int): Array<ReportInfo>?
    fun getIncidentReports(): Array<ReportInfo>?
}
