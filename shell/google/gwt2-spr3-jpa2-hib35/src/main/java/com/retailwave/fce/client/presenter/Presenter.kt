package com.retailwave.fce.client.presenter
/**
 * $Id: Presenter.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/presenter/Presenter.java $
 */

import com.google.gwt.user.client.ui.HasWidgets
import com.google.gwt.user.client.ui.Widget

interface Presenter {
    fun go(container: HasWidgets)

    interface ContentPresenter : Presenter {
        fun getName(): String?
        fun getDescription(): String?
        fun getHistoryTokens(): Array<String>?
        fun getContentView(): Widget?
    }
}
