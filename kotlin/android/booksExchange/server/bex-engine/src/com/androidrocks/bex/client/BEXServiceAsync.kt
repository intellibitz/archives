package com.androidrocks.bex.client

import com.google.gwt.user.client.rpc.AsyncCallback

/**
 * The interface for the RPC server endpoint that provides school calendar
 * information for clients that will be calling asynchronously.
 */
interface BEXServiceAsync {

//  fun getIncidentReports(startIndex: Int, maxCount: Int, callback: AsyncCallback<Array<ReportInfo>>)
    fun test(callback: AsyncCallback<Array<ReportInfo>>)

}
