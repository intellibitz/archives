package com.uc.irp.client

import com.google.gwt.core.client.GWT
import com.google.gwt.user.client.Command
import com.google.gwt.user.client.DeferredCommand
import com.google.gwt.user.client.rpc.AsyncCallback
import com.google.gwt.user.client.rpc.ServiceDefTarget
import com.google.gwt.user.client.ui.Composite
import com.uc.irp.client.DynaTableDataProvider.RowDataAcceptor

/**
 * A Composite widget that abstracts a DynaTableWidget and a data provider tied
 * to the <@link IncidentReportService> RPC endpoint.
 */
class IncidentReportsWidget(visibleRows: Int) : Composite() {

    /**
     * A data provider that bridges the provides row level updates from the data
     * available through a <@link SchoolCalendarService>.
     */
    inner class IncidentReportProvider : DynaTableDataProvider {

        private val incidentReportService: IncidentReportServiceAsync

        private var lastMaxRows = -1

        private var lastReports: Array<ReportInfo>? = null

        private var lastStartRow = -1

        init {
            // Initialize the service.
            //
            incidentReportService = GWT.create(IncidentReportService::class.java) as IncidentReportServiceAsync

            // By default, we assume we'll make RPCs to a servlet, but see
            // updateRowData(). There is special support for canned RPC responses.
            // (Which is a totally demo hack, by the way :-)
            //
            val target = incidentReportService as ServiceDefTarget

            // Use a module-relative URLs to ensure that this client code can find
            // its way home, even when the URL changes (as might happen when you
            // deploy this as a webapp under an external servlet container).
            val moduleRelativeURL = GWT.getModuleBaseURL() + "reports"
            target.serviceEntryPoint = moduleRelativeURL
        }

        override fun updateRowData(
            startRow: Int, maxRows: Int,
            acceptor: RowDataAcceptor
        ) {
            // Check the simple cache first.
            //
            if (startRow == lastStartRow) {
                if (maxRows == lastMaxRows) {
                    // Use the cached batch.
                    //
                    pushResults(acceptor, startRow, lastReports!!)
                    return
                }
            }

            // Fetch the data remotely.
            //
            incidentReportService.getIncidentReports(object : AsyncCallback<Array<ReportInfo>> {
                override fun onFailure(caught: Throwable) {
                    acceptor.failed(caught)
                }

                override fun onSuccess(result: Array<ReportInfo>) {
                    lastStartRow = startRow
                    lastMaxRows = maxRows
                    lastReports = result
                    pushResults(acceptor, startRow, result)
                }
            })
        }

        private fun pushResults(
            acceptor: RowDataAcceptor, startRow: Int,
            reports: Array<ReportInfo>
        ) {
            val rows = Array(reports.size) { arrayOfNulls<String>(5) as Array<String> }
            var i = 0
            val n = rows.size
            while (i < n) {
                val report = reports[i]
                rows[i] = arrayOfNulls(5)
                rows[i][0] = report.subscriberId
                rows[i][1] = report.event
                rows[i][2] = report.location
                rows[i][3] = report.captureTime.toString()
                rows[i][4] = report.content
                i++
            }
            acceptor.accept(startRow, rows)
        }
    }

    private val incidentReportProvider = IncidentReportProvider()

    private val eventFilter = booleanArrayOf(true, true, true, true)

    private val dynaTable: DynaTableWidget

    private var pendingRefresh: Command? = null

    init {
        val columns = arrayOf("Id", "Event", "Location", "Time", "Details")
        val styles = arrayOf("id", "event", "loc", "time", "details")
        dynaTable = DynaTableWidget(incidentReportProvider, columns, styles, visibleRows)
        initWidget(dynaTable)
    }

    fun getEventIncluded(event: Int): Boolean {
        return eventFilter[event]
    }

    override fun onLoad() {
        dynaTable.refresh()
    }

    fun setEventIncluded(event: Int, included: Boolean) {
        if (eventFilter[event] == included) {
            // No change.
            //
            return
        }

        eventFilter[event] = included
        if (pendingRefresh == null) {
            pendingRefresh = Command {
                pendingRefresh = null
                dynaTable.refresh()
            }
            DeferredCommand.addCommand(pendingRefresh)
        }
    }
}
