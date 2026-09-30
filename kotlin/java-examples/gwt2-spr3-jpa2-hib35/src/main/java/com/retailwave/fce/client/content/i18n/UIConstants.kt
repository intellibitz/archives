package com.retailwave.fce.client.content.i18n
/**
 * $Id: UIConstants.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/content/i18n/UIConstants.java $
 */

import com.google.gwt.i18n.client.Constants

interface UIConstants : Constants {
    fun saveClear(): Array<String>?
    fun okCancel(): Array<String>?
    fun saveChangesDialog(): String?
    fun loadProgressWait(): String?
    fun saveProgressWait(): String?
    fun trueFalse(): Array<String>?
    fun yesNo(): Array<String>?
    fun searchProgressWait(): String?
    fun searchEmpty(): String?
    fun searchClear(): Array<String>?
    fun searchFail(): String?
    fun saveFail(): String?
    fun fetchFail(): String?
}
