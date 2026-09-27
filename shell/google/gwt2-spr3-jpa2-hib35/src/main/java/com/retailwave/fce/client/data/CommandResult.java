package com.retailwave.fce.client.data;
/**
 * $Id: CommandResult.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/data/CommandResult.java $
 */

/**
 * UserDTO: mramados
 * Date: Oct 26, 2009
 * Time: 3:43:16 PM
 */
public class CommandResult {

    private String action;
    private Object result;
    private String parentTitle;

    public CommandResult() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setParentTitle(String parentTitle) {
        this.parentTitle = parentTitle;
    }

    public String getParentTitle() {
        return parentTitle;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public CommandResult clear() {
        action = null;
        result = null;
        parentTitle = null;
        return this;
    }

    @Override
    public String toString() {
        return "CommandResult{" +
                "action='" + action + '\'' +
                ", result=" + result +
                ", parentTitle='" + parentTitle + '\'' +
                '}';
    }
}