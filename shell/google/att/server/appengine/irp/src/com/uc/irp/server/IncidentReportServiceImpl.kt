package com.uc.irp.server

import com.google.gwt.user.server.rpc.RemoteServiceServlet
import com.uc.irp.client.IncidentReportService
import com.uc.irp.client.ReportInfo
import java.util.logging.Logger
import javax.jdo.Query

/**
 * The implemenation of the RPC service which runs on the server.
 */
class IncidentReportServiceImpl : RemoteServiceServlet(), IncidentReportService {

    override fun getIncidentReports(): Array<ReportInfo>? {
        val pm = PMF.get().persistenceManager
        val result: Array<ReportInfo>
        var i = 0
        try {
            val q: Query = pm.newQuery(
                "select from "
                        + IncidentReport::class.java.name
            )
            q.setOrdering("captureTime")
            @Suppress("UNCHECKED_CAST")
            val incidentReports = q.execute() as List<IncidentReport>
            result = arrayOfNulls<ReportInfo>(incidentReports.size) as Array<ReportInfo>
            for (incidentReport in incidentReports) {
                val info = ReportInfo()
                info.subscriberId = incidentReport.subscriberId
                info.event = incidentReport.event
                info.location = incidentReport.location
                info.captureTime = incidentReport.captureTime
                info.content = incidentReport.content.toString()
                result[i++] = info
            }
        } finally {
            pm.close()
        }
        return result
    }

    companion object {
        private val LOG = Logger.getLogger(IncidentReportServiceImpl::class.java.name)
    }
}
