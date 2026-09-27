package com.retailwave.fce.client.ui;
/**
 * $Id: UserResultsTable.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/ui/UserResultsTable.java $
 */

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.gen2.table.client.*;
import com.google.gwt.gen2.table.client.AbstractScrollTable.SortPolicy;
import com.google.gwt.gen2.table.client.TableModelHelper.Request;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.retailwave.fce.client.content.user.SearchUser;
import com.retailwave.fce.shared.dto.UserDTO;
import com.retailwave.fce.shared.rpc.UserServiceRemote;
import com.retailwave.fce.shared.rpc.UserServiceRemoteAsync;
import com.retailwave.fce.client.util.UIHelper;

import java.util.List;
import java.util.Set;

/**
 * UserResultsTable
 * <p/>
 * Paging scroll table to list search results for Users
 */
public class UserResultsTable extends DockLayoutPanel {

    // todo: generify the list, so the results table can be reused across types
    private PagingScrollTable<UserDTO> pagingScrollTable;
    private CachedTableModel<UserDTO> cachedTableModel;
    private DefaultTableDefinition<UserDTO> tableDefinition;
    private SearchUser parentContentWidget;

    private String action = SearchUser.actions[0];
    private UserDTO userCriteriaDTO;
    UserServiceRemoteAsync userServiceAsync = UserServiceRemote.App.getInstance();


    /**
     * Constructor.
     */
    public UserResultsTable() {
        super(Style.Unit.EM);
    }

    public void init(SearchUser contentWidget) {
        parentContentWidget = contentWidget;
        createPagingScrollTable();
        final PagingOptions pagingOptions = new PagingOptions(pagingScrollTable);
        addSouth(pagingOptions, 2);
        add(pagingScrollTable);
// todo: bug fix.. the table layout gets messed up if its not visible right away (investigate uibinder layout issues)       
//        setVisible(false);
    }

    public void search(UserDTO criteria) {
        UIHelper.scheduleProgress(SearchUser.uiConstants.searchProgressWait());
        userCriteriaDTO = criteria;
        cachedTableModel.clearCache();
        cachedTableModel.setPreCachedRowCount(10);
//        cachedTableModel.setPostCachedRowCount(10);
        cachedTableModel.setRowCount(100);
        if (null == criteria.getTypeSearch()) {
            userServiceAsync.countUsers(new AsyncCallback<Integer>() {
                @Override
                public void onFailure(Throwable throwable) {
                }

                @Override
                public void onSuccess(Integer integer) {
                    cachedTableModel.setRowCount(integer);
                }
            });
        } else {
            if (criteria.isPartnerUser()) {
                userServiceAsync.countPartnerUsers(new AsyncCallback<Integer>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        cachedTableModel.setRowCount(integer);
                    }
                });
            } else {
                userServiceAsync.countLexmarkUsers(new AsyncCallback<Integer>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        cachedTableModel.setRowCount(integer);
                    }
                });
            }
        }
        pagingScrollTable.setPageSize(10);
        pagingScrollTable.gotoPage(0, true);
    }

    private PagingScrollTable<UserDTO> createPagingScrollTable() {
        cachedTableModel = new CachedTableModel<UserDTO>(new DataSourceTableModel());
        tableDefinition = createTableDefinition();

        pagingScrollTable = new PagingScrollTable<UserDTO>(cachedTableModel, tableDefinition);
        FixedWidthGridBulkRenderer<UserDTO> bulkRenderer =
                new FixedWidthGridBulkRenderer<UserDTO>(pagingScrollTable.getDataTable(), pagingScrollTable);
        pagingScrollTable.setBulkRenderer(bulkRenderer);
        pagingScrollTable.setEmptyTableWidget(new HTML(SearchUser.uiConstants.searchEmpty()));
        pagingScrollTable.setCellPadding(3);
        pagingScrollTable.setCellSpacing(3);
//        pagingScrollTable.setResizePolicy(ScrollTable.ResizePolicy.FILL_WIDTH);
//        pagingScrollTable.setColumnResizePolicy(ScrollTable.ColumnResizePolicy.MULTI_CELL);
// note: sorting disabled for performance
// todo: revisit, if sorting required (note: sorting does not work due to bug.. investigate)       
        pagingScrollTable.setSortPolicy(SortPolicy.DISABLED);

//        pagingScrollTable.setHeight("250px");
        return pagingScrollTable;
    }

    private DefaultTableDefinition<UserDTO> createTableDefinition() {
        tableDefinition = new
                DefaultTableDefinition<UserDTO>();
        String[] rowColors = new String[]{"#FFFFDD", "#EEEEEE"};
        tableDefinition.setRowRenderer(new DefaultRowRenderer<UserDTO>(rowColors));

        {
            NameColumnDefinition colDef = new NameColumnDefinition();
            colDef.setHeader(0, "Name");
            colDef.setMinimumColumnWidth(75);
            colDef.setColumnTruncatable(false);
            tableDefinition.addColumnDefinition(colDef);
        }
        {
            FullNameColumnDefinition colDef = new FullNameColumnDefinition();
            colDef.setHeader(0, "Full Name");
            colDef.setMinimumColumnWidth(100);
            colDef.setColumnTruncatable(false);
            tableDefinition.addColumnDefinition(colDef);
        }

        {
            AbstractColumnDefinition<UserDTO, String> colDef = new EmailColumnDefinition();
            colDef.setHeader(0, "Email");
            colDef.setMinimumColumnWidth(100);
            colDef.setColumnTruncatable(false);
            tableDefinition.addColumnDefinition(colDef);
        }
        {
            AbstractColumnDefinition<UserDTO, String> colDef = new UserTypeColumnDefinition();
            colDef.setHeader(0, "Type");
            colDef.setMinimumColumnWidth(45);
            colDef.setPreferredColumnWidth(55);
            colDef.setMaximumColumnWidth(70);
            colDef.setColumnTruncatable(false);
            tableDefinition.addColumnDefinition(colDef);
        }

        {
            AbstractColumnDefinition<UserDTO, String> colDef = new ActiveColumnDefinition();
            colDef.setHeader(0, "Active");
            colDef.setMinimumColumnWidth(45);
            colDef.setPreferredColumnWidth(45);
            colDef.setMaximumColumnWidth(45);
            colDef.setColumnTruncatable(false);
            tableDefinition.addColumnDefinition(colDef);
        }

        {
            AbstractColumnDefinition<UserDTO, String> colDef = new ActionColumnDefinition();
            colDef.setHeader(0, "Action");
            colDef.setMinimumColumnWidth(45);
            colDef.setPreferredColumnWidth(55);
            colDef.setMaximumColumnWidth(70);
            final ClickHandler clickHandler = new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
// todo: operate in terms of raw generic type                   
                    Set<UserDTO> selectedRowValues = pagingScrollTable.getSelectedRowValues();
                    if (null != selectedRowValues && !selectedRowValues.isEmpty()) {
                        fireClickEvent(selectedRowValues.iterator().next());
                    }
                }
            };
            CellRenderer<UserDTO, String> editCellRenderer = new CellRenderer<UserDTO, String>() {
                @Override
                public void renderRowValue(UserDTO user, ColumnDefinition<UserDTO, String> userStringColumnDefinition,
                                           TableDefinition.AbstractCellView<UserDTO> userAbstractCellView) {
// local variable is required, for repeated rendition
// todo: investigate if button can be cached                   
                    Button editButton = new Button(action);
                    editButton.addClickHandler(clickHandler);
                    userAbstractCellView.setWidget(editButton);
                }
            };
            colDef.setCellRenderer(editCellRenderer);
            tableDefinition.addColumnDefinition(colDef);
        }

        return tableDefinition;
    }

    private void fireClickEvent(UserDTO userDTO) {
// todo: fire the modify userCriteriaDTO tree click event
        if (SearchUser.actions[0].equalsIgnoreCase(action)) {
            parentContentWidget.viewAction(userDTO);
        } else if (SearchUser.actions[1].equalsIgnoreCase(action)) {
            parentContentWidget.selectAction(userDTO);
        } else {
            parentContentWidget.actionPerformed(userDTO);
        }
    }

    // column definitions

    private final class NameColumnDefinition extends
            AbstractColumnDefinition<UserDTO, String> {
        @Override
        public String getCellValue(UserDTO rowValue) {
            return rowValue.getName();
        }

        @Override
        public void setCellValue(UserDTO rowValue, String cellValue) {
            rowValue.setName(cellValue);
        }
    }

    private final class FullNameColumnDefinition extends
            AbstractColumnDefinition<UserDTO, String> {
        @Override
        public String getCellValue(UserDTO rowValue) {
            return rowValue.getFullName();
        }

        @Override
        public void setCellValue(UserDTO rowValue, String cellValue) {
            rowValue.setFullName(cellValue);
        }
    }

    private final class EmailColumnDefinition extends
            AbstractColumnDefinition<UserDTO, String> {
        @Override
        public String getCellValue(UserDTO rowValue) {
            return rowValue.getEmailAddress();
        }

        @Override
        public void setCellValue(UserDTO rowValue, String cellValue) {
            rowValue.setEmailAddress(cellValue);
        }
    }

    private final class UserTypeColumnDefinition extends
            AbstractColumnDefinition<UserDTO, String> {
        @Override
        public String getCellValue(UserDTO rowValue) {
            if (rowValue.isPartnerUser()) {
                return "Partner";
            } else {
                return "Lexmark";
            }
        }

        @Override
        public void setCellValue(UserDTO rowValue, String cellValue) {
            rowValue.setPartnerUser("Lexmark".equalsIgnoreCase(cellValue));
        }
    }

    private final class ActiveColumnDefinition extends
            AbstractColumnDefinition<UserDTO, String> {
        @Override
        public String getCellValue(UserDTO rowValue) {
            if (rowValue.isActive()) {
                return "Yes";
            } else {
                return "No";
            }
        }

        @Override
        public void setCellValue(UserDTO rowValue, String cellValue) {
            rowValue.setPartnerUser("Yes".equalsIgnoreCase(cellValue));
        }
    }

    private final class ActionColumnDefinition extends
            AbstractColumnDefinition<UserDTO, String> {
        @Override
        public String getCellValue(UserDTO rowValue) {
            return action;
        }

        @Override
        public void setCellValue(UserDTO rowValue, String cellValue) {
//            rowValue.setActive(cellValue);
        }
    }

    public class DataSourceTableModel extends
            MutableTableModel<UserDTO> {

        @Override
        protected boolean onRowInserted(int beforeRow) {
            return true;
        }

        @Override
        protected boolean onRowRemoved(int row) {
            return true;
        }

        @Override
        protected boolean onSetRowValue(int row, UserDTO rowValue) {
            return true;
        }

        @Override
        public void requestRows(final Request request,
                                final Callback<UserDTO> callback) {
            userServiceAsync.searchUsers(userCriteriaDTO, request, new AsyncCallback<List<UserDTO>>() {
                @Override
                public void onFailure(Throwable throwable) {
                    UIHelper.cancelProgress();
                    UIHelper.showStatus(UIHelper.getUiConstants().searchFail());
                }

                @Override
                public void onSuccess(List<UserDTO> results) {
                    TableModelHelper.SerializableResponse<UserDTO> response =
                            new TableModelHelper.SerializableResponse<UserDTO>(results);
// todo: fix a better paging options display strategy if available                   
// hack: do the size fixing here.. since size ahead of time is a problem without actually fetching them
                    int sz = results.size();
                    if (sz < 10) {
                        int rows = ((request.getStartRow() / pagingScrollTable.getPageSize()) * 10) + sz;
                        cachedTableModel.setRowCount(rows);
                    }
                    callback.onRowsReady(request, response);
                    setVisible(true);
                    UIHelper.cancelProgress();
                    pagingScrollTable.fillWidth();
                }
            });
        }
    }

}