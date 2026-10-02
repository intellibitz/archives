package com.retailwave.fce.client.data
/**
 * $Id: CommandResult.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/data/CommandResult.java $
 */

/**
 * UserDTO: mramados
 * Date: Oct 26, 2009
 * Time: 3:43:16 PM
 */
class CommandResult {
    var action: String? = null
    var result: Any? = null
    var parentTitle: String? = null

    fun clear(): CommandResult {
        action = null
        result = null
        parentTitle = null
        return this
    }

    override fun toString(): String {
        return "CommandResult{" +
                "action='" + action + ''' +
                ", result=" + result +
                ", parentTitle='" + parentTitle + ''' +
                '}'
    }
}
