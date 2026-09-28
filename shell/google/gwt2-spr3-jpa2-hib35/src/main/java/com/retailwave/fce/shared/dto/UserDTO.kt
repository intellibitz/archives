package com.retailwave.fce.shared.dto
/**
 * $Id: UserDTO.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/shared/dto/UserDTO.java $
 */

import com.google.gwt.user.client.rpc.IsSerializable
import java.io.Serializable

class UserDTO : Serializable, IsSerializable {
    var userId: String? = null
    var externalId: String? = null
    var name: String? = null
    var fullName: String? = null
    var emailAddress: String? = null
    var role: String? = null
    var program: String? = null
    var country: String? = null
    var isActive: Boolean = false
    var activeSearch: String? = null
    var typeSearch: String? = null
    var isPartnerUser: Boolean = false
    var partnerId: Long? = null
    var partnerName: String? = null
    var isWildcard: Boolean = true

    override fun toString(): String {
        return "UserDTO{" +
                "userId='" + userId + ''' +
                ", name='" + name + ''' +
                ", fullName='" + fullName + ''' +
                ", emailAddress='" + emailAddress + ''' +
                ", role='" + role + ''' +
                ", program='" + program + ''' +
                ", country='" + country + ''' +
                ", active=" + isActive +
                ", partnerUser=" + isPartnerUser +
                '}'
    }

    companion object {
        private const val serialVersionUID = -1895382209235209986L
    }
}
