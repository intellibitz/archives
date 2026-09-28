package com.retailwave.fce.client.ui
/**
 * $Id: UserResultsTable.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/ui/UserResultsTable.java $
 */

import com.google.gwt.dom.client.Style
import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.event.dom.client.ClickHandler
import com.google.gwt.gen2.table.client.*
import com.google.gwt.gen2.table.client.AbstractScrollTable.SortPolicy
import com.google.gwt.gen2.table.client.TableModelHelper.Request
import com.google.gwt.user.client.rpc.AsyncCallback
import com.google.gwt.user.client.ui.Button
import com.google.gwt.user.client.ui.DockLayoutPanel
import com.google.gwt.user.client.ui.HTML
import com.retailwave.fce.client.content.user.SearchUser
import com.retailwave.fce.shared.dto.UserDTO
import com.retailwave.fce.shared.rpc.UserServiceRemote
import com.retailwave.fce.shared.rpc.UserServiceRemoteAsync
import com.retailwave.fce.client.util.UIHelper

import java.util.List
import java.util.Set

/**
 * UserResultsTable
 * <p/>
 * Paging scroll table to list search results for Users
 */
open class UserResultsTable : DockLayoutPanel() {

    // todo: generify the list, so the results table can be reused across types
    private var pagingScrollTable: PagingScrollTable<UserDTO>? = null
    private var cachedTableModel: CachedTableModel<UserDTO>? = null
    private var tableDefinition: DefaultTableDefinition<UserDTO>? = null
    private var parentContentWidget: SearchUser? = null

    private var action: String = SearchUser.actions[0]
    private var userCriteriaDTO: UserDTO? = null
    val userServiceAsync = UserServiceRemote.App.getInstance()

    /**
     * Constructor.
     */
    constructor() {
        super(Style.Unit.EM)
    }

    fun init(contentWidget: SearchUser) {
        parentContentWidget = contentWidget
        createPagingScrollTable()
        val pagingOptions = PagingOptions(pagingScrollTable)
        addSouth(pagingOptions, 2)
        add(pagingScrollTable)
// todo: bug fix.. the table layout gets messed up if its not visible right away (investigate uibinder layout issues)       
//        setVisible(false)
    }

    fun search(criteria: UserDTO) {
        UIHelper.scheduleProgress(SearchUser.uiConstants.searchProgressWait())
        userCriteriaDTO = criteria
        cachedTableModel.clearCache()
        cachedTableModel.setPreCachedRowCount(10)
//        cachedTableModel.setPostCachedRowCount(10)
        cachedTableModel.setRowCount(100)
        if (null == criteria.getTypeSearch()) {
            userServiceAsync.countUsers(object : AsyncCallback<Integer> {
                    override fun onFailure(throwable: Throwable) {
                }

                    override fun onSuccess(integer: Integer) {
                    cachedTableModel.setRowCount(integer)
                }
            })
        } else {
            if (criteria.isPartnerUser()) {
                userServiceAsync.countPartnerUsers(object : AsyncCallback<Integer> {
                        override fun onFailure(throwable: Throwable) {
                    }

                        override fun onSuccess(integer: Integer) {
                        cachedTableModel.setRowCount(integer)
                    }
                })
            } else {
                userServiceAsync.countLexmarkUsers(object : AsyncCallback<Integer> {
                        override fun onFailure(throwable: Throwable) {
                    }

                        override fun onSuccess(integer: Integer) {
                        cachedTableModel.setRowCount(integer)
                    }
                })
            }
        }
        pagingScrollTable.setPageSize(10)
        pagingScrollTable.gotoPage(0, true)
    }

    private fun createPagingScrollTable(): PagingScrollTable<UserDTO> {
        cachedTableModel = CachedTableModel<UserDTO>(DataSourceTableModel())
        tableDefinition = createTableDefinition()

        pagingScrollTable = PagingScrollTable<UserDTO>(cachedTableModel, tableDefinition)
        FixedWidthGridBulkRenderer<UserDTO> bulkRenderer =
                FixedWidthGridBulkRenderer<UserDTO>(pagingScrollTable.getDataTable(), pagingScrollTable)
        pagingScrollTable.setBulkRenderer(bulkRenderer)
        pagingScrollTable.setEmptyTableWidget(HTML(SearchUser.uiConstants.searchEmpty()))
        pagingScrollTable.setCellPadding(3)
        pagingScrollTable.setCellSpacing(3)
//        pagingScrollTable.setResizePolicy(ScrollTable.ResizePolicy.FILL_WIDTH)
//        pagingScrollTable.setColumnResizePolicy(ScrollTable.ColumnResizePolicy.MULTI_CELL)
// note: sorting disabled for performance
// todo: revisit, if sorting required (note: sorting does not work due to bug.. investigate)       
        pagingScrollTable.setSortPolicy(SortPolicy.DISABLED)

//        pagingScrollTable.setHeight("250px")
        return pagingScrollTable
    }

    private fun createTableDefinition(): DefaultTableDefinition<UserDTO> {
        tableDefinition = DefaultTableDefinition<UserDTO>()
        val rowColors = new String[]{"#FFFFDD", "#EEEEEE"}
        tableDefinition.setRowRenderer(DefaultRowRenderer<UserDTO>(rowColors))

        {
            val colDef = NameColumnDefinition()
            colDef.setHeader(0, "Name")
            colDef.setMinimumColumnWidth(75)
            colDef.setColumnTruncatable(false)
            tableDefinition.addColumnDefinition(colDef)
        }
        {
            val colDef = FullNameColumnDefinition()
            colDef.setHeader(0, "Full Name")
            colDef.setMinimumColumnWidth(100)
            colDef.setColumnTruncatable(false)
            tableDefinition.addColumnDefinition(colDef)
        }

        {
            val colDef = EmailColumnDefinition()
            colDef.setHeader(0, "Email")
            colDef.setMinimumColumnWidth(100)
            colDef.setColumnTruncatable(false)
            tableDefinition.addColumnDefinition(colDef)
        }
        {
            val colDef = UserTypeColumnDefinition()
            colDef.setHeader(0, "Type")
            colDef.setMinimumColumnWidth(45)
            colDef.setPreferredColumnWidth(55)
            colDef.setMaximumColumnWidth(70)
            colDef.setColumnTruncatable(false)
            tableDefinition.addColumnDefinition(colDef)
        }

        {
            val colDef = ActiveColumnDefinition()
            colDef.setHeader(0, "Active")
            colDef.setMinimumColumnWidth(45)
            colDef.setPreferredColumnWidth(45)
            colDef.setMaximumColumnWidth(45)
            colDef.setColumnTruncatable(false)
            tableDefinition.addColumnDefinition(colDef)
        }

        {
            val colDef = ActionColumnDefinition()
            colDef.setHeader(0, "Action")
            colDef.setMinimumColumnWidth(45)
            colDef.setPreferredColumnWidth(55)
            colDef.setMaximumColumnWidth(70)
            val clickHandler = object : ClickHandler() {
                    override fun onClick(clickEvent: ClickEvent) {
// todo: operate in terms of raw generic type                   
                    val selectedRowValues = pagingScrollTable.getSelectedRowValues()
                    if (null != selectedRowValues && !selectedRowValues.isEmpty()) {
                        fireClickEvent(selectedRowValues.iterator().next())
                    }
                }
            }
            val editCellRenderer = object : CellRenderer<UserDTO, String> {
                    override fun renderRowValue(user: UserDTO, userStringColumnDefinition: ColumnDefinition<UserDTO, String>, userAbstractCellView: TableDefinition.AbstractCellView<UserDTO>) {
// local variable is required, for repeated rendition
// todo: investigate if button can be cached                   
                    val editButton = Button(action)
                    editButton.addClickHandler(clickHandler)
                    userAbstractCellView.setWidget(editButton)
                }
            }
            colDef.setCellRenderer(editCellRenderer)
            tableDefinition.addColumnDefinition(colDef)
        }

        return tableDefinition
    }

    private fun fireClickEvent(userDTO: UserDTO) {
// todo: fire the modify userCriteriaDTO tree click event
        if (SearchUser.actions[0].equalsIgnoreCase(action)) {
            parentContentWidget.viewAction(userDTO)
        } else if (SearchUser.actions[1].equalsIgnoreCase(action)) {
            parentContentWidget.selectAction(userDTO)
        } else {
            parentContentWidget.actionPerformed(userDTO)
        }
    }

    // column definitions

    private inner class NameColumnDefinition : AbstractColumnDefinition<UserDTO, String> {
            override fun getCellValue(rowValue: UserDTO): String {
            return rowValue.getName()
        }

            override fun setCellValue(rowValue: UserDTO, cellValue: String) {
            rowValue.setName(cellValue)
        }
    }

    private inner class FullNameColumnDefinition : AbstractColumnDefinition<UserDTO, String> {
            override fun getCellValue(rowValue: UserDTO): String {
            return rowValue.getFullName()
        }

            override fun setCellValue(rowValue: UserDTO, cellValue: String) {
            rowValue.setFullName(cellValue)
        }
    }

    private inner class EmailColumnDefinition : AbstractColumnDefinition<UserDTO, String> {
            override fun getCellValue(rowValue: UserDTO): String {
            return rowValue.getEmailAddress()
        }

            override fun setCellValue(rowValue: UserDTO, cellValue: String) {
            rowValue.setEmailAddress(cellValue)
        }
    }

    private inner class UserTypeColumnDefinition : AbstractColumnDefinition<UserDTO, String> {
            override fun getCellValue(rowValue: UserDTO): String {
            if (rowValue.isPartnerUser()) {
                return "Partner"
            } else {
                return "Lexmark"
            }
        }

            override fun setCellValue(rowValue: UserDTO, cellValue: String) {
            rowValue.setPartnerUser("Lexmark".equalsIgnoreCase(cellValue))
        }
    }

    private inner class ActiveColumnDefinition : AbstractColumnDefinition<UserDTO, String> {
            override fun getCellValue(rowValue: UserDTO): String {
            if (rowValue.isActive()) {
                return "Yes"
            } else {
                return "No"
            }
        }

            override fun setCellValue(rowValue: UserDTO, cellValue: String) {
            rowValue.setPartnerUser("Yes".equalsIgnoreCase(cellValue))
        }
    }

    private inner class ActionColumnDefinition : AbstractColumnDefinition<UserDTO, String> {
            override fun getCellValue(rowValue: UserDTO): String {
            return action
        }

            override fun setCellValue(rowValue: UserDTO, cellValue: String) {
//            rowValue.setActive(cellValue)
        }
    }

    open class DataSourceTableModel : MutableTableModel<UserDTO>() {

            protected override fun onRowInserted(beforeRow: Int): Boolean {
            return true
        }

            protected override fun onRowRemoved(row: Int): Boolean {
            return true
        }

            protected override fun onSetRowValue(row: Int, rowValue: UserDTO): Boolean {
            return true
        }

            override fun requestRows(request: Request, callback: Callback<UserDTO>) {
            userServiceAsync.searchUsers(userCriteriaDTO, request, AsyncCallback<List<UserDTO>>() {
                    override fun onFailure(throwable: Throwable) {
                    UIHelper.cancelProgress()
                    UIHelper.showStatus(UIHelper.getUiConstants().searchFail())
                }

                    override fun onSuccess(results: List<UserDTO>) {
                    TableModelHelper.SerializableResponse<UserDTO> response =
                            TableModelHelper.SerializableResponse<UserDTO>(results)
// todo: fix a better paging options display strategy if available                   
// hack: do the size fixing here.. since size ahead of time is a problem without actually fetching them
                    var sz = results.size()
                    if (sz < 10) {
                        var rows = ((request.getStartRow() / pagingScrollTable.getPageSize()) * 10) + sz
                        cachedTableModel.setRowCount(rows)
                    }
                    callback.onRowsReady(request, response)
                    setVisible(true)
                    UIHelper.cancelProgress()
                    pagingScrollTable.fillWidth()
                }
            })
        }
    }

}