package com.ibt.intellimeet.app;

/*
<!--
$Id::                                                                           $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
$HeadURL::                                                                      $: Head URL of last commit
-->
*/

import com.ibt.intellimeet.data.DummyUser;
import static org.jboss.seam.ScopeType.EVENT;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: sara Date: Dec 15, 2007 Time: 12:42:47 PM To
 * change this template use File | Settings | File Templates.
 */
@Stateful
@Scope(EVENT)
@Name("dummyUserAction")
//@JndiName ("DummyUserAction/local")
public class DummyUserAction
        implements IDummyUserLocal, Serializable
{
    static final long serialVersionUID = 2973374377453022888L;

    @Logger
    transient Log log;

    @In(required = true)
    private DummyUser dummyUser;

    @PersistenceContext
    private EntityManager entityManager;

    private String verify;
    private boolean registered;

    public DummyUser findDummyUser(long id)
    {
        return entityManager.find(DummyUser.class, id);
    }

    public long register()
    {
        if (dummyUser.getPassword().equals(verify))
        {
            List existing = entityManager.createQuery
                    ("select u.username from DummyUser u where u.username=#{dummyUser.username}")
                    .getResultList();
            if (existing.isEmpty())
            {
                entityManager.persist(dummyUser);
                // facesMessages.add("Successfully registered as #{dummyUser.username}");
                log.info("Username #{dummyUser.username} already exists");
                registered = true;
            }
            else
            {
                log.info("Username #{dummyUser.username} already exists");
            }
        }
        else
        {
            log.info("Re-enter your password");
            verify = null;
        }
        return dummyUser.getId();
    }

    public void invalid()
    {
        log.info("Please try again");
    }

    public EntityManager getEntityManager()
    {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }

    public void setRegistered(boolean registered)
    {
        this.registered = registered;
    }

    public boolean isRegistered()
    {
        return registered;
    }

    public String getVerify()
    {
        return verify;
    }

    public void setVerify(String verify)
    {
        this.verify = verify;
    }

    public void run()
    {
        log.info("dummySeamBean#run.. Done!");
    }

    public void cancel()
    {
        log.info("dummySeamBean#cancel.. Done!");
    }

    @Destroy
    @Remove
    public void destroy()
    {
        log.info("dummySeamBean#destroy.. Done!");
    }

    public DummyUser getDummyUser()
    {
        return dummyUser;
    }

    public void setDummyUser(DummyUser dummyUser)
    {
        this.dummyUser = dummyUser;
    }
}
