/**
 *
 */
package com.androidrocks.bex.client.json

import java.io.Serializable

/**
 * @author muthu
 */
class Match : Serializable {
    var id: String? = null
    var friendId: String? = null

    // book
    var title: String? = null
    var publisher: String? = null
    var description: String? = null
    var image: String? = null

    // friend
    var location: String? = null
    var name: String? = null
    var screenName: String? = null
    var profileImageUrl: String? = null
}
