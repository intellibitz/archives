package com.retailwave.fce.client.content.i18n
/**
 * $Id: SearchUserConstants.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/content/i18n/SearchUserConstants.java $
 */

import com.google.gwt.i18n.client.Constants

interface SearchUserConstants : Constants {
    fun idPrefix(): String?
    fun title(): String?
    fun description(): String?
    fun shortName(): String?
    fun fullName(): String?
    fun email(): String?
    fun active(): String?
    fun role(): String?
    fun program(): String?
    fun country(): String?
    fun type(): String?
    fun typeValues(): Array<String>?
    fun activeValues(): Array<String>?
    fun actions(): Array<String>?
}
