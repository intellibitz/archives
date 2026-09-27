package com.retailwave.fce.client.presenter;
/**
 * $Id: Presenter.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/presenter/Presenter.java $
 */

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public abstract interface Presenter {
    public abstract void go(final HasWidgets container);

    static public abstract interface ContentPresenter extends Presenter {
        public abstract String getName();

        public abstract String getDescription();

        public abstract String[] getHistoryTokens();

        public abstract Widget getContentView();
    }
}
