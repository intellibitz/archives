package com.uc.irp.client

import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.event.dom.client.ClickHandler
import com.uc.irp.client.DynaTableDataProvider.RowDataAcceptor
import com.google.gwt.user.client.rpc.InvocationException
import com.google.gwt.user.client.ui.Button
import com.google.gwt.user.client.ui.Composite
import com.google.gwt.user.client.ui.DialogBox
import com.google.gwt.user.client.ui.DockPanel
import com.google.gwt.user.client.ui.Grid
import com.google.gwt.user.client.ui.HTML
import com.google.gwt.user.client.ui.HasAlignment
import com.google.gwt.user.client.ui.HorizontalPanel
import com.google.gwt.user.client.ui.VerticalPanel

/**
 * A composite Widget that implements the main interface for the dynamic table,
 * including the data table, status indicators, and paging buttons.
 */
class DynaTableWidget(
    private val provider: DynaTableDataProvider,
    columns: Array<String>,
    columnStyles: Array<String>?,
    rowCount: Int
) : Composite() {

    /**
     * A dialog box for displaying an error.
     */
    private class ErrorDialog : DialogBox(), ClickHandler {
        private val body = HTML("")

        init {
            setStylePrimaryName("DynaTable-ErrorDialog")
            val closeButton = Button("Close", this)
            val panel = VerticalPanel()
            panel.setSpacing(4)
            panel.add(body)
            panel.add(closeButton)
            panel.setCellHorizontalAlignment(closeButton, VerticalPanel.ALIGN_RIGHT)
            setWidget(panel)
        }

        fun getBody(): String {
            return body.html
        }

        override fun onClick(event: ClickEvent) {
            hide()
        }

        fun setBody(html: String?) {
            body.html = html
        }
    }

    private inner class NavBar : Composite(), ClickHandler {

        val bar = DockPanel()
        val gotoFirst = Button("&lt;&lt;", this)
        val gotoNext = Button("&gt;", this)
        val gotoPrev = Button("&lt;", this)
        val status = HTML()

        init {
            initWidget(bar)
            bar.setStyleName("navbar")
            status.setStyleName("status")

            val buttons = HorizontalPanel()
            buttons.add(gotoFirst)
            buttons.add(gotoPrev)
            buttons.add(gotoNext)
            bar.add(buttons, DockPanel.EAST)
            bar.setCellHorizontalAlignment(buttons, DockPanel.ALIGN_RIGHT)
            bar.add(status, DockPanel.CENTER)
            bar.setVerticalAlignment(DockPanel.ALIGN_MIDDLE)
            bar.setCellHorizontalAlignment(status, HasAlignment.ALIGN_RIGHT)
            bar.setCellVerticalAlignment(status, HasAlignment.ALIGN_MIDDLE)
            bar.setCellWidth(status, "100%")

            // Initialize prev & first button to disabled.
            //
            gotoPrev.isEnabled = false
            gotoFirst.isEnabled = false
        }

        override fun onClick(event: ClickEvent) {
            val source = event.source
            if (source === gotoNext) {
                startRow += getDataRowCount()
                refresh()
            } else if (source === gotoPrev) {
                startRow -= getDataRowCount()
                if (startRow < 0) {
                    startRow = 0
                }
                refresh()
            } else if (source === gotoFirst) {
                startRow = 0
                refresh()
            }
        }
    }

    private inner class RowDataAcceptorImpl : RowDataAcceptor {
        override fun accept(startRow: Int, data: Array<Array<String>>) {

            val destRowCount = getDataRowCount()
            val destColCount = grid.getCellCount(0)
            assert(data.size <= destRowCount) { "Too many rows" }

            var srcRowIndex = 0
            val srcRowCount = data.size
            var destRowIndex = 1 // skip navbar row
            while (srcRowIndex < srcRowCount) {
                val srcRowData = data[srcRowIndex]
                assert(srcRowData.size == destColCount) { " Column count mismatch" }
                for (srcColIndex in 0 until destColCount) {
                    val cellHTML = srcRowData[srcColIndex]
                    grid.setText(destRowIndex, srcColIndex, cellHTML)
                }
                ++srcRowIndex
                ++destRowIndex
            }

            // Clear remaining table rows.
            //
            var isLastPage = false
            while (destRowIndex < destRowCount + 1) {
                isLastPage = true
                for (destColIndex in 0 until destColCount) {
                    grid.clearCell(destRowIndex, destColIndex)
                }
                ++destRowIndex
            }

            // Synchronize the nav buttons.
            navbar.gotoNext.isEnabled = !isLastPage
            navbar.gotoFirst.isEnabled = startRow > 0
            navbar.gotoPrev.isEnabled = startRow > 0

            // Update the status message.
            //
            setStatusText((startRow + 1).toString() + " - " + (startRow + srcRowCount))
        }

        override fun failed(caught: Throwable) {
            setStatusText("Error")
            if (errorDialog == null) {
                errorDialog = ErrorDialog()
            }
            if (caught is InvocationException) {
                errorDialog!!.setText("An RPC server could not be reached")
                errorDialog!!.setBody(NO_CONNECTION_MESSAGE)
            } else {
                errorDialog!!.setText("Unexcepted Error processing remote call")
                errorDialog!!.setBody(caught.message)
            }
            errorDialog!!.center()
        }
    }

    private val acceptor: RowDataAcceptor = RowDataAcceptorImpl()

    private val grid = Grid()

    private val navbar = NavBar()

    private var errorDialog: ErrorDialog? = null

    private val outer = DockPanel()

    private var startRow = 0

    init {
        if (columns.isEmpty()) {
            throw IllegalArgumentException(
                "expecting a positive number of columns"
            )
        }

        if (columnStyles != null && columns.size != columnStyles.size) {
            throw IllegalArgumentException("expecting as many styles as columns")
        }

        initWidget(outer)
        grid.setStyleName("table")
        outer.add(navbar, DockPanel.NORTH)
        outer.add(grid, DockPanel.CENTER)
        initTable(columns, columnStyles, rowCount)
        setStyleName("DynaTable-DynaTableWidget")
    }

    fun clearStatusText() {
        navbar.status.html = "&nbsp;"
    }

    fun refresh() {
        // Disable buttons temporarily to stop the user from running off the end.
        //
        navbar.gotoFirst.isEnabled = false
        navbar.gotoPrev.isEnabled = false
        navbar.gotoNext.isEnabled = false

        setStatusText("Please wait...")
        provider.updateRowData(startRow, grid.rowCount - 1, acceptor)
    }

    fun setRowCount(rows: Int) {
        grid.resizeRows(rows)
    }

    fun setStatusText(text: String?) {
        navbar.status.setText(text)
    }

    private fun getDataRowCount(): Int {
        return grid.rowCount - 1
    }

    private fun initTable(columns: Array<String>, columnStyles: Array<String>?, rowCount: Int) {
        // Set up the header row. It's one greater than the number of visible rows.
        //
        grid.resize(rowCount + 1, columns.size)
        var i = 0
        val n = columns.size
        while (i < n) {
            grid.setText(0, i, columns[i])
            if (columnStyles != null) {
                grid.cellFormatter.setStyleName(0, i, columnStyles[i] + " header")
            }
            i++
        }
    }

    companion object {
        private const val NO_CONNECTION_MESSAGE =
            "<p>Failed to connect to Server.. Try Again."
    }
}
