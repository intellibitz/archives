/**
 *
 */
package com.androidrocks.bex.client.json;

import java.io.Serializable;

/**
 * @author muthu
 */
public class TwitterId
        implements Serializable {

    private String screenName;
    private String token;

    public TwitterId() {
    }

    /**
     * @return the screenName
     */
    public String getScreenName() {
        return screenName;
    }

    /**
     * @param screenName the screenName to set
     */
    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isScreenNameValid (){
        return null != screenName && screenName.length() > 0;
    }

    public boolean isValid() {
        return null != screenName && null != token
                && screenName.length() > 0 && token.length() > 0;
    }
}
