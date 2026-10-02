package com.retailwave.fce.shared.domain
/**
 * $Id: User.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/shared/domain/User.java $
 */

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "USER")
class User : Serializable {

    @Id
    @Column(name = "user_id")
    var userId: Long = 0

    @Column(name = "user_name", nullable = false, length = 30)
    var name: String? = null

    @Column(name = "user_fullName", nullable = false, length = 50)
    var fullName: String? = null

    @Column(name = "email", length = 50)
    var emailAddress: String? = null

    var externalId: String? = null
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
        private const val serialVersionUID = 1L
    }
}
