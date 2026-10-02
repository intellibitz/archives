package com.retailwave.fce.client.content.i18n
/**
 * $Id: FCEConstants.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/content/i18n/FCEConstants.java $
 */

import com.google.gwt.i18n.client.Constants

interface FCEConstants : Constants {
    fun idPrefix(): String?
    fun title(): String?
    fun description(): String?
    fun startupMessage(): String?
    fun users(): String?
}
