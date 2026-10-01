package com.ibt.intellimeet.data

/*
<!--
$Id::                                                                           $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
$HeadURL::                                                                      $: Head URL of last commit
-->
*/

import org.hibernate.validator.Length
import org.hibernate.validator.NotNull
import org.jboss.seam.ScopeType.SESSION
import org.jboss.seam.annotations.Name
import org.jboss.seam.annotations.Scope
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Name("user")
@Scope(SESSION)
@Entity
@Table(name = "USERS")
class User : Serializable {

    @get:Id
    @get:GeneratedValue(strategy = GenerationType.AUTO)
    @get:Column(name = "USER_ID")
    var id: Long = 0

    @get:NotNull
    @get:Length(min = 5, max = 15)
    var password: String? = null

    @get:NotNull
    @get:Length(max = 100)
    var email: String? = null

    /**
     * Default constructor
     */
    constructor() : super()

    constructor(email: String?, password: String?) {
        this.email = email
        this.password = password
    }

    override fun toString(): String {
        return "User($email)"
    }

    companion object {
        const val serialVersionUID = 2973374377453022888L
    }
}
