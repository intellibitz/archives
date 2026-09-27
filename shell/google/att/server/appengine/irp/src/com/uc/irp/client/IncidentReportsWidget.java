package com.uc.irp.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Composite;

/**
 * A Composite widget that abstracts a DynaTableWidget and a data provider tied
 * to the <@link IncidentReportService> RPC endpoint.
 */
public class IncidentReportsWidget extends Composite {

  /**
   * A data provider that bridges the provides row level updates from the data
   * available through a <@link SchoolCalendarService>.
   */
  public class IncidentReportProvider implements DynaTableDataProvider {

    private final IncidentReportServiceAsync incidentReportService;

    private int lastMaxRows = -1;

    private ReportInfo[] lastReports;

    private int lastStartRow = -1;

    public IncidentReportProvider() {
      // Initialize the service.
      //
      incidentReportService = (IncidentReportServiceAsync) GWT.create(IncidentReportService.class);

      // By default, we assume we'll make RPCs to a servlet, but see
      // updateRowData(). There is special support for canned RPC responses.
      // (Which is a totally demo hack, by the way :-)
      // 
      ServiceDefTarget target = (ServiceDefTarget) incidentReportService;

      // Use a module-relative URLs to ensure that this client code can find
      // its way home, even when the URL changes (as might happen when you
      // deploy this as a webapp under an external servlet container).
      String moduleRelativeURL = GWT.getModuleBaseURL() + "reports";
      target.setServiceEntryPoint(moduleRelativeURL);
    }

    public void updateRowData(final int startRow, final int maxRows,
        final RowDataAcceptor acceptor) {
      // Check the simple cache first.
      //
      if (startRow == lastStartRow) {
        if (maxRows == lastMaxRows) {
          // Use the cached batch.
          //
          pushResults(acceptor, startRow, lastReports);
          return;
        }
      }

      // Fetch the data remotely.
      //
      incidentReportService.getIncidentReports(new AsyncCallback<ReportInfo[]>() {
        public void onFailure(Throwable caught) {
          acceptor.failed(caught);
        }

        public void onSuccess(ReportInfo[] result) {
          lastStartRow = startRow;
          lastMaxRows = maxRows;
          lastReports = result;
          pushResults(acceptor, startRow, result);
        }

      });
    }

    private void pushResults(RowDataAcceptor acceptor, int startRow,
        ReportInfo[] reports) {
      String[][] rows = new String[reports.length][];
      for (int i = 0, n = rows.length; i < n; i++) {
        ReportInfo report = reports[i];
        rows[i] = new String[5];
        rows[i][0] = report.getSubscriberId();
        rows[i][1] = report.getEvent();
        rows[i][2] = report.getLocation();
        rows[i][3] = report.getCaptureTime().toString();
        rows[i][4] = report.getContent();
      }
      acceptor.accept(startRow, rows);
    }
  }

  private final IncidentReportProvider incidentReportProvider = new IncidentReportProvider();

  private final boolean[] eventFilter = new boolean[] {
      true, true, true, true};

  private final DynaTableWidget dynaTable;

  private Command pendingRefresh;

  public IncidentReportsWidget(int visibleRows) {
    String[] columns = new String[] {"Id", "Event", "Location", "Time", "Details"};
    String[] styles = new String[] {"id", "event", "loc", "time", "details"};
    dynaTable = new DynaTableWidget(incidentReportProvider, columns, styles, visibleRows);
    initWidget(dynaTable);
  }

  protected boolean getEventIncluded(int event) {
    return eventFilter[event];
  }

  @Override
  protected void onLoad() {
    dynaTable.refresh();
  }

  protected void setEventIncluded(int event, boolean included) {
    if (eventFilter[event] == included) {
      // No change.
      //
      return;
    }

    eventFilter[event] = included;
    if (pendingRefresh == null) {
      pendingRefresh = new Command() {
        public void execute() {
          pendingRefresh = null;
          dynaTable.refresh();
        }
      };
      DeferredCommand.addCommand(pendingRefresh);
    }
  }
}
