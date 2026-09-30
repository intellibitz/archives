package com.ibt.intellimeet.data

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import org.hibernate.validator.Length
import org.hibernate.validator.NotNull
import org.hibernate.validator.Pattern
import org.jboss.seam.ScopeType
import org.jboss.seam.annotations.Name
import org.jboss.seam.annotations.Scope;

/*
<!--
$Id::                                                                           $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
$HeadURL::                                                                      $: Head URL of last commit
-->
*/

@Name("groovyUser")
@Scope(ScopeType.SESSION)
@Entity
@Table(name = "GROOVYUSER")
class GroovyUser
        implements Serializable
{

    static final long serialVersionUID = 2973374377453022888L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "GROOVY_USER_ID")
    long id;
    @Length(min = 5, max = 15)
    @Pattern(regex = /^\\w*$/, message = "not a valid username")
    String username;
    @NotNull
    @Length(min = 5, max = 15)
    String password;
    @NotNull
    @Length(max = 100)
    String name;

    GroovyUser() {}

    GroovyUser(String name, String password, String username)
    {
        this.name = name;
        this.password = password;
        this.username = username;
    }

    @Override
    public String toString()
    {
        return "GroovyUser(" + username + ")";
    }
}
