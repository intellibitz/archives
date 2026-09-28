/**
 *
 */
package com.androidrocks.bex.client.json

import java.io.Serializable

/**
 * @author muthu
 */
class Friend : Serializable {
    var id: String? = null
    var location: String? = null
    var name: String? = null
    var screenName: String? = null
    var description: String? = null
    var profileImageUrl: String? = null
}
