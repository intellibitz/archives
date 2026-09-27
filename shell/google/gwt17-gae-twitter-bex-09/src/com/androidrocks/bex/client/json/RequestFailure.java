/**
 *
 */
package com.androidrocks.bex.client.json;

import java.io.Serializable;

/**
 * @author muthu
 */
public class RequestFailure
        implements Serializable
    {

    private String reason;

    public RequestFailure() {
    }

    /**
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(String reason) {
        this.reason = reason;
    }


}
