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
import org.testng.annotations.Test;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;
import java.util.logging.Logger;

/**
 * UserTest Unit test for the User Entity
 */
public class UserTest
        extends EJBTest
{

    Logger log = Logger.getLogger("UserTest");

    public UserTest()
    {
        super();
    }

    @Test
    public void testUser()
            throws Exception
    {

        new ComponentTest()
        {

            protected void testComponents()
                    throws Exception
            {
                EntityManager em = (EntityManager)
                        new InitialContext().lookup
                                ("java:/EntityManagers/DefaultDS");

                // Obtain JBoss transaction
                TransactionManager tm =
                        (TransactionManager) new InitialContext().lookup
                                ("java:/TransactionManager");

                // testing CREATION
                tm.begin();

                User user = new User();
                user.setEmail("test1@test.com");
                user.setPassword("test1");
                assert(user.getId() == 0);
                em.persist(user);
                tm.commit();

                assert(user.getId() > 0);
                long id = user.getId();
                log.info("created user 'test1@test.com' in DB with id: " + id);

                // testing RETREIVAL
                tm.begin();
                user = em.find(User.class, id);
                assert(null != user);
                assert(user.getId() > 0);
                assert ("test1".equals(user.getPassword()));
                assert ("test1@test.com".equals(user.getEmail()));
                tm.commit();
                log.info("found user 'test1@test.com' in DB with id: " + id);

                // testing REMOVE
/*
                tm.begin();
                em.refresh(user);
                em.remove(user);
                log.info (user.toString());
                tm.commit();
                log.info("removed user 'test1@test.com' in DB with id: " + id);
*/

            }
        }.run();

    }

}