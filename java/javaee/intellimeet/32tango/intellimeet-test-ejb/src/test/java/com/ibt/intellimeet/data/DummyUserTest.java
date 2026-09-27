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

import com.ibt.intellimeet.ejb.EJBTest;
import com.ibt.intellimeet.data.DummyUser;
import org.testng.annotations.Test;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;
import java.util.logging.Logger;

/**
 * DummyUserTest Unit test for the DummyUser Entity
 */
public class DummyUserTest
        extends EJBTest
{

    Logger log = Logger.getLogger("DummyUserTest");

    public DummyUserTest()
    {
        super();
    }

    @Test
    public void testDummyUser()
            throws Exception
    {

        new ComponentTest()
        {

            protected void testComponents()
                    throws Exception
            {
                // This is a transactionally aware EntityManager and must be accessed within a JTA transaction
                // Why aren't we using javax.persistence.Persistence?  Well, our persistence.xml file uses
                // jta-datasource which means that it is created by the EJB container/embedded JBoss.
                // using javax.persistence.Persistence will just cause us an error
                EntityManager em = (EntityManager)
                        new InitialContext().lookup
                                ("java:/EntityManagers/DefaultDS");
//                    getInstance("DefaultDS");

                // Obtain JBoss transaction
                TransactionManager tm =
                        (TransactionManager) new InitialContext().lookup
                                ("java:/TransactionManager");

                tm.begin();

                DummyUser dummyUser = new DummyUser();
                dummyUser.setName("test1");
                dummyUser.setUsername("test1");
                dummyUser.setPassword("test1");
                em.persist(dummyUser);

                assert (dummyUser.getId() > 0);

                tm.commit();
                long id = dummyUser.getId();
                log.info("created user 'test1' in DB with id: " + id);

                tm.begin();
                dummyUser = em.find(DummyUser.class, id);
                assert (null != dummyUser);
                tm.commit();
                log.info("found user 'test1' in DB with id: " + id);
            }
        }.run();

    }

}
