package com.ibt.intellimeet.data;

/*
<!--
$Id::                                                                           $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
$HeadURL::                                                                      $: Head URL of last commit
-->
*/

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;
import static org.jboss.seam.ScopeType.SESSION;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Name("dummyUser")
//@JndiName("DummyUser/local")
@Scope(SESSION)
@Entity
@Table(name = "DUMMYUSER")
public class DummyUser
        implements Serializable
{

    static final long serialVersionUID = 2973374377453022888L;

    private long id;
    private String username;
    private String password;
    private String name;

    /**
     * Default constructor
     */
    public DummyUser()
    {
        super ();
    }

    public DummyUser(String name, String password, String username)
    {
        this.name = name;
        this.password = password;
        this.username = username;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "USER_ID")
    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    @NotNull
    @Length(max = 100)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @NotNull
    @Length(min = 5, max = 15)
    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Length(min = 5, max = 15)
    @Pattern(regex = "^\\w*$", message = "not a valid username")
    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    @Override
    public String toString()
    {
        return "DummyUser(" + username + ")";
    }
}
