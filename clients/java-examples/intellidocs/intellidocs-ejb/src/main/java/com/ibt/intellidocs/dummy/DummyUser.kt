package com.ibt.intellidocs.dummy

import org.hibernate.validator.Length
import org.hibernate.validator.NotNull
import org.hibernate.validator.Pattern
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

@Name("dummyUser")
//@JndiName("DummyUser/local")
@Scope(SESSION)
@Entity
@Table(name = "DUMMYUSER")
class DummyUser : Serializable {

    @get:Id
    @get:GeneratedValue(strategy = GenerationType.AUTO)
    @get:Column(name = "USER_ID")
    var id: Long = 0

    @get:Length(min = 5, max = 15)
    @get:Pattern(regex = "^\\w*$", message = "not a valid username")
    var username: String? = null

    @get:NotNull
    @get:Length(min = 5, max = 15)
    var password: String? = null

    @get:NotNull
    @get:Length(max = 100)
    var name: String? = null

    constructor(name: String?, password: String?, username: String?) {
        this.name = name
        this.password = password
        this.username = username
    }

    constructor()

    override fun toString(): String {
        return "DummyUser($username)"
    }
}
