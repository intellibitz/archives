package com.uc.irp.client

/**
 * An interface for providing row-level updates of data, intended here to used
 * to update a DynaTableWidget.
 */
interface DynaTableDataProvider {

    /**
     * An interface allow a widget to accept or report failure when a row data
     * is issued for update.
     */
    interface RowDataAcceptor {
        fun accept(startRow: Int, rows: Array<Array<String>>)
        fun failed(caught: Throwable)
    }

    fun updateRowData(startRow: Int, maxRows: Int, acceptor: RowDataAcceptor)
}
