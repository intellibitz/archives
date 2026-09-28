/**
 *
 */
package com.androidrocks.bex.client.json

import java.io.Serializable

/**
 * @author muthu
 */
class TwitterId : Serializable {
    var screenName: String? = null
    var token: String? = null

    fun isScreenNameValid(): Boolean {
        return screenName != null && screenName!!.length > 0
    }

    fun isValid(): Boolean {
        return screenName != null && token != null
                && screenName!!.length > 0 && token!!.length > 0
    }
}
